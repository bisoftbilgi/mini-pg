package com.bisoft.minipg.service.pgwireprotocol.server.response;

import java.util.Arrays;
import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * CommandComplete
 */
public class CommandComplete extends AbstractMessageResponse {
    private static List<String> INCLUDE_NUM_VALUES_STRINGS = Arrays.asList("INSERT", "DELETE", "UPDATE", "SELECT",
            "CREATETABLEAS", "MOVE", "FETCH", "COPY");
    String command; // INSERT/DELETE/UPDATE/SELECT/CREATETABLEAS/MOVE/FETCH/COPY
    int rows = 0;
    int oid = 0;

    public CommandComplete(String command, int rows) {
        this.characterTag = 'C';
        this.command = command;
        this.rows = rows;
    }

    @Override
    public byte[] generateMessage() {
        byte[] commandBytes = commandBytes();
        length = commandBytes.length + LENGTH_OF_LENGTH_FIELD;
        byte[] result = characterTagAndLength();
        result = Util.concatByteArray(result, commandBytes);
        return result;
    }

    private byte[] commandBytes() {
        String commandStr = command;
        if (INCLUDE_NUM_VALUES_STRINGS.contains(command)) {
            commandStr = command + " " + rows;
        }
        return Util.toCString(commandStr);
    }
}