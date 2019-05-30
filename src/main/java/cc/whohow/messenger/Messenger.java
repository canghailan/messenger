package cc.whohow.messenger;

import java.util.Set;

public interface Messenger {
    String getUid();

    Set<String> getTags();
}
