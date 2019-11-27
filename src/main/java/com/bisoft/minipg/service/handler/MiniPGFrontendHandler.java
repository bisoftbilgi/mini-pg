package com.bisoft.minipg.service.handler;

import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.util.ByteUtil;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Slf4j
public class MiniPGFrontendHandler extends ChannelInboundHandlerAdapter {

	public static final Logger logger = LoggerFactory.getLogger(MiniPGFrontendHandler.class);
	private Channel outboundChannel;
	ChannelFuture channelFuture;

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
			byte[] response = wireProtocolPacket.response();
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

		ByteBuf buffer = Unpooled.copiedBuffer(response);
		Channel channel = ctx.channel();
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

}
