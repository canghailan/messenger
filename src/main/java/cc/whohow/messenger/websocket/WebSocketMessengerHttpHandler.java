package cc.whohow.messenger.websocket;

import cc.whohow.messenger.*;
import cc.whohow.messenger.util.Netty;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        log.info(request.uri());

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        if (!path.equals(decoder.path())) {
            Netty.send(context, HttpResponseStatus.NOT_FOUND);
            return;
        }

        try {
            String uid = Netty.getParameter(decoder, "uid");
            String tags = Netty.getParameter(decoder, "tags");
            Messenger messenger = messengerService.getMessengerManager().newMessenger(uid, tags);
            context.fireUserEventTriggered(messenger);
            context.fireChannelRead(request.retain());
        } catch (Throwable e) {
            log.error(e);
            Netty.send(context, HttpResponseStatus.BAD_REQUEST);
        }
    }
}
