package com.elearning.elearning_backend.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.migration.model.MigrationRecord;
import com.elearning.elearning_backend.migration.service.AuditService;
import com.elearning.elearning_backend.migration.service.MigrationService;
import com.elearning.elearning_backend.migration.service.MonitoringService;
import com.elearning.elearning_backend.migration.service.ValidationService;

@RestController
@RequestMapping("/api/demo")
public class MigrationDemoController {

    @Autowired(required = false)
    private MonitoringService monitoringService;
    
    @Autowired(required = false)
    private MigrationService migrationService;
    
    @Autowired(required = false)
    private ValidationService validationService;
    
    @Autowired(required = false)
    private AuditService auditService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "🎉 Migration System Demo is Working!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "SUCCESS");
        response.put("system", "Database Schema Migration Automation");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> systemStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // Check services
        response.put("migrationService", migrationService != null ? " LOADED" : "NOT LOADED");
        response.put("monitoringService", monitoringService != null ? " LOADED" : "NOT LOADED");
        response.put("validationService", validationService != null ? " LOADED" : "NOT LOADED");
        response.put("auditService", auditService != null ? " LOADED" : "NOT LOADED");
        
        // Check database connection
        try {
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            response.put("database", " CONNECTED - " + dbVersion);
        } catch (Exception e) {
            response.put("database", "CONNECTION ERROR - " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/database-tables")
    public ResponseEntity<Map<String, Object>> databaseTables() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check existing tables
            List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'elearning'", 
                String.class
            );
            
            response.put("totalTables", tables.size());
            response.put("tables", tables);
            
            // Check migration tables specifically
            Map<String, Boolean> migrationTables = new HashMap<>();
            migrationTables.put("migration_records", tables.contains("migration_records"));
            migrationTables.put("migration_audit_log", tables.contains("migration_audit_log"));
            migrationTables.put("migration_metadata", tables.contains("migration_metadata"));
            migrationTables.put("migration_locks", tables.contains("migration_locks"));
            migrationTables.put("migration_approvals", tables.contains("migration_approvals"));
            migrationTables.put("flyway_schema_history", tables.contains("flyway_schema_history"));
            
            response.put("migrationTables", migrationTables);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monitoring-stats")
    public ResponseEntity<Map<String, Object>> monitoringStats() {
        Map<String, Object> response = new HashMap<>();
        
        if (monitoringService != null) {
            try {
                MonitoringService.MigrationStats stats = monitoringService.getMigrationStats();
                response.put("totalMigrations", stats.getTotalMigrations());
                response.put("successfulMigrations", stats.getSuccessfulMigrations());
                response.put("failedMigrations", stats.getFailedMigrations());
                response.put("successRate", stats.getSuccessRate());
                response.put("averageExecutionTime", stats.getAverageExecutionTime());
                
                MonitoringService.MigrationHealthStatus health = monitoringService.getHealthStatus();
                response.put("healthStatus", health.getStatus().name());
                response.put("recentFailures", health.getRecentFailures());
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
            }
        } else {
            response.put("message", "MonitoringService not available");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-sample-data")
    public ResponseEntity<Map<String, Object>> createSampleData() {
        Map<String, Object> response = new HashMap<>();
        
        if (monitoringService != null) {
            try {
                // Create sample migration records
                MigrationRecord sample1 = MigrationRecord.builder()
                    .migrationId("DEMO_001")
                    .version("001")
                    .fileName("V001__Demo_Migration.sql")
                    .type(MigrationRecord.MigrationType.DDL)
                    .status(MigrationRecord.MigrationStatus.SUCCESS)
                    .environment("development")
                    .executionTimeMs(1500L)
                    .build();
                
                MigrationRecord sample2 = MigrationRecord.builder()
                    .migrationId("DEMO_002")
                    .version("002")
                    .fileName("V002__Demo_Data_Migration.sql")
                    .type(MigrationRecord.MigrationType.DML)
                    .status(MigrationRecord.MigrationStatus.SUCCESS)
                    .environment("development")
                    .executionTimeMs(2300L)
                    .build();
                
                monitoringService.recordSuccessfulMigration(sample1);
                monitoringService.recordSuccessfulMigration(sample2);
                
                response.put("message", " Sample data created successfully!");
                response.put("samplesCreated", 2);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
            }
        } else {
            response.put("message", "MonitoringService not available");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validation-test")
    public ResponseEntity<Map<String, Object>> validationTest() {
        Map<String, Object> response = new HashMap<>();
        
        if (validationService != null) {
            try {
                // Test file naming validation
                String[] validNames = {
                    "V001__Create_users_table.sql",
                    "V002__Add_user_indexes.sql",
                    "V100__Update_schema.sql"
                };
                
                String[] invalidNames = {
                    "invalid.sql",
                    "V001_missing_double_underscore.sql",
                    "001__missing_v_prefix.sql"
                };
                
                Map<String, Boolean> validResults = new HashMap<>();
                Map<String, Boolean> invalidResults = new HashMap<>();
                
                for (String name : validNames) {
                    validResults.put(name, name.matches("V\\d+__[a-zA-Z0-9_]+\\.sql"));
                }
                
                for (String name : invalidNames) {
                    invalidResults.put(name, name.matches("V\\d+__[a-zA-Z0-9_]+\\.sql"));
                }
                
                response.put("validNames", validResults);
                response.put("invalidNames", invalidResults);
                response.put("message", " Validation tests completed!");
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
            }
        } else {
            response.put("message", "ValidationService not available");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/full-demo")
    public ResponseEntity<Map<String, Object>> fullDemo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("title", "🚀 Database Schema Migration Automation - Full Demo");
        response.put("timestamp", System.currentTimeMillis());
        
        // System Status
        Map<String, Object> systemStatus = new HashMap<>();
        systemStatus.put("migrationService", migrationService != null ? " ACTIVE" : "INACTIVE");
        systemStatus.put("monitoringService", monitoringService != null ? " ACTIVE" : "INACTIVE");
        systemStatus.put("validationService", validationService != null ? " ACTIVE" : "INACTIVE");
        systemStatus.put("auditService", auditService != null ? " ACTIVE" : "INACTIVE");
        response.put("systemStatus", systemStatus);
        
        // Database Status
        try {
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            response.put("database", " MySQL " + dbVersion);
        } catch (Exception e) {
            response.put("database", "" + e.getMessage());
        }
        
        // Available Endpoints
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("System Status", "GET /api/demo/system-status");
        endpoints.put("Database Tables", "GET /api/demo/database-tables");
        endpoints.put("Monitoring Stats", "GET /api/demo/monitoring-stats");
        endpoints.put("Create Sample Data", "POST /api/demo/create-sample-data");
        endpoints.put("Validation Test", "GET /api/demo/validation-test");
        endpoints.put("Migration Health", "GET /api/migration/actuator/health");
        endpoints.put("Migration Stats", "GET /api/migration/stats");
        response.put("availableEndpoints", endpoints);
        
        response.put("instructions", "Use the endpoints above to see the Migration System in action!");
        
        return ResponseEntity.ok(response);
    }
} 