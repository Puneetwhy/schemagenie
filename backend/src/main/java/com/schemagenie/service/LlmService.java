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

/**
 * Calls the Groq API (OpenAI-compatible chat completions endpoint) to
 * generate a database schema from a plain-English description. Supports
 * MongoDB (document schema) and a family of relational databases
 * (PostgreSQL, MySQL, SQLite) that all share the same JSON contract -- the
 * actual dialect-specific SQL is produced later by LiquibaseGeneratorService,
 * which already abstracts dialect differences, so the LLM only needs to
 * know it's designing a normalized relational schema in general.
 */
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

            CRITICAL: every relationship "target" value must be EXACTLY IDENTICAL (same spelling,
            same casing, same singular/plural form) to the "name" of one of the collections you define
            in this same JSON response. Double-check every target against your own collection names
            before responding. Do not invent fields the user did not imply. Keep naming consistent
            across the schema. For every field and every relationship, write a short, clear "reasoning"
            explaining WHY that design choice was made -- this will be shown to the user as an
            explanation.
            """;

    private static final String SQL_SYSTEM_PROMPT_TEMPLATE = """
            You are a relational database architect. Given a plain-English description of an
            application, design a normalized (3NF) schema intended for %s.

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
            - Use the field types listed above regardless of target database; %s-specific column types
              are handled separately by the migration generator.

            CRITICAL: every relationship "target" value must be EXACTLY IDENTICAL (same spelling,
            same casing, same singular/plural form) to the "name" of one of the tables you define in
            this same JSON response. Double-check every target against your own table names before
            responding. Do not invent fields the user did not imply. Keep naming consistent across the
            schema. For every field and every relationship, write a short, clear "reasoning" explaining
            WHY that design choice was made -- this will be shown to the user as an explanation.
            """;

    private static final String RETRY_INSTRUCTION_INVALID_JSON =
            "\n\nYour previous response was not valid JSON. Return ONLY the JSON object, nothing else.";

    private static final String RETRY_INSTRUCTION_BAD_RELATIONSHIP =
            "\n\nYour previous response had a relationship whose \"target\" did not exactly match "
                    + "the name of any collection/table you defined (check spelling, casing, and "
                    + "singular/plural form). Fix every mismatched target so it exactly equals an "
                    + "existing collection/table name, and return ONLY the corrected JSON object, "
                    + "nothing else.";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaValidationService validationService;
    private final String apiKey;
    private final String model;

    public LlmService(@Value("${groq.api-key}") String apiKey,
                      @Value("${groq.model}") String model,
                      @Value("${groq.base-url}") String baseUrl,
                      SchemaValidationService validationService) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.validationService = validationService;
    }

    public record LlmSchemaResult(String rawJson, SchemaGenerationResult schema) {}

    public LlmSchemaResult generateSchema(String description, DatabaseType databaseType) {
        String systemPrompt = buildSystemPrompt(databaseType);

        String rawResponse = callGroq(systemPrompt, description);
        try {
            return parseAndValidate(rawResponse, databaseType);
        } catch (Exception firstFailure) {
            String retryInstruction = firstFailure instanceof com.schemagenie.exception.SchemaValidationException
                    ? RETRY_INSTRUCTION_BAD_RELATIONSHIP
                    : RETRY_INSTRUCTION_INVALID_JSON;
            String retryResponse = callGroq(systemPrompt, description + retryInstruction);
            try {
                return parseAndValidate(retryResponse, databaseType);
            } catch (Exception secondFailure) {
                throw new SchemaGenerationException(
                        "Couldn't understand that description, try being more specific.", secondFailure);
            }
        }
    }

    private LlmSchemaResult parseAndValidate(String rawResponse, DatabaseType databaseType) throws Exception {
        LlmSchemaResult result = parse(rawResponse, databaseType);
        validationService.validate(result.schema());
        return result;
    }

    private String buildSystemPrompt(DatabaseType databaseType) {
        if (databaseType == DatabaseType.MONGODB) {
            return MONGO_SYSTEM_PROMPT;
        }
        String dialectName = switch (databaseType) {
            case POSTGRESQL -> "PostgreSQL";
            case MYSQL -> "MySQL";
            case SQLITE -> "SQLite";
            default -> "a relational database";
        };
        return SQL_SYSTEM_PROMPT_TEMPLATE.formatted(dialectName, dialectName);
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

    /**
     * Calls Groq's OpenAI-compatible chat completions endpoint:
     * POST {base-url}/chat/completions
     * Auth via "Authorization: Bearer {apiKey}" header (not a query param,
     * unlike the old Gemini "?key=" style).
     */
    private String callGroq(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new SchemaGenerationException("GROQ_API_KEY is not configured on the server.");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "response_format", Map.of("type", "json_object"),
                "max_tokens", 4096
        );

        try {
            JsonNode response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode choices = response == null ? null : response.path("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new SchemaGenerationException("Empty response from LLM.");
            }

            JsonNode messageContent = choices.get(0).path("message").path("content");
            if (messageContent.isMissingNode() || messageContent.asText().isBlank()) {
                throw new SchemaGenerationException("Empty response from LLM.");
            }
            return messageContent.asText();
        } catch (SchemaGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaGenerationException("Failed to call the LLM: " + e.getMessage(), e);
        }
    }
}
