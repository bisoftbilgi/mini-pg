package com.bisoft.minipg.service.pgwireprotocol.server;

/**
 * WireProtocolPacket
 */
public interface WireProtocolPacket {
    Character getCharacterTag();

    int getLength();

    byte[] getPayload();

    String getPayloadString();

    WireProtocolPacket decode(byte[] buffer);

    WireProtocolPacket decodeBuffer(byte[] buffer);

    byte[] response();

}