# GenSoftrix — Verification & Test Cases (Section 9)

This document details the 10 functional and security test cases verifying **GenSoftrix – Customizable Management Software Generator**.

| Test ID | Feature Under Test | Test Steps & Input | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | **User Registration & Salted SHA-256 Login** | 1. Open `RegisterFrame`, enter username `student_demo` and password `password123`.\n2. Log out and sign in via `LoginFrame`. | User row inserted into `users` with `saltHex:sha256Hex` hash; login succeeds and opens `DashboardFrame`. | PASS |
| **TC-02** | **Application Creation & Persistence** | Create application `"Library Management"` with description `"Track books and student loans"`. Restart application. | Row saved in `applications` table via `ApplicationDAO`; appears in Dashboard & Application Manager after restart. | PASS |
| **TC-03** | **Module Builder CRUD** | Open `"Inventory Management"`, add module `"Warehouses"` (`MOD_WAREHOUSES`), edit description, view list. | Module saved in `modules` table referencing `application_id = 1`. | PASS |
| **TC-04** | **Field Builder Metadata Persistence** | Add fields to `Products`: `Product Name` (`TEXT`, Required), `Unit Price` (`DECIMAL`, Required), `Quantity` (`NUMBER`, Required), `Category` (`DROPDOWN`), `In Stock` (`BOOLEAN`). | Field metadata stored in `fields` table in MySQL with `display_order`. | PASS |
| **TC-05** | **Dynamic Swing Form Generation** | Open `DynamicFormDialog` / `DynamicFormPanel` for `Products`. | FormGenerator automatically constructs `JTextField`, `JSpinner`, `JComboBox`, and `JCheckBox` matching the 5 fields. | PASS |
| **TC-06** | **Record CRUD & Dynamic JTable** | Insert a new Product record, search by keyword, edit `Quantity` from `35` to `50`, and delete a test record with confirmation. | `TableGenerator` updates `JTable` columns dynamically; `RecordDAO` updates JSON payload in MySQL `records` table. | PASS |
| **TC-07** | **Cross-Restart Data Persistence** | Close GenSoftrix (`Exit` button) and relaunch `Main.java`. | All applications, modules, field definitions, business rules, and JSON records are reloaded from MySQL `gensoftrix`. | PASS |
| **TC-08** | **Validation & Business Rules Rejection** | Attempt to save a Product with negative `Unit Price` (`-15.00`), negative `Quantity` (`-5`), empty required `Product Name`, or duplicate `SKU` (`PRD-1001`). | `ValidationService` throws `IllegalArgumentException`; Swing warning dialog prevents invalid database insert. | PASS |
| **TC-09** | **Reports & Aggregate Calculations** | Open `ReportPanel` for `Inventory Management -> Products`. Select `Unit Price` and `Quantity`. | Displays accurate SQL `COUNT`, `Sum`, `Average`, `Min`, `Max`, and `Total Inventory Value = SUM(Unit Price * Quantity)`. | PASS |
| **TC-10** | **Multi-User Data Isolation** | Log in as `admin` (User #1) vs. `student_demo` (User #2). | `ApplicationDAO.findByUserId(userId)` ensures User #2 only sees their own private applications and cannot access User #1's data. | PASS |
