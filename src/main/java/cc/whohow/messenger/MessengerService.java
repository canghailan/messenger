package cc.whohow.messenger;

/**
 * 消息服务
 */
public interface MessengerService<MF extends MessageFactory, MM extends MessengerManager, MQ extends MessageQueue> extends Runnable, AutoCloseable {
    MF getMessageFactory();

    MM getMessengerManager();

    MQ getMessageQueue();

    Message newErrorMessage(Messenger to, String context, Throwable e);

    Message send(Messenger from, Message message);

    Message sendSystemMessage(Message message);

    void broadcastEventMessage(Messenger messenger, String event);
}
