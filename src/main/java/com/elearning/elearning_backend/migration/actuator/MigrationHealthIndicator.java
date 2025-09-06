package com.elearning.elearning_backend.migration.actuator;

import com.elearning.elearning_backend.migration.service.MonitoringService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/migration/actuator")
@RequiredArgsConstructor
@Slf4j
public class MigrationHealthIndicator {
    
    private final MonitoringService monitoringService;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            MonitoringService.MigrationHealthStatus healthStatus = monitoringService.getHealthStatus();
            
            response.put("status", healthStatus.getStatus().name());
            response.put("details", Map.of(
                "successRate", healthStatus.getSuccessRate(),
                "totalMigrations", healthStatus.getTotalMigrations(),
                "recentFailures", healthStatus.getRecentFailures(),
                "lastCheck", healthStatus.getLastCheck()
            ));
            
            // Determine HTTP status based on health
            return switch (healthStatus.getStatus()) {
                case HEALTHY -> ResponseEntity.ok(response);
                case WARNING -> ResponseEntity.status(200).body(response); // Still OK but with warning
                case CRITICAL -> ResponseEntity.status(503).body(response); // Service unavailable
            };
                
        } catch (Exception e) {
            log.error("Error checking migration health", e);
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        try {
            MonitoringService.MigrationStats stats = monitoringService.getMigrationStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalMigrations", stats.getTotalMigrations());
            response.put("successfulMigrations", stats.getSuccessfulMigrations());
            response.put("failedMigrations", stats.getFailedMigrations());
            response.put("successRate", stats.getSuccessRate());
            response.put("averageExecutionTime", stats.getAverageExecutionTime());
            response.put("migrationTypeStats", stats.getMigrationTypeStats());
            response.put("environmentStats", stats.getEnvironmentStats());
            response.put("lastMigrationByEnvironment", stats.getLastMigrationByEnvironment());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting migration metrics", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
} 