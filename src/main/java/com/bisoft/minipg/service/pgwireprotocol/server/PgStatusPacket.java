package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.ScriptExecutor;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import java.util.List;

public class PgStatusPacket extends AbstractWireProtocolPacket {

    private static final String PG_COMM_PREFIX = "-- pg_status";

    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = (new ScriptExecutor()).executeScript(
            ConfigurationService.GetValue("minipg.postgres_bin_path") + "pg_ctl", "status",
            "-D" + ConfigurationService.GetValue("minipg.postgres_data_path"));

        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
