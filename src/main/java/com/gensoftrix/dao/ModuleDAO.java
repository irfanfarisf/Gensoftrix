package com.gensoftrix.dao;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.model.Module;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the "modules" table.
 */
public class ModuleDAO {

    public List<Module> findByApplicationId(int applicationId) throws SQLException {
        String sql = "SELECT id, application_id, module_identifier, name, description, created_at FROM modules WHERE application_id = ? ORDER BY id ASC";
        List<Module> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Module findById(int moduleId) throws SQLException {
        String sql = "SELECT id, application_id, module_identifier, name, description, created_at FROM modules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Module create(int applicationId, String moduleIdentifier, String name, String description) throws SQLException {
        String sql = "INSERT INTO modules (application_id, module_identifier, name, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, applicationId);
            ps.setString(2, moduleIdentifier);
            ps.setString(3, name);
            ps.setString(4, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Module(
                            keys.getInt(1),
                            applicationId,
                            moduleIdentifier,
                            name,
                            description,
                            new Timestamp(System.currentTimeMillis())
                    );
                }
            }
        }
        throw new SQLException("Failed to create module.");
    }

    public void update(int id, String moduleIdentifier, String name, String description) throws SQLException {
        String sql = "UPDATE modules SET module_identifier = ?, name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, moduleIdentifier);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM modules WHERE id = ?";
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
        String sql = """
            SELECT COUNT(m.id)
            FROM modules m
            INNER JOIN applications a ON m.application_id = a.id
            WHERE a.user_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Module mapRow(ResultSet rs) throws SQLException {
        return new Module(
                rs.getInt("id"),
                rs.getInt("application_id"),
                rs.getString("module_identifier"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getTimestamp("created_at")
        );
    }
}
