package com.bisoft.minipg.service.util;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgProtocolParser {

    @Autowired
    protected ContextWrapper contextWrapper;

    public WireProtocolPacket parsePacket(byte[] buffer) {

        String strMessage = ByteUtil.byteArrayToAsciiDump(buffer);
        log.trace("parsePacket  " + strMessage);

        Class<? extends WireProtocolPacket> resultType = ParsePacket.class;

        if (HelloPacket.packetMatches(buffer)) {
            resultType = HelloPacket.class;
        } else if (StartupPacket.packetMatches(buffer)) {
            resultType = StartupPacket.class;
        } else if (PasswordPacket.packetMatches(buffer)) {
            resultType = PasswordPacket.class;
        } else if (PgRewindPacket.matches(strMessage)) {
            resultType = PgRewindPacket.class;
        } else if (PgStartPacket.matches(strMessage)) {
            resultType = PgStartPacket.class;
        } else if (PgStatusPacket.matches(strMessage)) {
            resultType = PgStatusPacket.class;
        } else if (PgStopPacket.matches(strMessage)) {
            resultType = PgStopPacket.class;
        } else if (PgPromotePacket.matches(strMessage)) {
            resultType = PgPromotePacket.class;
        } else if (Util.caseInsensitiveContains(strMessage, "version")) {
            resultType = VersionPacket.class;
        } else if (PgAddSyncSlave.matches(strMessage)) {
            resultType = PgAddSyncSlave.class;
        } else if (PgRemoveSyncSlave.matches(strMessage)) {
            resultType = PgRemoveSyncSlave.class;
        }else if (MiniPgStatusPacket.matches(strMessage)) {
            resultType = MiniPgStatusPacket.class;
        } else if (ParsePacket.packetMatches(buffer)) {
            resultType = ParsePacket.class;
        }

        return ((AbstractWireProtocolPacket) contextWrapper.createBean(resultType)).decodeBuffer(buffer);

    }

}
