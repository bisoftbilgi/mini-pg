package com.bisoft.minipg.service.pgwireprotocol.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.WireProtocolPacket;
import com.bisoft.minipg.service.pgwireprotocol.server.ErrorResponsePojo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope("prototype")
@Lazy
public class PasswordPacket extends AbstractWireProtocolPacket {

	private byte[] hashBytes;
	private byte[] salt;
	Object msg;
	private final Md5Authenticator authenticator;

	public WireProtocolPacket decode(byte[] buffer) {
		int packetLength = buffer.length; // ByteUtil.fromByteArray(buffer);

		hashBytes = Util.readByteArray(buffer, 5,
				packetLength);
		String hashStr = new String(hashBytes);
		log.trace("PasswordPacket hash : {}", hashStr);
		return this;
	}

	public Object getMsg() {
		return msg;
	}

	public void setMsg(Object message) {
		this.msg = message;
	}

	@Override
	public String toString() {
		return "[PasswordPacket:" + hashBytes + "]";
	}

	@Override
	public byte[] response() {
		if (isAuthenticated()) {
			return PgConstants.AUTH_OK;
		}
		return ErrorResponsePojo.generateErrorResponse("ERROR", "22000", "Minipg : Authentication Failed");
	}

	public byte[] getSalt() {
		return salt;
	}

	public boolean isAuthenticated() {
		return authenticator.authenticate(hashBytes, getSalt());
	}

	public static boolean packetMatches(byte[] buffer) {
		int passwordLength = Util.readInt32(buffer, 1);
		return buffer.length > 4 && buffer[0] == 'p' && (LENGTH_OF_CHARACTER_TAG + passwordLength) == buffer.length;
	}
}