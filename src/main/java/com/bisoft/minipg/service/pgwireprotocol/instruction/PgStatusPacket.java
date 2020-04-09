package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.ScriptExecutor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PgStatusPacket extends AbstractWireProtocolPacket {
    
    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;
    
    private static final String   PG_COMM_PREFIX = "-- pg_status";
    
    public WireProtocolPacket decode(byte[] buffer) {
        
        return this;
    }
    
    @Override
    public byte[] response() {
        
        List<String> cellValues = (new ScriptExecutor()).executeScript(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                "status",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
        // cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new
        // Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }
    
    public static boolean matches(String messageStr) {
        
        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
