package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * RowDescription
 */
public class RowDescription extends AbstractMessageResponse {

    private CellDescription[] cells;

    public RowDescription() {

        this.characterTag = 'T';
        cells = new CellDescription[]{};

    }

    public int getNumberOfFields() {

        return this.cells.length;
    }

    ;

    public CellDescription[] getCells() {

        return this.cells;
    }

    public RowDescription setCells(CellDescription[] value) {

        this.cells = value;
        return this;
    }

    @Override
    public byte[] generateMessage() {

        byte[] cellsResult = new byte[]{};
        for (CellDescription c : this.cells) {
            cellsResult = Util.concatByteArray(cellsResult, c.generateMessage());
        }
        length = cellsResult.length + 1 + 4 + 2;
        byte[] result = characterTagAndLength();
        result = Util.concatByteArray(result, Util.int16ByteArray(getNumberOfFields()));
        result = Util.concatByteArray(result, cellsResult);
        result = Util.concatByteArray(result, Util.int8ByteArray(0)); // end of row description

        return result;
    }

}