package cc.whohow.messenger;

import cc.whohow.messenger.util.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 默认客户端管理
 */
public class SimpleMessengerManager implements MessengerManager {
    private static final Set<String> RESERVED_UID = Collections.singleton(Messenger.SYSTEM_UID);
    protected final Map<Messenger, Long> messengers = new ConcurrentHashMap<>();
    protected final Map<String, Collection<Consumer<Message>>> subscribers = new ConcurrentHashMap<>();

    public Messenger newMessenger(String uid, String tags) {
        return newMessenger(uid, Text.splitAsSet(tags));
    }

    public Messenger newMessenger(String uid, Set<String> tags) {
        if (Text.isEmpty(uid) || RESERVED_UID.contains(uid)) {
            throw new IllegalArgumentException("uid");
        }
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags");
        }
        return new SimpleMessenger(uid, tags);
    }

    public Collection<Messenger> getMessengers() {
        return Collections.unmodifiableCollection(messengers.keySet());
    }

    @Override
    public Collection<Consumer<Message>> getMessageSender(String tag) {
        if (Text.isEmpty(tag)) {
            return Collections.emptyList();
        }
        Collection<Consumer<Message>> consumers = subscribers.get(tag);
        if (consumers == null) {
            return Collections.emptyList();
        }
        return consumers;
    }


    public void subscribe(Messenger messenger, Consumer<Message> subscriber) {
        messengers.put(messenger, System.currentTimeMillis());
        for (String tag : messenger.getTags()) {
            subscribers.computeIfAbsent(tag, key -> new ConcurrentLinkedDeque<>()).add(subscriber);
        }
    }

    public void unsubscribe(Messenger messenger, Consumer<Message> subscriber) {
        messengers.remove(messenger);
        for (String tag : messenger.getTags()) {
            Collection<Consumer<Message>> s = subscribers.get(tag);
            if (s != null) {
                s.remove(subscriber);
            }
        }
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public String toString() {
        if (messengers.size() < 16) {
            return messengers.size() + ": " + messengers.keySet().stream()
                    .map(Messenger::getUid)
                    .collect(Collectors.joining(", "));
        }
        return String.valueOf(messengers.size());
    }
}
