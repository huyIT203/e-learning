package com.elearning.elearning_backend.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.elearning.elearning_backend.migration.service.AuditService;
import com.elearning.elearning_backend.migration.service.MigrationService;
import com.elearning.elearning_backend.migration.service.MonitoringService;
import com.elearning.elearning_backend.migration.service.RollbackService;
import com.elearning.elearning_backend.migration.service.ValidationService;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "migration")
@Data
public class MigrationConfig {
    
    private boolean enabled = true;
    private boolean autoMigrate = false;
    private String environment = "development";
    private int maxRetries = 3;
    private int backupRetentionDays = 30;
    private boolean validateBeforeMigration = true;
    private boolean auditTrailEnabled = true;
    private String migrationPath = "classpath:db/migration";
    private String rollbackPath = "classpath:db/rollback";
    
    // Multi-environment configuration
    private EnvironmentConfig development = new EnvironmentConfig();
    private EnvironmentConfig staging = new EnvironmentConfig();
    private EnvironmentConfig production = new EnvironmentConfig();
    
    @Data
    public static class EnvironmentConfig {
        private boolean autoMigrate = false;
        private boolean requireApproval = true;
        private int maxConcurrentMigrations = 1;
        private boolean backupBeforeMigration = true;
        private String[] allowedMigrationTypes = {"DDL", "DML", "INDEX"};
    }
    
    @Bean
    @Profile("!test")
    public MigrationService migrationService() {
        return new MigrationService(this);
    }
    
    @Bean
    @Profile("!test")
    public RollbackService rollbackService() {
        return new RollbackService(this);
    }
    
    @Bean
    @Profile("!test")
    public ValidationService validationService() {
        return new ValidationService();
    }
    
    @Bean
    @Profile("!test")
    public AuditService auditService() {
        return new AuditService();
    }
    
    @Bean
    @Profile("!test")
    public MonitoringService monitoringService() {
        return new MonitoringService();
    }
    
    public EnvironmentConfig getCurrentEnvironmentConfig() {
        return switch (environment.toLowerCase()) {
            case "development" -> development;
            case "staging" -> staging;
            case "production" -> production;
            default -> development;
        };
    }
} 