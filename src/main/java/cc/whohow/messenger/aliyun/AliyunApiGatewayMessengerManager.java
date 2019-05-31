package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.Message;
import cc.whohow.messenger.Messenger;
import cc.whohow.messenger.MessengerManager;
import cc.whohow.messenger.SimpleMessenger;
import cc.whohow.messenger.util.Closeables;
import cc.whohow.redis.io.Codec;
import cc.whohow.redis.io.JacksonCodec;
import cc.whohow.redis.io.PrefixCodec;
import cc.whohow.redis.io.StringCodec;
import cc.whohow.redis.lettuce.ByteBufferCodec;
import cc.whohow.redis.util.RedisExpireKey;
import cc.whohow.redis.util.RedisSortedSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AliyunApiGatewayMessengerManager implements MessengerManager {
    private static final Codec<String> STRING_CODEC = new StringCodec();
    private static final Codec<String> DEV_KEY_CODEC = new PrefixCodec<>(new StringCodec(), "dev:");
    private static final Codec<ObjectNode> JSON_CODEC = new JacksonCodec<>(ObjectNode.class);
    private final RedisClient redisClient;
    private final StatefulRedisConnection<ByteBuffer, ByteBuffer> redisConnection;
    private final RedisCommands<ByteBuffer, ByteBuffer> redis;
    private final RedisExpireKey<ObjectNode> devices;

    public AliyunApiGatewayMessengerManager(JsonNode configuration) {
        try {
            this.redisClient = RedisClient.create(configuration.path("redis").textValue());
            this.redisConnection = redisClient.connect(ByteBufferCodec.INSTANCE);
            this.redis = redisConnection.sync();
            this.devices = new RedisExpireKey<>(redis, JSON_CODEC, DEV_KEY_CODEC, Duration.ofDays(1));
        } catch (Throwable e) {
            close();
            throw e;
        }
    }

    public void register(ObjectNode messenger) {
        String device = messenger.path("device").asText("");
        devices.put(device, messenger);
        for (JsonNode tag : messenger.path("tags")) {
            getTagDevices(tag.asText("")).put(device, System.currentTimeMillis());
        }
    }

    public void logout(String device) {
        ObjectNode messenger = devices.remove(device);
        for (JsonNode tag : messenger.path("tags")) {
            getTagDevices(tag.asText("")).remove(device);
        }
    }

    public Messenger getMessenger(String device) {
        ObjectNode messenger = devices.get(device);
        String uid = messenger.path("uid").asText("");
        Set<String> tags = new HashSet<>();
        for (JsonNode tag : messenger.path("tags")) {
            tags.add(tag.asText(""));
        }
        return new SimpleMessenger(uid, tags);
    }

    private RedisSortedSet<String> getTagDevices(String tag) {
        return new RedisSortedSet<>(redis, STRING_CODEC, "tag:" + tag);
    }

    @Override
    public Collection<Consumer<Message>> getMessageSender(String tag) {
        return getTagDevices(tag).get().keySet().stream()
                .map(AliyunApiGatewayMessageSender::new)
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        Closeables.close(redisConnection);
        redisClient.shutdown();
    }
}
