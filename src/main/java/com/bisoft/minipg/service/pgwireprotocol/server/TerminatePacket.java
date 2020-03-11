package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import java.util.Arrays;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy
public class TerminatePacket extends AbstractWireProtocolPacket {

    public static final byte[] SIGNATURE = new byte[]{'X', 0x00, 0x00, 0x00, 0x04};

    @Override
    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        return Util.EMPTY_BYTE_ARRAY;
    }

    @Override
    public WireProtocolPacket decodeBuffer(byte[] buffer) {

        payload = new byte[]{0x00, 0x00, 0x00, 0x04};
        return decode(buffer);
    }

    public static boolean packetMatches(byte[] buffer) {
        // return buffer.length > 3 && buffer[0] == 'X' ;
        return buffer.length > 3 && Arrays.equals(SIGNATURE, buffer);

    }
}
