package com.schemagenie.generator;

import com.schemagenie.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SchemaNormalizer {

    public NormalizedSchema normalize(SchemaGenerationResult result) {
        if (result.getDatabaseType() == DatabaseType.MONGODB) {
            return normalizeMongo(result.getMongoSchema());
        }
        return normalizeSql(result.getSqlSchema());
    }

    private NormalizedSchema normalizeMongo(MongoSchemaDto schema) {
        List<NormalizedSchema.Entity> entities = new ArrayList<>();
        for (CollectionDto c : schema.getCollections()) {
            List<NormalizedSchema.Field> fields = new ArrayList<>();
            if (c.getFields() != null) {
                for (FieldDto f : c.getFields()) {
                    fields.add(new NormalizedSchema.Field(f.getName(), f.getType(), f.isRequired()));
                }
            }
            List<NormalizedSchema.Relationship> relationships = new ArrayList<>();
            if (c.getRelationships() != null) {
                for (MongoRelationshipDto r : c.getRelationships()) {
                    String note = r.getStrategy() + (r.getReasoning() != null ? " -- " + r.getReasoning() : "");
                    relationships.add(new NormalizedSchema.Relationship(r.getType(), r.getTarget(), r.getFieldName(), note));
                }
            }
            entities.add(new NormalizedSchema.Entity(c.getName(), fields, relationships));
        }
        return new NormalizedSchema(entities);
    }

    private NormalizedSchema normalizeSql(SqlSchemaDto schema) {
        List<NormalizedSchema.Entity> entities = new ArrayList<>();
        for (TableDto t : schema.getTables()) {
            List<NormalizedSchema.Field> fields = new ArrayList<>();
            fields.add(new NormalizedSchema.Field("id", "Long", true));
            if (t.getFields() != null) {
                for (FieldDto f : t.getFields()) {
                    fields.add(new NormalizedSchema.Field(f.getName(), f.getType(), f.isRequired()));
                }
            }
            List<NormalizedSchema.Relationship> relationships = new ArrayList<>();
            if (t.getRelationships() != null) {
                for (SqlRelationshipDto r : t.getRelationships()) {
                    String note = r.getReasoning() != null ? r.getReasoning() : "";
                    relationships.add(new NormalizedSchema.Relationship(r.getType(), r.getTarget(), r.getFieldName(), note));
                }
            }
            entities.add(new NormalizedSchema.Entity(t.getName(), fields, relationships));
        }
        return new NormalizedSchema(entities);
    }
}