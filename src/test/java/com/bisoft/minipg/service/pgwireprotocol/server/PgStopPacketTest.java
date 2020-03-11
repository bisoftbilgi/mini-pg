package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PgStopPacketTest {

    private PgStopPacket sut;

    private PacketHelper packetHelper = new PacketHelper();

    @BeforeEach
    void init() {

        this.sut = new PgStopPacket();
    }

    @Test
    public void promotePacketTest() {

        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_stop");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgStopPacket.matches(strMessage);
        assertTrue(packetMatches);
    }
}