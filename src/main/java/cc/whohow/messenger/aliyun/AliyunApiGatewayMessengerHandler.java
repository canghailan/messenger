package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AliyunApiGatewayMessengerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LogManager.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        log.info(request.uri());

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
    }

    private void register(ChannelHandlerContext ctx, QueryStringDecoder request) {

    }

    private void logout(ChannelHandlerContext ctx, QueryStringDecoder request) {

    }

    private void onMessage(Message message) {

    }
}
