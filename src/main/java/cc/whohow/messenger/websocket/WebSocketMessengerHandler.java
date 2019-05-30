package cc.whohow.messenger.websocket;

import cc.whohow.messenger.Message;
import cc.whohow.messenger.MessageType;
import cc.whohow.messenger.Messenger;
import cc.whohow.messenger.MessengerService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketMessengerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LogManager.getLogger();
    private final MessengerService messengerService;
    private Channel channel;
    private Messenger messenger;

    public WebSocketMessengerHandler(MessengerService messengerService) {
        this.messengerService = messengerService;
    }

    private void onMessage(Message message) {
        channel.writeAndFlush(new TextWebSocketFrame(Unpooled.wrappedBuffer(message.toBytes())));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame frame) throws Exception {
        try {
            messengerService.send(messenger, messengerService.newMessage(frame.content().nioBuffer()));
        } catch (Throwable e) {
            onMessage(messengerService.newErrorMessage(messenger, frame.text(), e));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        super.channelActive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof Messenger) {
            messenger = (Messenger) evt;

            log.info("online {} {}", channel.id(), messenger);
            messengerService.subscribe(messenger, this::onMessage);
            messengerService.sendEventMessage(messenger, MessageType.ONLINE.toString());
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (messenger != null) {
            log.info("offline {} {}", channel.id(), messenger);
            messengerService.unsubscribe(messenger, this::onMessage);
            messengerService.sendEventMessage(messenger, MessageType.OFFLINE.toString());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause);
    }
}
