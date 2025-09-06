package com.elearning.elearning_backend.migration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationService {
    
    private static final Pattern DANGEROUS_OPERATIONS = Pattern.compile(
        "(?i)(DROP\\s+DATABASE|TRUNCATE\\s+TABLE|DELETE\\s+FROM\\s+\\w+\\s*;|DROP\\s+TABLE)"
    );
    
    private static final Pattern SQL_INJECTION_PATTERNS = Pattern.compile(
        "(?i)(UNION\\s+SELECT|OR\\s+1=1|';\\s*--)"
    );
    
    /**
     * Validate migration file before execution
     */
    public void validateMigration(String migrationFile) throws ValidationException {
        log.info("Validating migration: {}", migrationFile);
        
        List<String> validationErrors = new ArrayList<>();
        
        try {
            // Load migration content for validation
            String content = loadMigrationContent(migrationFile);
            
            // Run validation checks
            validateSyntax(content, validationErrors);
            validateSafety(content, validationErrors);
            validateNamingConventions(migrationFile, validationErrors);
            validateFileStructure(content, validationErrors);
            
            if (!validationErrors.isEmpty()) {
                throw new ValidationException("Migration validation failed: " + 
                    String.join(", ", validationErrors));
            }
            
            log.info("Migration validation passed: {}", migrationFile);
            
        } catch (Exception e) {
            log.error("Validation error for migration: {}", migrationFile, e);
            throw new ValidationException("Validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate SQL syntax
     */
    private void validateSyntax(String content, List<String> errors) {
        // Basic syntax validation
        if (content.trim().isEmpty()) {
            errors.add("Migration file is empty");
            return;
        }
        
        // Check for unmatched parentheses
        long openParens = content.chars().filter(ch -> ch == '(').count();
        long closeParens = content.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            errors.add("Unmatched parentheses in SQL");
        }
        
        // Check for proper statement termination
        String[] statements = content.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--") && !trimmed.endsWith(";") && !statement.equals(statements[statements.length - 1])) {
                // Last statement might not end with semicolon
                log.warn("Statement might be missing semicolon: {}", trimmed.substring(0, Math.min(50, trimmed.length())));
            }
        }
    }
    
    /**
     * Validate migration safety
     */
    private void validateSafety(String content, List<String> errors) {
        // Check for dangerous operations
        if (DANGEROUS_OPERATIONS.matcher(content).find()) {
            errors.add("Migration contains potentially dangerous operations (DROP DATABASE, TRUNCATE, etc.)");
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERNS.matcher(content).find()) {
            errors.add("Migration contains suspicious SQL patterns");
        }
        
        // Check for missing WHERE clauses in UPDATE/DELETE
        String upperContent = content.toUpperCase();
        if (upperContent.contains("UPDATE ") && !upperContent.contains("WHERE")) {
            log.warn("UPDATE statement without WHERE clause detected - please verify this is intentional");
        }
        
        if (upperContent.contains("DELETE FROM") && !upperContent.contains("WHERE")) {
            log.warn("DELETE statement without WHERE clause detected - please verify this is intentional");
        }
    }
    
    /**
     * Validate naming conventions
     */
    private void validateNamingConventions(String fileName, List<String> errors) {
        // Check file naming convention: V{version}__{description}.sql
        if (!fileName.matches("V\\d+__[a-zA-Z0-9_]+\\.sql")) {
            errors.add("Migration file name doesn't follow naming convention: V{version}__{description}.sql");
        }
    }
    
    /**
     * Validate file structure
     */
    private void validateFileStructure(String content, List<String> errors) {
        // Check for required metadata
        if (!content.contains("-- Description:")) {
            log.warn("Migration file missing description comment");
        }
        
        if (!content.contains("-- Author:")) {
            log.warn("Migration file missing author comment");
        }
        
        // Check for rollback script reference
        if (!content.contains("-- Rollback:")) {
            log.warn("Migration file missing rollback script reference");
        }
    }
    
    /**
     * Validate rollback script
     */
    public void validateRollbackScript(String rollbackScript) throws ValidationException {
        if (rollbackScript == null || rollbackScript.trim().isEmpty()) {
            throw new ValidationException("Rollback script is empty or missing");
        }
        
        // Basic validation for rollback script
        List<String> errors = new ArrayList<>();
        validateSyntax(rollbackScript, errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Rollback script validation failed: " + 
                String.join(", ", errors));
        }
    }
    
    private String loadMigrationContent(String fileName) {
        // This would load the actual file content
        // For now, return a placeholder
        return "-- Sample migration content";
    }
    
    /**
     * Custom validation exception
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 