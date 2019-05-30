package cc.whohow.messenger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SimpleMessage implements Message {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ObjectNode json;
    private ByteBuffer bytes;
    private String string;

    public SimpleMessage() {
        this(OBJECT_MAPPER.createObjectNode());
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
            try {
                return bytes = ByteBuffer.wrap(OBJECT_MAPPER.writeValueAsBytes(json));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
            try (InputStream stream = new ByteBufInputStream(Unpooled.wrappedBuffer(bytes))) {
                return json = OBJECT_MAPPER.readValue(stream, ObjectNode.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (string != null) {
            try {
                return json = OBJECT_MAPPER.readValue(string, ObjectNode.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
