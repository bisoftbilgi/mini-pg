package com.bisoft.minipg.service.handler.decoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.service.util.PgProtocolParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class PgProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {
	public static final Logger logger = LoggerFactory.getLogger(PgProtocolDecoder.class);
	PgProtocolParser pgProtocolParser = new PgProtocolParser();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {

		ByteBuf copiedMsg = logPackage(byteBuf);

		WireProtocolPacket parseResult = pgProtocolParser.parsePacket(ByteUtil.decodeAsBytes(copiedMsg));
		try {
			String message = byteBuf.toString(StandardCharsets.UTF_8);
			logger.trace("decoder...:" + message);
			if (parseResult != null) {
				logger.trace("packet decoded :" + message + parseResult);
				out.add(parseResult);
				byteBuf.retain();
			} else
				out.add(byteBuf.retain());
		} catch (Exception e) {
			logger.error("Error", e);
		}

	}

	private ByteBuf logPackage(ByteBuf byteBuf) {
		// message dump...
		ByteBuf copiedMsg = ((ByteBuf) byteBuf).copy();

		String result = ByteUtil.byteArrayToHexAndAsciiAndDecDumpWithTab(ByteUtil.decodeAsBytes(copiedMsg));
		logger.trace("=================FROM CLIENT---===============\n{}", result);
		logger.trace("================================");
		copiedMsg.release();
		return byteBuf;
	}

}
