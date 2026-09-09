package com.schemagenie.generator;

import com.schemagenie.dto.DatabaseType;
import com.schemagenie.dto.ProgrammingLanguage;
import com.schemagenie.dto.SchemaGenerationResult;
import org.springframework.stereotype.Component;

@Component
public class CodeGeneratorFactory {

    private final MongoCodeGeneratorService mongoCodeGenerator;
    private final MongockGeneratorService mongockGenerator;
    private final JpaCodeGeneratorService jpaCodeGenerator;
    private final LiquibaseGeneratorService liquibaseGenerator;
    private final DiagramGeneratorService diagramGenerator;
    private final SchemaNormalizer schemaNormalizer;
    private final MultiLanguageModelGenerator multiLanguageGenerator;

    public CodeGeneratorFactory(MongoCodeGeneratorService mongoCodeGenerator,
                                MongockGeneratorService mongockGenerator,
                                JpaCodeGeneratorService jpaCodeGenerator,
                                LiquibaseGeneratorService liquibaseGenerator,
                                DiagramGeneratorService diagramGenerator,
                                SchemaNormalizer schemaNormalizer,
                                MultiLanguageModelGenerator multiLanguageGenerator) {
        this.mongoCodeGenerator = mongoCodeGenerator;
        this.mongockGenerator = mongockGenerator;
        this.jpaCodeGenerator = jpaCodeGenerator;
        this.liquibaseGenerator = liquibaseGenerator;
        this.diagramGenerator = diagramGenerator;
        this.schemaNormalizer = schemaNormalizer;
        this.multiLanguageGenerator = multiLanguageGenerator;
    }

    public record GeneratedOutputs(String modelClasses, String migrationScript, String erDiagram) {}

    public GeneratedOutputs generate(SchemaGenerationResult result, ProgrammingLanguage language) {
        boolean isMongo = result.getDatabaseType() == DatabaseType.MONGODB;

        String diagram = isMongo
                ? diagramGenerator.generateFromMongo(result.getMongoSchema())
                : diagramGenerator.generateFromSql(result.getSqlSchema());

        // Java keeps the full JPA/Mongo-annotated generators + Mongock/Liquibase migrations.
        if (language == ProgrammingLanguage.JAVA) {
            if (isMongo) {
                var schema = result.getMongoSchema();
                return new GeneratedOutputs(
                        mongoCodeGenerator.generate(schema),
                        mongockGenerator.generate(schema),
                        diagram
                );
            } else {
                var schema = result.getSqlSchema();
                return new GeneratedOutputs(
                        jpaCodeGenerator.generate(schema),
                        liquibaseGenerator.generate(schema),
                        diagram
                );
            }
        }

        // Every other language: idiomatic model code via MultiLanguageModelGenerator.
        // Migration script stays as Liquibase YAML for SQL targets (tool-level, not
        // tied to app language); for MongoDB it's a plain mongosh script since
        // Mongock is Java-specific.
        NormalizedSchema normalized = schemaNormalizer.normalize(result);
        String modelClasses = multiLanguageGenerator.generate(language, normalized);

        String migrationScript = isMongo
                ? buildMongoShellMigration(normalized)
                : liquibaseGenerator.generate(result.getSqlSchema());

        return new GeneratedOutputs(modelClasses, migrationScript, diagram);
    }

    private String buildMongoShellMigration(NormalizedSchema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("// MongoDB shell migration script (run with: mongosh < migration.js)\n\n");
        for (NormalizedSchema.Entity e : schema.getEntities()) {
            String collection = Character.toLowerCase(e.name().charAt(0)) + e.name().substring(1) + "s";
            String indexField = e.fields().isEmpty() ? "_id" : e.fields().get(0).name();
            sb.append("db.createCollection(\"").append(collection).append("\");\n");
            sb.append("db.").append(collection).append(".createIndex({ ").append(indexField).append(": 1 });\n\n");
        }
        return sb.toString();
    }
}