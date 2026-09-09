package com.schemagenie.service;

import com.schemagenie.dto.*;
import org.springframework.stereotype.Service;

/**
 * Builds a plain-English explanation of the generated schema, code, and
 * diagram -- reusing the per-field/per-relationship "reasoning" the LLM
 * already produced, rather than making a second LLM call.
 */
@Service
public class ExplanationService {

    public String explain(SchemaGenerationResult result) {
        StringBuilder sb = new StringBuilder();

        if (result.getDatabaseType() == DatabaseType.MONGODB) {
            explainMongo(result.getMongoSchema(), sb);
        } else {
            explainSql(result.getDatabaseType(), result.getSqlSchema(), sb);
        }

        sb.append("\nModel classes: one class was generated per ")
                .append(result.getDatabaseType() == DatabaseType.MONGODB ? "collection" : "table")
                .append(", matching the fields and relationships described above.\n");

        sb.append("Migration script: creates each ")
                .append(result.getDatabaseType() == DatabaseType.MONGODB ? "collection with a starter index" : "table, then adds foreign keys and join tables")
                .append(" in dependency order, so referenced ")
                .append(result.getDatabaseType() == DatabaseType.MONGODB ? "collections" : "tables")
                .append(" always exist first.\n");

        sb.append("ER diagram: visualizes every entity and its fields, with connecting lines showing ")
                .append("each relationship's cardinality (one-to-one, one-to-many, or many-to-many).\n");

        return sb.toString();
    }

    private void explainMongo(MongoSchemaDto schema, StringBuilder sb) {
        sb.append("Schema overview (MongoDB)\n");
        sb.append("=========================\n");
        for (CollectionDto c : schema.getCollections()) {
            int fieldCount = c.getFields() == null ? 0 : c.getFields().size();
            sb.append("\n- ").append(c.getName()).append(" (").append(fieldCount).append(" fields)\n");
            if (c.getRelationships() != null) {
                for (MongoRelationshipDto rel : c.getRelationships()) {
                    sb.append("    -> ").append(rel.getType()).append(" with ").append(rel.getTarget())
                            .append(" via \"").append(rel.getFieldName()).append("\" (")
                            .append(rel.getStrategy()).append("): ")
                            .append(rel.getReasoning() != null ? rel.getReasoning() : "").append("\n");
                }
            }
        }
    }

    private void explainSql(DatabaseType dbType, SqlSchemaDto schema, StringBuilder sb) {
        sb.append("Schema overview (").append(dbType).append(")\n");
        sb.append("=========================\n");
        for (TableDto t : schema.getTables()) {
            int fieldCount = t.getFields() == null ? 0 : t.getFields().size();
            sb.append("\n- ").append(t.getName()).append(" (").append(fieldCount).append(" fields)\n");
            if (t.getRelationships() != null) {
                for (SqlRelationshipDto rel : t.getRelationships()) {
                    sb.append("    -> ").append(rel.getType()).append(" with ").append(rel.getTarget())
                            .append(" via \"").append(rel.getFieldName()).append("\"");
                    if (rel.getJoinTable() != null) {
                        sb.append(" (join table: ").append(rel.getJoinTable()).append(")");
                    }
                    sb.append(": ").append(rel.getReasoning() != null ? rel.getReasoning() : "").append("\n");
                }
            }
        }
    }
}