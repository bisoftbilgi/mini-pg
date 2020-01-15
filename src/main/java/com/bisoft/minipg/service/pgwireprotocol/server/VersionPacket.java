package com.bisoft.minipg.service.pgwireprotocol.server;

import java.util.Arrays;
import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.server.Response.Table;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.TableHelper;

public class VersionPacket extends AbstractWireProtocolPacket {
	private static final String POSTGRE_SQL_10_7 = "PostgreSQL 10.7 (Debian 10.7-1.pgdg90+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 6.3.0-18+deb9u1) 6.3.0 20170516, 64-bit";
	private static final String BISOFT_BFM_PROXY = "BiSoft MiniPg v.0.0.1";
	String hash;
	Object msg;

	public WireProtocolPacket decode(byte[] buffer) {
		return this;
	}

	@Override
	public byte[] response() {
		List<String> cellValues = Arrays.asList(BISOFT_BFM_PROXY);
		Table table = (new TableHelper()).generateSingleColumnTable("version", cellValues, "SELECT");
		return table.generateMessage();
	}

}
