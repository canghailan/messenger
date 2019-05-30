package cc.whohow.messenger;

import cc.whohow.messenger.kafka.KafkaMessengerService;
import cc.whohow.messenger.websocket.WebSocketMessengerInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MessengerServer implements Runnable {
    private static final Logger log = LogManager.getLogger();
    private JsonNode configuration;
    private MessengerService messengerService;

    public static JsonNode getConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        File file = new File("messenger.yml");
        if (file.exists()) {
            try {
                return objectMapper.readTree(file);
            } catch (Throwable e) {
                log.error(e);
                System.exit(1);
            }
        }
        return objectMapper.createObjectNode();
    }

    public static void main(String[] args) {
        new MessengerServer().run();
    }

    private ChannelInitializer<SocketChannel> newChannelInitializer(MessengerService messengerService) {
        JsonNode ws = configuration.path("ws");
        String path = ws.path("path").asText("/ws/");
        return new WebSocketMessengerInitializer(path, messengerService);
    }

    private MessengerService newMessengerService() {
        JsonNode kafka = configuration.get("kafka");
        if (kafka != null) {
            return new KafkaMessengerService(kafka);
        }
        return new SimpleMessengerService();
    }

    private int getPort() {
        return configuration.path("port").asInt(80);
    }

    private int getLogInterval() {
        return configuration.path("log-interval").asInt(15);
    }

    @Override
    public void run() {
        configuration = getConfiguration();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            messengerService = newMessengerService();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(newChannelInitializer(messengerService))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channel = bootstrap.bind(getPort());
            channel.sync();
            log.info("listening {}", getPort());

            workerGroup.submit(messengerService);
            workerGroup.scheduleAtFixedRate(this::log, 0, getLogInterval(), TimeUnit.SECONDS);

            channel.channel().closeFuture().sync();
        } catch (Throwable e) {
            log.error("error");
            log.error(e.getMessage(), e);
        } finally {
            log.info("shutdown");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void log() {
        log.info(messengerService);
    }
}
