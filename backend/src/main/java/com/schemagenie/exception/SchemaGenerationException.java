package com.schemagenie.exception;

public class SchemaGenerationException extends RuntimeException {
    public SchemaGenerationException(String message) {
        super(message);
    }
    public SchemaGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
