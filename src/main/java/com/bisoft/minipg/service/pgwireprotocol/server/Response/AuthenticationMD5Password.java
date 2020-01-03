package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.Util;
import com.bisoft.minipg.service.pgwireprotocol.server.Response.AbstractMessageResponse;

public class AuthenticationMD5Password extends AbstractMessageResponse {
    private static final int LENGTH_OF_MD5_REQUIRED = 4;
    private static final int LENGTH_OF_SALT = 4;
    private static final byte[] MD5_REQUIRED = new byte[] { 0x00, 0x00, 0x00, 0x05 };
    private byte[] salt = Util.EMPTY_BYTE_ARRAY;

    public AuthenticationMD5Password(byte[] salt) {
        super();
        this.characterTag = 'R';
        this.salt = salt;
    }

    public byte[] getSalt() {
        return salt;
    }

    @Override
    public byte[] generateMessage() {
        this.length = LENGTH_OF_LENGTH_FIELD + LENGTH_OF_MD5_REQUIRED + LENGTH_OF_SALT;
        byte[] result = characterTagAndLength();
        result = Util.concatByteArray(result, MD5_REQUIRED);
        result = Util.concatByteArray(result, getSalt());
        return result;
    }
}
