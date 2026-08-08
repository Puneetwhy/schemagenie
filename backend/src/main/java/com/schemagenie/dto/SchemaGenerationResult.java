package com.schemagenie.dto;

/**
 * Wraps the databaseType alongside whichever DTO shape applies, so downstream
 * code (generators, controller) can branch on a single field instead of
 * repeatedly checking types.
 */
public class SchemaGenerationResult {
    private DatabaseType databaseType;
    private MongoSchemaDto mongoSchema; // populated only when databaseType == MONGODB
    private SqlSchemaDto sqlSchema;     // populated only when databaseType == POSTGRESQL

    public SchemaGenerationResult() {}

    public static SchemaGenerationResult ofMongo(MongoSchemaDto schema) {
        SchemaGenerationResult r = new SchemaGenerationResult();
        r.databaseType = DatabaseType.MONGODB;
        r.mongoSchema = schema;
        return r;
    }

    public static SchemaGenerationResult ofSql(SqlSchemaDto schema) {
        SchemaGenerationResult r = new SchemaGenerationResult();
        r.databaseType = DatabaseType.POSTGRESQL;
        r.sqlSchema = schema;
        return r;
    }

    public DatabaseType getDatabaseType() { return databaseType; }
    public void setDatabaseType(DatabaseType databaseType) { this.databaseType = databaseType; }
    public MongoSchemaDto getMongoSchema() { return mongoSchema; }
    public void setMongoSchema(MongoSchemaDto mongoSchema) { this.mongoSchema = mongoSchema; }
    public SqlSchemaDto getSqlSchema() { return sqlSchema; }
    public void setSqlSchema(SqlSchemaDto sqlSchema) { this.sqlSchema = sqlSchema; }
}
