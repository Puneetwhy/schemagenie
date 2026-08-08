package com.schemagenie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GenerateSchemaRequest {
    @NotBlank
    private String description;

    @NotNull
    private DatabaseType databaseType;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DatabaseType getDatabaseType() { return databaseType; }
    public void setDatabaseType(DatabaseType databaseType) { this.databaseType = databaseType; }
}
