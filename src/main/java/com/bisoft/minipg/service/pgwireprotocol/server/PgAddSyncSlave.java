package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;

@Slf4j
public class PgAddSyncSlave extends AbstractWireProtocolPacket {

    private static final String PG_ADD_SYNC_SLAVE = "-- pg_add_sync_slave";
    private static final String SLAVE_APP_NAME    = "(?<slaveAppName>.*)";
    private static final String RIGHT_PARANTHESIS = "[)]";
    private static final String LEFT_PARANTHESIS  = "[(]";
    String REGEX_ADD_SYNC_SLAVE = ".*" + PG_ADD_SYNC_SLAVE + LEFT_PARANTHESIS + SLAVE_APP_NAME
        + RIGHT_PARANTHESIS + ".*";
    private String slaveAppName;

    public WireProtocolPacket decode(byte[] buffer) {

        // pg_rewind([master_ip])
        Pattern p = Pattern.compile(REGEX_ADD_SYNC_SLAVE, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        slaveAppName = m.group("slaveAppName");
        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = new ArrayList<>();

        try {
            addSyncNode(slaveAppName);
            cellValues.add("added :" + slaveAppName);

        } catch (Exception e) {
            log.error("error during the sync node adding process", e);
            cellValues.add("exception :" + e.getMessage());
        }

        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    private void addSyncNode(String syncName) throws Exception {

        if (checkIfMaster()) {
            addSubProp(ConfigurationService.GetValue("minipg.postgres_data_path") + "data/postgresql.auto.conf",
                "synchronous_standby_names", syncName);
        } else {
            throw new Exception("I'm not a master!");
        }
    }

    public Boolean checkIfMaster() {

        Boolean res = true;
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

    private void addSubProp(String propertyFile, String propertyKey, String propValue) throws Exception {

        PropertiesConfiguration       config = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
        config.setLayout(layout);
        layout.load(config, new FileReader(propertyFile));

        // remove comments if exists
        String syncNodes = config.getProperty(propertyKey).toString().replace("'", "");
        int    offset    = syncNodes.indexOf("#");

        if (-1 != offset) {
            syncNodes = syncNodes.substring(0, offset);
        }

        String[]     syncNodesArr = syncNodes.split(",");
        List<String> ipList       = new LinkedList<>(Arrays.asList(syncNodesArr));

        List<String> newList = ipList.stream()                // convert list to stream
            .filter(line -> !propValue.equals(line) && line.trim().length() > 0)
            .collect(Collectors.toList());

        newList.add(propValue);
        String line = "'" + newList.stream().collect(Collectors.joining(",")) + "'";
        config.setProperty("synchronous_standby_names", line);

        layout.save(config, new FileWriter(propertyFile));
    }

    public static boolean matches(String messageStr) {

        return Util.caseInsensitiveContains(messageStr, PG_ADD_SYNC_SLAVE);
    }
}
