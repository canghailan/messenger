package cc.whohow.messenger.websocket;

import cc.whohow.messenger.Messenger;
import cc.whohow.messenger.MessengerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.util.List;

public class WebSocketMessengerHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String path;
    private final MessengerService messengerService;

    public WebSocketMessengerHttpHandler(String path, MessengerService messengerService) {
        this.path = path;
        this.messengerService = messengerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        if (!path.equals(decoder.path())) {
            error(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        try {
            String uid = getParameter(decoder, "uid");
            String tags = getParameter(decoder, "tags");
            Messenger messenger = messengerService.newMessenger(uid, tags);
            ctx.fireUserEventTriggered(messenger);
            ctx.fireChannelRead(request.retain());
        } catch (Throwable e) {
            error(ctx, HttpResponseStatus.BAD_REQUEST);
        }
    }

    private void error(ChannelHandlerContext context, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        context.write(response);
        context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        context.close();
    }

    private String getParameter(QueryStringDecoder decoder, String key) {
        List<String> values = decoder.parameters().get(key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }
}
