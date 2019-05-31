package cc.whohow.messenger.websocket;

import cc.whohow.messenger.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * WebSocket连接处理
 */
public class WebSocketMessengerHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LogManager.getLogger();
    private final String path;
    private final MessengerService<MessageFactory, SimpleMessengerManager, MessageQueue> messengerService;

    public WebSocketMessengerHttpHandler(String path, MessengerService<MessageFactory, SimpleMessengerManager, MessageQueue> messengerService) {
        this.path = path;
        this.messengerService = messengerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        log.info(request.uri());

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        if (!path.equals(decoder.path())) {
            error(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        try {
            String uid = getParameter(decoder, "uid");
            String tags = getParameter(decoder, "tags");
            Messenger messenger = messengerService.getMessengerManager().newMessenger(uid, tags);
            ctx.fireUserEventTriggered(messenger);
            ctx.fireChannelRead(request.retain());
        } catch (Throwable e) {
            log.error(e);
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
