package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MiniPgStatusPacket extends AbstractWireProtocolPacket {

    private static final String PG_COMM_PREFIX = "-- minipg_status";

    @Override
    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = new ArrayList<>();
        cellValues.add(0, "true");
        cellValues.add(1, PG_COMM_PREFIX + " received.. " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}