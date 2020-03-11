package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PgStartPacketTest {

    private PgStartPacket sut;

    private PacketHelper packetHelper = new PacketHelper();

    @BeforeEach
    void init() {

        this.sut = new PgStartPacket();
    }

    @Test
    public void promotePacketTest() {

        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_start");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgStartPacket.matches(strMessage);
        assertTrue(packetMatches);
    }
}