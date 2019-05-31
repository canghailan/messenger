package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.MessageFactory;
import cc.whohow.messenger.MessageQueue;
import cc.whohow.messenger.MessengerService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class AliyunApiGatewayMessengerInitializer extends
        ChannelInitializer<SocketChannel> {
    private static final int MAX_CONTENT_LENGTH = 65536;
    private static final CorsConfig CORS_CONFIG = CorsConfigBuilder
            .forAnyOrigin()
            .build();
    private final MessengerService<MessageFactory, AliyunApiGatewayMessengerManager, MessageQueue> messengerService;

    public AliyunApiGatewayMessengerInitializer(MessengerService<MessageFactory, AliyunApiGatewayMessengerManager, MessageQueue> messengerService) {
        this.messengerService = messengerService;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpServerKeepAliveHandler());
        pipeline.addLast(new CorsHandler(CORS_CONFIG));
        pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new AliyunApiGatewayMessengerHandler(messengerService));
    }
}
