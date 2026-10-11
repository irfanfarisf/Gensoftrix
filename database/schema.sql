-- ============================================================================
-- GenSoftrix: Customizable Management Software Generator
-- MySQL Database Schema & Initial Sample Data (database/schema.sql)
-- Database: gensoftrix
-- ============================================================================

CREATE DATABASE IF NOT EXISTS gensoftrix
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gensoftrix;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
) ENGINE=InnoDB;

-- 2. APPLICATIONS TABLE
CREATE TABLE IF NOT EXISTS applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_applications_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    INDEX idx_applications_user (user_id)
) ENGINE=InnoDB;

-- 3. MODULES TABLE
CREATE TABLE IF NOT EXISTS modules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    module_identifier VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_modules_application
        FOREIGN KEY (application_id) REFERENCES applications(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_module_app_identifier (application_id, module_identifier),
    INDEX idx_modules_app (application_id)
) ENGINE=InnoDB;

-- 4. FIELDS TABLE
CREATE TABLE IF NOT EXISTS fields (
    id INT AUTO_INCREMENT PRIMARY KEY,
    module_id INT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(30) NOT NULL COMMENT 'TEXT, NUMBER, DECIMAL, DATE, EMAIL, BOOLEAN, DROPDOWN, REFERENCE',
    required BOOLEAN NOT NULL DEFAULT FALSE,
    default_value VARCHAR(255) DEFAULT NULL,
    options_csv TEXT DEFAULT NULL,
    reference_module_id INT DEFAULT NULL,
    display_order INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_fields_module
        FOREIGN KEY (module_id) REFERENCES modules(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_fields_ref_module
        FOREIGN KEY (reference_module_id) REFERENCES modules(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_field_module_name (module_id, field_name)
) ENGINE=InnoDB;

-- 5. RECORDS TABLE
CREATE TABLE IF NOT EXISTS records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    module_id INT NOT NULL,
    record_data JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_module
        FOREIGN KEY (module_id) REFERENCES modules(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. BUSINESS RULES TABLE
CREATE TABLE IF NOT EXISTS business_rules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    module_id INT NOT NULL,
    field_id INT DEFAULT NULL,
    rule_type VARCHAR(40) NOT NULL COMMENT 'REQUIRED, MIN_VALUE, MAX_VALUE, UNIQUE, DEFAULT_VALUE, FORMULA',
    rule_value VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_rules_module
        FOREIGN KEY (module_id) REFERENCES modules(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_rules_field
        FOREIGN KEY (field_id) REFERENCES fields(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
