package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.ScriptExecutor;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgPromotePacket extends AbstractWireProtocolPacket {

    private static final String PG_COMM_PREFIX = "-- pg_promote";

    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = (new ScriptExecutor()).executeScript(
            ConfigurationService.GetValue("minipg.postgres_bin_path") + "pg_ctl", "promote",
            "-D" + ConfigurationService.GetValue("minipg.postgres_data_path"));
        cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    public static boolean matches(String messageStr) {

        log.debug(messageStr);
        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
