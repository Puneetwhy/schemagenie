package com.schemagenie.generator;

import com.schemagenie.dto.*;
import org.springframework.stereotype.Component;

@Component
public class DiagramGeneratorService {

    public String generateFromMongo(MongoSchemaDto schema) {
        StringBuilder sb = new StringBuilder("erDiagram\n");

        for (CollectionDto c : schema.getCollections()) {
            sb.append("    ").append(c.getName()).append(" {\n");
            if (c.getFields() != null) {
                for (FieldDto f : c.getFields()) {
                    sb.append("        ").append(mongoDiagramType(f.getType())).append(" ")
                            .append(f.getName()).append("\n");
                }
            }
            sb.append("    }\n");
        }

        for (CollectionDto c : schema.getCollections()) {
            if (c.getRelationships() == null) continue;
            for (MongoRelationshipDto rel : c.getRelationships()) {
                sb.append("    ").append(c.getName()).append(" ")
                        .append(cardinality(rel.getType())).append(" ")
                        .append(rel.getTarget()).append(" : \"")
                        .append(rel.getFieldName()).append("\"\n");
            }
        }
        return sb.toString();
    }

    public String generateFromSql(SqlSchemaDto schema) {
        StringBuilder sb = new StringBuilder("erDiagram\n");

        for (TableDto t : schema.getTables()) {
            sb.append("    ").append(t.getName()).append(" {\n");
            sb.append("        Long id PK\n");
            if (t.getFields() != null) {
                for (FieldDto f : t.getFields()) {
                    sb.append("        ").append(f.getType()).append(" ").append(f.getName()).append("\n");
                }
            }
            sb.append("    }\n");
        }

        for (TableDto t : schema.getTables()) {
            if (t.getRelationships() == null) continue;
            for (SqlRelationshipDto rel : t.getRelationships()) {
                sb.append("    ").append(t.getName()).append(" ")
                        .append(cardinality(rel.getType())).append(" ")
                        .append(rel.getTarget()).append(" : \"")
                        .append(rel.getFieldName()).append("\"\n");
            }
        }
        return sb.toString();
    }

    private String cardinality(String type) {
        if (type == null) return "||--o{";
        return switch (type.toUpperCase()) {
            case "ONE_TO_ONE" -> "||--||";
            case "MANY_TO_MANY" -> "}o--o{";
            default -> "||--o{"; // ONE_TO_MANY
        };
    }

    private String mongoDiagramType(String type) {
        return type == null ? "string" : type.toLowerCase();
    }
}
