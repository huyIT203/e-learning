-- Flyway Rollback: R001__Drop_migration_tables
-- Description: Rollback script to drop migration tracking tables
-- Author: Migration System
-- WARNING: This will permanently delete all migration history!

-- Drop foreign key constraints first
SET FOREIGN_KEY_CHECKS = 0;

-- Drop tables in reverse order of creation
DROP TABLE IF EXISTS migration_approvals;
DROP TABLE IF EXISTS migration_locks;
DROP TABLE IF EXISTS migration_metadata;
DROP TABLE IF EXISTS migration_audit_log;
DROP TABLE IF EXISTS migration_records;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1; 