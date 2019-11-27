package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * AbstractResponse
 */
public abstract class AbstractMessageResponse extends AbstractResponse {

    public final int LENGTH_OF_CHARACTER_TAG = 1;
    public final int LENGTH_OF_LENGTH_FIELD = 4;
    public final int LENGTH_OF_CHARACTER_TAG_AND_LENGTH_FIELD = LENGTH_OF_CHARACTER_TAG + LENGTH_OF_LENGTH_FIELD;
    protected char characterTag = 0;

    protected byte[] characterTagAndLength() {
        byte[] result = { (byte) characterTag, };
        result = Util.concatByteArray(result, Util.int32ByteArray(length));
        return result;
    }

}