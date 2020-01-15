package com.bisoft.minipg.service;

import com.bisoft.minipg.service.handler.MiniPGFrontendHandler;
import com.bisoft.minipg.service.handler.decoder.PgProtocolDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public final class MiniPGService {

    public static final Logger logger = LoggerFactory.getLogger(MiniPGService.class);

    final static int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort", "9998"));

    public void start() {

        new Thread(() -> {
            try {
                run();
            } catch (Exception e) {
                log.error("Error", e);
            }
        }).start();
    }

    private void run() throws InterruptedException {

        log.info("Mini-Pg started ");

        // Configure the bootstrap.
        EventLoopGroup bossGroup   = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            log.info("listening on port: {}", LOCAL_PORT);

            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            // TODO: for package tracing operations please  open the commented   following line
            // b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {

                    log.debug("HANDLING NEW SERVICE....");
                    MiniPGFrontendHandler pgProxyService = new MiniPGFrontendHandler();

                    socketChannel.pipeline().addLast(new PgProtocolDecoder());
                    socketChannel.pipeline().addLast(pgProxyService);
                }

            });

            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.childOption(ChannelOption.AUTO_READ, false);
            ChannelFuture f = b.bind(LOCAL_PORT).sync();
            f.channel().closeFuture().sync();

        } finally {

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
