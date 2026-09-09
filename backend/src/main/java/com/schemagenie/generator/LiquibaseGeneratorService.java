package com.schemagenie.generator;

import com.schemagenie.dto.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LiquibaseGeneratorService {

    public String generate(SqlSchemaDto schema) {
        List<TableDto> ordered = orderByDependency(schema.getTables());

        StringBuilder sb = new StringBuilder();
        sb.append("databaseChangeLog:\n");

        int id = 1;
        for (TableDto table : ordered) {
            sb.append(createTableChangeset(table, id++));
        }

        // Foreign key constraints, added after all tables exist.
        for (TableDto table : ordered) {
            if (table.getRelationships() == null) continue;
            for (SqlRelationshipDto rel : table.getRelationships()) {
                if ("ONE_TO_MANY".equalsIgnoreCase(rel.getType()) || "ONE_TO_ONE".equalsIgnoreCase(rel.getType())) {
                    sb.append(addForeignKeyChangeset(table, rel, id++));
                }
            }
        }

        // Join tables for MANY_TO_MANY, created last since they reference two tables.
        Set<String> createdJoinTables = new HashSet<>();
        for (TableDto table : ordered) {
            if (table.getRelationships() == null) continue;
            for (SqlRelationshipDto rel : table.getRelationships()) {
                if ("MANY_TO_MANY".equalsIgnoreCase(rel.getType()) && rel.getJoinTable() != null
                        && createdJoinTables.add(rel.getJoinTable())) {
                    sb.append(createJoinTableChangeset(table.getName(), rel, id++));
                }
            }
        }

        return sb.toString();
    }

    private List<TableDto> orderByDependency(List<TableDto> tables) {
        Map<String, TableDto> byName = new LinkedHashMap<>();
        for (TableDto t : tables) byName.put(t.getName(), t);

        List<TableDto> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (TableDto t : tables) {
            visit(t, byName, visited, ordered, new HashSet<>());
        }
        return ordered;
    }

    private void visit(TableDto t, Map<String, TableDto> byName, Set<String> visited,
                        List<TableDto> ordered, Set<String> visiting) {
        if (visited.contains(t.getName()) || visiting.contains(t.getName())) return;
        visiting.add(t.getName());

        if (t.getRelationships() != null) {
            for (SqlRelationshipDto rel : t.getRelationships()) {
                // The "many" side references the "one" side; the referenced (parent) table
                // must exist first, so for ONE_TO_MANY the *target* is the parent here only
                // when this table is actually the child (holds the FK). We approximate by
                // ensuring any ONE_TO_ONE / ONE_TO_MANY target is created before this table.
                if ("ONE_TO_ONE".equalsIgnoreCase(rel.getType())) {
                    TableDto target = byName.get(rel.getTarget());
                    if (target != null && !target.getName().equals(t.getName())) {
                        visit(target, byName, visited, ordered, visiting);
                    }
                }
            }
        }

        visiting.remove(t.getName());
        visited.add(t.getName());
        ordered.add(t);
    }

    private String createTableChangeset(TableDto table, int id) {
        String tableName = toSnakeCase(table.getName());
        StringBuilder sb = new StringBuilder();
        sb.append("  - changeSet:\n");
        sb.append("      id: ").append(id).append("-create-").append(tableName).append("\n");
        sb.append("      author: schemagenie\n");
        sb.append("      changes:\n");
        sb.append("        - createTable:\n");
        sb.append("            tableName: ").append(tableName).append("\n");
        sb.append("            columns:\n");
        sb.append("              - column:\n");
        sb.append("                  name: id\n");
        sb.append("                  type: BIGSERIAL\n");
        sb.append("                  constraints:\n");
        sb.append("                    primaryKey: true\n");
        sb.append("                    nullable: false\n");

        if (table.getFields() != null) {
            for (FieldDto field : table.getFields()) {
                sb.append("              - column:\n");
                sb.append("                  name: ").append(toSnakeCase(field.getName())).append("\n");
                sb.append("                  type: ").append(sqlType(field.getType())).append("\n");
                if (field.isRequired() || field.isUnique()) {
                    sb.append("                  constraints:\n");
                    if (field.isRequired()) sb.append("                    nullable: false\n");
                    if (field.isUnique()) sb.append("                    unique: true\n");
                }
            }
        }
        return sb.toString();
    }

    private String addForeignKeyChangeset(TableDto table, SqlRelationshipDto rel, int id) {
        String childTable = toSnakeCase(rel.getTarget()); // child holds the FK for ONE_TO_MANY
        String parentTable = toSnakeCase(table.getName());
        String fkColumn = toSnakeCase(table.getName()) + "_id";

        StringBuilder sb = new StringBuilder();
        sb.append("  - changeSet:\n");
        sb.append("      id: ").append(id).append("-fk-").append(childTable).append("-").append(parentTable).append("\n");
        sb.append("      author: schemagenie\n");
        sb.append("      changes:\n");
        sb.append("        - addColumn:\n");
        sb.append("            tableName: ").append(childTable).append("\n");
        sb.append("            columns:\n");
        sb.append("              - column:\n");
        sb.append("                  name: ").append(fkColumn).append("\n");
        sb.append("                  type: BIGINT\n");
        sb.append("        - addForeignKeyConstraint:\n");
        sb.append("            baseTableName: ").append(childTable).append("\n");
        sb.append("            baseColumnNames: ").append(fkColumn).append("\n");
        sb.append("            referencedTableName: ").append(parentTable).append("\n");
        sb.append("            referencedColumnNames: id\n");
        sb.append("            constraintName: fk_").append(childTable).append("_").append(parentTable).append("\n");
        return sb.toString();
    }

    private String createJoinTableChangeset(String tableName, SqlRelationshipDto rel, int id) {
        String joinTable = rel.getJoinTable();
        String left = toSnakeCase(tableName);
        String right = toSnakeCase(rel.getTarget());

        StringBuilder sb = new StringBuilder();
        sb.append("  - changeSet:\n");
        sb.append("      id: ").append(id).append("-create-").append(joinTable).append("\n");
        sb.append("      author: schemagenie\n");
        sb.append("      changes:\n");
        sb.append("        - createTable:\n");
        sb.append("            tableName: ").append(joinTable).append("\n");
        sb.append("            columns:\n");
        sb.append("              - column:\n");
        sb.append("                  name: ").append(left).append("_id\n");
        sb.append("                  type: BIGINT\n");
        sb.append("                  constraints:\n");
        sb.append("                    nullable: false\n");
        sb.append("              - column:\n");
        sb.append("                  name: ").append(right).append("_id\n");
        sb.append("                  type: BIGINT\n");
        sb.append("                  constraints:\n");
        sb.append("                    nullable: false\n");
        sb.append("        - addForeignKeyConstraint:\n");
        sb.append("            baseTableName: ").append(joinTable).append("\n");
        sb.append("            baseColumnNames: ").append(left).append("_id\n");
        sb.append("            referencedTableName: ").append(left).append("\n");
        sb.append("            referencedColumnNames: id\n");
        sb.append("            constraintName: fk_").append(joinTable).append("_").append(left).append("\n");
        sb.append("        - addForeignKeyConstraint:\n");
        sb.append("            baseTableName: ").append(joinTable).append("\n");
        sb.append("            baseColumnNames: ").append(right).append("_id\n");
        sb.append("            referencedTableName: ").append(right).append("\n");
        sb.append("            referencedColumnNames: id\n");
        sb.append("            constraintName: fk_").append(joinTable).append("_").append(right).append("\n");
        return sb.toString();
    }

    private String sqlType(String schemaType) {
        if (schemaType == null) return "VARCHAR(255)";
        return switch (schemaType) {
            case "String" -> "VARCHAR(255)";
            case "Integer" -> "INT";
            case "Long" -> "BIGINT";
            case "Double" -> "DOUBLE PRECISION";
            case "Boolean" -> "BOOLEAN";
            case "Date" -> "DATE";
            case "Timestamp" -> "TIMESTAMP";
            default -> "VARCHAR(255)";
        };
    }

    private String toSnakeCase(String s) {
        return s.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
