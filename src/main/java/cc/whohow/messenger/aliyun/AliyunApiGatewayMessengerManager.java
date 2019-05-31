package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.Message;
import cc.whohow.messenger.Messenger;
import cc.whohow.messenger.MessengerManager;
import cc.whohow.messenger.SimpleMessenger;
import cc.whohow.redis.io.Codec;
import cc.whohow.redis.io.JacksonCodec;
import cc.whohow.redis.io.PrefixCodec;
import cc.whohow.redis.io.StringCodec;
import cc.whohow.redis.util.RedisExpireKey;
import cc.whohow.redis.util.RedisSet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private RedisCommands<ByteBuffer, ByteBuffer> redis;
    private RedisExpireKey<ObjectNode> devices;

    public AliyunApiGatewayMessengerManager(JsonNode configuration) {
        this.redis = redis;
        this.devices = new RedisExpireKey<>(redis, JSON_CODEC, DEV_KEY_CODEC, Duration.ofDays(1));
    }

    public void register(ObjectNode messenger) {
        String deviceId = messenger.path("device").asText("");
        if (deviceId.isEmpty()) {
            return;
        }
        devices.put(deviceId, messenger);
        for (JsonNode tag : messenger.path("tags")) {
            String t = tag.asText("");
            if (t.isEmpty()) {
                continue;
            }
            getDevices(t).add(deviceId);
        }
    }

    public void logout(String deviceId) {
        ObjectNode messenger = devices.remove(deviceId);
        for (JsonNode tag : messenger.path("tags")) {
            String t = tag.asText("");
            if (t.isEmpty()) {
                continue;
            }
            getDevices(t).remove(deviceId);
        }
    }

    public Messenger getMessenger(String deviceId) {
        ObjectNode messenger = devices.get(deviceId);
        String uid = messenger.path("uid").textValue();
        Set<String> tags = new HashSet<>();
        for (JsonNode tag : messenger.path("tags")) {
            tags.add(tag.textValue());
        }
        return new SimpleMessenger(uid, tags);
    }

    public RedisSet<String> getDevices(String tag) {
        return new RedisSet<>(redis, STRING_CODEC, "tag:" + tag);
    }

    @Override
    public Collection<Consumer<Message>> getMessageSender(String tag) {
        return getDevices(tag).stream()
                .map(AliyunApiGatewayMessageSender::new)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
    }
}
