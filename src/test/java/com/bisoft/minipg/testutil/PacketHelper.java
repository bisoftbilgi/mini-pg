package com.bisoft.minipg.testutil;

import org.springframework.stereotype.Component;

@Component
public class PacketHelper {

    public byte[] convertToClientParsePacket(final String inputString) {

        ParsePacket packet = new ParsePacket();
        packet.setQueryString(inputString);
        packet.setName("");
        return packet.response();
    }
}
