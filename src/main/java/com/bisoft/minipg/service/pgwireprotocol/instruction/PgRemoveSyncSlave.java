package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import com.bisoft.minipg.service.util.CommandExecutor;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgRemoveSyncSlave extends AbstractWireProtocolPacket {
    
    private static final String PG_ADD_SYNC_SLAVE    = "-- pg_remove_sync_slave";
    private static final String SLAVE_APP_NAME       = "(?<slaveAppName>.*)";
    private static final String RIGHT_PARENTHESES    = "[)]";
    private static final String LEFT_PARENTHESES     = "[(]";
    String                      REGEX_ADD_SYNC_SLAVE = ".*"
            + PG_ADD_SYNC_SLAVE
            + LEFT_PARENTHESES
            + SLAVE_APP_NAME
            + RIGHT_PARENTHESES
            + ".*";
    private String              slaveAppName;
    
    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;
    
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
            removeSyncNode(slaveAppName);
            cellValues.add("removed :" + slaveAppName);
            
        } catch (Exception e) {
            log.error("error during the sync node removing process", e);
            cellValues.add("exception :" + e.getMessage());
        }
        
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }
    
    private void removeSyncNode(String syncName) throws Exception {
        
        if (checkIfMaster()) {
            removeSubProp(miniPGlocalSetings.getPostgresDataPath() + "data/postgresql.auto.conf",
                    "synchronous_standby_names", syncName);
            runReload();
        } else {
            throw new Exception("I'm not a master!");
        }
    }
    
    private void runReload() {
        
        
        (new CommandExecutor()).executeCommand(miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "reload",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
    }
    
    public Boolean checkIfMaster() {
        
        Boolean res = true;
        try {
            File file = new File(miniPGlocalSetings.getPostgresDataPath()+ "recovery.conf");
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
    
    private void removeSubProp(String propertyFile, String propertyKey, String propValue) throws Exception {
        
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
        
        List<String> newList = ipList.stream() // convert list to stream
                .filter(line -> !propValue.equals(line) && line.trim().length() > 0).collect(Collectors.toList());
        
        String line = "'" + newList.stream().collect(Collectors.joining(",")) + "'";
        config.setProperty("synchronous_standby_names", line);
        
        layout.save(config, new FileWriter(propertyFile));
    }
    
    public static boolean matches(String messageStr) {
        
        return Util.caseInsensitiveContains(messageStr, PG_ADD_SYNC_SLAVE);
    }
}
