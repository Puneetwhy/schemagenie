package com.schemagenie.dto;

import java.util.List;

public class CollectionDto {
    private String name;
    private List<FieldDto> fields;
    private List<MongoRelationshipDto> relationships;

    public CollectionDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<FieldDto> getFields() { return fields; }
    public void setFields(List<FieldDto> fields) { this.fields = fields; }
    public List<MongoRelationshipDto> getRelationships() { return relationships; }
    public void setRelationships(List<MongoRelationshipDto> relationships) { this.relationships = relationships; }
}
