package cc.whohow.messenger.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.List;

public class Netty {
    public static String getParameter(QueryStringDecoder decoder, String key) {
        List<String> values = decoder.parameters().get(key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    public static void send(ChannelHandlerContext context, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        context.write(response);
        context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
}
