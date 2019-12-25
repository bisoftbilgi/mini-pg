package com.bisoft.minipg.service.util;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.HelloPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.ParsePacket;
import com.bisoft.minipg.service.pgwireprotocol.server.PasswordPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.PgAddSyncSlave;
import com.bisoft.minipg.service.pgwireprotocol.server.PgPromotePacket;
import com.bisoft.minipg.service.pgwireprotocol.server.PgRemoveSyncSlave;
import com.bisoft.minipg.service.pgwireprotocol.server.PgRewindPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.PgStartPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.PgStatusPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.PgStopPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.StartupPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.VersionPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class PgProtocolParser {
    
    public static final Logger logger = LoggerFactory.getLogger(PgProtocolParser.class);
    
    public WireProtocolPacket parsePacket(byte[] buffer) {
        
        WireProtocolPacket result     = null;
        String             strMessage = ByteUtil.byteArrayToAsciiDump(buffer);
        log.trace("parsePacket  " + strMessage);
        if (HelloPacket.packetMatches(buffer)) {
            result = new HelloPacket().decodeBuffer(buffer);
        } else if (StartupPacket.packetMatches(buffer)) {
            result = new StartupPacket().decodeBuffer(buffer);
        } else if (PasswordPacket.packetMatches(buffer)) {
            result = new PasswordPacket().decodeBuffer(buffer);
        } else if (PgRewindPacket.matches(strMessage)) {
            result = new PgRewindPacket().decodeBuffer(buffer);
        } else if (PgStartPacket.matches(strMessage)) {
            result = new PgStartPacket().decodeBuffer(buffer);
        } else if (PgStatusPacket.matches(strMessage)) {
            result = new PgStatusPacket().decodeBuffer(buffer);
        } else if (PgStopPacket.matches(strMessage)) {
            result = new PgStopPacket().decodeBuffer(buffer);
        } else if (PgPromotePacket.matches(strMessage)) {
            result = new PgPromotePacket().decodeBuffer(buffer);
        } else if (Util.caseInsensitiveContains(strMessage, "version")) {
            result = new VersionPacket().decodeBuffer(buffer);
        } else if (PgAddSyncSlave.matches(strMessage)) {
            result = new PgAddSyncSlave().decodeBuffer(buffer);
        } else if (PgRemoveSyncSlave.matches(strMessage)) {
            result = new PgRemoveSyncSlave().decodeBuffer(buffer);
        } else if (ParsePacket.packetMatches(buffer)) {
            result = new ParsePacket().decodeBuffer(buffer);
        }
        
        return result;
        
    }
    
}
