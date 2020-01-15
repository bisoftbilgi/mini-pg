package com.bisoft.minipg.service.pgwireprotocol.server.Response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TableHelper
 */
public class TableHelper {

    public Table generateSingleColumnTable(String fieldName, List<String> cellValues, String command) {
        // T... .. : 54 00 00 00 20 00 01
        RowDescription rowDescription = rowDescription(fieldName);

        List<DataRow> dataRows = dataRows(rowDescription, cellValues);
        return new Table(rowDescription, command).setRows(dataRows);
    }

    private List<DataRow> dataRows(RowDescription rowDescription, List<String> cellValues) {
        List<DataRow> dataRows = cellValues.stream().map(x -> dataRow(rowDescription, x)).collect(Collectors.toList());
        return dataRows;
    }

    private DataRow dataRow(RowDescription rowDescription, String cellValue) {
        DataRow dataRow = new DataRow(rowDescription);
        DataCell cell = dataCellVersion(rowDescription, cellValue);
        DataCell[] cells = { cell };
        dataRow.setCells(cells);
        return dataRow;
    }

    private DataCell dataCellVersion(RowDescription rowDescription, String cellValue) {
        DataCell dataCell = new DataCell(rowDescription.getCells()[0]);
        dataCell.setValue(cellValue);
        // dataCell.setValue(POSTGRE_SQL_10_7);

        return dataCell;
    }

    private RowDescription rowDescription(String fieldName) {
        RowDescription rowDescription = new RowDescription();
        CellDescription cdVersion = versionCell(fieldName);
        CellDescription[] cells = { cdVersion };
        rowDescription.setCells(cells);
        return rowDescription;
    }

    private CellDescription versionCell(String fieldName) {
        // version : 76 65 72 73 69 6f 6e
        // ...................:
        // column Number: 00 00 00 00
        // attribute number :00 00
        // object ID: 00 00 00 00
        // typlen: 19 ff
        // atttypmod : ff ff ff ff
        // format code: ff 00
        // 00
        CellDescription cellDescription = new CellDescription();
        cellDescription.fieldName = fieldName;
        cellDescription.typlen = 0x19ff; //
        cellDescription.typeModifier = 0xffffffff;
        cellDescription.formatCode = 0xff00;
        return cellDescription;
    }

}