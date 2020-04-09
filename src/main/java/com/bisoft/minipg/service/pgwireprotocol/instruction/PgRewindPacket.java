package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
import com.bisoft.minipg.service.util.ScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PgRewindPacket extends AbstractWireProtocolPacket {

    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;

    private static final String PG_REWIND = "-- pg_rewind";
    private static final String MASTER_IP = "(?<masterIp>.*)";
    private static final String RIGHT_PARANTHESIS = "[)]";
    private static final String LEFT_PARANTHESIS = "[(]";
    String REGEX_REWIND = ".*"
            + PG_REWIND
            + LEFT_PARANTHESIS
            + MASTER_IP
            + RIGHT_PARANTHESIS
            + ".*";
    public String pgRewindMasterIp;

    public WireProtocolPacket decode(byte[] buffer) {

        // pg_rewind([master_ip] *****)
        Pattern p = Pattern.compile(REGEX_REWIND, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        pgRewindMasterIp = m.group("masterIp");
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

        List<String> cellValues = (new ScriptExecutor()).executeScriptSync(
                "bash", "-c",
                miniPGlocalSetings.getPgCtlBinPath() + "pg_rewind --target-pgdata="
                        + miniPGlocalSetings.getPostgresDataPath()
                        + " --source-server='host=" + pgRewindMasterIp + "'");

        reGenerateRecoveryConf(pgRewindMasterIp, "5432");

        /*a start and stop */
        (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c " + "CHECKPOINT"
        );

        (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop",
                "-D" + miniPGlocalSetings.getPostgresDataPath());

        cellValues.add(0, PG_REWIND + " pg_rewind command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    // @Deprecated
    private void reGenerateRecoveryConf(String masterIP, String masterPort) {

        log.info("REGENERATING recovery.conf with " + masterIP + ":" + masterPort);

        String recoveryConfTemplate = null;
        String hostName = getHostName();
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

        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(miniPGlocalSetings.getPostgresDataPath() + "recovery.conf"), "utf-8"));
            writer.write(recoveryConfTemplate.replace("{MASTER_IP}", masterIP).replace("{MASTER_PORT}", masterPort));
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

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_REWIND);
    }

    private String getHostName() {

        InetAddress ip;
        String hostname = UUID.randomUUID().toString();
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return hostname;
    }
}
