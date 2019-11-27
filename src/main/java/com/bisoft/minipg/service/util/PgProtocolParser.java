package com.bisoft.minipg.service.util;

import com.bisoft.minipg.service.pgwireprotocol.server.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bisoft.minipg.service.pgwireprotocol.Util;

@Slf4j
public class PgProtocolParser {
	public static final Logger logger = LoggerFactory.getLogger(PgProtocolParser.class);

	public WireProtocolPacket parsePacket(byte[] buffer) {
		WireProtocolPacket result = null;
		String strMessage = ByteUtil.byteArrayToAsciiDump(buffer);
		log.trace("parsePacket  " + strMessage);
		if (HelloPacket.packetMatches(buffer)) {
			result = new HelloPacket().decodeBuffer(buffer);
		} else if (StartupPacket.packetMatches(buffer)) {
			result = new StartupPacket().decodeBuffer(buffer);
		} else if (PasswordPacket.packetMatches(buffer)) {
			result = new PasswordPacket().decodeBuffer(buffer);
		} else if (PgRewindPacket.matches(strMessage)) {
			result = new PgRewindPacket().decodeBuffer(buffer);
		} else if (PgStartPacket.matches(strMessage)) {
			result = new PgStartPacket().decodeBuffer(buffer);
		} else if (PgStatusPacket.matches(strMessage)) {
			result = new PgStatusPacket().decodeBuffer(buffer);
		} else if (PgStopPacket.matches(strMessage)) {
			result = new PgStopPacket().decodeBuffer(buffer);
		} else if (PgPromotePacket.matches(strMessage)) {
			result = new PgPromotePacket().decodeBuffer(buffer);
		} else if (Util.caseInsensitiveContains(strMessage, "version")) {
			result = new VersionPacket().decodeBuffer(buffer);
		} else if (ParsePacket.packetMatches(buffer)) {
			result = new ParsePacket().decodeBuffer(buffer);
		}else if(PgChangeFromAsyncToSync.matches(strMessage)){
			result=new PgChangeFromAsyncToSync().decodeBuffer(buffer);
		}else if(PgChangeFromSyncToAsync.matches(strMessage)){
			result=new PgChangeFromSyncToAsync().decodeBuffer(buffer);
		}


		return result;

	}

}
