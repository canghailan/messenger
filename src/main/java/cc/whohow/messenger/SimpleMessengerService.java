package cc.whohow.messenger;

import cc.whohow.messenger.util.Closeables;
import cc.whohow.messenger.util.Json;
import cc.whohow.messenger.util.Text;
import cc.whohow.redis.distributed.SnowflakeId;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Clock;
import java.util.function.BiFunction;
import java.util.function.LongSupplier;

public class SimpleMessengerService<MF extends MessageFactory, MM extends MessengerManager, MQ extends MessageQueue>
        implements MessengerService<MF, MM, MQ> {
    protected final MF messageFactory;
    protected final MM messengerManager;
    protected final MQ messageQueue;
    protected final Clock clock;
    protected final LongSupplier id;

    public SimpleMessengerService(MF messageFactory, MM messengerManager, BiFunction<MF, MM, MQ> messageQueueFactory) {
        this(messageFactory, messengerManager, messageQueueFactory.apply(messageFactory, messengerManager));
    }

    public SimpleMessengerService(MF messageFactory, MM messengerManager, MQ messageQueue) {
        this(messageFactory, messengerManager, messageQueue, Clock.systemDefaultZone());
    }

    public SimpleMessengerService(MF messageFactory, MM messengerManager, MQ messageQueue, Clock clock) {
        this(messageFactory, messengerManager, messageQueue, clock, new SnowflakeId(clock, new SnowflakeId.Worker(0)));
    }

    public SimpleMessengerService(MF messageFactory, MM messengerManager, MQ messageQueue, Clock clock, LongSupplier id) {
        this.messageFactory = messageFactory;
        this.messengerManager = messengerManager;
        this.messageQueue = messageQueue;
        this.clock = clock;
        this.id = id;
    }

    @Override
    public MF getMessageFactory() {
        return messageFactory;
    }

    @Override
    public MM getMessengerManager() {
        return messengerManager;
    }

    @Override
    public MQ getMessageQueue() {
        return messageQueue;
    }

    @Override
    public Message newErrorMessage(Messenger to, String context, Throwable e) {
        ObjectNode message = Json.newObject();
        message.put(Message.ID, String.valueOf(id.getAsLong()));
        message.put(Message.TIMESTAMP, clock.millis());
        message.put(Message.FROM, Messenger.SYSTEM_UID);
        message.put(Message.TYPE, MessageType.ERROR.toString());
        message.put(Message.ERROR, e.getMessage());
        message.put(Message.DATA, context);
        return messageFactory.newMessage(message);
    }

    @Override
    public Message send(Messenger from, Message message) {
        return send(initialize(from, message));
    }

    @Override
    public Message sendSystemMessage(Message message) {
        ObjectNode messageObject = message.toJson();
        messageObject.put(Message.ID, String.valueOf(id.getAsLong()));
        messageObject.put(Message.TIMESTAMP, clock.millis());
        if (messageObject.path(Message.FROM).asText("").isEmpty()) {
            messageObject.put(Message.FROM, Messenger.SYSTEM_UID);
        }
        return send(messageFactory.newMessage(messageObject));
    }

    @Override
    public void broadcastEventMessage(Messenger messenger, String event) {
        for (String tag : messenger.getTags()) {
            ObjectNode messageObject = Json.newObject();
            messageObject.put(Message.ID, String.valueOf(id.getAsLong()));
            messageObject.put(Message.TIMESTAMP, clock.millis());
            messageObject.put(Message.FROM, Messenger.SYSTEM_UID);
            messageObject.put(Message.TO, tag);
            messageObject.put(Message.TYPE, event);
            messageObject.put(Message.UID, messenger.getUid());
            messageQueue.send(messageFactory.newMessage(messageObject));
        }
    }

    protected Message initialize(Messenger from, Message message) {
        String to = message.getTo();
        if (Text.isEmpty(to)) {
            throw new IllegalArgumentException(Message.TO);
        }
        ObjectNode object = message.toJson();
        object.put(Message.ID, String.valueOf(id.getAsLong()));
        object.put(Message.TIMESTAMP, clock.millis());
        object.put(Message.FROM, from.getUid());
        return messageFactory.newMessage(object);
    }

    protected Message send(Message message) {
        messageQueue.send(message);
        return message;
    }

    @Override
    public void run() {
        messageQueue.run();
    }

    @Override
    public void close() throws Exception {
        Closeables.close(messageQueue);
        Closeables.close(messengerManager);
        Closeables.close(messageFactory);
    }

    @Override
    public String toString() {
        return messengerManager.toString();
    }
}
