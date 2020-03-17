package com.bisoft.minipg.service.pgwireprotocol.server;

import java.util.Date;
import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.util.ScriptExecuter;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import org.springframework.beans.factory.annotation.Value;

public class PgStopPacket extends com.bisoft.minipg.service.pgwireprotocol.server.AbstractWireProtocolPacket {

	private static final String PG_COMM_PREFIX = "-- pg_stop";

	@Value("${server.pg_ctl_path}")
	public String pgCtlPath;

	@Value("${server.postgres_data_path}")
	public String postgresDataPath;

	public WireProtocolPacket decode(byte[] buffer) {
		return this;
	}

	@Override
	public byte[] response() {
		List<String> cellValues = (new ScriptExecuter()).executeScript(
				pgCtlPath+ "pg_ctl", "stop",
				"-D" + postgresDataPath);
		cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new Date());
		Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
		return table.generateMessage();
	}

	public static boolean matches(String messageStr) {
		
		return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
	}
}
