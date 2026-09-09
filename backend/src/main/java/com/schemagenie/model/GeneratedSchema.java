package com.schemagenie.model;

import com.schemagenie.dto.DatabaseType;
import com.schemagenie.dto.ProgrammingLanguage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "generated_schemas")
public class GeneratedSchema {
    @Id
    private String sessionId;

    private String description;
    private DatabaseType databaseType;
    private ProgrammingLanguage language = ProgrammingLanguage.JAVA;
    private String rawJson;
    private String modelClasses;
    private String migrationScript;
    private String erDiagram;
    private String explanation;

    @Indexed
    private String userId;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public GeneratedSchema() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DatabaseType getDatabaseType() { return databaseType; }
    public void setDatabaseType(DatabaseType databaseType) { this.databaseType = databaseType; }
    public ProgrammingLanguage getLanguage() { return language; }
    public void setLanguage(ProgrammingLanguage language) { this.language = language; }
    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
    public String getModelClasses() { return modelClasses; }
    public void setModelClasses(String modelClasses) { this.modelClasses = modelClasses; }
    public String getMigrationScript() { return migrationScript; }
    public void setMigrationScript(String migrationScript) { this.migrationScript = migrationScript; }
    public String getErDiagram() { return erDiagram; }
    public void setErDiagram(String erDiagram) { this.erDiagram = erDiagram; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}