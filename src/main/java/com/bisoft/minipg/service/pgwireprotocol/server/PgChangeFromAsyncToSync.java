package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


@Slf4j
public class PgChangeFromAsyncToSync extends AbstractWireProtocolPacket {

    private static final String PG_TOSYNC = "-- pg_tosync";
    private static final String SLAVE_NAME = "(?<slaveName>.*)";
    String REGEX_TOSYNC = ".*" + PG_TOSYNC + SLAVE_NAME + ".*";
    String filePath = ConfigurationService.GetValue("minipg.postgres_data_path") + "postgresql.conf";
    private String slaveApplicationName;

    private static String readLineByLine(String filePathPostgresqlConf) {
        String filePath = ConfigurationService.GetValue("minipg.postgres_data_path") + "postgresql.conf";

        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePathPostgresqlConf), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static boolean matches(String messageStr) {
        return Util.caseInsensitiveContains(messageStr, PG_TOSYNC);
    }

    public WireProtocolPacket decode(byte[] buffer) {
        Pattern p = Pattern.compile(REGEX_TOSYNC, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        slaveApplicationName = m.group("slaveName");
        return this;
    }

    public Boolean checkIfMaster() {
        Boolean res = false;
        try {
            File file = new File(ConfigurationService.GetValue("minipg.postgres_data_path") + "recovery.conf");
            if (file.exists() && !file.isDirectory()) {
                res = true;
            } else {
                res = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public byte[] response() {
        Boolean checkMaster = checkIfMaster();
        String postgresqlConfFileTemplate = readLineByLine(filePath);
        Writer writer = null;
        if (checkMaster.equals(false)) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(ConfigurationService.GetValue("minipg.postgres_data_path") + "postgresql.conf"), "utf-8"));
                writer.write(postgresqlConfFileTemplate.replace("{SLAVE_1}", slaveApplicationName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        List<String> cellValues = new ArrayList<>();
        cellValues.add(0, PG_TOSYNC + " command executed at:" + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }


}
