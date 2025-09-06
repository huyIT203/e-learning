package com.elearning.elearning_backend.migration.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "migration_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String migrationId;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String checksum;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MigrationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MigrationStatus status;
    
    @Column(nullable = false)
    private String environment;
    
    @Column(name = "executed_by")
    private String executedBy;
    
    @CreationTimestamp
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "rollback_script")
    private String rollbackScript;
    
    @Column(name = "backup_location")
    private String backupLocation;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @ElementCollection
    @CollectionTable(name = "migration_metadata", joinColumns = @JoinColumn(name = "migration_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata;
    
    public enum MigrationType {
        DDL, DML, INDEX, PROCEDURE, TRIGGER, VIEW, DATA_MIGRATION, ROLLBACK
    }
    
    public enum MigrationStatus {
        PENDING, RUNNING, SUCCESS, FAILED, ROLLED_BACK, SKIPPED
    }
} 