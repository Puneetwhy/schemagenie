package com.schemagenie.service;

import com.schemagenie.dto.DatabaseType;
import com.schemagenie.model.GeneratedSchema;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    public byte[] buildZip(GeneratedSchema session) throws IOException {
        boolean isMongo = session.getDatabaseType() == DatabaseType.MONGODB;
        String modelExt = ".java";
        String migrationName = isMongo ? "migration/changeUnits.java" : "migration/changelog.yaml";
        String modelDir = isMongo ? "model/documents" : "model/entities";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            writeEntry(zos, modelDir + modelExt, session.getModelClasses());
            writeEntry(zos, migrationName, session.getMigrationScript());
            writeEntry(zos, "diagram/schema.mmd", session.getErDiagram());
            writeEntry(zos, "raw/schema.json", session.getRawJson());
            writeEntry(zos, "README.txt", buildReadme(session));
        }
        return baos.toByteArray();
    }

    private void writeEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write((content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String buildReadme(GeneratedSchema session) {
        return """
                SchemaGenie generated output
                =============================
                Database target: %s
                Description: %s
                Session ID: %s

                Contents:
                  model/           - generated model classes
                  migration/       - migration script (Mongock changeUnits or Liquibase changelog)
                  diagram/         - Mermaid.js ER diagram source (schema.mmd)
                  raw/schema.json  - the raw schema JSON returned by the LLM
                """.formatted(session.getDatabaseType(), session.getDescription(), session.getSessionId());
    }
}
