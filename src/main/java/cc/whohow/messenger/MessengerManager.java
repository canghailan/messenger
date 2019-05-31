package cc.whohow.messenger;

import java.util.Collection;
import java.util.function.Consumer;

public interface MessengerManager extends AutoCloseable {
    //    Collection<Messenger> getMessengers();
//    Collection<Messenger> getMessengers(String tag);
    Collection<Consumer<Message>> getMessageSender(String tag);
}
