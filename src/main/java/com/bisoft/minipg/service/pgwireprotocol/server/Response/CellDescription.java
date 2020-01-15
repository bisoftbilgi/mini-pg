package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import java.nio.charset.StandardCharsets;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * RowDescriptionCell
 */
public class CellDescription {
    public String fieldName = "";
    public int tableObjectId = 0;
    public int attributeNumber = 0;
    public int dataTypeObjectId = 0;
    public int typlen = 0;
    public int typeModifier = 0;
    public int formatCode = 0; // zero (text) or one (binary).

    public byte[] generateMessage() {
        byte[] result = fieldName.getBytes(StandardCharsets.UTF_8);
        result = Util.concatByteArray(result, Util.int32ByteArray(tableObjectId));
        result = Util.concatByteArray(result, Util.int16ByteArray(attributeNumber));
        result = Util.concatByteArray(result, Util.int32ByteArray(dataTypeObjectId));
        result = Util.concatByteArray(result, Util.int16ByteArray(typlen));
        result = Util.concatByteArray(result, Util.int32ByteArray(typeModifier));
        result = Util.concatByteArray(result, Util.int16ByteArray(formatCode));
        return result;
    }
}