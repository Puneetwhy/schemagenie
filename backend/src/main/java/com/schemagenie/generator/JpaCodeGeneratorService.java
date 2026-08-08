package com.schemagenie.generator;

import com.schemagenie.dto.*;
import org.springframework.stereotype.Component;

@Component
public class JpaCodeGeneratorService {

    public String generate(SqlSchemaDto schema) {
        StringBuilder out = new StringBuilder();
        for (TableDto table : schema.getTables()) {
            out.append(generateEntity(table)).append("\n\n");
        }
        return out.toString();
    }

    private String generateEntity(TableDto table) {
        StringBuilder sb = new StringBuilder();
        String className = table.getName();
        String tableName = toSnakeCase(className);

        sb.append("package com.schemagenie.generated.model;\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import java.util.Date;\n");
        sb.append("import java.util.List;\n\n");

        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(tableName).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");
        sb.append("    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n    private Long id;\n\n");

        if (table.getFields() != null) {
            for (FieldDto field : table.getFields()) {
                String columnAttrs = "nullable = " + !field.isRequired()
                        + (field.isUnique() ? ", unique = true" : "");
                sb.append("    @Column(").append(columnAttrs).append(")\n");
                sb.append("    private ").append(javaType(field.getType())).append(" ")
                        .append(field.getName()).append(";\n\n");
            }
        }

        if (table.getRelationships() != null) {
            for (SqlRelationshipDto rel : table.getRelationships()) {
                sb.append("    // ").append(rel.getReasoning()).append("\n");
                switch (rel.getType() == null ? "" : rel.getType().toUpperCase()) {
                    case "ONE_TO_MANY" -> {
                        sb.append("    @OneToMany(mappedBy = \"").append(lowerFirst(className))
                                .append("\", cascade = CascadeType.ALL, orphanRemoval = true)\n");
                        sb.append("    private List<").append(rel.getTarget()).append("> ")
                                .append(rel.getFieldName()).append(";\n\n");
                    }
                    case "ONE_TO_ONE" -> {
                        sb.append("    @OneToOne\n");
                        sb.append("    @JoinColumn(name = \"").append(toSnakeCase(rel.getFieldName())).append("_id\")\n");
                        sb.append("    private ").append(rel.getTarget()).append(" ")
                                .append(rel.getFieldName()).append(";\n\n");
                    }
                    case "MANY_TO_MANY" -> {
                        sb.append("    @ManyToMany\n");
                        sb.append("    @JoinTable(\n");
                        sb.append("        name = \"").append(rel.getJoinTable()).append("\",\n");
                        sb.append("        joinColumns = @JoinColumn(name = \"").append(toSnakeCase(className)).append("_id\"),\n");
                        sb.append("        inverseJoinColumns = @JoinColumn(name = \"").append(toSnakeCase(rel.getTarget())).append("_id\")\n");
                        sb.append("    )\n");
                        sb.append("    private List<").append(rel.getTarget()).append("> ")
                                .append(rel.getFieldName()).append(";\n\n");
                    }
                    default -> {
                        // MANY_TO_ONE style child side, if the LLM ever emits it directly
                        sb.append("    @ManyToOne\n");
                        sb.append("    @JoinColumn(name = \"").append(toSnakeCase(rel.getFieldName())).append("_id\")\n");
                        sb.append("    private ").append(rel.getTarget()).append(" ")
                                .append(rel.getFieldName()).append(";\n\n");
                    }
                }
            }
        }

        sb.append("    public ").append(className).append("() {}\n\n");
        sb.append(generateAccessors(table));
        sb.append("}\n");
        return sb.toString();
    }

    private String generateAccessors(TableDto table) {
        StringBuilder sb = new StringBuilder();
        sb.append("    public Long getId() { return id; }\n");
        sb.append("    public void setId(Long id) { this.id = id; }\n\n");

        if (table.getFields() != null) {
            for (FieldDto field : table.getFields()) {
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
            case "Long" -> "Long";
            case "Double" -> "Double";
            case "Boolean" -> "Boolean";
            case "Date" -> "Date";
            case "Timestamp" -> "java.sql.Timestamp";
            default -> "Object";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String lowerFirst(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private String toSnakeCase(String s) {
        return s.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
