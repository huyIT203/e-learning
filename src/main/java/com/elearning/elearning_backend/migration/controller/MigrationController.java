package com.elearning.elearning_backend.migration.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.migration.model.MigrationRecord;
import com.elearning.elearning_backend.migration.service.MigrationService;
import com.elearning.elearning_backend.migration.service.MonitoringService;
import com.elearning.elearning_backend.migration.service.RollbackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {
    
    private final MigrationService migrationService;
    private final RollbackService rollbackService;
    private final MonitoringService monitoringService;
    
    /**
     * Execute pending migrations
     */
    @PostMapping("/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executePendingMigrations() {
        try {
            log.info("API request to execute pending migrations");
            List<MigrationRecord> results = migrationService.executePendingMigrations();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Migration execution completed",
                "results", results,
                "totalMigrations", results.size()
            ));
            
        } catch (Exception e) {
            log.error("Failed to execute pending migrations", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Migration execution failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Execute a specific migration
     */
    @PostMapping("/execute/{migrationFile}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executeSingleMigration(@PathVariable String migrationFile) {
        try {
            log.info("API request to execute migration: {}", migrationFile);
            MigrationRecord result = migrationService.executeSingleMigration(migrationFile);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Migration executed successfully",
                "result", result
            ));
            
        } catch (Exception e) {
            log.error("Failed to execute migration: {}", migrationFile, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Migration execution failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Rollback a specific migration
     */
    @PostMapping("/rollback/{migrationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rollbackMigration(@PathVariable String migrationId) {
        try {
            log.info("API request to rollback migration: {}", migrationId);
            RollbackService.RollbackResult result = rollbackService.rollbackMigration(migrationId);
            
            return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "message", result.isSuccess() ? "Rollback completed successfully" : "Rollback failed",
                "result", result
            ));
            
        } catch (Exception e) {
            log.error("Failed to rollback migration: {}", migrationId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Rollback failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Rollback to a specific version
     */
    @PostMapping("/rollback-to/{version}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rollbackToVersion(@PathVariable String version) {
        try {
            log.info("API request to rollback to version: {}", version);
            List<RollbackService.RollbackResult> results = rollbackService.rollbackToVersion(version);
            
            boolean allSuccessful = results.stream().allMatch(RollbackService.RollbackResult::isSuccess);
            
            return ResponseEntity.ok(Map.of(
                "success", allSuccessful,
                "message", allSuccessful ? "Rollback to version completed successfully" : "Some rollbacks failed",
                "results", results,
                "totalRollbacks", results.size()
            ));
            
        } catch (Exception e) {
            log.error("Failed to rollback to version: {}", version, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Rollback to version failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get rollback plan for a version
     */
    @GetMapping("/rollback-plan/{version}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRollbackPlan(@PathVariable String version) {
        try {
            log.info("API request to get rollback plan for version: {}", version);
            RollbackService.RollbackPlan plan = rollbackService.getRollbackPlan(version);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "plan", plan
            ));
            
        } catch (Exception e) {
            log.error("Failed to get rollback plan for version: {}", version, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get rollback plan: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get migration statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMigrationStats() {
        try {
            MonitoringService.MigrationStats stats = monitoringService.getMigrationStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));
            
        } catch (Exception e) {
            log.error("Failed to get migration stats", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get migration stats: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get migration health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> getMigrationHealth() {
        try {
            MonitoringService.MigrationHealthStatus health = monitoringService.getHealthStatus();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "health", health
            ));
            
        } catch (Exception e) {
            log.error("Failed to get migration health", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get migration health: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Reset migration metrics (for testing/maintenance)
     */
    @PostMapping("/reset-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetMetrics() {
        try {
            log.info("API request to reset migration metrics");
            monitoringService.resetMetrics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Migration metrics reset successfully"
            ));
            
        } catch (Exception e) {
            log.error("Failed to reset migration metrics", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to reset metrics: " + e.getMessage()
            ));
        }
    }
} 