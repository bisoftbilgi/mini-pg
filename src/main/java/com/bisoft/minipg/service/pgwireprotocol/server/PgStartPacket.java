package com.bisoft.minipg.service.pgwireprotocol.server;

import java.util.Date;
import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.util.CommandExecutor;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import org.springframework.stereotype.Component;

@Component
public class PgStartPacket extends AbstractWireProtocolPacket {
	private static final String PG_START = "-- pg_start";

	public WireProtocolPacket decode(byte[] buffer) {
		return this;
	}

	@Override
	public byte[] response() {
		List<String> cellValues = (new CommandExecutor()).executeCommand(
				ConfigurationService.GetValue("minipg.postgres_bin_path") + "pg_ctl", "start",
				"-D" + ConfigurationService.GetValue("minipg.postgres_data_path"));
		cellValues.add(0, PG_START + " received.. Command executed at : " + new Date());
		Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
		return table.generateMessage();
	}

	public static boolean matches(String messageStr) {
		return Util.caseInsensitiveContains(messageStr, PG_START);
	}
}
