package com.bisoft.minipg.service.handler;

import com.bisoft.minipg.service.SessionState;
import com.bisoft.minipg.service.pgwireprotocol.server.PasswordPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.StartupPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class MiniPGFrontendHandler extends ChannelInboundHandlerAdapter {

    private Channel outboundChannel;
    ChannelFuture channelFuture;

    private boolean isAuthenticated;

    @Autowired
    private SessionState sessionState;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        log.debug("handlerRemoved: {}", ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        log.info("channelActive :" + ctx);
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        log.debug("channelRead   {} {}", msg, ctx);
        if (msg instanceof WireProtocolPacket) {
            WireProtocolPacket wireProtocolPacket = (WireProtocolPacket) msg;

            // do customization for types
            checkIfStartupPacketResponse(wireProtocolPacket);
            checkIfAuthenticationOkResponse(wireProtocolPacket);

            // and generate the result
            byte[] response = wireProtocolPacket.response();
            // add them to the response channel..
            sendBytesAndAddListener(ctx, response);

        } else if (msg instanceof ByteBuf) {
            String strMessage = ByteUtil.byteArrayToHexAndAsciiAndDecDump(ByteUtil.decodeAsBytes((ByteBuf) msg));
            log.trace("channelRead  ByteBuf {} \n{}", ctx, strMessage);
            handleByteBuf(ctx, msg);
        } else
            throw new Exception("Unknown package type.");
    }

    private void sendBytesAndAddListener(final ChannelHandlerContext ctx, final byte[] response) {

        String strResponse = ByteUtil.byteArrayToHexAndAsciiAndDecDumpWithTab(response);
        log.trace("sendBytesAndAddListener : \n{}", strResponse);

        ByteBuf       buffer        = Unpooled.copiedBuffer(response);
        Channel       channel       = ctx.channel();
        ChannelFuture channelFuture = channel.writeAndFlush(buffer);

        channelFuture.addListener((ChannelFutureListener) (future) -> {
            log.debug("sendBytesAndAddListener listener success:" + future.isSuccess());
            if (future.isSuccess()) {
                channel.read();
            } else {
                channel.close();
            }
        });
    }

    private void handleByteBuf(ChannelHandlerContext ctx, Object msg) {

        log.trace("handleByteBuf " + msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        log.debug("channel incative triggered...");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    static void closeOnFlush(Channel ch) {

        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void checkIfStartupPacketResponse(WireProtocolPacket wireProtocolPacket) {

        if (wireProtocolPacket instanceof StartupPacket) {
            StartupPacket packet = (StartupPacket) wireProtocolPacket;
            this.sessionState.setDbName(packet.getDbName());
            this.sessionState.setUserName(packet.getUserName());
            this.sessionState.setSalt(packet.getSalt());
        }
    }

    private void checkIfAuthenticationOkResponse(WireProtocolPacket wireProtocolPacket) {

        if (wireProtocolPacket instanceof PasswordPacket) {
            PasswordPacket passwordPacket = (PasswordPacket) wireProtocolPacket;
            passwordPacket.setSessionState(this.sessionState);
        }
    }
}
