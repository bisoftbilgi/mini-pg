package com.bisoft.minipg.service.pgwireprotocol.server.Response;

/**
 * ParseComplete
 */
public class ParseComplete extends AbstractMessageResponse {

    public ParseComplete() {
        this.characterTag = '1';
        this.length = LENGTH_OF_LENGTH_FIELD;
    }

    @Override
    public byte[] generateMessage() {
        return characterTagAndLength();
    }
}