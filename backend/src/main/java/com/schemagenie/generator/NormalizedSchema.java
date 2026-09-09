package com.schemagenie.generator;

import java.util.List;

public class NormalizedSchema {

    public record Field(String name, String type, boolean required) {}

    public record Relationship(String type, String target, String fieldName, String note) {}

    public record Entity(String name, List<Field> fields, List<Relationship> relationships) {}

    private final List<Entity> entities;

    public NormalizedSchema(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() { return entities; }
}