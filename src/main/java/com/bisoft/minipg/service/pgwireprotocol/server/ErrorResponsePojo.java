package com.bisoft.minipg.service.pgwireprotocol.server;

/**
 * ErrorResponsePojo
 */
public class ErrorResponsePojo {
    /*
     * byte[] Severity; byte[] VSeverity; byte[]
     * Code="58000".getBytes(StandardCharsets.UTF_8) ;// system_error; String
     * Message; String Message; String Message; String Message;
     * 
     * D Detail: an optional secondary error message carrying more detail about the
     * problem. Might run to multiple lines.
     * 
     * H Hint: an optional suggestion what to do about the problem. This is intended
     * to differ from Detail in that it offers advice (potentially inappropriate)
     * rather than hard facts. Might run to multiple lines.
     * 
     * P Position: the field value is a decimal ASCII integer, indicating an error
     * cursor position as an index into the original query string. The first
     * character has index 1, and positions are measured in characters not bytes.
     * 
     * p Internal position: this is defined the same as the P field, but it is used
     * when the cursor position refers to an internally generated command rather
     * than the one submitted by the client. The q field will always appear when
     * this field appears.
     * 
     * q Internal query: the text of a failed internally-generated command. This
     * could be, for example, a SQL query issued by a PL/pgSQL function.
     * 
     * W Where: an indication of the context in which the error occurred. Presently
     * this includes a call stack traceback of active procedural language functions
     * and internally-generated queries. The trace is one entry per line, most
     * recent first.
     * 
     * s Schema name: if the error was associated with a specific database object,
     * the name of the schema containing that object, if any.
     * 
     * t Table name: if the error was associated with a specific table, the name of
     * the table. (Refer to the schema name field for the name of the table's
     * schema.)
     * 
     * c Column name: if the error was associated with a specific table column, the
     * name of the column. (Refer to the schema and table name fields to identify
     * the table.)
     * 
     * d Data type name: if the error was associated with a specific data type, the
     * name of the data type. (Refer to the schema name field for the name of the
     * data type's schema.)
     * 
     * n Constraint name: if the error was associated with a specific constraint,
     * the name of the constraint. Refer to fields listed above for the associated
     * table or domain. (For this purpose, indexes are treated as constraints, even
     * if they weren't created with constraint syntax.)
     * 
     * F File: the file name of the source-code location where the error was
     * reported.
     * 
     * L Line: the line number of the source-code location where the error was
     * reported.
     * 
     * R Routine: the name of the source-code routine reporting the error.
     */
}