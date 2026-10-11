package com.gensoftrix.dao;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.model.User;

import java.sql.*;

/**
 * Data Access Object for the "users" table using JDBC PreparedStatements.
 */
public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    public User create(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new User(id, username, passwordHash, new Timestamp(System.currentTimeMillis()));
                }
            }
        }
        throw new SQLException("Failed to create user record.");
    }
}
