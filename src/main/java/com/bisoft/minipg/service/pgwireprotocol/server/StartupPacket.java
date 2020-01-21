package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.util.ByteUtil;
import java.util.Arrays;
import java.util.Random;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class StartupPacket extends AbstractWireProtocolPacket {

    private int protocolVersionNumber;

    private String remoteHost;
    private int    remotePort;
    private String dbName;
    private String userName;
    private byte[] salt;

    public StartupPacket() {

        super();
        salt = new byte[4];
        new Random().nextBytes(salt);
    }

    @Override
    public String toString() {

        return remoteHost + ":" + remotePort;
    }

    @Override
    public WireProtocolPacket decode(byte[] buffer) {

        log.trace(
            "this is a startup pack... that includes: ...b....user.postgres.database.northwind.client_encoding.UTF8.DateStyle.ISO.extra_float_digits."
                + "=======================");
        int packetLengt = ByteUtil.fromByteArray(buffer);

        String params = new String(Arrays.copyOfRange(buffer, 12, packetLengt - 2));
        log.trace("length:" + params);

        return this;
    }

    @Override
    public byte[] response() {

        byte[] result = new byte[]{0x52, 0x00, 0x00, 0x00, 0x0c, 0x00,
            0x00, 0x00, 0x05};

        // R........D...
        return Util.concatByteArray(result, salt);
    }

    public static boolean packetMatches(byte[] buffer) {

        return buffer.length > 8 && buffer[5] == 3 && buffer[7] == 0;
        // protocol 3.0
    }
}
