-- Flyway Migration: V001__Create_migration_tables
-- Description: Create migration tracking and audit tables for Database Schema Migration Automation
-- Author: Migration System
-- Rollback: R001__Drop_migration_tables.sql

-- Create migration records table
CREATE TABLE IF NOT EXISTS migration_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    migration_id VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    checksum VARCHAR(255) NOT NULL,
    type ENUM('DDL', 'DML', 'INDEX', 'PROCEDURE', 'TRIGGER', 'VIEW', 'DATA_MIGRATION', 'ROLLBACK') NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'ROLLED_BACK', 'SKIPPED') NOT NULL,
    environment VARCHAR(50) NOT NULL,
    executed_by VARCHAR(100),
    executed_at DATETIME,
    updated_at DATETIME,
    execution_time_ms BIGINT,
    rollback_script TEXT,
    backup_location VARCHAR(500),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_migration_id (migration_id),
    INDEX idx_version (version),
    INDEX idx_status (status),
    INDEX idx_environment (environment),
    INDEX idx_executed_at (executed_at),
    INDEX idx_type (type)
);

-- Create migration audit log table
CREATE TABLE IF NOT EXISTS migration_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    migration_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    environment VARCHAR(50),
    executed_by VARCHAR(100),
    timestamp DATETIME NOT NULL,
    execution_time_ms BIGINT,
    error_message TEXT,
    additional_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_migration_id (migration_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action),
    INDEX idx_environment (environment)
);

-- Create migration metadata table for storing key-value pairs
CREATE TABLE IF NOT EXISTS migration_metadata (
    migration_id BIGINT NOT NULL,
    meta_key VARCHAR(100) NOT NULL,
    meta_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (migration_id, meta_key),
    FOREIGN KEY (migration_id) REFERENCES migration_records(id) ON DELETE CASCADE,
    INDEX idx_meta_key (meta_key)
);

-- Create migration locks table to prevent concurrent migrations
CREATE TABLE IF NOT EXISTS migration_locks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lock_name VARCHAR(100) NOT NULL UNIQUE,
    environment VARCHAR(50) NOT NULL,
    locked_by VARCHAR(100) NOT NULL,
    locked_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    
    INDEX idx_lock_name (lock_name),
    INDEX idx_environment (environment),
    INDEX idx_expires_at (expires_at)
);

-- Create migration approvals table for production environments
CREATE TABLE IF NOT EXISTS migration_approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    migration_id VARCHAR(255) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    requested_at DATETIME NOT NULL,
    approved_by VARCHAR(100),
    approved_at DATETIME,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    comments TEXT,
    
    UNIQUE KEY unique_migration_env (migration_id, environment),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at),
    INDEX idx_approved_at (approved_at)
);

-- Insert initial migration record for this script
INSERT INTO migration_records (
    migration_id, version, description, file_name, checksum, type, 
    status, environment, executed_by, executed_at, execution_time_ms
) VALUES (
    'V001__Create_migration_tables.sql_SYSTEM', 
    '001', 
    'Create migration tracking and audit tables', 
    'V001__Create_migration_tables.sql',
    SHA2('V001__Create_migration_tables.sql_content', 256),
    'DDL', 
    'SUCCESS', 
    'development', 
    'SYSTEM', 
    NOW(), 
    0
) ON DUPLICATE KEY UPDATE updated_at = NOW(); 