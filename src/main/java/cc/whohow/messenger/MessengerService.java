package cc.whohow.messenger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.Consumer;

public interface MessengerService extends Runnable, AutoCloseable {
    Message newMessage(String message);

    Message newMessage(ByteBuffer message);

    Message newMessage(ObjectNode message);

    Message newErrorMessage(Messenger to, String message, Throwable e);

    Messenger newMessenger(String uid, String tags);

    Messenger newMessenger(String uid, Collection<String> tags);

    void send(Messenger from, Message message);

    void sendEventMessage(Messenger messenger, String event);

    void sendSystemMessage(Message message);

    void subscribe(Messenger messenger, Consumer<Message> subscriber);

    void unsubscribe(Messenger messenger, Consumer<Message> subscriber);

    Collection<Messenger> getMessengers();
}
