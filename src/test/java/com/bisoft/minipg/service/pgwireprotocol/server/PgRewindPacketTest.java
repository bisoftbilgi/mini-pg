package com.bisoft.minipg.service.pgwireprotocol.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.bisoft.minipg.service.pgwireprotocol.instruction.PgRewindPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.Test;

public class PgRewindPacketTest {
  
    private PacketHelper packetHelper = new PacketHelper();

     

    @Test
    public void rewindTest() {

        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_rewind(192.168.2.91)");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgRewindPacket.matches(strMessage);
        assertTrue(packetMatches);
    }
}
