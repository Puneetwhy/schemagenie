package com.schemagenie.controller;

import com.schemagenie.dto.GenerateSchemaRequest;
import com.schemagenie.exception.UnauthorizedException;
import com.schemagenie.model.GeneratedSchema;
import com.schemagenie.security.UserPrincipal;
import com.schemagenie.service.SchemaService;
import com.schemagenie.service.ZipService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schema")
public class SchemaController {

    private final SchemaService schemaService;
    private final ZipService zipService;

    public SchemaController(SchemaService schemaService, ZipService zipService) {
        this.schemaService = schemaService;
        this.zipService = zipService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@Valid @RequestBody GenerateSchemaRequest request, Authentication authentication) {
        String userId = currentUserIdOrNull(authentication);
        GeneratedSchema session = schemaService.generate(request.getDescription(), request.getDatabaseType(), userId);
        return ResponseEntity.ok(toResponseBody(session));
    }

    @PostMapping("/refine/{sessionId}")
    public ResponseEntity<?> refine(@PathVariable String sessionId,
                                     @RequestBody Map<String, String> body,
                                     Authentication authentication) {
        String userId = currentUserIdOrNull(authentication);
        String note = body.getOrDefault("refinement", body.getOrDefault("note", ""));
        GeneratedSchema session = schemaService.refine(sessionId, note, userId);
        return ResponseEntity.ok(toResponseBody(session));
    }

    @GetMapping("/download/{sessionId}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String sessionId) throws Exception {
        GeneratedSchema session = schemaService.getById(sessionId);
        byte[] zipBytes = zipService.buildZip(session);
        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("schemagenie-" + sessionId + ".zip").build().toString())
                .body(resource);
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(Authentication authentication) {
        String userId = requireUserId(authentication);
        List<GeneratedSchema> sessions = schemaService.getHistory(userId);
        return ResponseEntity.ok(sessions.stream().map(this::toHistoryItem).toList());
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId, Authentication authentication) {
        String userId = currentUserIdOrNull(authentication);
        GeneratedSchema session = schemaService.getForViewing(sessionId, userId);
        return ResponseEntity.ok(toResponseBody(session));
    }

    @PostMapping("/session/{sessionId}/save")
    public ResponseEntity<?> saveToHistory(@PathVariable String sessionId, Authentication authentication) {
        String userId = requireUserId(authentication);
        GeneratedSchema session = schemaService.saveToHistory(sessionId, userId);
        return ResponseEntity.ok(toResponseBody(session));
    }

    private String currentUserIdOrNull(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }

    private String requireUserId(Authentication authentication) {
        String userId = currentUserIdOrNull(authentication);
        if (userId == null) {
            throw new UnauthorizedException("You must be logged in to do that.");
        }
        return userId;
    }

    private Map<String, Object> toResponseBody(GeneratedSchema session) {
        return Map.ofEntries(
                Map.entry("sessionId", session.getSessionId()),
                Map.entry("description", session.getDescription()),
                Map.entry("databaseType", session.getDatabaseType()),
                Map.entry("modelClasses", session.getModelClasses()),
                Map.entry("migrationScript", session.getMigrationScript()),
                Map.entry("erDiagram", session.getErDiagram()),
                Map.entry("rawJson", session.getRawJson()),
                Map.entry("owned", session.getUserId() != null),
                Map.entry("createdAt", session.getCreatedAt().toString())
        );
    }

    private Map<String, Object> toHistoryItem(GeneratedSchema session) {
        String snippet = session.getDescription().length() > 120
                ? session.getDescription().substring(0, 120) + "..."
                : session.getDescription();
        return Map.of(
                "sessionId", session.getSessionId(),
                "descriptionSnippet", snippet,
                "databaseType", session.getDatabaseType(),
                "createdAt", session.getCreatedAt().toString()
        );
    }
}
