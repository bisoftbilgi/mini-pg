package com.bisoft.minipg.service.pgwireprotocol.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.bisoft.minipg.service.pgwireprotocol.instruction.PgCheckpointPacket;
import com.bisoft.minipg.service.util.ByteUtil;
import com.bisoft.minipg.testutil.PacketHelper;
import org.junit.jupiter.api.Test;

class PgCheckpointPacketTest {
    
    private PacketHelper packetHelper = new PacketHelper();
    
    @Test
    public void checkpointPacketTest() {
        
        byte[]  buffer        = packetHelper.convertToClientParsePacket("-- pg_checkpoint(5432,postgres,080419)");
        String  strMessage    = ByteUtil.byteArrayToAsciiDump(buffer);
        boolean packetMatches = PgCheckpointPacket.matches(strMessage);
        assertTrue(packetMatches);
        
    }
}