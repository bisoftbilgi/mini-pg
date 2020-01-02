package com.bisoft.minipg.service.pgwireprotocol.server;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.AbstractMessageResponse;


public class ErrorResponsePojo extends AbstractMessageResponse {
    List<AbstractMap.SimpleImmutableEntry<Byte, String>> identifiedFields = new CopyOnWriteArrayList<>();

    public ErrorResponsePojo() {
        super();
        this.characterTag = 'E';
    }

    public ErrorResponsePojo addMessage(String sSeverity, String sqlState, String humanReadableMessage) {
        addField('S', sSeverity);
        addField('V', sSeverity);
        addField('C', sqlState);
        addField('M', humanReadableMessage);
        return this;
    }

    public ErrorResponsePojo addField(char character, String message) {
        identifiedFields.add(new SimpleImmutableEntry<Byte, String>((byte) character, message));
        return this;
    }

    @Override
    public byte[] generateMessage() {
        byte[] fields = Util.EMPTY_BYTE_ARRAY;
        for (SimpleImmutableEntry<Byte, String> entry : this.identifiedFields) {
            byte[] value = Util.toCString(entry.getValue());
            byte[] key = Util.int8ByteArray(entry.getKey());
            byte[] keyValue = Util.concatByteArray(key, value);
            fields = Util.concatByteArray(fields, keyValue);
        }
        fields = Util.concatByteArray(fields, Util.byteZero());
        this.length = fields.length + LENGTH_OF_LENGTH_FIELD;
        return Util.concatByteArray(characterTagAndLength(), fields);
    }

    public static byte[] generateErrorResponse(String sSeverity, String sqlState, String humanReadableMessage) {
        ErrorResponsePojo response = new ErrorResponsePojo();
        response.addMessage(sSeverity, sqlState, humanReadableMessage);
        return response.generateMessage();
    }

}