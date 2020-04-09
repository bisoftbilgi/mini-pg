package com.bisoft.minipg.service.pgwireprotocol.server.response;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * ReadyForQuery
 */
public class ReadyForQuery extends AbstractMessageResponse {

    char indicator = 'I'; // 'I' : idle 'T' : transaction block; 'E' : failed transaction block

    public ReadyForQuery(char indicator) {
        this.indicator = indicator;
        this.characterTag = 'Z';
        this.length = LENGTH_OF_CHARACTER_TAG_AND_LENGTH_FIELD;
    }

    @Override
    public byte[] generateMessage() {
        byte[] result = characterTagAndLength();
        result = Util.concatByteArray(result, Util.int8ByteArray(indicator));
        return result;
    }
}