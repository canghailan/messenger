package cc.whohow.messenger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;

/**
 * 消息工厂
 */
public interface MessageFactory extends AutoCloseable {
    Message newMessage(String message);

    Message newMessage(ByteBuffer message);

    Message newMessage(ObjectNode message);
}
