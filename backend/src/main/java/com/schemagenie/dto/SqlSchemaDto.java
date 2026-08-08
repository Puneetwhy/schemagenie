package com.schemagenie.dto;

import java.util.List;

public class SqlSchemaDto {
    private List<TableDto> tables;

    public SqlSchemaDto() {}

    public List<TableDto> getTables() { return tables; }
    public void setTables(List<TableDto> tables) { this.tables = tables; }
}
