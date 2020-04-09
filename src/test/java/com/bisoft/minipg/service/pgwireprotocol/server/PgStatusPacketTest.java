package com.bisoft.minipg.service.pgwireprotocol.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.bisoft.minipg.service.pgwireprotocol.instruction.PgStatusPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.Test;

class PgStatusPacketTest {
    
    private PacketHelper packetHelper = new PacketHelper();
    
    @Test
    public void promotePacketTest() {
        
        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_status");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgStatusPacket.matches(strMessage);
        assertTrue(packetMatches);
    }
    
}