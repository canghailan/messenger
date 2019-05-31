package cc.whohow.messenger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 简单消息队列，直接转发
 */
public class SimpleMessageQueue implements MessageQueue {
    private static final Logger log = LogManager.getLogger();
    protected final MessageFactory messageFactory;
    protected final MessengerManager messengerManager;

    public SimpleMessageQueue(MessageFactory messageFactory, MessengerManager messengerManager) {
        this.messageFactory = messageFactory;
        this.messengerManager = messengerManager;
    }

    @Override
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    @Override
    public MessengerManager getMessengerManager() {
        return messengerManager;
    }

    @Override
    public void send(Message message) {
        receive(message);
    }

    @Override
    public void receive(Message message) {
        String to = message.getTo();
        if (to == null) {
            return;
        }
        Collection<Consumer<Message>> senders = messengerManager.getMessageSender(to);
        if (senders == null || senders.isEmpty()) {
            return;
        }
        for (Consumer<Message> sender : senders) {
            try {
                sender.accept(message);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void run() {
    }
}
