package com.bisoft.minipg.service.pgwireprotocol.server;

import ch.qos.logback.core.joran.conditional.PropertyWrapperForScripts;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PgChangeFromSyncToAsync extends AbstractWireProtocolPacket {
    private static final String PG_TOASYNC = "-- pg_toasync";
    private static final String SLAVE_NAME = "(?<slaveName>.*)";
    String REGEX_TOASYNC = ".*" + PG_TOASYNC + SLAVE_NAME + ".*";
    private String slaveApplicationName;



    public Boolean checkIfMaster() {
        Boolean res = false;
        try {
            File file = new File(ConfigurationService.GetValue("minipg.postgres_data_path") + "recovery.conf");
            if (file.exists() && !file.isDirectory()) {
                res = true;
            }
            else
                { res= false;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean matches(String messageStr) {
        return Util.caseInsensitiveContains(messageStr, PG_TOASYNC);
    }


    @Override
    public WireProtocolPacket decode(byte[] buffer) {
        Pattern p = Pattern.compile(REGEX_TOASYNC, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(getPayloadString());
        m.matches();
        slaveApplicationName = m.group("slaveName");
        return this;
    }

    @Override
    public byte[] response() {
        Boolean master=checkIfMaster();
        Properties properties=new Properties();
        Reader reader= null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(ConfigurationService.GetValue("minipg.postgres_data_path")+"postgresql.conf"),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ;
        StringBuilder sb = new StringBuilder();
        if(master.equals(false))
        {
            try {
                properties.load(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String slaves=properties.getProperty("synchronous_standby_names");
            List<String> items = Arrays.asList(slaves.split("\\s*,\\s*"));
            if (items.contains(slaveApplicationName))
            {
                for (String names:items)
                {
                    sb.append(names);
                    sb.append(",");
                }
                items.remove(slaveApplicationName);
                properties.setProperty("synchronous_standby_names",sb.toString());
            }
        }
        List<String> cellValues =new ArrayList<>();
        cellValues.add(0,PG_TOASYNC+ " command executed at:" + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }
}
