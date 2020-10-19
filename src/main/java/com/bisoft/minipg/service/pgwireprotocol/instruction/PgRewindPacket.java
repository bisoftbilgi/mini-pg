package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.model.PgVersion;
import com.bisoft.minipg.service.pgwireprotocol.Util;
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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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

        List<String> cellValues = new ArrayList<String>();

        String[] parameters = localCommandParams.split(",");

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

        PgVersion localVersion = PgVersion.valueOf(miniPGlocalSetings.getPgVersion());
        // PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);
//        PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], "wentsy", "wentsy123");
//        localVersion = PgVersion.V12X;
        if (localVersion == PgVersion.V12X || localVersion == PgVersion.V11X) {
            reGenerateRecoveryConfByExecutingStatement(parameters);
        } else {
            reGenerateRecoveryConfByEditingConfFile(parameters);
        }
        /*a start and stop */
        (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
            "-D" + miniPGlocalSetings.getPostgresDataPath());

        (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
            "-D" + miniPGlocalSetings.getPostgresDataPath());

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
        String recoveryConfTemplate = getRecoveryConfTemplateV12();
        String recoveryConfSql = "alter system set "
            + recoveryConfTemplate.replace("{MASTER_IP}", parameters[MASTER_IP_ORDER]).replace("{MASTER_PORT}", parameters[PORT_ORDER]);
//        PgVersion localVersion = localSqlExecutor.getPgVersion(parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);

        try {
            //step:3  1. touch <data>/standby.signal
            (new CommandExecutor()).executeCommandSync("touch",
                miniPGlocalSetings.getPostgresDataPath() + "standby.signal");

            //step 4:  2. start the server
            (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

            //step 5:  3. execute sql
            localSqlExecutor.executeLocalSql(recoveryConfSql, parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);
            // 4. reload conf
            localSqlExecutor.executeLocalSql("SELECT pg_reload_conf()", parameters[PORT_ORDER], parameters[USER_ORDER], parameters[PASS_ORDER]);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    private String getRecoveryConfTemplate() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();
        if (miniPGlocalSetings.getOs().startsWith("windows")) {
            recoveryConfTemplate = "standby_mode ='on'\n"
                + " primary_conninfo = 'user=postgres host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres application_name="
                + hostName
                + "'\n"
                + " recovery_target_timeline='latest' ";

        } else {
            recoveryConfTemplate = "standby_mode ='on'\n"
                + " primary_conninfo = 'user=postgres host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres target_session_attrs=any application_name="
                + hostName
                + "'\n"
                + " recovery_target_timeline='latest' ";

        }
        return recoveryConfTemplate;
    }

    private String getRecoveryConfTemplateV12() {
        // --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres password=080419"
        String recoveryConfTemplate = null;
        String hostName             = getHostName();

        recoveryConfTemplate = " primary_conninfo ='user=postgres passfile=''/var/lib/pgsql/.pgpass'' host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=0 krbsrvname=postgres target_session_attrs=any application_name="
            + hostName
            + "'";

        return recoveryConfTemplate;
    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_COMMAND);
    }

    private String getHostName() {

        InetAddress ip;
        String      hostname = UUID.randomUUID().toString();
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return hostname;
    }
}
