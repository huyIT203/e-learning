package com.elearning.elearning_backend.migration.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.migration.model.MigrationRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MonitoringService {
    
    private final AtomicLong totalMigrations = new AtomicLong(0);
    private final AtomicLong successfulMigrations = new AtomicLong(0);
    private final AtomicLong failedMigrations = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    private final Map<String, Long> migrationTypeCount = new ConcurrentHashMap<>();
    private final Map<String, Long> environmentStats = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastMigrationByEnvironment = new ConcurrentHashMap<>();
    
    /**
     * Record successful migration metrics
     */
    public void recordSuccessfulMigration(MigrationRecord record) {
        log.info("Recording successful migration metrics: {}", record.getMigrationId());
        
        totalMigrations.incrementAndGet();
        successfulMigrations.incrementAndGet();
        totalExecutionTime.addAndGet(record.getExecutionTimeMs());
        
        // Track by migration type
        migrationTypeCount.merge(record.getType().name(), 1L, Long::sum);
        
        // Track by environment
        environmentStats.merge(record.getEnvironment() + "_success", 1L, Long::sum);
        lastMigrationByEnvironment.put(record.getEnvironment(), LocalDateTime.now());
        
        // Log performance metrics
        logPerformanceMetrics(record);
    }
    
    /**
     * Record failed migration metrics
     */
    public void recordFailedMigration(MigrationRecord record, Exception error) {
        log.error("Recording failed migration metrics: {}", record.getMigrationId());
        
        totalMigrations.incrementAndGet();
        failedMigrations.incrementAndGet();
        
        if (record.getExecutionTimeMs() != null) {
            totalExecutionTime.addAndGet(record.getExecutionTimeMs());
        }
        
        // Track by environment
        environmentStats.merge(record.getEnvironment() + "_failed", 1L, Long::sum);
        
        // Log error metrics
        logErrorMetrics(record, error);
    }
    
    /**
     * Record general migration error
     */
    public void recordMigrationError(Exception error) {
        log.error("Recording migration error: {}", error.getMessage());
        
        // Could integrate with external monitoring systems here
        // Like sending alerts to Slack, email, or monitoring dashboards
    }
    
    /**
     * Get migration statistics
     */
    public MigrationStats getMigrationStats() {
        return MigrationStats.builder()
            .totalMigrations(totalMigrations.get())
            .successfulMigrations(successfulMigrations.get())
            .failedMigrations(failedMigrations.get())
            .successRate(calculateSuccessRate())
            .averageExecutionTime(calculateAverageExecutionTime())
            .migrationTypeStats(new ConcurrentHashMap<>(migrationTypeCount))
            .environmentStats(new ConcurrentHashMap<>(environmentStats))
            .lastMigrationByEnvironment(new ConcurrentHashMap<>(lastMigrationByEnvironment))
            .build();
    }
    
    /**
     * Get health status of migration system
     */
    public MigrationHealthStatus getHealthStatus() {
        double successRate = calculateSuccessRate();
        long recentFailures = getRecentFailures();
        
        MigrationHealthStatus.Status status;
        if (successRate >= 95.0 && recentFailures == 0) {
            status = MigrationHealthStatus.Status.HEALTHY;
        } else if (successRate >= 90.0 && recentFailures <= 2) {
            status = MigrationHealthStatus.Status.WARNING;
        } else {
            status = MigrationHealthStatus.Status.CRITICAL;
        }
        
        return MigrationHealthStatus.builder()
            .status(status)
            .successRate(successRate)
            .recentFailures(recentFailures)
            .lastCheck(LocalDateTime.now())
            .totalMigrations(totalMigrations.get())
            .build();
    }
    
    /**
     * Reset all metrics (for testing or maintenance)
     */
    public void resetMetrics() {
        log.info("Resetting migration metrics");
        
        totalMigrations.set(0);
        successfulMigrations.set(0);
        failedMigrations.set(0);
        totalExecutionTime.set(0);
        
        migrationTypeCount.clear();
        environmentStats.clear();
        lastMigrationByEnvironment.clear();
    }
    
    private void logPerformanceMetrics(MigrationRecord record) {
        log.info("Migration Performance - ID: {}, Type: {}, Time: {}ms, Environment: {}",
            record.getMigrationId(),
            record.getType(),
            record.getExecutionTimeMs(),
            record.getEnvironment()
        );
        
        // Alert on slow migrations (over 30 seconds)
        if (record.getExecutionTimeMs() > 30000) {
            log.warn("SLOW MIGRATION DETECTED: {} took {}ms", 
                record.getMigrationId(), record.getExecutionTimeMs());
        }
    }
    
    private void logErrorMetrics(MigrationRecord record, Exception error) {
        log.error("Migration Error - ID: {}, Environment: {}, Error: {}", 
            record.getMigrationId(), 
            record.getEnvironment(), 
            error.getMessage()
        );
        
        // Could send alerts to external systems here
    }
    
    private double calculateSuccessRate() {
        long total = totalMigrations.get();
        if (total == 0) return 100.0;
        
        return (successfulMigrations.get() * 100.0) / total;
    }
    
    private double calculateAverageExecutionTime() {
        long total = totalMigrations.get();
        if (total == 0) return 0.0;
        
        return totalExecutionTime.get() / (double) total;
    }
    
    private long getRecentFailures() {
        // This is simplified - in production, you'd track failures over time
        return failedMigrations.get();
    }
    
    /**
     * Migration statistics data class
     */
    public static class MigrationStats {
        private final long totalMigrations;
        private final long successfulMigrations;
        private final long failedMigrations;
        private final double successRate;
        private final double averageExecutionTime;
        private final Map<String, Long> migrationTypeStats;
        private final Map<String, Long> environmentStats;
        private final Map<String, LocalDateTime> lastMigrationByEnvironment;
        
        private MigrationStats(Builder builder) {
            this.totalMigrations = builder.totalMigrations;
            this.successfulMigrations = builder.successfulMigrations;
            this.failedMigrations = builder.failedMigrations;
            this.successRate = builder.successRate;
            this.averageExecutionTime = builder.averageExecutionTime;
            this.migrationTypeStats = builder.migrationTypeStats;
            this.environmentStats = builder.environmentStats;
            this.lastMigrationByEnvironment = builder.lastMigrationByEnvironment;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public long getTotalMigrations() { return totalMigrations; }
        public long getSuccessfulMigrations() { return successfulMigrations; }
        public long getFailedMigrations() { return failedMigrations; }
        public double getSuccessRate() { return successRate; }
        public double getAverageExecutionTime() { return averageExecutionTime; }
        public Map<String, Long> getMigrationTypeStats() { return migrationTypeStats; }
        public Map<String, Long> getEnvironmentStats() { return environmentStats; }
        public Map<String, LocalDateTime> getLastMigrationByEnvironment() { return lastMigrationByEnvironment; }
        
        public static class Builder {
            private long totalMigrations;
            private long successfulMigrations;
            private long failedMigrations;
            private double successRate;
            private double averageExecutionTime;
            private Map<String, Long> migrationTypeStats;
            private Map<String, Long> environmentStats;
            private Map<String, LocalDateTime> lastMigrationByEnvironment;
            
            public Builder totalMigrations(long totalMigrations) {
                this.totalMigrations = totalMigrations;
                return this;
            }
            
            public Builder successfulMigrations(long successfulMigrations) {
                this.successfulMigrations = successfulMigrations;
                return this;
            }
            
            public Builder failedMigrations(long failedMigrations) {
                this.failedMigrations = failedMigrations;
                return this;
            }
            
            public Builder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }
            
            public Builder averageExecutionTime(double averageExecutionTime) {
                this.averageExecutionTime = averageExecutionTime;
                return this;
            }
            
            public Builder migrationTypeStats(Map<String, Long> migrationTypeStats) {
                this.migrationTypeStats = migrationTypeStats;
                return this;
            }
            
            public Builder environmentStats(Map<String, Long> environmentStats) {
                this.environmentStats = environmentStats;
                return this;
            }
            
            public Builder lastMigrationByEnvironment(Map<String, LocalDateTime> lastMigrationByEnvironment) {
                this.lastMigrationByEnvironment = lastMigrationByEnvironment;
                return this;
            }
            
            public MigrationStats build() {
                return new MigrationStats(this);
            }
        }
    }
    
    /**
     * Migration health status data class
     */
    public static class MigrationHealthStatus {
        private final Status status;
        private final double successRate;
        private final long recentFailures;
        private final LocalDateTime lastCheck;
        private final long totalMigrations;
        
        private MigrationHealthStatus(Builder builder) {
            this.status = builder.status;
            this.successRate = builder.successRate;
            this.recentFailures = builder.recentFailures;
            this.lastCheck = builder.lastCheck;
            this.totalMigrations = builder.totalMigrations;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public Status getStatus() { return status; }
        public double getSuccessRate() { return successRate; }
        public long getRecentFailures() { return recentFailures; }
        public LocalDateTime getLastCheck() { return lastCheck; }
        public long getTotalMigrations() { return totalMigrations; }
        
        public enum Status {
            HEALTHY, WARNING, CRITICAL
        }
        
        public static class Builder {
            private Status status;
            private double successRate;
            private long recentFailures;
            private LocalDateTime lastCheck;
            private long totalMigrations;
            
            public Builder status(Status status) {
                this.status = status;
                return this;
            }
            
            public Builder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }
            
            public Builder recentFailures(long recentFailures) {
                this.recentFailures = recentFailures;
                return this;
            }
            
            public Builder lastCheck(LocalDateTime lastCheck) {
                this.lastCheck = lastCheck;
                return this;
            }
            
            public Builder totalMigrations(long totalMigrations) {
                this.totalMigrations = totalMigrations;
                return this;
            }
            
            public MigrationHealthStatus build() {
                return new MigrationHealthStatus(this);
            }
        }
    }
} 