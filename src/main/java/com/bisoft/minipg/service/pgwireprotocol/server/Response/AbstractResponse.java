package com.bisoft.minipg.service.pgwireprotocol.server.Response;

/**
 * AbstractResponse
 */
public abstract class AbstractResponse {

    protected int length = 0;

    public int getLength() {
        return length;
    }

    public AbstractResponse setLength(int value) {
        length = value;
        return this;
    }

    public abstract byte[] generateMessage();
}