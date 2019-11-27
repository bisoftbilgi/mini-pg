package com.bisoft.minipg.service.pgwireprotocol.server;

public class HelloPacket extends AbstractWireProtocolPacket {

	@Override
	public WireProtocolPacket decode(byte[] buffer) {
		return this;
	}

	@Override
	public byte[] response() {
		return PgConstants.R_HELLO_RESPONSE;
	}

	public static boolean packetMatches(byte[] buffer) {
		// 00 00 00 08 04 d2 16 2f

		return buffer.length > 7 && buffer[0] == 0 && buffer[1] == 0 && buffer[2] == 0 && buffer[3] == 0x08
				&& buffer[4] == 0x04 && buffer[5] == (byte) 0xd2 && buffer[6] == 0x16 && buffer[7] == 0x2f; // hello
																											// signature
	}
}
