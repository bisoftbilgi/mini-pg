package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.ScriptExecutor;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class PgStopPacket extends com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket {

    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;

    private static final String PG_COMM_PREFIX = "-- pg_stop";

    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        boolean result = false;

        List<String> cellValues = doStop();
        for (String cell : cellValues) {
            if (cell.contains("done")) {
                result = true;
                break;
            }

        }
        cellValues.add(0, "true");
        cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    private List<String> doStop() {

        return (new ScriptExecutor()).executeScript(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
            "stop",
            "-D" + miniPGlocalSetings.getPostgresDataPath());
    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
