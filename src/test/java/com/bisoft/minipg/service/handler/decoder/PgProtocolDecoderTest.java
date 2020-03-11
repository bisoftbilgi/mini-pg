package com.bisoft.minipg.service.handler.decoder;

import com.bisoft.minipg.testutil.PacketHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PgProtocolDecoderTest {

    private PacketHelper packetHelper = new PacketHelper();

    @Test
    public void testPgProtocolDecoder() {

        byte[] buffer = packetHelper.convertToClientParsePacket("-- pg_status");

        ByteBuf buf = Unpooled.buffer();
        for (byte item : buffer) {
            buf.writeByte(item);
        }

        ByteBuf         input   = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new PgProtocolDecoder());
        assertTrue(channel.writeOutbound(input.retain()));
        assertTrue(channel.finish());

//        buf.release();
//        input.release();

    }

}