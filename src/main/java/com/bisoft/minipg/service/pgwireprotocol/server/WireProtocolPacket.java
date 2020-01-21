package com.bisoft.minipg.service.pgwireprotocol.server;

import org.springframework.stereotype.Component;

/**
 * WireProtocolPacket
 */
@Component
public interface WireProtocolPacket {

    Character getCharacterTag();

    int getLength();

    byte[] getPayload();

    String getPayloadString();

    WireProtocolPacket decode(byte[] buffer);

    WireProtocolPacket decodeBuffer(byte[] buffer);

    byte[] response();

}