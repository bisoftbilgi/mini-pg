package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class VersionPacket extends AbstractWireProtocolPacket {

    private static final String BISOFT_BFM_PROXY = "BiSoft MiniPg v.0.0.1";

    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = Arrays.asList(BISOFT_BFM_PROXY);
        Table        table      = (new TableHelper()).generateSingleColumnTable("version", cellValues, "SELECT");
        return table.generateMessage();
    }

}
