package com.bisoft.minipg.service.pgwireprotocol.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.AbstractMessageResponse;
import com.bisoft.minipg.service.util.ByteUtil;

/**
 * AbstractWireProtocolPacket
 */
public abstract class AbstractWireProtocolPacket implements WireProtocolPacket {

    public static final int LENGTH_OF_CHARACTER_TAG = 1;
    public static final int LENGTH_OF_LENGTH_FIELD = 4;
    public static final int LENGTH_OF_CHARACTER_TAG_AND_LENGTH_FIELD = LENGTH_OF_CHARACTER_TAG + LENGTH_OF_LENGTH_FIELD;
    protected char characterTag;
    protected int length;
    protected byte[] payload;
    protected byte[] receivedBuffer;

    @Override
    public WireProtocolPacket decodeBuffer(byte[] buffer) {

        receivedBuffer = buffer;

        this.characterTag = (char) buffer[0];

        byte[] lengthBuffer = Arrays.copyOfRange(buffer, 1, 5);
        int payloadStart = 5;
        int payloadEnd = buffer.length - 1;

        if (characterTag == 0) {
            lengthBuffer = Arrays.copyOfRange(buffer, 0, 4);
            payloadStart = 4;
            payloadEnd = buffer.length;
        }

        this.length = ByteUtil.fromByteArray(lengthBuffer);
        payload = Arrays.copyOfRange(buffer, payloadStart, payloadEnd);
        return decode(buffer);
    }

    @Override
    public Character getCharacterTag() {
        return (char) this.characterTag;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String getPayloadString() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public byte[] errorResponse(String string) {
        return null;
    }

    public byte[] characterTagAndLangth() {
        return Util.concatByteArray(new byte[] { (byte) characterTag }, Util.int32ByteArray(length));
    }

    @Override
    public byte[] response() {
        AbstractMessageResponse messageResponse = generateReponse();
        return messageResponse.generateMessage();
    }

    public AbstractMessageResponse generateReponse() {
        throw new UnsupportedOperationException();
    }

}