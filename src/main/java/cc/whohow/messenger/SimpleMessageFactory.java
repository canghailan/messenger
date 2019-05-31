package cc.whohow.messenger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;

public class SimpleMessageFactory implements MessageFactory {
    @Override
    public Message newMessage(String message) {
        return new SimpleMessage(message);
    }

    @Override
    public Message newMessage(ByteBuffer message) {
        return new SimpleMessage(message);
    }

    @Override
    public Message newMessage(ObjectNode message) {
        return new SimpleMessage(message);
    }

    @Override
    public void close() {
    }
}
