package com.bisoft.minipg.service.pgwireprotocol.server.response;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * DataRow
 */
public class DataRow extends AbstractMessageResponse {
    private RowDescription rowDescription;
    private DataCell cells[];

    int getNumberOfColumns() {
        return cells.length;
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
    public DataRow setRowDescription(RowDescription rowDescription) {
        this.rowDescription = rowDescription;
        return this;
    }

    /**
     * @return the cells
     */
    public DataCell[] getCells() {
        return cells;
    }

    /**
     * @param cells the cells to set
     */
    public DataRow setCells(DataCell cells[]) {
        this.cells = cells;
        return this;
    }

    public DataRow(RowDescription rowDescription) {
        this.characterTag = 'D';
        this.setRowDescription(rowDescription);
    }

    @Override
    public byte[] generateMessage() {

        byte[] cellsResult = null;
        for (DataCell c : this.cells) {
            cellsResult = Util.concatByteArray(cellsResult, c.generateMessage());
        }
        length = cellsResult.length + 4 + 2;
        byte[] result = characterTagAndLength();
        result = Util.concatByteArray(result, Util.int16ByteArray(this.getNumberOfColumns()));
        result = Util.concatByteArray(result, cellsResult);
        return result;
    }
}