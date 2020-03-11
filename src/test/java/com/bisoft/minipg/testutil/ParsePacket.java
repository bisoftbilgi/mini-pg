package com.bisoft.minipg.testutil;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy
public class ParsePacket extends AbstractWireProtocolPacket {

    /*

1BYTE: Byte1('P'): Identifies the message as a Parse command.
4BYTE: Int32: Length of message contents in bytes, including self.
NBYTE: String: The name of the destination prepared statement (an empty string selects the unnamed prepared statement).
MBYTE: String The query string to be parsed.
2BYTE: Int16: The number of parameter data types specified (can be zero). Note that this is not an indication of the number of parameters that might appear in the query string, only the number that the frontend wants to prespecify types for.
Then, for each parameter, there is the following:
OPTIONAL-ARRAY Int32: Specifies the object ID of the parameter data type. Placing a zero here is equivalent to leaving the type unspecified.
* */

    public static final int LENGTH_OF_LENGTH_FIELD = 4;

    private static final byte[] PACKET_START = new byte[]{0x52, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x05};

    private String queryString;
    private String name;


    @Override
    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        this.characterTag = 'P';


        byte[] result = Util.concatByteArray(name.getBytes(), Util.byteZero());
        result = Util.concatByteArray(result,queryString.getBytes());
        result = Util.concatByteArray(result,Util.byteZero());
        result = Util.concatByteArray(result,Util.int16ByteArray(0));

        this.length = result.length + LENGTH_OF_LENGTH_FIELD;
        result = Util.concatByteArray(characterTagAndLength(), result);

        return result;

    }

    public byte[] characterTagAndLength() {

        return Util.concatByteArray(new byte[]{(byte) characterTag}, Util.int32ByteArray(length));
    }

    public void setQueryString(String sentence) {

        this.queryString = sentence;
    }

    public void setName(String name) {

        this.name = name;
    }

}
