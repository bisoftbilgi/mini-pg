package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.model.PgVersion;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.instruction.util.InstructionUtil;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
import com.bisoft.minipg.service.util.LocalSqlExecutor;
import com.bisoft.minipg.service.util.ScriptExecutor;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgRewindPacket extends AbstractWireProtocolPacket {

    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;

    @Autowired
    private InstructionUtil instructionUtil;

    @Autowired
    private LocalSqlExecutor localSqlExecutor;

    private final static int MASTER_IP_ORDER = 0;
    private final static int PORT_ORDER      = 1;
    private final static int USER_ORDER      = 2;
    private final static int PASS_ORDER      = 3;

    private static final String PG_COMMAND               = "-- pg_rewind";
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

        // pg_rewind([master_ip] *****)
        Pattern p = Pattern.compile(REGEX_COMMAND, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        localCommandParams = m.group("connParams");
        return this;
    }

    @Override
    public byte[] response() {
        // /usr/pgsql-10/bin/pg_rewind --target-pgdata=/var/lib/pgsql/10/data/.
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres
        // password=080419"

//		host=192.168.2.90 port=5432 user=postgres

//        List<String> cellValues = (new ScriptExecutor()).executeScriptSync(
//                miniPGlocalSetings.getPgCtlBinPath() + "pg_rewind",
//                "--target-pgdata=" + miniPGlocalSetings.getPostgresDataPath(),
//                "--source-server='host="+pgRewindMasterIp+"'");

        List<String> cellValues;

        String[] parameters = localCommandParams.split(",");

        // TODO: server is open now you can get the exact version from the server.
        PgVersion localVersion = PgVersion.valueOf(miniPGlocalSetings.getPgVersion());
        if (localVersion == PgVersion.V12X) {
            try {
                //step:3  1. touch <data>/standby.signal
                (new CommandExecutor()).executeCommandSync("touch",
                    miniPGlocalSetings.getPostgresDataPath() + "standby.signal");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // open
            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
        }

        cellValues = (new ScriptExecutor()).executeScriptSync(
            "bash", "-c",
            miniPGlocalSetings.getPgCtlBinPath() + "pg_rewind --target-pgdata="
                + miniPGlocalSetings.getPostgresDataPath()
                + " --source-server='host="
                + parameters[MASTER_IP_ORDER]
                + " port=" + parameters[PORT_ORDER]
                + " user=" + parameters[USER_ORDER]
                + " dbname=postgres"
                + " password=" + parameters[PASS_ORDER]
                + "'");

        // PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);
//        PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], "wentsy", "wentsy123");
//        localVersion = PgVersion.V12X;
        if (localVersion == PgVersion.V12X) {
            reGenerateRecoveryConfByExecutingStatement(parameters);
        } else {
            reGenerateRecoveryConfByEditingConfFile(parameters);
            /*a start and stop */
            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        }

        cellValues.add(0, PG_COMMAND + " pg_rewind command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    // @Deprecated
    private void reGenerateRecoveryConfByEditingConfFile(String[] parameters) {

        log.info("REGENERATING recovery.conf with " + parameters[MASTER_IP_ORDER] + ":" + parameters[PORT_ORDER]);

        String recoveryConfTemplate = getRecoveryConfTemplate();

        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(miniPGlocalSetings.getPostgresDataPath() + "recovery.conf"), "utf-8"));
            writer.write(recoveryConfTemplate.replace("{MASTER_IP}", parameters[MASTER_IP_ORDER]).replace("{MASTER_PORT}", parameters[PORT_ORDER]));
        } catch (IOException ex) {

            ex.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // @Deprecated
    private boolean reGenerateRecoveryConfByExecutingStatement(String[] parameters) {

        log.info("REGENERATING recovery.conf with " + parameters[MASTER_IP_ORDER] + ":" + parameters[PORT_ORDER]);
        String recoveryConfTemplate = instructionUtil.getRecoveryConfTemplateV12();
        String recoveryConfSql = "alter system set "
            + recoveryConfTemplate.replace("{MASTER_IP}", parameters[MASTER_IP_ORDER]).replace("{MASTER_PORT}", parameters[PORT_ORDER]);
//        PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);

        try {
            //step:3  1. touch <data>/standby.signal
            (new CommandExecutor()).executeCommandSync("touch",
                miniPGlocalSetings.getPostgresDataPath() + "standby.signal");

            //step:3  1. touch <data>/standby.signal
            (new CommandExecutor()).executeCommandSync("touch",
                miniPGlocalSetings.getPostgresDataPath() + "recovery.signal");

            //step 4:  2. start the server
            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

            //step 5:  3. execute sql
            localSqlExecutor.executeLocalSql(recoveryConfSql, parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);

            // 4. reload conf
            localSqlExecutor.executeLocalSql("SELECT pg_reload_conf()", parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);
            //step 4-a:  2. start the server
            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    private String getRecoveryConfTemplate() {

        if (miniPGlocalSetings.getOs().startsWith("windows")) {

            return instructionUtil.getRecoveryConfTemplateForWindows();
        } else {
            return instructionUtil.getRecovertConfTemplateForLinux();
        }

    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_COMMAND);
    }

}
