package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
import com.bisoft.minipg.service.util.LocalSqlExecutor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgCheckpointPacket extends AbstractWireProtocolPacket {
    private final static int PORT_ORDER = 0;
    private final static int USER_ORDER = 1;
    private static final int PASS_ORDER = 2;
    
    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;
    
    // expected format is: --pg_checkpoint(5432,postgres,080419)
    // localPort,localUser,localPassword
    private static final String PG_COMMAND               = "-- pg_checkpoint";
    private static final String LOCAL_COMMAND_PARAMETERS = "(?<connParams>.*)";
    private static final String RIGHT_PARANTHESIS        = "[)]";
    private static final String LEFT_PARANTHESIS         = "[(]";
    String                      REGEX_COMMAND            = ".*"
            + PG_COMMAND
            + LEFT_PARANTHESIS
            + LOCAL_COMMAND_PARAMETERS
            + RIGHT_PARANTHESIS
            + ".*";
    public String               localCommandParams;
    
    public WireProtocolPacket decode(byte[] buffer) {
        
        // pg_rewind([master_ip] *****)
        Pattern p = Pattern.compile(REGEX_COMMAND, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        localCommandParams = m.group("connParams");
        return this;
    }
    
    @Override
    public byte[] response() {
        
        List<String> cellValues = new ArrayList<String>();
        
        String[] parameters = localCommandParams.split(",");
        
        (new LocalSqlExecutor()).executeLocalSql("CHECKPOINT", parameters[PORT_ORDER], parameters[USER_ORDER],
                parameters[PASS_ORDER]);
        cellValues.add(0, " checkpoint command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }
    
    public static boolean matches(String messageStr) {
        
        return Util.caseInsensitiveContains(messageStr, PG_COMMAND);
    }
    
}
