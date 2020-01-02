package com.bisoft.minipg.service.pgwireprotocol.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
//
//import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
//import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;

@Configuration
@Lazy
public class UnknownPacket extends AbstractWireProtocolPacket {

    @Override
    public WireProtocolPacket decode(byte[] buffer) {
        this.receivedBuffer = buffer;
        return this;
    }

    @Override
    public byte[] response() {
        return this.receivedBuffer;
    }
}
