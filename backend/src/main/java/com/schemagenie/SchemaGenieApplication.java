package com.schemagenie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SchemaGenie: given a plain-English app description, generates a matching
 * MongoDB or PostgreSQL schema, model classes, a migration script, and an
 * ER diagram.
 *
 * This app's own data (users, sessions, history) is stored in MongoDB.
 * The database a user *picks in the UI* (MongoDB or PostgreSQL) only affects
 * the generated output schema -- it's unrelated to where this app's own data lives.
 */
@SpringBootApplication
public class SchemaGenieApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchemaGenieApplication.class, args);
    }
}
