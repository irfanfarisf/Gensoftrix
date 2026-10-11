package com.gensoftrix.dao;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the "applications" table.
 */
public class ApplicationDAO {

    public List<Application> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, name, description, created_at FROM applications WHERE user_id = ? ORDER BY created_at DESC";
        List<Application> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Application(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public Application create(int userId, String name, String description) throws SQLException {
        String sql = "INSERT INTO applications (user_id, name, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Application(
                            keys.getInt(1),
                            userId,
                            name,
                            description,
                            new Timestamp(System.currentTimeMillis())
                    );
                }
            }
        }
        throw new SQLException("Failed to create application.");
    }

    public void update(int id, String name, String description) throws SQLException {
        String sql = "UPDATE applications SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes an application and cascades to its modules, fields, rules, and records within a transaction.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM applications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
