-- ============================================================================
-- GenSoftrix: Customizable Management Software Generator
-- MySQL Database Schema & Initial Sample Data
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
    options_csv TEXT DEFAULT NULL COMMENT 'Comma-separated options for DROPDOWN type',
    reference_module_id INT DEFAULT NULL COMMENT 'Target module_id when field_type is REFERENCE',
    display_order INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_fields_module
        FOREIGN KEY (module_id) REFERENCES modules(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_fields_ref_module
        FOREIGN KEY (reference_module_id) REFERENCES modules(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_field_module_name (module_id, field_name),
    INDEX idx_fields_module_order (module_id, display_order)
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
        ON DELETE CASCADE,
    INDEX idx_records_module (module_id)
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
        ON DELETE CASCADE,
    INDEX idx_rules_module (module_id)
) ENGINE=InnoDB;

-- ============================================================================
-- SAMPLE SEED DATA
-- Default demo user: username = "admin", password = "password123"
-- Password format: salt_hex:sha256_hex
-- ============================================================================

INSERT IGNORE INTO users (id, username, password_hash) VALUES
(1, 'admin', '47656e536f667472697853616c743031:6f5c99f62f5a76d2b6a9a2232c229a0878a5d8e5f7c27c10a1a5f9847c13a818');

-- Sample Applications
INSERT IGNORE INTO applications (id, user_id, name, description) VALUES
(1, 1, 'Inventory Management', 'Track warehouse products, supplier directory, stock quantities, and purchase orders.'),
(2, 1, 'Student Management', 'Manage academic departments, student enrollments, contact records, and GPA tracking.');

-- Sample Modules for Inventory Management (app_id = 1)
INSERT IGNORE INTO modules (id, application_id, module_identifier, name, description) VALUES
(1, 1, 'MOD_SUPPLIERS', 'Suppliers', 'Approved hardware and component vendors'),
(2, 1, 'MOD_PRODUCTS', 'Products', 'Catalog of stocked items with pricing and category metadata'),
(3, 1, 'MOD_ORDERS', 'Purchase Orders', 'Restock orders linking products and calculated order totals');

-- Sample Modules for Student Management (app_id = 2)
INSERT IGNORE INTO modules (id, application_id, module_identifier, name, description) VALUES
(4, 2, 'MOD_DEPARTMENTS', 'Departments', 'Academic faculties and department codes'),
(5, 2, 'MOD_STUDENTS', 'Students', 'Enrolled student roster referencing academic departments');

-- Fields for Suppliers (module_id = 1)
INSERT IGNORE INTO fields (id, module_id, field_name, field_type, required, default_value, options_csv, reference_module_id, display_order) VALUES
(1, 1, 'Supplier Name', 'TEXT', TRUE, '', NULL, NULL, 1),
(2, 1, 'Contact Email', 'EMAIL', TRUE, '', NULL, NULL, 2),
(3, 1, 'Region', 'DROPDOWN', TRUE, 'North America', 'North America,Europe,Asia-Pacific,Latin America', NULL, 3),
(4, 1, 'Active Partner', 'BOOLEAN', FALSE, 'true', NULL, NULL, 4);

-- Fields for Products (module_id = 2)
INSERT IGNORE INTO fields (id, module_id, field_name, field_type, required, default_value, options_csv, reference_module_id, display_order) VALUES
(5, 2, 'SKU', 'TEXT', TRUE, 'PRD-', NULL, NULL, 1),
(6, 2, 'Product Name', 'TEXT', TRUE, '', NULL, NULL, 2),
(7, 2, 'Category', 'DROPDOWN', TRUE, 'Electronics', 'Electronics,Networking,Storage,Accessories,Office Supplies', NULL, 3),
(8, 2, 'Supplier', 'REFERENCE', TRUE, '', NULL, 1, 4),
(9, 2, 'Unit Price', 'DECIMAL', TRUE, '0.00', NULL, NULL, 5),
(10, 2, 'Quantity', 'NUMBER', TRUE, '0', NULL, NULL, 6),
(11, 2, 'In Stock', 'BOOLEAN', FALSE, 'true', NULL, NULL, 7);

-- Fields for Purchase Orders (module_id = 3)
INSERT IGNORE INTO fields (id, module_id, field_name, field_type, required, default_value, options_csv, reference_module_id, display_order) VALUES
(12, 3, 'Order Number', 'TEXT', TRUE, 'PO-2026-', NULL, NULL, 1),
(13, 3, 'Product', 'REFERENCE', TRUE, '', NULL, 2, 2),
(14, 3, 'Order Date', 'DATE', TRUE, '2026-10-10', NULL, NULL, 3),
(15, 3, 'Unit Price', 'DECIMAL', TRUE, '0.00', NULL, NULL, 4),
(16, 3, 'Order Qty', 'NUMBER', TRUE, '1', NULL, NULL, 5),
(17, 3, 'Total Cost', 'DECIMAL', FALSE, '0.00', NULL, NULL, 6),
(18, 3, 'Status', 'DROPDOWN', TRUE, 'Pending', 'Pending,Approved,Shipped,Delivered', NULL, 7);

-- Fields for Departments (module_id = 4)
INSERT IGNORE INTO fields (id, module_id, field_name, field_type, required, default_value, options_csv, reference_module_id, display_order) VALUES
(19, 4, 'Department Name', 'TEXT', TRUE, '', NULL, NULL, 1),
(20, 4, 'Building Code', 'TEXT', TRUE, 'ENG-A', NULL, NULL, 2),
(21, 4, 'Faculty Advisor Email', 'EMAIL', TRUE, '', NULL, NULL, 3);

-- Fields for Students (module_id = 5)
INSERT IGNORE INTO fields (id, module_id, field_name, field_type, required, default_value, options_csv, reference_module_id, display_order) VALUES
(22, 5, 'Student ID', 'TEXT', TRUE, 'STU-', NULL, NULL, 1),
(23, 5, 'Full Name', 'TEXT', TRUE, '', NULL, NULL, 2),
(24, 5, 'Email', 'EMAIL', TRUE, '', NULL, NULL, 3),
(25, 5, 'Department', 'REFERENCE', TRUE, '', NULL, 4, 4),
(26, 5, 'Enrollment Date', 'DATE', TRUE, '2026-09-01', NULL, NULL, 5),
(27, 5, 'GPA', 'DECIMAL', TRUE, '3.50', NULL, NULL, 6),
(28, 5, 'Scholarship Active', 'BOOLEAN', FALSE, 'false', NULL, NULL, 7);

-- Sample Business Rules
INSERT IGNORE INTO business_rules (id, module_id, field_id, rule_type, rule_value, enabled) VALUES
(1, 2, 5, 'UNIQUE', 'SKU must be unique across all products', TRUE),
(2, 2, 9, 'MIN_VALUE', '0.01', TRUE),
(3, 2, 10, 'MIN_VALUE', '0', TRUE),
(4, 3, 16, 'MIN_VALUE', '1', TRUE),
(5, 3, 17, 'FORMULA', 'Unit Price * Order Qty', TRUE),
(6, 5, 22, 'UNIQUE', 'Student ID must be unique', TRUE),
(7, 5, 27, 'MIN_VALUE', '0.0', TRUE),
(8, 5, 27, 'MAX_VALUE', '4.0', TRUE);

-- Sample Records
INSERT IGNORE INTO records (id, module_id, record_data) VALUES
(1, 1, '{"Supplier Name": "Apex Silicon Works", "Contact Email": "orders@apexsilicon.com", "Region": "North America", "Active Partner": "true"}'),
(2, 1, '{"Supplier Name": "Nordic Optic Systems", "Contact Email": "supply@nordicoptic.eu", "Region": "Europe", "Active Partner": "true"}'),
(3, 2, '{"SKU": "PRD-1001", "Product Name": "Enterprise Managed Switch 24P", "Category": "Networking", "Supplier": "Apex Silicon Works", "Unit Price": "420.00", "Quantity": "35", "In Stock": "true"}'),
(4, 2, '{"SKU": "PRD-1002", "Product Name": "NVMe Gen4 Server SSD 2TB", "Category": "Storage", "Supplier": "Nordic Optic Systems", "Unit Price": "185.50", "Quantity": "64", "In Stock": "true"}'),
(5, 3, '{"Order Number": "PO-2026-081", "Product": "PRD-1001", "Order Date": "2026-10-08", "Unit Price": "420.00", "Order Qty": "10", "Total Cost": "4200.00", "Status": "Approved"}'),
(6, 4, '{"Department Name": "Computer Science", "Building Code": "CS-301", "Faculty Advisor Email": "chair.cs@university.edu"}'),
(7, 4, '{"Department Name": "Electrical Engineering", "Building Code": "EE-104", "Faculty Advisor Email": "chair.ee@university.edu"}'),
(8, 5, '{"Student ID": "STU-202601", "Full Name": "Elena Rostova", "Email": "elena.rostova@university.edu", "Department": "Computer Science", "Enrollment Date": "2026-09-01", "GPA": "3.92", "Scholarship Active": "true"}');
