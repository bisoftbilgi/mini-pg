package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class PgStartPacket extends AbstractWireProtocolPacket {

    private static final String PG_START = "-- pg_start";

    @Value("${server.pg_ctl_path}")
    public String pgCtlPath;

    @Value("${server.postgres_data_path}")
    public String postgresDataPath;

    public WireProtocolPacket decode(byte[] buffer) {
        return this;
    }

    @Override
    public byte[] response() {
        List<String> cellValues = (new CommandExecutor()).executeCommand(
                pgCtlPath + "pg_ctl", "start",
                "-D" + postgresDataPath);
        cellValues.add(0, PG_START + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    public static boolean matches(String messageStr) {
        return Util.caseInsensitiveContains(messageStr, PG_START);
    }
}
