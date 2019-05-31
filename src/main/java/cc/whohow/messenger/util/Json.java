package cc.whohow.messenger.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class Json {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ArrayNode newArray() {
        return OBJECT_MAPPER.createArrayNode();
    }

    public static ObjectNode newObject() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static <T> T parse(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String stringify(Object json) {
        try {
            return OBJECT_MAPPER.writeValueAsString(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T deserialize(ByteBuffer json, Class<T> type) {
        try (InputStream stream = new ByteBufInputStream(Unpooled.wrappedBuffer(json))) {
            return OBJECT_MAPPER.readValue(stream, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteBuffer serialize(Object json) {
        try {
            return ByteBuffer.wrap(OBJECT_MAPPER.writeValueAsBytes(json));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
