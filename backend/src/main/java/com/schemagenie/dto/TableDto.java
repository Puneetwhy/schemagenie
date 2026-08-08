package com.schemagenie.dto;

import java.util.List;

public class TableDto {
    private String name;
    private List<FieldDto> fields;
    private List<SqlRelationshipDto> relationships;

    public TableDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<FieldDto> getFields() { return fields; }
    public void setFields(List<FieldDto> fields) { this.fields = fields; }
    public List<SqlRelationshipDto> getRelationships() { return relationships; }
    public void setRelationships(List<SqlRelationshipDto> relationships) { this.relationships = relationships; }
}
