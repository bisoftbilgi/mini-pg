package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PgRewindPacketTest {
    private PgRewindPacket sut;

    private PacketHelper packetHelper = new PacketHelper();

    @BeforeEach
    void init() {

        this.sut = new PgRewindPacket();
    }

    @Test
    public void rewindTest() {

        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_rewind(192.168.2.91)");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgRewindPacket.matches(strMessage);
        assertTrue(packetMatches);
    }
}
