package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.ErrorResponsePojo;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.ReadyForQuery;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy
public class UnauthorizedPacket extends AbstractWireProtocolPacket {

    @Override
    public WireProtocolPacket decode(byte[] buffer) {
        return this;
    }

    @Override
    public byte[] response() {
        ErrorResponsePojo response = new ErrorResponsePojo();
        response.addMessage("FATAL", "22000", "Minipg : You are not authorized to perform this operation.");
        byte[] errorResponse = response.generateMessage();
        ReadyForQuery readyForQuery = new ReadyForQuery('I');
        byte[] readyForQueryResponse = readyForQuery.generateMessage();
        return Util.concatByteArray(errorResponse, readyForQueryResponse);
    }

    public static boolean isUnauthorizedPacket(String strMessage) {
        return Util.caseInsensitiveContains(strMessage, "--unauthorized");
    }
}
