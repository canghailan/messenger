package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.MessageFactory;
import cc.whohow.messenger.MessageQueue;
import cc.whohow.messenger.MessengerService;
import cc.whohow.messenger.util.Json;
import cc.whohow.messenger.util.Netty;
import cc.whohow.messenger.util.Text;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class AliyunApiGatewayMessengerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LogManager.getLogger();
    private final MessengerService<MessageFactory, AliyunApiGatewayMessengerManager, MessageQueue> messengerService;
    private ChannelHandlerContext context;
    private FullHttpRequest request;
    private QueryStringDecoder decoder;

    public AliyunApiGatewayMessengerHandler(MessengerService<MessageFactory, AliyunApiGatewayMessengerManager, MessageQueue> messengerService) {
        this.messengerService = messengerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        log.info(request.uri());
        this.context = context;
        this.request = request;
        this.decoder = new QueryStringDecoder(request.uri());

        switch (decoder.path()) {
            case "/register": {
                register();
                return;
            }
            case "/logout": {
                logout();
                return;
            }
            default: {
                Netty.send(this.context, HttpResponseStatus.NOT_FOUND);
            }
        }
    }

    private void register() {
        String device = getDevice();
        String uid = Netty.getParameter(decoder, "uid");
        String tags = Netty.getParameter(decoder, "tags");
        Set<String> tagSet = Text.splitAsSet(tags);
        if (Text.isEmpty(device) || Text.isEmpty(uid) || tagSet.isEmpty()) {
            Netty.send(context, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        ArrayNode tagArray = Json.newArray();
        Text.splitAsSet(tags).forEach(tagArray::add);

        ObjectNode messenger = Json.newObject();
        messenger.put("device", device);
        messenger.put("uid", uid);
        messenger.set("tags", tagArray);
        messengerService.getMessengerManager().register(messenger);

        Netty.send(context, HttpResponseStatus.OK);
    }

    private void logout() {
        messengerService.getMessengerManager().logout(getDevice());

        Netty.send(context, HttpResponseStatus.OK);
    }

    private String getDevice() {
        return request.headers().get("x-ca-deviceid");
    }
}
