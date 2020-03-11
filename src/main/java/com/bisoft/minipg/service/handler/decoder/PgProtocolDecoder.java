package com.bisoft.minipg.service.handler.decoder;

import com.bisoft.minipg.service.pgwireprotocol.PgProtocolParser;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PgProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Autowired
    PgProtocolParser pgProtocolParser;
    //= new PgProtocolParser();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {

        ByteBuf copiedMsg = logPackage(byteBuf);

        WireProtocolPacket parseResult = pgProtocolParser.parsePacket(ByteUtil.decodeAsBytes(copiedMsg));
        try {
            String message = byteBuf.toString(StandardCharsets.UTF_8);
            log.trace("decoder...:" + message);
            if (parseResult != null) {
                log.trace("packet decoded :" + message + parseResult);
                out.add(parseResult);
                byteBuf.retain();
            } else
                out.add(byteBuf.retain());
        } catch (Exception e) {
            log.error("Error", e);
        }

    }

    private ByteBuf logPackage(ByteBuf byteBuf) {
        // message dump...
        ByteBuf copiedMsg = ((ByteBuf) byteBuf).copy();

        String result = ByteUtil.byteArrayToHexAndAsciiAndDecDumpWithTab(ByteUtil.decodeAsBytes(copiedMsg));
        log.trace("=================FROM CLIENT---===============\n{}", result);
        log.trace("================================");
        copiedMsg.release();
        return byteBuf;
    }

}
