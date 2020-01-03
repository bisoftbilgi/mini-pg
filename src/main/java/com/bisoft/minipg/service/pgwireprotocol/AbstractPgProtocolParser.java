package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.pgwireprotocol.PgProtocolParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.bisoft.minipg.service.util.ContextWrapper;

@Component
@Scope("singleton")
public abstract class AbstractPgProtocolParser implements PgProtocolParser {
    public static final int LENGTH_OF_CHARACTER_TAG = 1;
    public static final int LENGTH_OF_LENGTH_FIELD = 4;
    public static final int LENGTH_OF_CHARACTER_TAG_AND_LENGTH_FIELD = LENGTH_OF_CHARACTER_TAG + LENGTH_OF_LENGTH_FIELD;
    @Autowired
    protected ContextWrapper contextWrapper;
}
