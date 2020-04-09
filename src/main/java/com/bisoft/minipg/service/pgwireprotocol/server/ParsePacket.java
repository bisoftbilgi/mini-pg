package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.response.BindComplete;
import com.bisoft.minipg.service.pgwireprotocol.server.response.CommandComplete;
import com.bisoft.minipg.service.pgwireprotocol.server.response.ParseComplete;
import com.bisoft.minipg.service.pgwireprotocol.server.response.ReadyForQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ParsePacket extends AbstractWireProtocolPacket {
    private static final int CHARACTER_TAG_START = 0;
    private static final int LENGTH_START = CHARACTER_TAG_START + 1;
    private static final int NAME_START = LENGTH_START + 4;

    private String name = "";
    private String queryString = "";
    private int lengthParameterDataTypes = 0;
    private int[] parameterDataTypes;

    public WireProtocolPacket decode(byte[] buffer) {
        int packetLength = Util.readInt32(buffer, LENGTH_START);
        if (packetLength != getLength()) {
            log.trace("ParsePacket.decode Length MISMATCH");
        }
        this.name = Util.readCString(buffer, NAME_START);
        int index = NAME_START + name.length() + 1;
        this.queryString = Util.readCString(buffer, index);
        index = index + (queryString.length() + 1);
        if (index + 1 < packetLength) {
            this.lengthParameterDataTypes = Util.readInt16(buffer, index);
            this.parameterDataTypes = Util.readInt32Array(buffer, index + 2, lengthParameterDataTypes);
            log.warn("unknown command? you should look at this:", this.toString());
            log.trace(this.toString());
        }
        return this;
    }

    @Override
    public byte[] response() {
        byte[] result = Util.concatByteArray((new ParseComplete()).generateMessage(),
                (new BindComplete()).generateMessage());
        result = handleJdbcPacket(result);

        result = Util.concatByteArray(result, (new ReadyForQuery('I')).generateMessage());
        return result;
    }

    private byte[] handleJdbcPacket(byte[] result) {
        if (this.queryString.contains("SET extra_float_digits = 3")) {
            result = Util.concatByteArray(result, (new CommandComplete("SET", 0)).generateMessage());
        }

        return result;
    }

    public static boolean packetMatches(byte[] buffer) {
        return buffer.length > 10 && buffer[0] == 'P';
    }

    @Override
    public String toString() {
        return this.name + " | " + queryString + " | " + lengthParameterDataTypes + parameterDataTypes + " | " + length;
    }
}
