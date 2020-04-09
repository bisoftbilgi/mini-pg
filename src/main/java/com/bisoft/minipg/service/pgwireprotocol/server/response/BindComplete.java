package com.bisoft.minipg.service.pgwireprotocol.server.response;

/**
 * BindComplete
 */
public class BindComplete extends AbstractMessageResponse {

    public BindComplete() {
        this.characterTag = '2';
        this.length = LENGTH_OF_LENGTH_FIELD;
    }

    @Override
    public byte[] generateMessage() {
        return characterTagAndLength();
    }
}