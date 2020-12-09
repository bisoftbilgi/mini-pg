package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PgStartPacket extends AbstractWireProtocolPacket {

    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;

    private static final String PG_START = "-- pg_start";

    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        boolean      result     = false;
        List<String> cellValues = doStart();
        for (String cell : cellValues) {
            if (cell.contains("done")) {
                result = true;
                break;
            }
        }
        cellValues.add(0, "true");
        cellValues.add(0, PG_START + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    private List<String> doStart() {

        return (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
            "-D" + miniPGlocalSetings.getPostgresDataPath());
    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_START);
    }
}
