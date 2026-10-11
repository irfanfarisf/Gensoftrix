package com.gensoftrix.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages JDBC connections to the MySQL "gensoftrix" database.
 * Reads credentials from environment variables or src/main/resources/db.properties.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/gensoftrix?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Could not load db.properties, falling back to environment/defaults: " + e.getMessage());
        }

        String envUrl = System.getenv("GENSOFTRIX_DB_URL");
        String envUser = System.getenv("GENSOFTRIX_DB_USER");
        String envPass = System.getenv("GENSOFTRIX_DB_PASSWORD");

        url = (envUrl != null && !envUrl.isBlank()) ? envUrl : props.getProperty("db.url", DEFAULT_URL);
        user = (envUser != null && !envUser.isBlank()) ? envUser : props.getProperty("db.user", DEFAULT_USER);
        password = (envPass != null) ? envPass : props.getProperty("db.password", DEFAULT_PASSWORD);
    }

    /**
     * Opens and returns a new JDBC Connection to the MySQL database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Ensures required tables exist on startup without blocking the Swing Event Dispatch Thread.
     */
    public static void initializeSchemaAsync() {
        Thread initThread = new Thread(() -> {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(60) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS applications (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL,
                        name VARCHAR(120) NOT NULL,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS modules (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        application_id INT NOT NULL,
                        module_identifier VARCHAR(64) NOT NULL,
                        name VARCHAR(120) NOT NULL,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_modules_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
                        UNIQUE KEY uk_module_app_identifier (application_id, module_identifier)
                    ) ENGINE=InnoDB
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS fields (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        module_id INT NOT NULL,
                        field_name VARCHAR(100) NOT NULL,
                        field_type VARCHAR(30) NOT NULL,
                        required BOOLEAN NOT NULL DEFAULT FALSE,
                        default_value VARCHAR(255) DEFAULT NULL,
                        options_csv TEXT DEFAULT NULL,
                        reference_module_id INT DEFAULT NULL,
                        display_order INT NOT NULL DEFAULT 1,
                        CONSTRAINT fk_fields_module FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
                        CONSTRAINT fk_fields_ref_module FOREIGN KEY (reference_module_id) REFERENCES modules(id) ON DELETE SET NULL,
                        UNIQUE KEY uk_field_module_name (module_id, field_name)
                    ) ENGINE=InnoDB
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS records (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        module_id INT NOT NULL,
                        record_data JSON NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_records_module FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS business_rules (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        module_id INT NOT NULL,
                        field_id INT DEFAULT NULL,
                        rule_type VARCHAR(40) NOT NULL,
                        rule_value VARCHAR(255) NOT NULL,
                        enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        CONSTRAINT fk_rules_module FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
                        CONSTRAINT fk_rules_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);

            } catch (SQLException e) {
                System.err.println("Database initialization warning: " + e.getMessage());
            }
        }, "GenSoftrix-DB-Init");
        initThread.setDaemon(true);
        initThread.start();
    }
}
