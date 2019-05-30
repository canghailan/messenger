package cc.whohow.messenger.websocket;

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
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketMessengerInitializer extends
        ChannelInitializer<SocketChannel> {
    private static final int MAX_CONTENT_LENGTH = 65536;
    private static final CorsConfig CORS_CONFIG = CorsConfigBuilder
            .forAnyOrigin()
            .build();
    private final String path;
    private final MessengerService messengerService;

    public WebSocketMessengerInitializer(String path, MessengerService messengerService) {
        this.path = path;
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
        pipeline.addLast(new WebSocketMessengerHttpHandler(path, messengerService));
        pipeline.addLast(new WebSocketServerProtocolHandler(path, true));
        pipeline.addLast(new WebSocketMessengerHandler(messengerService));
    }
}
