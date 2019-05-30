package cc.whohow.messenger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleMessenger implements Messenger {
    private static final Pattern COMMA = Pattern.compile(",");
    private final String uid;
    private final Set<String> tags;

    public SimpleMessenger(String uid, String tags) {
        this(trimToEmpty(uid), splitAsSet(tags));
    }

    public SimpleMessenger(String uid, Collection<String> tags) {
        this(trimToEmpty(uid), new HashSet<>(tags));
    }

    protected SimpleMessenger(String uid, Set<String> tags) {
        if (uid == null || uid.isEmpty()) {
            throw new IllegalArgumentException("uid");
        }
        if (tags.isEmpty()) {
            throw new IllegalArgumentException("tags");
        }
        this.uid = uid;
        this.tags = tags;
    }

    private static String trimToEmpty(String string) {
        if (string == null) {
            return "";
        }
        return string.trim();
    }

    private static Set<String> splitAsSet(String csv) {
        if (csv == null || csv.isEmpty()) {
            return Collections.emptySet();
        }
        return COMMA.splitAsStream(csv)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
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
        return uid + "@" + tags;
    }
}
