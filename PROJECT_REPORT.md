# Project Report: GenSoftrix – Customizable Management Software Generator

- **Project Title**: GenSoftrix – Customizable Management Software Generator
- **Technology Stack**: Java 17, Java Swing, JDBC, MySQL 8.0
- **Project Type**: Desktop Application / BTech CS Java Mini-Project

---

## 1. Introduction

Small businesses, college departments, and administrative offices frequently need structured software to manage domain-specific records—such as Inventory Management, Student Enrollment Systems, Employee Directories, and Library Catalogs. Writing separate Java programs, SQL tables, and GUI forms for every new dataset is repetitive and time-consuming.

**GenSoftrix** is a desktop management software generator built using **Java**, **Java Swing**, **JDBC**, and **MySQL**. Instead of hardcoding forms for a single domain, GenSoftrix allows users to define custom **Applications**, **Modules**, **Fields**, and **Business Rules**, and then automatically generates the corresponding **Java Swing data-entry forms** (`JTextField`, `JSpinner`, `JComboBox`, `JCheckBox`) and **dynamic `JTable` views** at runtime.

---

## 2. Project Objectives

1. **Eliminate Hardcoded Forms**: Generate Java Swing forms and `JTable` columns dynamically from field metadata stored in MySQL.
2. **Demonstrate Core Java OOP Principles**: Apply Encapsulation, Inheritance (`BaseEntity`), Polymorphism (`getDisplaySummary()`, `getSwingComponentName()`), Abstraction (`CrudDAO<T>`, `Validator`), Constructors, Method Overloading (`RecordDAO.create`, `RecordDAO.searchRecords`), Collections (`ArrayList`, `LinkedHashMap`), and Exception Handling (`try-with-resources`).
3. **Enforce Data Integrity & Business Rules**: Validate required fields, data types, non-negative prices/quantities, unique keys, default values, and arithmetic formulas before saving to MySQL.
4. **Provide Built-in Reporting & Calculations**: Calculate module record counts, numeric field Sum, Average, Minimum, Maximum, and cross-field totals (such as Total Inventory Value = `Price * Quantity`).
5. **Ensure Multi-User Security**: Authenticate users with salted SHA-256 password hashes and isolate each user's applications via foreign-key ownership (`user_id`).

---

## 3. System Design & Object-Oriented Programming (OOP) Architecture

GenSoftrix is organized into five cohesive Java packages (`config`, `model`, `dao`, `service`, `ui`):

- **Encapsulation**: All entity attributes in `User`, `Application`, `Module`, `FieldDefinition`, `RecordData`, and `BusinessRule` are `private` and accessed through validated getters and setters.
- **Inheritance**: `BaseEntity` serves as the abstract superclass for all domain models, providing shared `id` and `createdAt` properties.
- **Polymorphism**: Subclasses override `BaseEntity.getDisplaySummary()`, and `FieldDefinition.getSwingComponentName()` polymorphically maps metadata types to Swing controls.
- **Abstraction**: `CrudDAO<T>` defines standard JDBC persistence contracts, and `Validator` defines the record validation contract implemented by `ValidationService`.
- **Method Overloading**: `RecordDAO` overloads `create(RecordData)` and `create(int, Map<String, String>)`, as well as `searchRecords(int, String)` (global keyword search) and `searchRecords(int, String, String)` (field-specific filter).

---

## 4. Functional Modules (Modules 1 to 9)

1. **Module 1 – Login System (`LoginFrame`, `RegisterFrame`, `AuthenticationService`)**: Provides Username/Password fields, Login, Registration, and Exit buttons with salted SHA-256 password verification.
2. **Module 2 – Dashboard (`DashboardFrame`)**: Displays Total Applications, Total Modules, Total Records, Recently Created Applications, and sidebar navigation for *My Applications*, *Module Builder*, *Field Builder*, *Records*, *Reports*, and *Logout*.
3. **Module 3 – Application Manager (`ApplicationPanel`)**: Create, view, edit, delete, and open management applications stored in MySQL.
4. **Module 4 – Module Builder (`ModulePanel`)**: Create and manage modules (e.g., *Products*, *Suppliers*, *Purchase Orders*) inside an application.
5. **Module 5 – Field Builder (`FieldBuilderPanel`)**: Configure custom fields (`TEXT`, `NUMBER`, `DECIMAL`, `DATE`, `EMAIL`, `DROPDOWN`, `BOOLEAN`, `REFERENCE`) with required flags and default values.
6. **Module 6 – Dynamic Form Generator (`FormGenerator`, `DynamicFormDialog`, `DynamicFormPanel`)**: Dynamically builds Swing forms with Save and Clear buttons from stored field metadata.
7. **Module 7 – Record Manager & Dynamic Tables (`TableGenerator`, `RecordTablePanel`)**: Renders dynamic `JTable` columns with Add, View, Edit, Delete (with confirmation dialog), Search, and Refresh operations.
8. **Module 8 – Business Rules & Validation (`ValidationService`, `BusinessRuleDAO`)**: Enforces required fields, minimum/maximum numeric values (e.g., `Price >= 0`, `Quantity >= 0`), unique field values (`SKU`, `Student ID`), default values, and formulas.
9. **Module 9 – Reports & Calculations (`ReportService`, `ReportPanel`)**: Computes total records, record counts per module (`SQL COUNT / GROUP BY`), numeric field **Sum**, **Average**, **Min**, **Max**, and **Total Inventory Value** (`SUM(Unit Price * Quantity)`).

---

## 5. Database Design (`gensoftrix`)

The MySQL database contains 6 normalized InnoDB tables:
1. **`users`** (`id`, `username` UNIQUE, `password_hash`, `created_at`)
2. **`applications`** (`id`, `user_id` FK → `users.id`, `name`, `description`, `created_at`)
3. **`modules`** (`id`, `application_id` FK → `applications.id`, `module_identifier`, `name`, `description`, `created_at`)
4. **`fields`** (`id`, `module_id` FK → `modules.id`, `field_name`, `field_type`, `required`, `default_value`, `options_csv`, `reference_module_id`, `display_order`)
5. **`records`** (`id`, `module_id` FK → `modules.id`, `record_data` JSON, `created_at`, `updated_at`)
6. **`business_rules`** (`id`, `module_id` FK → `modules.id`, `field_id` FK → `fields.id`, `rule_type`, `rule_value`, `enabled`)

---

## 6. Conclusion

GenSoftrix demonstrates how core Java SE, Swing event-driven GUI programming, OOP design principles, and JDBC/MySQL persistence work together to create a practical customizable software generator suitable for academic evaluation and real-world data management.
