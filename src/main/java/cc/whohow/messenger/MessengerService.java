package cc.whohow.messenger;

/**
 * 消息服务
 */
public interface MessengerService<MF extends MessageFactory, MM extends MessengerManager, MQ extends MessageQueue> extends Runnable, AutoCloseable {
    MF getMessageFactory();

    MM getMessengerManager();

    MQ getMessageQueue();

    Message newErrorMessage(Messenger to, String context, Throwable e);

    void send(Messenger from, Message message);

    void sendEventMessage(Messenger messenger, String event);

    void sendSystemMessage(Message message);
}
