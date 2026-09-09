package com.schemagenie.service;

import com.schemagenie.dto.*;
import com.schemagenie.exception.ResourceNotFoundException;
import com.schemagenie.exception.SchemaGenerationException;
import com.schemagenie.exception.UnauthorizedException;
import com.schemagenie.generator.CodeGeneratorFactory;
import com.schemagenie.model.GeneratedSchema;
import com.schemagenie.repository.GeneratedSchemaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SchemaService {

    private static final int MIN_DESCRIPTION_WORDS = 10;

    private final LlmService llmService;
    private final SchemaValidationService validationService;
    private final CodeGeneratorFactory generatorFactory;
    private final ExplanationService explanationService;
    private final GeneratedSchemaRepository repository;

    public SchemaService(LlmService llmService,
                         SchemaValidationService validationService,
                         CodeGeneratorFactory generatorFactory,
                         ExplanationService explanationService,
                         GeneratedSchemaRepository repository) {
        this.llmService = llmService;
        this.validationService = validationService;
        this.generatorFactory = generatorFactory;
        this.explanationService = explanationService;
        this.repository = repository;
    }

    public GeneratedSchema generate(String description, DatabaseType databaseType,
                                    ProgrammingLanguage language, String requestingUserId) {
        validateDescription(description);

        LlmService.LlmSchemaResult llmResult = llmService.generateSchema(description, databaseType);
        validationService.validate(llmResult.schema());
        CodeGeneratorFactory.GeneratedOutputs outputs = generatorFactory.generate(llmResult.schema(), language);
        String explanation = explanationService.explain(llmResult.schema());

        GeneratedSchema session = new GeneratedSchema();
        session.setSessionId(UUID.randomUUID().toString());
        session.setDescription(description);
        session.setDatabaseType(databaseType);
        session.setLanguage(language);
        session.setRawJson(llmResult.rawJson());
        session.setModelClasses(outputs.modelClasses());
        session.setMigrationScript(outputs.migrationScript());
        session.setErDiagram(outputs.erDiagram());
        session.setExplanation(explanation);
        session.setUserId(requestingUserId);

        return repository.save(session);
    }

    public GeneratedSchema refine(String sessionId, String refinementNote, String requestingUserId) {
        GeneratedSchema existing = getById(sessionId);
        assertCanAccess(existing, requestingUserId);

        String combinedDescription = existing.getDescription() + "\n\nRefinement: " + refinementNote;
        validateDescription(combinedDescription);

        LlmService.LlmSchemaResult llmResult = llmService.generateSchema(combinedDescription, existing.getDatabaseType());
        validationService.validate(llmResult.schema());
        CodeGeneratorFactory.GeneratedOutputs outputs = generatorFactory.generate(llmResult.schema(), existing.getLanguage());
        String explanation = explanationService.explain(llmResult.schema());

        existing.setDescription(combinedDescription);
        existing.setRawJson(llmResult.rawJson());
        existing.setModelClasses(outputs.modelClasses());
        existing.setMigrationScript(outputs.migrationScript());
        existing.setErDiagram(outputs.erDiagram());
        existing.setExplanation(explanation);
        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }

    public GeneratedSchema getById(String sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found."));
    }

    public GeneratedSchema getForViewing(String sessionId, String requestingUserId) {
        GeneratedSchema session = getById(sessionId);
        if (session.getUserId() != null && !session.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You don't have access to this session.");
        }
        return session;
    }

    public List<GeneratedSchema> getHistory(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public GeneratedSchema saveToHistory(String sessionId, String userId) {
        GeneratedSchema session = getById(sessionId);
        if (session.getUserId() != null && !session.getUserId().equals(userId)) {
            throw new UnauthorizedException("This session already belongs to another account.");
        }
        session.setUserId(userId);
        return repository.save(session);
    }

    private void assertCanAccess(GeneratedSchema session, String requestingUserId) {
        if (session.getUserId() != null && !session.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You don't have access to this session.");
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            throw new SchemaGenerationException("Description is required.");
        }
        long wordCount = java.util.Arrays.stream(description.trim().split("\\s+"))
                .filter(w -> !w.isBlank()).count();
        if (wordCount < MIN_DESCRIPTION_WORDS) {
            throw new SchemaGenerationException(
                    "Please describe your app in a bit more detail (at least " + MIN_DESCRIPTION_WORDS + " words) so we can design a good schema.");
        }
    }
}