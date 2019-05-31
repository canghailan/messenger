package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.Message;
import cc.whohow.messenger.MessageFactory;
import cc.whohow.messenger.MessageQueue;
import cc.whohow.messenger.MessengerService;
import cc.whohow.messenger.util.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AliyunApiGatewayMessengerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LogManager.getLogger();
    private final MessengerService<MessageFactory, AliyunApiGatewayMessengerManager, MessageQueue> messengerService;

    public AliyunApiGatewayMessengerHandler(MessengerService<MessageFactory, AliyunApiGatewayMessengerManager, MessageQueue> messengerService) {
        this.messengerService = messengerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        log.info(request.uri());

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        switch (decoder.path()) {
            case "register": {
                register(ctx, decoder);
                return;
            }
            case "logout": {
                logout(ctx, decoder);
                return;
            }
        }
    }

    private void register(ChannelHandlerContext ctx, QueryStringDecoder request) {
        ObjectNode messenger = Json.newObject();
        messengerService.getMessengerManager().register(messenger);

    }

    private void logout(ChannelHandlerContext ctx, QueryStringDecoder request) {
        String deviceId = "";
        if (deviceId.isEmpty()) {
            return;
        }
        messengerService.getMessengerManager().logout(deviceId);
    }

    private void sendMessage(String deviceId, Message message) {
        messengerService.send(messengerService.getMessengerManager().getMessenger(deviceId), message);
    }
}
