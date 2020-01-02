package com.bisoft.minipg.service.util;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Slf4j
public class PgProtocolParser {
    private static final int LENGTH_OF_CHARACTER_TAG = 1;

    public WireProtocolPacket parsePacket(byte[] buffer) {

        WireProtocolPacket result = null;
        String strMessage = ByteUtil.byteArrayToAsciiDump(buffer);
        Class<? extends WireProtocolPacket> resultType = detectPacket(buffer, strMessage);
        log.trace("parsePacket  " + strMessage);
        result.decodeBuffer(buffer);
        return result;
    }

    private Class<? extends WireProtocolPacket> detectPacket(byte[] buffer, String strMessage) {
        Class<? extends WireProtocolPacket> resultType = UnknownPacket.class;

        if (HelloPacket.packetMatches(buffer)) {
            resultType = HelloPacket.class;
        } else if (StartupPacket.packetMatches(buffer)) {
            resultType = StartupPacket.class;
        } else if (PasswordPacket.packetMatches(buffer)) {
            resultType = PasswordPacket.class;
        } else if (UnauthorizedPacket.isUnauthorizedPacket(strMessage)) {
            resultType = UnauthorizedPacket.class;
        } else if (PgRewindPacket.matches(strMessage)) {
            resultType = PgRewindPacket.class;
        } else if (PgStartPacket.matches(strMessage)) {
            resultType = PgStartPacket.class;
        } else if (PgStopPacket.matches(strMessage)) {
            resultType = PgStopPacket.class;
        } else if (PgStatusPacket.matches(strMessage)) {
            resultType = PgStatusPacket.class;
        } else if (PgPromotePacket.matches(strMessage)) {
            resultType = PgPromotePacket.class;
        } else if (Util.caseInsensitiveContains(strMessage, "version")) {
            resultType = VersionPacket.class;
        } else if (PgAddSyncSlave.matches(strMessage)) {
                resultType = PgAddSyncSlave.class;
            } else if (PgRemoveSyncSlave.matches(strMessage)) {
                resultType = PgRemoveSyncSlave.class;
        } else if (ParsePacket.packetMatches(buffer)) {
            resultType = ParsePacket.class;
        }
        return resultType;
    }
}
