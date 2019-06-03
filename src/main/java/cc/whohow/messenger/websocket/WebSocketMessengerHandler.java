package cc.whohow.messenger.websocket;

import cc.whohow.messenger.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket消息处理
 */
public class WebSocketMessengerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LogManager.getLogger();
    private final MessengerService<MessageFactory, SimpleMessengerManager, MessageQueue> messengerService;
    private Channel channel;
    private Messenger messenger;
    private long timestamp;

    public WebSocketMessengerHandler(MessengerService<MessageFactory, SimpleMessengerManager, MessageQueue> messengerService) {
        this.messengerService = messengerService;
    }

    /**
     * 接收消息，发送给客户端
     */
    private void onMessage(Message message) {
        channel.writeAndFlush(new TextWebSocketFrame(Unpooled.wrappedBuffer(message.toBytes())));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame frame) throws Exception {
        try {
            messengerService.send(messenger, messengerService.getMessageFactory().newMessage(frame.content().nioBuffer()));
        } catch (Throwable e) {
            onMessage(messengerService.newErrorMessage(messenger, frame.text(), e));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        channel = context.channel();
        super.channelActive(context);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object e) throws Exception {
        if (e instanceof Messenger) {
            messenger = (Messenger) e;
            timestamp = System.currentTimeMillis();

            log.info(" online {} {}", channel.id(), messenger);
            messengerService.getMessengerManager().subscribe(messenger, this::onMessage);
            messengerService.broadcastEventMessage(messenger, MessageType.ONLINE.toString());
        }
        super.userEventTriggered(context, e);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        if (messenger != null) {
            long time = System.currentTimeMillis() - timestamp;
            log.info("offline {} {} {}s", channel.id(), messenger, TimeUnit.MILLISECONDS.toSeconds(time));
            messengerService.getMessengerManager().unsubscribe(messenger, this::onMessage);
            messengerService.broadcastEventMessage(messenger, MessageType.OFFLINE.toString());
        }
        super.channelInactive(context);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        log.error(cause);
    }
}
