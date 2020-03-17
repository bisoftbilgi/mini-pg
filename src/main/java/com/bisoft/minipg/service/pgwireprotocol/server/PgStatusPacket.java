package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.util.ScriptExecuter;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PgStatusPacket extends AbstractWireProtocolPacket {
    private static final String PG_COMM_PREFIX = "-- pg_status";

    @Value("${server.pg_ctl_path}")
    public String pgCtlPath;

    @Value("${server.postgres_data_path}")
    public String postgresDataPath;

    public WireProtocolPacket decode(byte[] buffer) {
        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = (new ScriptExecuter()).executeScript(
                pgCtlPath + "pg_ctl", "status",
                "-D" + postgresDataPath);

        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    public static boolean matches(String messageStr) {
        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
