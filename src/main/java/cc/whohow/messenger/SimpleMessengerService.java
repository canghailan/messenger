package cc.whohow.messenger;

import cc.whohow.redis.distributed.SnowflakeId;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public class SimpleMessengerService implements MessengerService {
    private static final Logger log = LogManager.getLogger();
    private static final String SYSTEM_UID = "__SYSTEM__";
    private static final Set<String> RESERVED_UID = Collections.singleton(SYSTEM_UID);
    protected final Map<String, Messenger> messengers = new ConcurrentHashMap<>();
    protected final Map<String, Collection<Consumer<Message>>> subscribers = new ConcurrentHashMap<>();
    protected final Clock clock = Clock.systemDefaultZone();
    protected final LongSupplier id = new SnowflakeId(clock, new SnowflakeId.Worker(0));

    @Override
    public Message newMessage(String message) {
        return new SimpleMessage(message);
    }

    @Override
    public Message newMessage(ByteBuffer message) {
        return new SimpleMessage(message);
    }

    @Override
    public Message newMessage(ObjectNode message) {
        return new SimpleMessage(message);
    }

    @Override
    public Message newErrorMessage(Messenger to, String context, Throwable e) {
        Message message = new SimpleMessage();
        ObjectNode object = message.toJson();
        object.put(Message.ID, String.valueOf(id.getAsLong()));
        object.put(Message.TIMESTAMP, clock.millis());
        object.put(Message.FROM, SYSTEM_UID);
        object.put(Message.TYPE, MessageType.ERROR.toString());
        object.put(Message.ERROR, e.getMessage());
        object.put(Message.DATA, context);
        return message;
    }

    @Override
    public Messenger newMessenger(String uid, String tags) {
        if (uid == null || uid.isEmpty() || RESERVED_UID.contains(uid)) {
            throw new IllegalArgumentException("uid");
        }
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags");
        }
        return new SimpleMessenger(uid, tags);
    }

    @Override
    public Messenger newMessenger(String uid, Collection<String> tags) {
        if (uid == null || uid.isEmpty() || RESERVED_UID.contains(uid)) {
            throw new IllegalArgumentException("uid");
        }
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags");
        }
        return new SimpleMessenger(uid, tags);
    }

    @Override
    public void send(Messenger from, Message message) {
        send(initialize(from, message));
    }

    @Override
    public void sendEventMessage(Messenger messenger, String event) {
        for (String tag : messenger.getTags()) {
            Message message = new SimpleMessage();
            ObjectNode object = message.toJson();
            object.put(Message.ID, String.valueOf(id.getAsLong()));
            object.put(Message.TIMESTAMP, clock.millis());
            object.put(Message.FROM, SYSTEM_UID);
            object.put(Message.TO, tag);
            object.put(Message.TYPE, event);
            object.put(Message.UID, messenger.getUid());
            send(message);
        }
    }

    @Override
    public void sendSystemMessage(Message message) {
        ObjectNode object = message.toJson();
        object.put(Message.ID, String.valueOf(id.getAsLong()));
        object.put(Message.TIMESTAMP, clock.millis());
        object.put(Message.FROM, SYSTEM_UID);
        send(newMessage(object));
    }

    protected Message initialize(Messenger from, Message message) {
        String to = message.getTo();
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException(Message.TO);
        }
        ObjectNode object = message.toJson();
        object.put(Message.ID, String.valueOf(id.getAsLong()));
        object.put(Message.TIMESTAMP, clock.millis());
        object.put(Message.FROM, from.getUid());
        return newMessage(object);
    }

    protected void send(Message message) {
        receive(message);
    }

    protected void receive(Message message) {
        String to = message.getTo();
        if (to == null) {
            return;
        }
        Collection<Consumer<Message>> consumers = subscribers.get(to);
        if (consumers == null || consumers.isEmpty()) {
            return;
        }
        for (Consumer<Message> consumer : consumers) {
            try {
                consumer.accept(message);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void subscribe(Messenger messenger, Consumer<Message> subscriber) {
        messengers.put(messenger.getUid(), messenger);
        for (String tag : messenger.getTags()) {
            subscribers.computeIfAbsent(tag, key -> new ConcurrentLinkedDeque<>()).add(subscriber);
        }
    }

    @Override
    public void unsubscribe(Messenger messenger, Consumer<Message> subscriber) {
        messengers.remove(messenger.getUid());
        for (String tag : messenger.getTags()) {
            Collection<Consumer<Message>> s = subscribers.get(tag);
            if (s != null) {
                s.remove(subscriber);
            }
        }
    }

    @Override
    public Collection<Messenger> getMessengers() {
        return Collections.unmodifiableCollection(messengers.values());
    }

    @Override
    public void run() {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public String toString() {
        if (messengers.size() < 16) {
            return messengers.size() + ": " + messengers.keySet();
        }
        return String.valueOf(messengers.size());
    }
}
