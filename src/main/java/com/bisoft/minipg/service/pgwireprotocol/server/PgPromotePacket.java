package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.util.ScriptExecuter;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgPromotePacket extends AbstractWireProtocolPacket {

    @Value("${server.pg_ctl_path}")
    public String pgCtlPath;

    @Value("${server.postgres_data_path}")
    public String postgresDataPath;

    private static final String PG_COMM_PREFIX = "-- pg_promote";

    public WireProtocolPacket decode(byte[] buffer) {

        return this;
    }

    @Override
    public byte[] response() {

        List<String> cellValues = (new ScriptExecuter()).executeScript(
                pgCtlPath+ "pg_ctl", "promote",
            "-D" + postgresDataPath);
        cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new Date());
        Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
        return table.generateMessage();
    }

    public static boolean matches(String messageStr) {

        log.debug(messageStr);
        // System.out.println(ByteUtil.byteArrayToHexAndAsciiAndDecDump(messageStr.getBytes()));
        return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
    }
}
