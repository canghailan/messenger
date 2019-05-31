package cc.whohow.messenger;

import java.util.Set;

public class SimpleMessenger implements Messenger {
    private final String uid;
    private final Set<String> tags;

    public SimpleMessenger(String uid, Set<String> tags) {
        this.uid = uid;
        this.tags = tags;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return uid + " @ " + tags;
    }
}
