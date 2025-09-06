package com.elearning.elearning_backend.migration.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elearning.elearning_backend.migration.config.MigrationConfig;
import com.elearning.elearning_backend.migration.model.MigrationRecord;
import com.elearning.elearning_backend.migration.model.MigrationRecord.MigrationStatus;
import com.elearning.elearning_backend.migration.model.MigrationRecord.MigrationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationService {
    
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
     * Execute pending migrations
     */
    @Transactional
    public List<MigrationRecord> executePendingMigrations() {
        log.info("Starting migration execution for environment: {}", migrationConfig.getEnvironment());
        
        List<MigrationRecord> results = new ArrayList<>();
        
        try {
            // Get pending migrations
            List<String> migrationFiles = getPendingMigrationFiles();
            
            for (String migrationFile : migrationFiles) {
                MigrationRecord record = executeSingleMigration(migrationFile);
                results.add(record);
                
                if (record.getStatus() == MigrationStatus.FAILED) {
                    log.error("Migration failed: {}, stopping execution", migrationFile);
                    break;
                }
            }
            
        } catch (Exception e) {
            log.error("Error during migration execution", e);
            monitoringService.recordMigrationError(e);
        }
        
        return results;
    }
    
    /**
     * Execute a single migration
     */
    @Transactional
    public MigrationRecord executeSingleMigration(String migrationFile) {
        log.info("Executing migration: {}", migrationFile);
        
        MigrationRecord record = MigrationRecord.builder()
            .migrationId(generateMigrationId(migrationFile))
            .fileName(migrationFile)
            .environment(migrationConfig.getEnvironment())
            .executedBy(getCurrentUser())
            .status(MigrationStatus.PENDING)
            .build();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validation phase
            if (migrationConfig.isValidateBeforeMigration()) {
                validationService.validateMigration(migrationFile);
            }
            
            // Load and parse migration
            String migrationContent = loadMigrationContent(migrationFile);
            record.setChecksum(calculateChecksum(migrationContent));
            record.setType(determineMigrationType(migrationContent));
            record.setDescription(extractDescription(migrationContent));
            record.setVersion(extractVersion(migrationFile));
            
            // Create backup if required
            MigrationConfig.EnvironmentConfig envConfig = migrationConfig.getCurrentEnvironmentConfig();
            if (envConfig.isBackupBeforeMigration()) {
                String backupLocation = createBackup();
                record.setBackupLocation(backupLocation);
            }
            
            // Execute migration
            record.setStatus(MigrationStatus.RUNNING);
            auditService.recordMigrationStart(record);
            
            executeMigrationSQL(migrationContent);
            
            // Success
            record.setStatus(MigrationStatus.SUCCESS);
            record.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            
            auditService.recordMigrationSuccess(record);
            monitoringService.recordSuccessfulMigration(record);
            
            log.info("Migration completed successfully: {} in {}ms", 
                migrationFile, record.getExecutionTimeMs());
            
        } catch (Exception e) {
            // Failure
            record.setStatus(MigrationStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            record.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            
            auditService.recordMigrationFailure(record, e);
            monitoringService.recordFailedMigration(record, e);
            
            log.error("Migration failed: {}", migrationFile, e);
        }
        
        return record;
    }
    
    /**
     * Get list of pending migration files
     */
    private List<String> getPendingMigrationFiles() throws IOException {
        // This is a simplified implementation
        // In a real scenario, you'd scan the migration directory and compare with executed migrations
        List<String> allMigrations = scanMigrationDirectory();
        Set<String> executedMigrations = getExecutedMigrations();
        
        return allMigrations.stream()
            .filter(migration -> !executedMigrations.contains(migration))
            .sorted()
            .toList();
    }
    
    private List<String> scanMigrationDirectory() {
        // Simplified implementation - you'd implement directory scanning here
        return Arrays.asList(
            "V001__Create_migration_tables.sql",
            "V002__Add_user_indexes.sql"
        );
    }
    
    private Set<String> getExecutedMigrations() {
        try {
            return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT file_name FROM migration_records WHERE status = 'SUCCESS'", 
                String.class));
        } catch (Exception e) {
            log.warn("Could not query executed migrations, assuming empty: {}", e.getMessage());
            return new HashSet<>();
        }
    }
    
    private String loadMigrationContent(String fileName) throws IOException {
        Resource resource = resourceLoader.getResource(migrationConfig.getMigrationPath() + "/" + fileName);
        return Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
    }
    
    private void executeMigrationSQL(String sql) {
        // Split SQL by semicolon and execute each statement
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }
    
    private String generateMigrationId(String fileName) {
        return fileName + "_" + System.currentTimeMillis();
    }
    
    private String calculateChecksum(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private MigrationType determineMigrationType(String content) {
        String upperContent = content.toUpperCase();
        if (upperContent.contains("CREATE TABLE") || upperContent.contains("ALTER TABLE")) {
            return MigrationType.DDL;
        } else if (upperContent.contains("INSERT") || upperContent.contains("UPDATE") || upperContent.contains("DELETE")) {
            return MigrationType.DML;
        } else if (upperContent.contains("CREATE INDEX")) {
            return MigrationType.INDEX;
        }
        return MigrationType.DDL;
    }
    
    private String extractDescription(String content) {
        // Extract description from comment at the top of the file
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("-- Description:")) {
                return line.replace("-- Description:", "").trim();
            }
        }
        return "No description provided";
    }
    
    private String extractVersion(String fileName) {
        // Extract version from filename like V001__description.sql
        if (fileName.startsWith("V") && fileName.contains("__")) {
            return fileName.substring(1, fileName.indexOf("__"));
        }
        return "unknown";
    }
    
    private String createBackup() {
        // Implement database backup logic here
        String backupLocation = "backup_" + System.currentTimeMillis() + ".sql";
        log.info("Creating backup at: {}", backupLocation);
        return backupLocation;
    }
    
    private String getCurrentUser() {
        // Get current user from security context or system user
        return "system";
    }
} 