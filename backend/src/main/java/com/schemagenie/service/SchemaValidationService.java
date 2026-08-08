package com.schemagenie.service;

import com.schemagenie.dto.*;
import com.schemagenie.exception.SchemaValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * After parsing the LLM's response, verifies every relationship target exists
 * as its own collection/table name in the same schema.
 */
@Service
public class SchemaValidationService {

    public void validate(SchemaGenerationResult result) {
        if (result.getDatabaseType() == DatabaseType.MONGODB) {
            validateMongo(result.getMongoSchema());
        } else {
            validateSql(result.getSqlSchema());
        }
    }

    private void validateMongo(MongoSchemaDto schema) {
        Set<String> names = schema.getCollections().stream()
                .map(CollectionDto::getName)
                .collect(Collectors.toSet());

        for (CollectionDto collection : schema.getCollections()) {
            List<MongoRelationshipDto> rels = collection.getRelationships();
            if (rels == null) continue;
            for (MongoRelationshipDto rel : rels) {
                if (!names.contains(rel.getTarget())) {
                    throw new SchemaValidationException(
                            "Relationship in '" + collection.getName() + "' targets unknown collection '"
                                    + rel.getTarget() + "'.");
                }
            }
        }
    }

    private void validateSql(SqlSchemaDto schema) {
        Set<String> names = schema.getTables().stream()
                .map(TableDto::getName)
                .collect(Collectors.toSet());

        for (TableDto table : schema.getTables()) {
            List<SqlRelationshipDto> rels = table.getRelationships();
            if (rels == null) continue;
            for (SqlRelationshipDto rel : rels) {
                if (!names.contains(rel.getTarget())) {
                    throw new SchemaValidationException(
                            "Relationship in '" + table.getName() + "' targets unknown table '"
                                    + rel.getTarget() + "'.");
                }
            }
        }
    }
}
