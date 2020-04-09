package com.bisoft.minipg.service.pgwireprotocol.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.bisoft.minipg.service.pgwireprotocol.instruction.PgStartPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.Test;

class PgStartPacketTest {
    
    private PacketHelper packetHelper = new PacketHelper();
    
    @Test
    public void promotePacketTest() {
        
        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_start");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgStartPacket.matches(strMessage);
        assertTrue(packetMatches);
    }
}