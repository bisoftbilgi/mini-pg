package com.bisoft.minipg.service.pgwireprotocol.server;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.CommandExecutor;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;
import com.bisoft.minipg.service.subservice.ConfigurationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgRewindPacket extends AbstractWireProtocolPacket {
//	private static final String SCRIPT = "./cmds/rewind.sh";
	private static final String PG_REWIND = "-- pg_rewind";
	private static final String MASTER_IP = "(?<masterIp>.*)";
	private static final String RIGHT_PARANTHESIS = "[)]";
	private static final String LEFT_PARANTHESIS = "[(]";
	String REGEX_REWIND = ".*" + PG_REWIND + LEFT_PARANTHESIS + MASTER_IP + RIGHT_PARANTHESIS + ".*";
	public String pgRewindMasterIp;

	public WireProtocolPacket decode(byte[] buffer) {
		// pg_rewind([master_ip])
		Pattern p = Pattern.compile(REGEX_REWIND, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(getPayloadString());
		m.matches();
		pgRewindMasterIp = m.group("masterIp");
		return this;
	}

	@Override
	public byte[] response() {
		// /usr/pgsql-10/bin/pg_rewind --target-pgdata=/var/lib/pgsql/10/data/.
		// --source-server="host=192.168.2.90 port=5432 user=postgres dbname=postgres
		// password=080419"

//		host=192.168.2.90 port=5432 user=postgres 
		String rewindCommand = ConfigurationService.GetValue("minipg.postgres_bin_path") + "pg_rewind"
				+ " --target-pgdata=" + ConfigurationService.GetValue("minipg.postgres_data_path")
				+ " --source-server=\"host=" + pgRewindMasterIp + "\"";
		log.info("EXECUTING THIS COMMAND for REWINDING===> " + rewindCommand);
		List<String> cellValues = (new CommandExecutor()).executeCommand(
				ConfigurationService.GetValue("minipg.postgres_bin_path") + "pg_rewind",
				" --target-pgdata=" + ConfigurationService.GetValue("minipg.postgres_data_path"),
				" --source-server=\"host=" + pgRewindMasterIp + "\"");

		reGenerateRecoveryConf(pgRewindMasterIp, "5432");
		cellValues.add(0, PG_REWIND + " received.." + rewindCommand + " command executed at : " + new Date());
		Table table = (new TableHelper()).generateSingleColumnTable("result", cellValues, "SELECT");
		return table.generateMessage();
	}

//	@Deprecated
	private void reGenerateRecoveryConf(String masterIP, String masterPort) {
		log.info("REGENERATING recovery.conf with " + masterIP + ":" + masterPort);
		String recoveryConfTemplate = "standby_mode ='on'\n"
				+ " primary_conninfo = 'user=postgres passfile=''/var/lib/pgsql/.pgpass'' host={MASTER_IP} port={MASTER_PORT} sslmode=prefer sslcompression=1 krbsrvname=postgres target_session_attrs=any application_name=mini-pg-did'\n"
				+ " recovery_target_timeline='latest' ";

		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(ConfigurationService.GetValue("minipg.postgres_data_path") + "recovery.conf"),
					"utf-8"));
			writer.write(recoveryConfTemplate.replace("{MASTER_IP}", masterIP).replace("{MASTER_PORT}", masterPort));
		} catch (IOException ex) {

			ex.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static boolean matches(String messageStr) {
		return Util.caseInsensitiveContains(messageStr, PG_REWIND);
	}

//	private static boolean generateRecoveryConf()
//	{
//		String recoveryConfTemplate="standby_mode = 'on' \n"
//				+"primary_conninfo = 'user=postgres passfile=''"++".pgpass'' host=192.168.2.91 port=5432 sslmode=prefer sslcompression=1 krbsrvname=postgres target_session_attrs=any'
//
//	}
}
