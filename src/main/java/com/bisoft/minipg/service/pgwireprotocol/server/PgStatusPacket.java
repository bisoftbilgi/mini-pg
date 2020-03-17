package com.bisoft.minipg.service.pgwireprotocol.server;

import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.ScriptExecuter;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PgStatusPacket extends AbstractWireProtocolPacket {
	private static final String PG_COMM_PREFIX = "-- pg_status";

	@Value("${server.postgres_bin_path}")
	public String postgresBinPath;

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
				pgCtlPath + "pg_ctl", "status",
				"-D" + postgresDataPath);
		// cellValues.add(0, PG_COMM_PREFIX + " received.. Command executed at : " + new
		// Date());
		Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
		return table.generateMessage();
	}

	public static boolean matches(String messageStr) {
		return Util.caseInsensitiveContains(messageStr, PG_COMM_PREFIX);
	}
}
