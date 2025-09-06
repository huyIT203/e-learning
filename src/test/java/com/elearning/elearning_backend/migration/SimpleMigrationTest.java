package com.elearning.elearning_backend.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.elearning.elearning_backend.migration.config.MigrationConfig;
import com.elearning.elearning_backend.migration.service.MonitoringService;
import com.elearning.elearning_backend.migration.service.ValidationService;
import com.elearning.elearning_backend.migration.model.MigrationRecord;

/**
 * Simple unit tests for Migration System without Docker dependencies
 */
class SimpleMigrationTest {

    private MigrationConfig migrationConfig;
    private MonitoringService monitoringService;
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        migrationConfig = new MigrationConfig();
        migrationConfig.setEnvironment("test");
        migrationConfig.setEnabled(true);
        migrationConfig.setValidateBeforeMigration(true);
        
        monitoringService = new MonitoringService();
        validationService = new ValidationService();
    }

    @Test
    void testMigrationConfigInitialization() {
        assertNotNull(migrationConfig);
        assertEquals("test", migrationConfig.getEnvironment());
        assertTrue(migrationConfig.isEnabled());
        assertTrue(migrationConfig.isValidateBeforeMigration());
    }

    @Test
    void testMonitoringServiceInitialization() {
        assertNotNull(monitoringService);
        
        MonitoringService.MigrationStats stats = monitoringService.getMigrationStats();
        assertNotNull(stats);
        assertEquals(0, stats.getTotalMigrations());
        assertEquals(0, stats.getSuccessfulMigrations());
        assertEquals(0, stats.getFailedMigrations());
        assertEquals(100.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testMonitoringServiceRecordMigration() {
        MigrationRecord testRecord = MigrationRecord.builder()
            .migrationId("test_migration_001")
            .version("001")
            .fileName("V001__Test.sql")
            .type(MigrationRecord.MigrationType.DDL)
            .status(MigrationRecord.MigrationStatus.SUCCESS)
            .environment("test")
            .executionTimeMs(1500L)
            .build();

        monitoringService.recordSuccessfulMigration(testRecord);

        MonitoringService.MigrationStats stats = monitoringService.getMigrationStats();
        assertEquals(1, stats.getTotalMigrations());
        assertEquals(1, stats.getSuccessfulMigrations());
        assertEquals(0, stats.getFailedMigrations());
        assertEquals(100.0, stats.getSuccessRate(), 0.01);
        assertEquals(1500.0, stats.getAverageExecutionTime(), 0.01);
    }

    @Test
    void testMonitoringHealthStatus() {
        MonitoringService.MigrationHealthStatus health = monitoringService.getHealthStatus();
        
        assertNotNull(health);
        assertEquals(MonitoringService.MigrationHealthStatus.Status.HEALTHY, health.getStatus());
        assertEquals(100.0, health.getSuccessRate(), 0.01);
        assertEquals(0, health.getRecentFailures());
    }

    @Test
    void testValidationServiceInitialization() {
        assertNotNull(validationService);
    }

    @Test
    void testMigrationFileNamingValidation() {
        // Test valid naming conventions
        String[] validNames = {
            "V001__Create_users_table.sql",
            "V002__Add_user_indexes.sql",
            "V100__Update_schema.sql"
        };

        for (String validName : validNames) {
            assertTrue(validName.matches("V\\d+__[a-zA-Z0-9_]+\\.sql"), 
                "Valid name should match pattern: " + validName);
        }

        // Test invalid naming conventions
        String[] invalidNames = {
            "invalid.sql",
            "V001_missing_double_underscore.sql",
            "001__missing_v_prefix.sql"
        };

        for (String invalidName : invalidNames) {
            assertFalse(invalidName.matches("V\\d+__[a-zA-Z0-9_]+\\.sql"), 
                "Invalid name should not match pattern: " + invalidName);
        }
    }

    @Test
    void testMigrationRecordBuilder() {
        MigrationRecord record = MigrationRecord.builder()
            .migrationId("test_001")
            .version("001")
            .description("Test migration")
            .fileName("V001__Test.sql")
            .checksum("abc123")
            .type(MigrationRecord.MigrationType.DDL)
            .status(MigrationRecord.MigrationStatus.SUCCESS)
            .environment("test")
            .executedBy("test_user")
            .executionTimeMs(2000L)
            .build();

        assertNotNull(record);
        assertEquals("test_001", record.getMigrationId());
        assertEquals("001", record.getVersion());
        assertEquals("Test migration", record.getDescription());
        assertEquals("V001__Test.sql", record.getFileName());
        assertEquals("abc123", record.getChecksum());
        assertEquals(MigrationRecord.MigrationType.DDL, record.getType());
        assertEquals(MigrationRecord.MigrationStatus.SUCCESS, record.getStatus());
        assertEquals("test", record.getEnvironment());
        assertEquals("test_user", record.getExecutedBy());
        assertEquals(2000L, record.getExecutionTimeMs());
    }

    @Test
    void testEnvironmentConfiguration() {
        MigrationConfig.EnvironmentConfig devConfig = migrationConfig.getDevelopment();
        assertNotNull(devConfig);
        assertFalse(devConfig.isAutoMigrate());
        assertTrue(devConfig.isRequireApproval());
        assertEquals(1, devConfig.getMaxConcurrentMigrations());
        assertTrue(devConfig.isBackupBeforeMigration());

        // Test current environment config
        migrationConfig.setEnvironment("development");
        MigrationConfig.EnvironmentConfig currentConfig = migrationConfig.getCurrentEnvironmentConfig();
        assertNotNull(currentConfig);
        assertEquals(devConfig, currentConfig);
    }

    @Test
    void testMetricsReset() {
        // Add some data
        MigrationRecord testRecord = MigrationRecord.builder()
            .migrationId("test_migration_reset")
            .type(MigrationRecord.MigrationType.DDL)
            .status(MigrationRecord.MigrationStatus.SUCCESS)
            .environment("test")
            .executionTimeMs(1000L)
            .build();

        monitoringService.recordSuccessfulMigration(testRecord);
        
        // Verify data exists
        MonitoringService.MigrationStats statsBefore = monitoringService.getMigrationStats();
        assertEquals(1, statsBefore.getTotalMigrations());

        // Reset metrics
        monitoringService.resetMetrics();

        // Verify data is reset
        MonitoringService.MigrationStats statsAfter = monitoringService.getMigrationStats();
        assertEquals(0, statsAfter.getTotalMigrations());
        assertEquals(0, statsAfter.getSuccessfulMigrations());
        assertEquals(0, statsAfter.getFailedMigrations());
        assertEquals(100.0, statsAfter.getSuccessRate(), 0.01);
    }
} 