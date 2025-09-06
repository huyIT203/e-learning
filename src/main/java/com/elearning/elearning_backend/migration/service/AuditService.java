package com.elearning.elearning_backend.migration.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.migration.model.MigrationRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuditService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Record migration start in audit trail
     */
    public void recordMigrationStart(MigrationRecord record) {
        log.info("Recording migration start: {}", record.getMigrationId());
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("migration_id", record.getMigrationId());
            auditData.put("action", "MIGRATION_START");
            auditData.put("environment", record.getEnvironment());
            auditData.put("executed_by", record.getExecutedBy());
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("file_name", record.getFileName());
            auditData.put("version", record.getVersion());
            
            insertAuditRecord(auditData);
            
        } catch (Exception e) {
            log.error("Failed to record migration start audit", e);
        }
    }
    
    /**
     * Record successful migration in audit trail
     */
    public void recordMigrationSuccess(MigrationRecord record) {
        log.info("Recording migration success: {}", record.getMigrationId());
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("migration_id", record.getMigrationId());
            auditData.put("action", "MIGRATION_SUCCESS");
            auditData.put("environment", record.getEnvironment());
            auditData.put("executed_by", record.getExecutedBy());
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("execution_time_ms", record.getExecutionTimeMs());
            auditData.put("checksum", record.getChecksum());
            
            insertAuditRecord(auditData);
            saveMigrationRecord(record);
            
        } catch (Exception e) {
            log.error("Failed to record migration success audit", e);
        }
    }
    
    /**
     * Record failed migration in audit trail
     */
    public void recordMigrationFailure(MigrationRecord record, Exception error) {
        log.error("Recording migration failure: {}", record.getMigrationId());
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("migration_id", record.getMigrationId());
            auditData.put("action", "MIGRATION_FAILURE");
            auditData.put("environment", record.getEnvironment());
            auditData.put("executed_by", record.getExecutedBy());
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("error_message", error.getMessage());
            auditData.put("execution_time_ms", record.getExecutionTimeMs());
            
            insertAuditRecord(auditData);
            saveMigrationRecord(record);
            
        } catch (Exception e) {
            log.error("Failed to record migration failure audit", e);
        }
    }
    
    /**
     * Record rollback start in audit trail
     */
    public void recordRollbackStart(String migrationId, String executedBy) {
        log.info("Recording rollback start: {}", migrationId);
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("migration_id", migrationId);
            auditData.put("action", "ROLLBACK_START");
            auditData.put("executed_by", executedBy);
            auditData.put("timestamp", LocalDateTime.now());
            
            insertAuditRecord(auditData);
            
        } catch (Exception e) {
            log.error("Failed to record rollback start audit", e);
        }
    }
    
    /**
     * Record successful rollback in audit trail
     */
    public void recordRollbackSuccess(String migrationId, String executedBy, long executionTimeMs) {
        log.info("Recording rollback success: {}", migrationId);
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("migration_id", migrationId);
            auditData.put("action", "ROLLBACK_SUCCESS");
            auditData.put("executed_by", executedBy);
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("execution_time_ms", executionTimeMs);
            
            insertAuditRecord(auditData);
            
        } catch (Exception e) {
            log.error("Failed to record rollback success audit", e);
        }
    }
    
    /**
     * Record failed rollback in audit trail
     */
    public void recordRollbackFailure(String migrationId, String executedBy, Exception error) {
        log.error("Recording rollback failure: {}", migrationId);
        
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("migration_id", migrationId);
            auditData.put("action", "ROLLBACK_FAILURE");
            auditData.put("executed_by", executedBy);
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("error_message", error.getMessage());
            
            insertAuditRecord(auditData);
            
        } catch (Exception e) {
            log.error("Failed to record rollback failure audit", e);
        }
    }
    
    /**
     * Insert audit record into database
     */
    private void insertAuditRecord(Map<String, Object> auditData) {
        try {
            String sql = """
                INSERT INTO migration_audit_log 
                (migration_id, action, environment, executed_by, timestamp, 
                 execution_time_ms, error_message, additional_data) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            jdbcTemplate.update(sql,
                auditData.get("migration_id"),
                auditData.get("action"),
                auditData.get("environment"),
                auditData.get("executed_by"),
                auditData.get("timestamp"),
                auditData.get("execution_time_ms"),
                auditData.get("error_message"),
                convertToJson(auditData)
            );
            
        } catch (Exception e) {
            // Create audit table if it doesn't exist
            createAuditTableIfNotExists();
            // Retry insert
            try {
                String sql = """
                    INSERT INTO migration_audit_log 
                    (migration_id, action, environment, executed_by, timestamp, 
                     execution_time_ms, error_message, additional_data) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                
                jdbcTemplate.update(sql,
                    auditData.get("migration_id"),
                    auditData.get("action"),
                    auditData.get("environment"),
                    auditData.get("executed_by"),
                    auditData.get("timestamp"),
                    auditData.get("execution_time_ms"),
                    auditData.get("error_message"),
                    convertToJson(auditData)
                );
            } catch (Exception retryError) {
                log.error("Failed to insert audit record after retry", retryError);
            }
        }
    }
    
    /**
     * Save migration record to database
     */
    private void saveMigrationRecord(MigrationRecord record) {
        try {
            String sql = """
                INSERT INTO migration_records 
                (migration_id, version, description, file_name, checksum, type, 
                 status, environment, executed_by, executed_at, updated_at, 
                 execution_time_ms, rollback_script, backup_location, error_message) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                status = VALUES(status),
                updated_at = VALUES(updated_at),
                execution_time_ms = VALUES(execution_time_ms),
                error_message = VALUES(error_message)
                """;
            
            jdbcTemplate.update(sql,
                record.getMigrationId(),
                record.getVersion(),
                record.getDescription(),
                record.getFileName(),
                record.getChecksum(),
                record.getType().name(),
                record.getStatus().name(),
                record.getEnvironment(),
                record.getExecutedBy(),
                record.getExecutedAt(),
                record.getUpdatedAt(),
                record.getExecutionTimeMs(),
                record.getRollbackScript(),
                record.getBackupLocation(),
                record.getErrorMessage()
            );
            
        } catch (Exception e) {
            log.error("Failed to save migration record", e);
        }
    }
    
    /**
     * Create audit table if it doesn't exist
     */
    private void createAuditTableIfNotExists() {
        try {
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS migration_audit_log (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    migration_id VARCHAR(255) NOT NULL,
                    action VARCHAR(50) NOT NULL,
                    environment VARCHAR(50),
                    executed_by VARCHAR(100),
                    timestamp DATETIME NOT NULL,
                    execution_time_ms BIGINT,
                    error_message TEXT,
                    additional_data JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_migration_id (migration_id),
                    INDEX idx_timestamp (timestamp),
                    INDEX idx_action (action)
                )
                """;
            
            jdbcTemplate.execute(createTableSql);
            log.info("Created migration_audit_log table");
            
        } catch (Exception e) {
            log.error("Failed to create audit table", e);
        }
    }
    
    /**
     * Convert map to JSON string
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            // Simple JSON conversion - in production use Jackson or similar
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
} 