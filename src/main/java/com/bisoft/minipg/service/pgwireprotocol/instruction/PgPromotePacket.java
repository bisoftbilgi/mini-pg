package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.ScriptExecutor;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgPromotePacket extends AbstractWireProtocolPacket {
    
    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;
    
    private static final String PG_COMM_PREFIX = "-- pg_promote";
    
    public WireProtocolPacket decode(byte[] buffer) {
        
        return this;
    }
    
    @Override
    public byte[] response() {
        
        List<String> cellValues = (new ScriptExecutor()).executeScriptSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
                "promote", 
                "-D" + miniPGlocalSetings.getPostgresDataPath());
        cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }
    
    public static boolean matches(String messageStr) {
        
        log.debug(messageStr);
        // System.out.println(ByteUtil.byteArrayToHexAndAsciiAndDecDump(messageStr.getBytes()));
        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
