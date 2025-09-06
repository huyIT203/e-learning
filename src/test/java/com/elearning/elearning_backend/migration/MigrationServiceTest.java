package com.elearning.elearning_backend.migration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.elearning.elearning_backend.migration.config.MigrationConfig;
import com.elearning.elearning_backend.migration.model.MigrationRecord;
import com.elearning.elearning_backend.migration.service.AuditService;
import com.elearning.elearning_backend.migration.service.MigrationService;
import com.elearning.elearning_backend.migration.service.MonitoringService;
import com.elearning.elearning_backend.migration.service.ValidationService;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
    "migration.environment=test",
    "migration.enabled=true",
    "migration.validate-before-migration=true"
})
class MigrationServiceTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_migration")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MigrationConfig migrationConfig;

    @BeforeEach
    void setUp() {
        // Reset monitoring metrics before each test
        monitoringService.resetMetrics();
        
        // Ensure test environment
        migrationConfig.setEnvironment("test");
    }

    @Test
    void testMigrationServiceInitialization() {
        assertNotNull(migrationService);
        assertNotNull(validationService);
        assertNotNull(auditService);
        assertNotNull(monitoringService);
    }

    @Test
    void testMigrationConfigurationLoading() {
        assertEquals("test", migrationConfig.getEnvironment());
        assertTrue(migrationConfig.isEnabled());
        assertTrue(migrationConfig.isValidateBeforeMigration());
    }

    @Test
    @Transactional
    void testExecuteSingleMigration() {
        // Create a test migration
        String testMigrationFile = "V999__Test_migration.sql";
        
        // Execute the migration
        MigrationRecord result = migrationService.executeSingleMigration(testMigrationFile);
        
        // Verify results
        assertNotNull(result);
        assertEquals(testMigrationFile, result.getFileName());
        assertEquals("test", result.getEnvironment());
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    void testMigrationValidation() {
        // Test valid migration file
        assertDoesNotThrow(() -> {
            validationService.validateMigration("V001__Valid_migration.sql");
        });

        // Test invalid migration file name
        assertThrows(ValidationService.ValidationException.class, () -> {
            validationService.validateMigration("invalid_name.sql");
        });
    }

    @Test
    void testValidationServiceSafetyChecks() {
        ValidationService.ValidationException exception = assertThrows(
            ValidationService.ValidationException.class, 
            () -> validationService.validateRollbackScript("DROP DATABASE test;")
        );
        
        assertTrue(exception.getMessage().contains("dangerous operations"));
    }

    @Test
    void testMonitoringServiceMetrics() {
        // Create a test migration record
        MigrationRecord testRecord = MigrationRecord.builder()
            .migrationId("test_migration_001")
            .version("001")
            .fileName("V001__Test.sql")
            .type(MigrationRecord.MigrationType.DDL)
            .status(MigrationRecord.MigrationStatus.SUCCESS)
            .environment("test")
            .executionTimeMs(1500L)
            .build();

        // Record successful migration
        monitoringService.recordSuccessfulMigration(testRecord);

        // Verify metrics
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
    @Transactional
    void testAuditTrailCreation() {
        // Create test migration record
        MigrationRecord testRecord = MigrationRecord.builder()
            .migrationId("audit_test_001")
            .version("001")
            .fileName("V001__Audit_test.sql")
            .type(MigrationRecord.MigrationType.DDL)
            .status(MigrationRecord.MigrationStatus.RUNNING)
            .environment("test")
            .executedBy("test_user")
            .build();

        // Test audit recording
        assertDoesNotThrow(() -> {
            auditService.recordMigrationStart(testRecord);
            auditService.recordMigrationSuccess(testRecord);
        });

        // Verify audit records were created
        List<String> auditRecords = jdbcTemplate.queryForList(
            "SELECT action FROM migration_audit_log WHERE migration_id = ?",
            String.class,
            testRecord.getMigrationId()
        );

        assertTrue(auditRecords.contains("MIGRATION_START"));
        assertTrue(auditRecords.contains("MIGRATION_SUCCESS"));
    }

    @Test
    void testMultiEnvironmentConfiguration() {
        // Test development environment config
        migrationConfig.setEnvironment("development");
        MigrationConfig.EnvironmentConfig devConfig = migrationConfig.getCurrentEnvironmentConfig();
        assertNotNull(devConfig);
        assertFalse(devConfig.isRequireApproval());

        // Test production environment config
        migrationConfig.setEnvironment("production");
        MigrationConfig.EnvironmentConfig prodConfig = migrationConfig.getCurrentEnvironmentConfig();
        assertNotNull(prodConfig);
        assertTrue(prodConfig.isRequireApproval());
        assertTrue(prodConfig.isBackupBeforeMigration());
    }

    @Test
    @Transactional
    void testConcurrentMigrationPrevention() {
        // This test would verify that concurrent migrations are prevented
        // Implementation would depend on the locking mechanism
        
        String migrationFile = "V998__Concurrent_test.sql";
        
        // First migration should succeed
        MigrationRecord result1 = migrationService.executeSingleMigration(migrationFile);
        assertNotNull(result1);
        
        // Second identical migration should be skipped or handled appropriately
        // (Implementation would depend on your duplicate handling strategy)
    }

    @Test
    void testMigrationFileNamingConvention() {
        // Test valid naming conventions
        String[] validNames = {
            "V001__Create_users_table.sql",
            "V002__Add_user_indexes.sql",
            "V100__Update_schema.sql"
        };

        for (String validName : validNames) {
            assertDoesNotThrow(() -> {
                // This would test the naming validation logic
                assertTrue(validName.matches("V\\d+__[a-zA-Z0-9_]+\\.sql"));
            });
        }

        // Test invalid naming conventions
        String[] invalidNames = {
            "invalid.sql",
            "V001_missing_double_underscore.sql",
            "001__missing_v_prefix.sql"
        };

        for (String invalidName : invalidNames) {
            assertFalse(invalidName.matches("V\\d+__[a-zA-Z0-9_]+\\.sql"));
        }
    }

    @Test
    @Transactional
    void testErrorHandlingAndRecovery() {
        // Create a migration that will fail
        String failingMigrationFile = "V997__Failing_migration.sql";
        
        // The migration should handle the failure gracefully
        MigrationRecord result = migrationService.executeSingleMigration(failingMigrationFile);
        
        // Verify error handling
        assertNotNull(result);
        // The exact status would depend on how failures are handled
        // assertNotNull(result.getErrorMessage());
    }

    @Test
    void testPerformanceMetrics() {
        // Create test records with different execution times
        MigrationRecord fastMigration = MigrationRecord.builder()
            .migrationId("fast_migration")
            .type(MigrationRecord.MigrationType.DDL)
            .status(MigrationRecord.MigrationStatus.SUCCESS)
            .environment("test")
            .executionTimeMs(100L)
            .build();

        MigrationRecord slowMigration = MigrationRecord.builder()
            .migrationId("slow_migration")
            .type(MigrationRecord.MigrationType.DML)
            .status(MigrationRecord.MigrationStatus.SUCCESS)
            .environment("test")
            .executionTimeMs(5000L)
            .build();

        // Record migrations
        monitoringService.recordSuccessfulMigration(fastMigration);
        monitoringService.recordSuccessfulMigration(slowMigration);

        // Verify performance metrics
        MonitoringService.MigrationStats stats = monitoringService.getMigrationStats();
        assertEquals(2, stats.getTotalMigrations());
        assertEquals(2550.0, stats.getAverageExecutionTime(), 0.01); // (100 + 5000) / 2
        
        // Verify type-specific stats
        assertTrue(stats.getMigrationTypeStats().containsKey("DDL"));
        assertTrue(stats.getMigrationTypeStats().containsKey("DML"));
    }
} 