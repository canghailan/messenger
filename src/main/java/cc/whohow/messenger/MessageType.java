package cc.whohow.messenger;

/**
 * 消息类型
 */
public enum MessageType {
    ERROR("error"),
    ONLINE("online"),
    OFFLINE("offline");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
