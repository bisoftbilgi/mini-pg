package com.bisoft.minipg.service.pgwireprotocol.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class PasswordPacket extends AbstractWireProtocolPacket {

    public static final Logger logger = LoggerFactory.getLogger(PasswordPacket.class);

    String hash;
    Object msg;

    public WireProtocolPacket decode(byte[] buffer) {
        // 0000 70 00 00 00 28 6d 64 35 64 31 65 61 65 64 31 31
        // p . . . ( m d 5 d 1eaed11

        int packetLengt = buffer.length; // ByteUtil.fromByteArray(buffer);

        hash = new String(Arrays.copyOfRange(buffer, 8, packetLengt));
        log.trace("PasswordPacket hash : " + hash);
        byte[] hashBytes = hash.getBytes(StandardCharsets.UTF_8);
        log.trace("PasswordPacket hash : " + hashBytes);
        return this;
    }

    public String getHash() {

        return hash;
    }

    public void setHash(String value) {

        hash = value;
    }

    public Object getMsg() {

        return msg;
    }

    public void setMsg(Object message) {

        this.msg = message;
    }

    @Override
    public String toString() {

        return "[PasswordPacket:" + hash + "]";
    }

    @Override
    public byte[] response() {

        return PgConstants.AUTH_OK;
    }

    public static boolean packetMatches(byte[] buffer) {
        // TODO: We have to implement a password authentication here!!!

        return buffer.length > 8 && buffer[0] == 112 && buffer[4] == 40 && buffer[5] == 109 && buffer[6] == 100
            && buffer[7] == 53;
    }
}
