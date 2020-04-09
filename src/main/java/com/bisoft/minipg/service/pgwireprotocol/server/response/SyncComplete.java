package com.bisoft.minipg.service.pgwireprotocol.server.response;

/**
 * SyncComplete
 */
public class SyncComplete extends AbstractMessageResponse {

    public SyncComplete() {
        this.characterTag = 'S';
        this.length = LENGTH_OF_CHARACTER_TAG_AND_LENGTH_FIELD;
    }

    @Override
    public byte[] generateMessage() {
        return characterTagAndLength();
    }
}