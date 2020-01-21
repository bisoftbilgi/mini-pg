package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ErrorResponse extends AbstractMessageResponse {

    List<AbstractMap.SimpleImmutableEntry<Byte, String>> identifiedFields = new CopyOnWriteArrayList<>();

    public ErrorResponse() {

        super();
        this.characterTag = 'E';
    }

    public ErrorResponse addMessage(String sSeverity, String sqlState, String humanReadableMessage) {
        // sSeverity:ERROR, FATAL, PANIC , WARNING, NOTICE, DEBUG, INFO, or LOG
        // sqlState: https://www.postgresql.org/docs/current/errcodes-appendix.html
        addField('S', sSeverity);
        addField('V', sSeverity);
        addField('C', sqlState);
        addField('M', humanReadableMessage);
        return this;
    }

    public ErrorResponse addField(char character, String message) {

        identifiedFields.add(new AbstractMap.SimpleImmutableEntry<Byte, String>((byte) character, message));
        return this;
    }

    @Override
    public byte[] generateMessage() {

        byte[] fields = Util.EMPTY_BYTE_ARRAY;
        for (AbstractMap.SimpleImmutableEntry<Byte, String> entry : this.identifiedFields) {
            byte[] value    = Util.toCString(entry.getValue());
            byte[] key      = Util.int8ByteArray(entry.getKey());
            byte[] keyValue = Util.concatByteArray(key, value);
            fields = Util.concatByteArray(fields, keyValue);
        }
        fields = Util.concatByteArray(fields, Util.byteZero());
        this.length = fields.length + LENGTH_OF_LENGTH_FIELD;
        return Util.concatByteArray(characterTagAndLength(), fields);
    }

    public static byte[] generateErrorResponse(String sSeverity, String sqlState, String humanReadableMessage) {

        ErrorResponse response = new ErrorResponse();
        response.addMessage(sSeverity, sqlState, humanReadableMessage);
        return response.generateMessage();
    }

}
