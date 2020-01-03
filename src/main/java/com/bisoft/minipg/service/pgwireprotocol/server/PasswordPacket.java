package com.bisoft.minipg.service.pgwireprotocol.server;

import java.util.Arrays;
import com.bisoft.minipg.service.SessionState;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PasswordPacket extends AbstractWireProtocolPacket {

	public static final Logger logger = LoggerFactory.getLogger(PasswordPacket.class);

	private static final int LENGTH_OF_TRAILING_ZERO_BYTE = 1;
	private byte[] hashBytes;

	@Autowired
	private Md5Authenticator authenticator;
	private SessionState sessionState;

	public WireProtocolPacket decode(byte[] buffer) {
		int packetLength = buffer.length; // ByteUtil.fromByteArray(buffer);
		hashBytes = Util.readByteArray(buffer, 5,
				packetLength - LENGTH_OF_CHARACTER_TAG_AND_LENGTH_FIELD - LENGTH_OF_TRAILING_ZERO_BYTE);
		String hashStr = new String(hashBytes);
		log.trace("PasswordPacket hash : {}", hashStr);
		return this;
	}

	@Override
	public String toString() {
		return "[PasswordPacket:" +  Arrays.toString(hashBytes) + "]";
	}

	@Override
	public byte[] response() {
		if (isAuthenticated()) {
			return PgConstants.AUTH_OK;
		}
		return ErrorResponsePojo.generateErrorResponse("ERROR", "22000", "Minipg : Authentication Failed");
	}

	public void setSessionState(SessionState sessionState) {
		this.sessionState = sessionState;
	}

	public boolean isAuthenticated() {
		return authenticator.authenticate(hashBytes, sessionState.getSalt());
	}

	public static boolean packetMatches(byte[] buffer) {
		int passwordLength = Util.readInt32(buffer, 1);
		return buffer.length > 4 && buffer[0] == 'p' && (LENGTH_OF_CHARACTER_TAG + passwordLength) == buffer.length;
	}
}