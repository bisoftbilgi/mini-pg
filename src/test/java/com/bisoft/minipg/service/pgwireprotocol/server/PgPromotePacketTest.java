package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.testutil.PacketHelper;
import com.bisoft.minipg.service.util.ByteUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PgPromotePacketTest {

    private PgPromotePacket sut;

    private PacketHelper packetHelper=new PacketHelper();

    @BeforeEach
    void init() {

        this.sut = new PgPromotePacket();
    }

    @Test
    public void promotePacketTest() {


//        // P   ! -- pg_rewind(192.168.5.8)   B            D   P E         S   
//        byte[] buffer = new byte[] { 80, 0, 0, 0, 33, 0, 45, 45, 32, 112, 103, 95, 114, 101, 119, 105, 110, 100, 40, 49,
//            57, 50, 46, 49, 54, 56, 46, 53, 46, 56, 41, 0, 0, 0, 66, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 68, 0, 0,
//            0, 6, 80, 0, 69, 0, 0, 0, 9, 0, 0, 0, 0, 0, 83, 0, 0, 0, 4 };

//        strMessage = ByteUtil.byteArrayToAsciiDump(buffer);
        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_rewind(192.168.5.8)");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgRewindPacket.matches(strMessage);
        assertTrue(packetMatches);

    }
}