package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class PgBaseBackup extends AbstractWireProtocolPacket {

    @Value("${server.postgres_data_path}")
    public String postgresDataPath;

    @Value("${server.postgres_bin_path}")
    public String postgresBinPath;

    @Value("${minipg.os}")
    public String operatingSystem;

    private static final String PG_BASEBACKUP     = "-- pg_basebackup";
    private static final String MASTER_IP         = "(?<masterIp>.*)";
    private static final String RIGHT_PARANTHESIS = "[)]";
    private static final String LEFT_PARANTHESIS  = "[(]";
    String BASE_BACKUP = ".*" + PG_BASEBACKUP + LEFT_PARANTHESIS + MASTER_IP + RIGHT_PARANTHESIS + ".*";
    public String IpOfMaster;
//    public String IpOfTarget;

    public WireProtocolPacket decode(byte[] buffer) {

        Pattern p = Pattern.compile(BASE_BACKUP, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        IpOfMaster = m.group("masterIp");
//        IpOfTarget = m.group("targetIp");
        return this;
    }

    @Override
    public byte[] response() {

        Table table = null;

        String basebackupCommand = postgresBinPath + "pg_basebackup " + "-h "
            + "--source-server=\"host=" + IpOfMaster + "\"" + " -D " + postgresDataPath;

        log.info("EXECUTING THIS COMMAND for basebackup===> " + basebackupCommand);

        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
            postgresBinPath + "pg_basebackup", "-h ",
            "--source-server=\"host=" + IpOfMaster + "\"", "-D " + postgresDataPath);
        cellValues.add(0, PG_BASEBACKUP + " received.." + basebackupCommand + " command executed at : " + new Date());
        table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table != null ? table.generateMessage() : null;
    }

    public static boolean matches(String messageStr) {

        log.debug(messageStr);
        return Util.caseInsensitiveContains(messageStr, PG_BASEBACKUP);
    }
}
