
package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import java.nio.charset.StandardCharsets;

import com.bisoft.minipg.service.pgwireprotocol.Util;

/**
 * DataRow
 */
public class DataCell extends AbstractMessageResponse {
    int columnLength = 0;
    byte[] value;
    private CellDescription cellDescription;

    public DataCell setValue(byte[] v) {
        value = v;
        columnLength = v.length;
        return this;
    }

    /**
     * @return the cellDescription
     */
    public CellDescription getCellDescription() {
        return cellDescription;
    }

    /**
     * @param cellDescription the cellDescription to set
     */
    public void setCellDescription(CellDescription cellDescription) {
        this.cellDescription = cellDescription;
    }

    public byte[] getValue() {
        return value;
    }

    public DataCell(CellDescription cellDescription) {
        this.setCellDescription(cellDescription);
    }

    @Override
    public byte[] generateMessage() {
        return Util.concatByteArray(Util.int32ByteArray(value.length), value);
    }

    public void setValue(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        setValue(bytes);

    }
}