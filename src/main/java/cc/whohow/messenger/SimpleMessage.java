package cc.whohow.messenger;

import cc.whohow.messenger.util.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SimpleMessage implements Message {
    private ObjectNode json;
    private ByteBuffer bytes;
    private String string;

    public SimpleMessage() {
        this(Json.newObject());
    }

    public SimpleMessage(ObjectNode json) {
        this.json = json;
    }

    public SimpleMessage(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    public SimpleMessage(String string) {
        this.string = string;
    }

    @Override
    public String getId() {
        return toJson().path(Message.ID).asText(null);
    }

    @Override
    public String getFrom() {
        return toJson().path(Message.FROM).asText(null);
    }

    @Override
    public String getTo() {
        return toJson().path(Message.TO).asText(null);
    }

    @Override
    public ByteBuffer toBytes() {
        if (bytes != null) {
            return bytes;
        }
        if (json != null) {
            return bytes = Json.serialize(json);
        }
        if (string != null) {
            return bytes = StandardCharsets.UTF_8.encode(string);
        }
        throw new IllegalStateException();
    }

    @Override
    public ObjectNode toJson() {
        if (json != null) {
            return json;
        }
        if (bytes != null) {
            return json = Json.deserialize(bytes, ObjectNode.class);
        }
        if (string != null) {
            return json = Json.parse(string, ObjectNode.class);
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        if (json != null) {
            return string = json.toString();
        }
        if (bytes != null) {
            return string = StandardCharsets.UTF_8.decode(bytes).toString();
        }
        throw new IllegalStateException();
    }
}
