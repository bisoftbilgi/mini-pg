package com.bisoft.minipg.service.pgwireprotocol.instruction;

import com.bisoft.minipg.model.MiniPGLocalSettings;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.response.TableHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VersionPacket extends AbstractWireProtocolPacket {
    
    @Autowired
    protected MiniPGLocalSettings miniPGlocalSetings;
    
    private static final String PG_COMM_PREFIX = "-- minipg_version";
    
    @Override
    public WireProtocolPacket decode(byte[] buffer) {
        
        return this;
    }
    
    @Override
    public byte[] response() {
        
        List<String> cellValues = new ArrayList<>();
//        cellValues.add(0, miniPGlocalSetings.getVersion());
        cellValues.add(0, getEmbeddedSystemValue("minipg.version"));
        cellValues.add(1, PG_COMM_PREFIX + " received.. " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }
    
    public static boolean matches(String messageStr) {
        
        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
    
    public String getEmbeddedSystemValue(String key) {

        Properties properties = new Properties();
        
        String propFileName = "embedded_version.properties";
        
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
        
        try {
            
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }
    
}
