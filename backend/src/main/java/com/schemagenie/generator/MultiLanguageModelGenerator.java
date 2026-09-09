package com.schemagenie.generator;

import com.schemagenie.dto.ProgrammingLanguage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Generates idiomatic model classes/structs in a chosen non-Java language
 * from the language-agnostic NormalizedSchema. Java keeps using the
 * existing JPA/Mongo-annotated generators (MongoCodeGeneratorService /
 * JpaCodeGeneratorService) since those map to Java-specific frameworks.
 */
@Component
public class MultiLanguageModelGenerator {

    public String generate(ProgrammingLanguage language, NormalizedSchema schema) {
        return switch (language) {
            case JAVASCRIPT -> generateJavaScript(schema);
            case PYTHON -> generatePython(schema);
            case PHP -> generatePhp(schema);
            case GO -> generateGo(schema);
            case C -> generateC(schema);
            case CPP -> generateCpp(schema);
            case CSHARP -> generateCSharp(schema);
            case JAVA -> throw new IllegalArgumentException("Java uses the dedicated JPA/Mongo generators.");
        };
    }

    // ---------- JavaScript (ES6 class, JSDoc types) ----------
    private String generateJavaScript(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "string", "Integer", "number", "Long", "number", "Double", "number",
                "Boolean", "boolean", "Date", "Date", "Timestamp", "Date", "ObjectId", "string", "Array", "Array"
        );
        StringBuilder sb = new StringBuilder();
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("/**\n * ").append(e.name()).append(" model\n");
            for (NormalizedSchema.Field f : e.fields()) {
                sb.append(" * @property {").append(types.getOrDefault(f.type(), "any")).append("} ")
                        .append(f.name()).append(f.required() ? "" : " (optional)").append("\n");
            }
            sb.append(" */\n");
            sb.append("class ").append(e.name()).append(" {\n");
            sb.append("  constructor({ ")
                    .append(String.join(", ", e.fields().stream().map(NormalizedSchema.Field::name).toList()))
                    .append(" } = {}) {\n");
            for (NormalizedSchema.Field f : e.fields()) {
                sb.append("    this.").append(f.name()).append(" = ").append(f.name()).append(";\n");
            }
            sb.append("  }\n");
            appendRelComments(sb, e, "  //");
            sb.append("}\n\nmodule.exports = ").append(e.name()).append(";\n\n");
        }
        return sb.toString();
    }

    // ---------- Python (dataclass) ----------
    private String generatePython(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "str", "Integer", "int", "Long", "int", "Double", "float",
                "Boolean", "bool", "Date", "date", "Timestamp", "datetime", "ObjectId", "str", "Array", "list"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("from dataclasses import dataclass, field\n");
        sb.append("from datetime import date, datetime\n");
        sb.append("from typing import Optional, List\n\n\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("@dataclass\n");
            sb.append("class ").append(e.name()).append(":\n");
            if (e.fields().isEmpty()) sb.append("    pass\n");
            for (NormalizedSchema.Field f : e.fields()) {
                String type = types.getOrDefault(f.type(), "str");
                sb.append("    ").append(f.name()).append(": ")
                        .append(f.required() ? type : "Optional[" + type + "] = None").append("\n");
            }
            appendRelComments(sb, e, "    #");
            sb.append("\n\n");
        }
        return sb.toString();
    }

    // ---------- PHP (typed class properties) ----------
    private String generatePhp(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "string", "Integer", "int", "Long", "int", "Double", "float",
                "Boolean", "bool", "Date", "\\DateTime", "Timestamp", "\\DateTime", "ObjectId", "string", "Array", "array"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("<?php\n\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("class ").append(e.name()).append(" {\n");
            for (NormalizedSchema.Field f : e.fields()) {
                String type = types.getOrDefault(f.type(), "string");
                sb.append("    public ").append(f.required() ? "" : "?").append(type)
                        .append(" $").append(f.name()).append(";\n");
            }
            appendRelComments(sb, e, "    //");
            sb.append("}\n\n");
        }
        return sb.toString();
    }

    // ---------- Go (struct with json tags) ----------
    private String generateGo(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "string", "Integer", "int", "Long", "int64", "Double", "float64",
                "Boolean", "bool", "Date", "time.Time", "Timestamp", "time.Time", "ObjectId", "string", "Array", "[]interface{}"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("package models\n\n");
        sb.append("import \"time\"\n\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("type ").append(e.name()).append(" struct {\n");
            for (NormalizedSchema.Field f : e.fields()) {
                String type = types.getOrDefault(f.type(), "string");
                String goFieldName = capitalize(f.name());
                sb.append("\t").append(goFieldName).append(" ").append(type)
                        .append(" `json:\"").append(f.name()).append("\"`\n");
            }
            appendRelComments(sb, e, "\t//");
            sb.append("}\n\n");
        }
        return sb.toString();
    }

    // ---------- C (plain struct) ----------
    private String generateC(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "char*", "Integer", "int", "Long", "long", "Double", "double",
                "Boolean", "int", "Date", "char*", "Timestamp", "char*", "ObjectId", "char*", "Array", "void*"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("#include <stdbool.h>\n\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("typedef struct {\n");
            for (NormalizedSchema.Field f : e.fields()) {
                String type = types.getOrDefault(f.type(), "char*");
                sb.append("    ").append(type).append(" ").append(f.name()).append(";\n");
            }
            appendRelComments(sb, e, "    //");
            sb.append("} ").append(e.name()).append(";\n\n");
        }
        return sb.toString();
    }

    // ---------- C++ (class with std types) ----------
    private String generateCpp(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "std::string", "Integer", "int", "Long", "long", "Double", "double",
                "Boolean", "bool", "Date", "std::string", "Timestamp", "std::string", "ObjectId", "std::string", "Array", "std::vector<std::string>"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("#include <string>\n#include <vector>\n\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("class ").append(e.name()).append(" {\n public:\n");
            for (NormalizedSchema.Field f : e.fields()) {
                String type = types.getOrDefault(f.type(), "std::string");
                sb.append("    ").append(type).append(" ").append(f.name()).append(";\n");
            }
            appendRelComments(sb, e, "    //");
            sb.append("};\n\n");
        }
        return sb.toString();
    }

    // ---------- C# (class with properties) ----------
    private String generateCSharp(NormalizedSchema schema) {
        Map<String, String> types = Map.of(
                "String", "string", "Integer", "int", "Long", "long", "Double", "double",
                "Boolean", "bool", "Date", "DateTime", "Timestamp", "DateTime", "ObjectId", "string", "Array", "List<object>"
        );
        StringBuilder sb = new StringBuilder();
        sb.append("using System;\nusing System.Collections.Generic;\n\n");
        sb.append("namespace SchemaGenie.Generated.Models\n{\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            sb.append("    public class ").append(e.name()).append("\n    {\n");
            for (NormalizedSchema.Field f : e.fields()) {
                String type = types.getOrDefault(f.type(), "string");
                sb.append("        public ").append(type).append(" ")
                        .append(capitalize(f.name())).append(" { get; set; }\n");
            }
            appendRelComments(sb, e, "        //");
            sb.append("    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private void appendRelComments(StringBuilder sb, NormalizedSchema.Entity e, String prefix) {
        if (e.relationships() == null || e.relationships().isEmpty()) return;
        for (NormalizedSchema.Relationship r : e.relationships()) {
            sb.append(prefix).append(" ").append(r.type()).append(" -> ").append(r.target())
                    .append(" (").append(r.fieldName()).append("): ").append(r.note()).append("\n");
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** File extension to use in the ZIP / display for a given language. */
    public String fileExtension(ProgrammingLanguage language) {
        return switch (language) {
            case JAVA -> "java";
            case JAVASCRIPT -> "js";
            case PYTHON -> "py";
            case PHP -> "php";
            case GO -> "go";
            case C -> "c";
            case CPP -> "cpp";
            case CSHARP -> "cs";
        };
    }

    /** Language identifier used by the frontend's syntax highlighter. */
    public String highlightLanguage(ProgrammingLanguage language) {
        return switch (language) {
            case JAVA -> "java";
            case JAVASCRIPT -> "javascript";
            case PYTHON -> "python";
            case PHP -> "php";
            case GO -> "go";
            case C -> "c";
            case CPP -> "cpp";
            case CSHARP -> "csharp";
        };
    }
}