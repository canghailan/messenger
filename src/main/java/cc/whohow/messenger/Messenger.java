package cc.whohow.messenger;

import java.util.Set;

/**
 * 消息发送、接收人
 */
public interface Messenger {
    String SYSTEM_UID = "__SYSTEM__";

    String getUid();

    Set<String> getTags();
}
