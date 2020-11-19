package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.model.PgVersion;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.instruction.util.InstructionUtil;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.LocalSqlExecutor;
import com.bisoft.minipg.service.util.ScriptExecutor;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgPromotePacket extends AbstractWireProtocolPacket {

    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;
    @Autowired
    private   InstructionUtil     instructionUtil;
    @Autowired
    private   LocalSqlExecutor    localSqlExecutor;

    private final static int MASTER_IP_ORDER = 0;
    private final static int PORT_ORDER      = 1;
    private final static int USER_ORDER      = 2;
    private final static int PASS_ORDER      = 3;

    private static final String PG_COMMAND               = "-- pg_promote";
    private static final String LOCAL_COMMAND_PARAMETERS = "(?<connParams>.*)";
    private static final String RIGHT_PARANTHESIS        = "[)]";
    private static final String LEFT_PARANTHESIS         = "[(]";

    String REGEX_COMMAND = ".*"
        + PG_COMMAND
        + LEFT_PARANTHESIS
        + LOCAL_COMMAND_PARAMETERS
        + RIGHT_PARANTHESIS
        + ".*";
    public String localCommandParams;

    public WireProtocolPacket decode(byte[] buffer) {

        Pattern p = Pattern.compile(REGEX_COMMAND, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        localCommandParams = m.group("connParams");
        return this;
//        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues;

        String[]  parameters   = localCommandParams.split(",");
        PgVersion localVersion = PgVersion.valueOf(miniPGlocalSetings.getPgVersion());
        if (localVersion == PgVersion.V12X) {
            cellValues = promoteV12(parameters);
        } else {
            cellValues = promoteV10();
        }

        cellValues.add(0, PG_COMMAND + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    private List<String> promoteV10() {

        return (new ScriptExecutor()).executeScriptSync(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
            "promote",
            "-D" + miniPGlocalSetings.getPostgresDataPath());
    }

    private List<String> promoteV12(final String[] parameters) {

        List<String> result = (new ScriptExecutor()).executeScriptSync(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl",
            "promote",
            "-D" + miniPGlocalSetings.getPostgresDataPath());

        (new LocalSqlExecutor()).executeLocalSql("CHECKPOINT", parameters[PORT_ORDER], parameters[USER_ORDER],
            parameters[PASS_ORDER]);

        log.info("REGENERATING recovery.conf with " + parameters[MASTER_IP_ORDER] + ":" + parameters[PORT_ORDER]);
        String recoveryConfTemplate = instructionUtil.getRecoveryConfTemplateV12();
        String recoveryConfSql = "alter system set "
            + recoveryConfTemplate.replace("{MASTER_IP}", parameters[MASTER_IP_ORDER]).replace("{MASTER_PORT}", parameters[PORT_ORDER]);
//        PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);
        //step 5:  3. execute sql
        localSqlExecutor.executeLocalSql(recoveryConfSql, parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);

        // 4. reload conf
        localSqlExecutor.executeLocalSql("SELECT pg_reload_conf()", parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);

        return result;
    }

    public static boolean matches(String messageStr) {

        log.debug(messageStr);
        // System.out.println(ByteUtil.byteArrayToHexAndAsciiAndDecDump(messageStr.getBytes()));
        return Util.caseInsensitiveContains(messageStr, PG_COMMAND);
    }
}
