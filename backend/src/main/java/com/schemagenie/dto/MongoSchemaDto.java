package com.schemagenie.dto;

import java.util.List;

public class MongoSchemaDto {
    private List<CollectionDto> collections;

    public MongoSchemaDto() {}

    public List<CollectionDto> getCollections() { return collections; }
    public void setCollections(List<CollectionDto> collections) { this.collections = collections; }
}
