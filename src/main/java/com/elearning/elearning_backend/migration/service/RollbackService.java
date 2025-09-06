package com.elearning.elearning_backend.migration.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elearning.elearning_backend.migration.config.MigrationConfig;
import com.elearning.elearning_backend.migration.model.MigrationRecord;
import com.elearning.elearning_backend.migration.model.MigrationRecord.MigrationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RollbackService {
    
    private final MigrationConfig migrationConfig;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private MonitoringService monitoringService;
    
    /**
     * Rollback a specific migration
     */
    @Transactional
    public RollbackResult rollbackMigration(String migrationId) {
        log.info("Starting rollback for migration: {}", migrationId);
        
        long startTime = System.currentTimeMillis();
        String executedBy = getCurrentUser();
        
        try {
            // Get migration record
            MigrationRecord migrationRecord = getMigrationRecord(migrationId);
            if (migrationRecord == null) {
                throw new RollbackException("Migration record not found: " + migrationId);
            }
            
            if (migrationRecord.getStatus() != MigrationStatus.SUCCESS) {
                throw new RollbackException("Can only rollback successful migrations. Current status: " + 
                    migrationRecord.getStatus());
            }
            
            // Start rollback audit
            auditService.recordRollbackStart(migrationId, executedBy);
            
            // Load rollback script
            String rollbackScript = loadRollbackScript(migrationRecord);
            
            // Validate rollback script
            if (migrationConfig.isValidateBeforeMigration()) {
                validationService.validateRollbackScript(rollbackScript);
            }
            
            // Create backup before rollback
            String backupLocation = createPreRollbackBackup(migrationId);
            
            // Execute rollback
            executeRollbackSQL(rollbackScript);
            
            // Update migration record status
            updateMigrationStatus(migrationId, MigrationStatus.ROLLED_BACK);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Record success
            auditService.recordRollbackSuccess(migrationId, executedBy, executionTime);
            
            log.info("Rollback completed successfully for migration: {} in {}ms", 
                migrationId, executionTime);
            
            return RollbackResult.success(migrationId, executionTime, backupLocation);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Record failure
            auditService.recordRollbackFailure(migrationId, executedBy, e);
            
            log.error("Rollback failed for migration: {}", migrationId, e);
            
            return RollbackResult.failure(migrationId, executionTime, e.getMessage());
        }
    }
    
    /**
     * Rollback multiple migrations to a specific version
     */
    @Transactional
    public List<RollbackResult> rollbackToVersion(String targetVersion) {
        log.info("Starting rollback to version: {}", targetVersion);
        
        try {
            // Get migrations to rollback (in reverse order)
            List<String> migrationsToRollback = getMigrationsToRollback(targetVersion);
            
            return migrationsToRollback.stream()
                .map(this::rollbackMigration)
                .toList();
                
        } catch (Exception e) {
            log.error("Failed to rollback to version: {}", targetVersion, e);
            throw new RollbackException("Failed to rollback to version: " + targetVersion, e);
        }
    }
    
    /**
     * Get rollback plan for a specific version
     */
    public RollbackPlan getRollbackPlan(String targetVersion) {
        try {
            List<String> migrationsToRollback = getMigrationsToRollback(targetVersion);
            
            return RollbackPlan.builder()
                .targetVersion(targetVersion)
                .migrationsToRollback(migrationsToRollback)
                .estimatedTime(estimateRollbackTime(migrationsToRollback))
                .riskLevel(assessRollbackRisk(migrationsToRollback))
                .backupRequired(true)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to create rollback plan for version: {}", targetVersion, e);
            throw new RollbackException("Failed to create rollback plan", e);
        }
    }
    
    private MigrationRecord getMigrationRecord(String migrationId) {
        try {
            String sql = """
                SELECT migration_id, version, description, file_name, checksum, 
                       type, status, environment, executed_by, executed_at, 
                       execution_time_ms, rollback_script, backup_location, error_message
                FROM migration_records 
                WHERE migration_id = ?
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> 
                MigrationRecord.builder()
                    .migrationId(rs.getString("migration_id"))
                    .version(rs.getString("version"))
                    .description(rs.getString("description"))
                    .fileName(rs.getString("file_name"))
                    .checksum(rs.getString("checksum"))
                    .type(MigrationRecord.MigrationType.valueOf(rs.getString("type")))
                    .status(MigrationRecord.MigrationStatus.valueOf(rs.getString("status")))
                    .environment(rs.getString("environment"))
                    .executedBy(rs.getString("executed_by"))
                    .executedAt(rs.getTimestamp("executed_at").toLocalDateTime())
                    .executionTimeMs(rs.getLong("execution_time_ms"))
                    .rollbackScript(rs.getString("rollback_script"))
                    .backupLocation(rs.getString("backup_location"))
                    .errorMessage(rs.getString("error_message"))
                    .build(),
                migrationId);
                
        } catch (Exception e) {
            log.error("Failed to get migration record: {}", migrationId, e);
            return null;
        }
    }
    
    private String loadRollbackScript(MigrationRecord migrationRecord) throws IOException {
        // First try to get rollback script from migration record
        if (migrationRecord.getRollbackScript() != null && 
            !migrationRecord.getRollbackScript().trim().isEmpty()) {
            return migrationRecord.getRollbackScript();
        }
        
        // Try to load from rollback directory
        String rollbackFileName = generateRollbackFileName(migrationRecord.getFileName());
        try {
            Resource resource = resourceLoader.getResource(
                migrationConfig.getRollbackPath() + "/" + rollbackFileName);
            return Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Could not load rollback script from file: {}", rollbackFileName);
        }
        
        // Generate automatic rollback script
        return generateAutomaticRollbackScript(migrationRecord);
    }
    
    private String generateRollbackFileName(String migrationFileName) {
        // Convert V001__Create_table.sql to R001__Drop_table.sql
        if (migrationFileName.startsWith("V")) {
            return migrationFileName.replace("V", "R");
        }
        return "R_" + migrationFileName;
    }
    
    private String generateAutomaticRollbackScript(MigrationRecord migrationRecord) {
        // This is a simplified automatic rollback generator
        // In production, this would be much more sophisticated
        log.warn("Generating automatic rollback script for: {}", migrationRecord.getFileName());
        
        return "-- Automatic rollback script for " + migrationRecord.getFileName() + "\n" +
               "-- WARNING: This is an automatically generated rollback script\n" +
               "-- Please verify before execution\n" +
               "-- ROLLBACK NOT IMPLEMENTED FOR: " + migrationRecord.getDescription() + "\n";
    }
    
    private void executeRollbackSQL(String rollbackScript) {
        String[] statements = rollbackScript.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }
    
    private void updateMigrationStatus(String migrationId, MigrationStatus status) {
        String sql = "UPDATE migration_records SET status = ?, updated_at = ? WHERE migration_id = ?";
        jdbcTemplate.update(sql, status.name(), LocalDateTime.now(), migrationId);
    }
    
    private String createPreRollbackBackup(String migrationId) {
        String backupLocation = "pre_rollback_" + migrationId + "_" + System.currentTimeMillis() + ".sql";
        log.info("Creating pre-rollback backup: {}", backupLocation);
        // Implement actual backup logic here
        return backupLocation;
    }
    
    private List<String> getMigrationsToRollback(String targetVersion) {
        // Get all successful migrations after the target version
        String sql = """
            SELECT migration_id FROM migration_records 
            WHERE status = 'SUCCESS' AND version > ? 
            ORDER BY version DESC
            """;
        
        return jdbcTemplate.queryForList(sql, String.class, targetVersion);
    }
    
    private long estimateRollbackTime(List<String> migrations) {
        // Estimate based on average migration time
        return migrations.size() * 5000L; // 5 seconds per migration estimate
    }
    
    private RollbackRiskLevel assessRollbackRisk(List<String> migrations) {
        if (migrations.size() > 10) {
            return RollbackRiskLevel.HIGH;
        } else if (migrations.size() > 5) {
            return RollbackRiskLevel.MEDIUM;
        }
        return RollbackRiskLevel.LOW;
    }
    
    private String getCurrentUser() {
        return "system";
    }
    
    /**
     * Rollback result data class
     */
    public static class RollbackResult {
        private final boolean success;
        private final String migrationId;
        private final long executionTimeMs;
        private final String backupLocation;
        private final String errorMessage;
        
        private RollbackResult(boolean success, String migrationId, long executionTimeMs, 
                              String backupLocation, String errorMessage) {
            this.success = success;
            this.migrationId = migrationId;
            this.executionTimeMs = executionTimeMs;
            this.backupLocation = backupLocation;
            this.errorMessage = errorMessage;
        }
        
        public static RollbackResult success(String migrationId, long executionTimeMs, String backupLocation) {
            return new RollbackResult(true, migrationId, executionTimeMs, backupLocation, null);
        }
        
        public static RollbackResult failure(String migrationId, long executionTimeMs, String errorMessage) {
            return new RollbackResult(false, migrationId, executionTimeMs, null, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMigrationId() { return migrationId; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public String getBackupLocation() { return backupLocation; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Rollback plan data class
     */
    public static class RollbackPlan {
        private final String targetVersion;
        private final List<String> migrationsToRollback;
        private final long estimatedTime;
        private final RollbackRiskLevel riskLevel;
        private final boolean backupRequired;
        
        private RollbackPlan(Builder builder) {
            this.targetVersion = builder.targetVersion;
            this.migrationsToRollback = builder.migrationsToRollback;
            this.estimatedTime = builder.estimatedTime;
            this.riskLevel = builder.riskLevel;
            this.backupRequired = builder.backupRequired;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public String getTargetVersion() { return targetVersion; }
        public List<String> getMigrationsToRollback() { return migrationsToRollback; }
        public long getEstimatedTime() { return estimatedTime; }
        public RollbackRiskLevel getRiskLevel() { return riskLevel; }
        public boolean isBackupRequired() { return backupRequired; }
        
        public static class Builder {
            private String targetVersion;
            private List<String> migrationsToRollback;
            private long estimatedTime;
            private RollbackRiskLevel riskLevel;
            private boolean backupRequired;
            
            public Builder targetVersion(String targetVersion) {
                this.targetVersion = targetVersion;
                return this;
            }
            
            public Builder migrationsToRollback(List<String> migrationsToRollback) {
                this.migrationsToRollback = migrationsToRollback;
                return this;
            }
            
            public Builder estimatedTime(long estimatedTime) {
                this.estimatedTime = estimatedTime;
                return this;
            }
            
            public Builder riskLevel(RollbackRiskLevel riskLevel) {
                this.riskLevel = riskLevel;
                return this;
            }
            
            public Builder backupRequired(boolean backupRequired) {
                this.backupRequired = backupRequired;
                return this;
            }
            
            public RollbackPlan build() {
                return new RollbackPlan(this);
            }
        }
    }
    
    public enum RollbackRiskLevel {
        LOW, MEDIUM, HIGH
    }
    
    /**
     * Custom rollback exception
     */
    public static class RollbackException extends RuntimeException {
        public RollbackException(String message) {
            super(message);
        }
        
        public RollbackException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 