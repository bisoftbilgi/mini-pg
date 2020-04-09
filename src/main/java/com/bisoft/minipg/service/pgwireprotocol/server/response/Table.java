package com.bisoft.minipg.service.pgwireprotocol.server.response;

import java.util.List;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * Table
 */
public class Table extends AbstractMessageResponse {
    private RowDescription rowDescription;
    private List<DataRow> rows;
    private String command;

    int getNumberOfColumns() {
        return rows.size();
    }

    /**
     * @return the rowDescription
     */
    public RowDescription getRowDescription() {
        return rowDescription;
    }

    /**
     * @param rowDescription the rowDescription to set
     */
    public Table setRowDescription(RowDescription rowDescription) {
        this.rowDescription = rowDescription;
        return this;
    }

    /**
     * @return the Rows
     */
    public List<DataRow> getRows() {
        return rows;
    }

    /**
     * @param Rows the Rows to set
     */
    public Table setRows(List<DataRow> Rows) {
        this.rows = Rows;
        return this;
    }

    public Table(RowDescription rowDescription, String command) {
        this.command = command;
        this.setRowDescription(rowDescription);
    }

    @Override
    public byte[] generateMessage() {

        byte[] result = rowDescription.generateMessage();
        for (DataRow r : this.rows) {
            result = Util.concatByteArray(result, r.generateMessage());
        }
        CommandComplete commandComplete = new CommandComplete(command, getLength());
        ReadyForQuery readyForQuery = new ReadyForQuery('I');
        result = Util.concatByteArray(result, commandComplete.generateMessage());
        result = Util.concatByteArray(result, readyForQuery.generateMessage());
        return result;
    }
}