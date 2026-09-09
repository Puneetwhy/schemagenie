package com.schemagenie.generator;

import com.schemagenie.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoCodeGeneratorService {

    public String generate(MongoSchemaDto schema) {
        StringBuilder out = new StringBuilder();
        for (CollectionDto collection : schema.getCollections()) {
            out.append(generateClass(collection)).append("\n\n");
        }
        return out.toString();
    }

    private String generateClass(CollectionDto collection) {
        StringBuilder sb = new StringBuilder();
        String className = collection.getName();

        sb.append("package com.schemagenie.generated.model;\n\n");
        sb.append("import org.springframework.data.annotation.Id;\n");
        sb.append("import org.springframework.data.mongodb.core.mapping.Document;\n");
        sb.append("import org.springframework.data.mongodb.core.mapping.DBRef;\n");
        sb.append("import java.util.Date;\n");
        sb.append("import java.util.List;\n\n");

        sb.append("@Document(collection = \"").append(lower(className)).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");
        sb.append("    @Id\n    private String id;\n\n");

        if (collection.getFields() != null) {
            for (FieldDto field : collection.getFields()) {
                sb.append("    private ").append(javaType(field.getType())).append(" ")
                        .append(field.getName()).append(";\n");
            }
            sb.append("\n");
        }

        if (collection.getRelationships() != null) {
            for (MongoRelationshipDto rel : collection.getRelationships()) {
                if ("EMBED".equalsIgnoreCase(rel.getStrategy())) {
                    sb.append("    // Embedded: ").append(rel.getReasoning()).append("\n");
                    if ("ONE_TO_MANY".equalsIgnoreCase(rel.getType()) || "MANY_TO_MANY".equalsIgnoreCase(rel.getType())) {
                        sb.append("    private List<").append(rel.getTarget()).append("Embedded> ")
                                .append(rel.getFieldName()).append(";\n");
                    } else {
                        sb.append("    private ").append(rel.getTarget()).append("Embedded ")
                                .append(rel.getFieldName()).append(";\n");
                    }
                } else {
                    sb.append("    // Referenced: ").append(rel.getReasoning()).append("\n");
                    sb.append("    @DBRef\n");
                    if ("ONE_TO_MANY".equalsIgnoreCase(rel.getType()) || "MANY_TO_MANY".equalsIgnoreCase(rel.getType())) {
                        sb.append("    private List<").append(rel.getTarget()).append("> ")
                                .append(rel.getFieldName()).append(";\n");
                    } else {
                        sb.append("    private ").append(rel.getTarget()).append(" ")
                                .append(rel.getFieldName()).append(";\n");
                    }
                }
            }
            sb.append("\n");
        }

        sb.append("    public ").append(className).append("() {}\n\n");
        sb.append(generateAccessors(className, collection));

        // Nested embedded classes for EMBED relationships
        if (collection.getRelationships() != null) {
            for (MongoRelationshipDto rel : collection.getRelationships()) {
                if ("EMBED".equalsIgnoreCase(rel.getStrategy())) {
                    sb.append("\n    public static class ").append(rel.getTarget()).append("Embedded {\n");
                    sb.append("        // Fields mirror the ").append(rel.getTarget())
                            .append(" collection's fields, embedded directly.\n");
                    sb.append("        // Populate based on the ").append(rel.getTarget()).append(" schema.\n");
                    sb.append("    }\n");
                }
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateAccessors(String className, CollectionDto collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("    public String getId() { return id; }\n");
        sb.append("    public void setId(String id) { this.id = id; }\n\n");

        if (collection.getFields() != null) {
            for (FieldDto field : collection.getFields()) {
                String type = javaType(field.getType());
                String cap = capitalize(field.getName());
                sb.append("    public ").append(type).append(" get").append(cap).append("() { return ")
                        .append(field.getName()).append("; }\n");
                sb.append("    public void set").append(cap).append("(").append(type).append(" ")
                        .append(field.getName()).append(") { this.").append(field.getName())
                        .append(" = ").append(field.getName()).append("; }\n\n");
            }
        }
        return sb.toString();
    }

    private String javaType(String schemaType) {
        if (schemaType == null) return "Object";
        return switch (schemaType) {
            case "String" -> "String";
            case "Integer" -> "Integer";
            case "Double" -> "Double";
            case "Boolean" -> "Boolean";
            case "Date" -> "Date";
            case "ObjectId" -> "String";
            case "Array" -> "List<Object>";
            default -> "Object";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String lower(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1) + "s";
    }
}
