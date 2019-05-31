package cc.whohow.messenger;

/**
 * 消息队列，发送、接收、分发消息
 */
public interface MessageQueue extends Runnable, AutoCloseable {
    MessageFactory getMessageFactory();

    MessengerManager getMessengerManager();

    void send(Message message);

    void receive(Message message);
}
