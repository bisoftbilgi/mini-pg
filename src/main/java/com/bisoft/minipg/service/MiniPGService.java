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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public final class MiniPGService {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

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

            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {

                    log.debug("HANDLING NEW SERVICE....");
                    MiniPGFrontendHandler pgProxyService = getMiniPGFrontendHandler();

                    socketChannel.pipeline().addLast(getPgProtocolDecoder());
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

    public PgProtocolDecoder getPgProtocolDecoder() {

        return (PgProtocolDecoder) autowireCapableBeanFactory.createBean(PgProtocolDecoder.class,
            AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
    }

    private MiniPGFrontendHandler getMiniPGFrontendHandler() {

        return (MiniPGFrontendHandler) autowireCapableBeanFactory.createBean(MiniPGFrontendHandler.class,
            AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
    }
}
