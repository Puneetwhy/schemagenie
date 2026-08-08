package com.schemagenie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemagenie.dto.*;
import com.schemagenie.exception.SchemaGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final String MONGO_SYSTEM_PROMPT = """
            You are a MongoDB database architect. Given a plain-English description of an application,
            your job is to design a normalized MongoDB schema.

            Respond with ONLY valid JSON - no markdown formatting, no code fences, no explanatory text
            before or after. The JSON must strictly follow this structure:

            {
              "collections": [
                {
                  "name": "string (PascalCase collection name)",
                  "fields": [
                    { "name": "string (camelCase)", "type": "String | Integer | Double | Boolean | Date | ObjectId | Array", "required": true }
                  ],
                  "relationships": [
                    { "type": "ONE_TO_ONE | ONE_TO_MANY | MANY_TO_MANY", "target": "string", "strategy": "REFERENCE or EMBED", "fieldName": "string", "reasoning": "string" }
                  ]
                }
              ]
            }

            Rules for REFERENCE vs EMBED:
            - EMBED when data is small, always accessed with parent, rarely queried independently.
            - REFERENCE when data is large, independently updated, or independently queried.
            - MANY_TO_MANY uses REFERENCE on both sides unless one side is small and bounded.

            Every relationship target must exist as its own collection entry. Do not invent fields
            the user did not imply. Keep naming consistent across the schema.
            """;

    private static final String SQL_SYSTEM_PROMPT = """
            You are a relational database architect. Given a plain-English description of an
            application, design a normalized (3NF) PostgreSQL schema.

            Respond with ONLY valid JSON - no markdown formatting, no code fences, no explanatory text
            before or after. The JSON must strictly follow this structure:

            {
              "tables": [
                {
                  "name": "string (PascalCase entity/table name)",
                  "fields": [
                    { "name": "string (camelCase)", "type": "String | Integer | Long | Double | Boolean | Date | Timestamp", "required": true, "unique": false }
                  ],
                  "relationships": [
                    { "type": "ONE_TO_ONE | ONE_TO_MANY | MANY_TO_MANY", "target": "string", "fieldName": "string", "joinTable": "string or null (only for MANY_TO_MANY)", "reasoning": "string" }
                  ]
                }
              ]
            }

            Rules:
            - Every table needs a primary key (assume auto-generated Long id, don't list it explicitly).
            - MANY_TO_MANY relationships must specify a joinTable name (e.g., "student_course").
            - Foreign keys are implied by ONE_TO_MANY/MANY_TO_ONE relationships, not listed as fields directly.
            - Normalize properly - don't duplicate data across tables that a foreign key can reference.

            Every relationship target must exist as its own table entry. Do not invent fields the
            user did not imply. Keep naming consistent across the schema.
            """;

    private static final String RETRY_INSTRUCTION =
            "\n\nYour previous response was not valid JSON. Return ONLY the JSON object, nothing else.";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public LlmService(@Value("${gemini.api-key}") String apiKey,
                      @Value("${gemini.model}") String model,
                      @Value("${gemini.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public record LlmSchemaResult(String rawJson, SchemaGenerationResult schema) {}

    public LlmSchemaResult generateSchema(String description, DatabaseType databaseType) {
        String systemPrompt = databaseType == DatabaseType.MONGODB ? MONGO_SYSTEM_PROMPT : SQL_SYSTEM_PROMPT;

        String rawResponse = callGemini(systemPrompt, description);
        try {
            return parse(rawResponse, databaseType);
        } catch (Exception firstFailure) {
            String retryResponse = callGemini(systemPrompt, description + RETRY_INSTRUCTION);
            try {
                return parse(retryResponse, databaseType);
            } catch (Exception secondFailure) {
                throw new SchemaGenerationException(
                        "Couldn't understand that description, try being more specific.", secondFailure);
            }
        }
    }

    private LlmSchemaResult parse(String rawResponse, DatabaseType databaseType) throws Exception {
        String cleaned = stripCodeFences(rawResponse);
        if (databaseType == DatabaseType.MONGODB) {
            MongoSchemaDto dto = objectMapper.readValue(cleaned, MongoSchemaDto.class);
            if (dto.getCollections() == null || dto.getCollections().isEmpty()) {
                throw new IllegalStateException("No collections returned");
            }
            return new LlmSchemaResult(cleaned, SchemaGenerationResult.ofMongo(dto));
        } else {
            SqlSchemaDto dto = objectMapper.readValue(cleaned, SqlSchemaDto.class);
            if (dto.getTables() == null || dto.getTables().isEmpty()) {
                throw new IllegalStateException("No tables returned");
            }
            return new LlmSchemaResult(cleaned, SchemaGenerationResult.ofSql(dto));
        }
    }

    private String stripCodeFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    private String callGemini(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new SchemaGenerationException(
                    "GEMINI_API_KEY is not configured on the server.");
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userMessage))
                )),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "maxOutputTokens", 4096
                )
        );

        try {
            JsonNode response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode candidates = response == null ? null : response.path("candidates");
            if (candidates == null || !candidates.isArray() || candidates.isEmpty()) {
                throw new SchemaGenerationException("Empty response from LLM.");
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            StringBuilder text = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    text.append(part.get("text").asText());
                }
            }
            return text.toString();
        } catch (SchemaGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaGenerationException("Failed to call the LLM: " + e.getMessage(), e);
        }
    }
}