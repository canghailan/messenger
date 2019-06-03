package cc.whohow.messenger.websocket;

import cc.whohow.messenger.*;
import cc.whohow.messenger.util.Json;
import cc.whohow.messenger.util.Netty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.Unpooled;
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
    private ChannelHandlerContext context;
    private FullHttpRequest request;
    private QueryStringDecoder decoder;

    public WebSocketMessengerHttpHandler(String path, MessengerService<MessageFactory, SimpleMessengerManager, MessageQueue> messengerService) {
        this.path = path;
        this.messengerService = messengerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        this.context = context;
        this.request = request;
        this.decoder = new QueryStringDecoder(request.uri());

        log.info(request.uri());

        String path = trimPath();
        if (path == null) {
            Netty.send(context, HttpResponseStatus.NOT_FOUND);
            return;
        }

        switch (path) {
            case "": {
                connect();
                break;
            }
            case "send": {
                send();
                break;
            }
            default: {
                Netty.send(context, HttpResponseStatus.NOT_FOUND);
                break;
            }
        }
    }

    private String trimPath() {
        if (decoder.path().startsWith(path)) {
            return decoder.path().substring(path.length());
        }
        return null;
    }

    private void connect() {
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

    private void send() {
        ObjectNode body = Json.deserialize(request.content(), ObjectNode.class);
        Message message = messengerService.getMessageFactory().newMessage(body);
        messengerService.sendSystemMessage(message);
        Netty.send(context, Unpooled.wrappedBuffer(message.toBytes()));
    }
}
