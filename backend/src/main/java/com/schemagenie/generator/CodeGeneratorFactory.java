package com.schemagenie.generator;

import com.schemagenie.dto.DatabaseType;
import com.schemagenie.dto.SchemaGenerationResult;
import org.springframework.stereotype.Component;

/**
 * Strategy factory: given a databaseType, produces the (models, migration, diagram)
 * output bundle without the controller needing to branch on if/else.
 */
@Component
public class CodeGeneratorFactory {

    private final MongoCodeGeneratorService mongoCodeGenerator;
    private final MongockGeneratorService mongockGenerator;
    private final JpaCodeGeneratorService jpaCodeGenerator;
    private final LiquibaseGeneratorService liquibaseGenerator;
    private final DiagramGeneratorService diagramGenerator;

    public CodeGeneratorFactory(MongoCodeGeneratorService mongoCodeGenerator,
                                 MongockGeneratorService mongockGenerator,
                                 JpaCodeGeneratorService jpaCodeGenerator,
                                 LiquibaseGeneratorService liquibaseGenerator,
                                 DiagramGeneratorService diagramGenerator) {
        this.mongoCodeGenerator = mongoCodeGenerator;
        this.mongockGenerator = mongockGenerator;
        this.jpaCodeGenerator = jpaCodeGenerator;
        this.liquibaseGenerator = liquibaseGenerator;
        this.diagramGenerator = diagramGenerator;
    }

    public record GeneratedOutputs(String modelClasses, String migrationScript, String erDiagram) {}

    public GeneratedOutputs generate(SchemaGenerationResult result) {
        if (result.getDatabaseType() == DatabaseType.MONGODB) {
            var schema = result.getMongoSchema();
            return new GeneratedOutputs(
                    mongoCodeGenerator.generate(schema),
                    mongockGenerator.generate(schema),
                    diagramGenerator.generateFromMongo(schema)
            );
        } else {
            var schema = result.getSqlSchema();
            return new GeneratedOutputs(
                    jpaCodeGenerator.generate(schema),
                    liquibaseGenerator.generate(schema),
                    diagramGenerator.generateFromSql(schema)
            );
        }
    }
}
