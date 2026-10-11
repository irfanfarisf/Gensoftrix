package com.gensoftrix.dao;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.model.RecordData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Data Access Object for the "records" table storing dynamic JSON payloads.
 * Implements CrudDAO<RecordData> (OOP Abstraction) and provides overloaded create/search methods
 * (OOP Method Overloading).
 */
public class RecordDAO implements CrudDAO<RecordData> {

    @Override
    public RecordData findById(int id) throws SQLException {
        String sql = "SELECT id, module_id, record_data, created_at, updated_at FROM records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<RecordData> findAllByParentId(int moduleId) throws SQLException {
        return findByModuleId(moduleId);
    }

    public List<RecordData> findByModuleId(int moduleId) throws SQLException {
        String sql = "SELECT id, module_id, record_data, created_at, updated_at FROM records WHERE module_id = ? ORDER BY id DESC";
        List<RecordData> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Overloaded create method #1: accepts a RecordData entity.
     */
    @Override
    public RecordData create(RecordData record) throws SQLException {
        return create(record.getModuleId(), record.getValues());
    }

    /**
     * Overloaded create method #2: accepts moduleId and field-value map.
     */
    public RecordData create(int moduleId, Map<String, String> values) throws SQLException {
        RecordData record = new RecordData(0, moduleId, values, null, null);
        String sql = "INSERT INTO records (module_id, record_data) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, moduleId);
            ps.setString(2, record.toJsonString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    record.setCreatedAt(now);
                    record.setUpdatedAt(now);
                    return record;
                }
            }
        }
        throw new SQLException("Failed to save record.");
    }

    @Override
    public void update(RecordData entity) throws SQLException {
        update(entity.getId(), entity.getValues());
    }

    public void update(int recordId, Map<String, String> values) throws SQLException {
        RecordData temp = new RecordData(recordId, 0, values, null, null);
        String sql = "UPDATE records SET record_data = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, temp.toJsonString());
            ps.setInt(2, recordId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int recordId) throws SQLException {
        String sql = "DELETE FROM records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            ps.executeUpdate();
        }
    }

    /**
     * Overloaded searchRecords method #1: searches across all fields by keyword.
     */
    public List<RecordData> searchRecords(int moduleId, String keyword) throws SQLException {
        List<RecordData> all = findByModuleId(moduleId);
        if (keyword == null || keyword.isBlank()) {
            return all;
        }
        String q = keyword.trim().toLowerCase(Locale.ROOT);
        List<RecordData> matched = new ArrayList<>();
        for (RecordData r : all) {
            for (String val : r.getValues().values()) {
                if (val != null && val.toLowerCase(Locale.ROOT).contains(q)) {
                    matched.add(r);
                    break;
                }
            }
        }
        return matched;
    }

    /**
     * Overloaded searchRecords method #2: searches a specific field name for a matching value.
     */
    public List<RecordData> searchRecords(int moduleId, String fieldName, String expectedValue) throws SQLException {
        List<RecordData> all = findByModuleId(moduleId);
        if (fieldName == null || expectedValue == null) {
            return all;
        }
        String q = expectedValue.trim().toLowerCase(Locale.ROOT);
        List<RecordData> matched = new ArrayList<>();
        for (RecordData r : all) {
            String val = r.getValue(fieldName);
            if (val != null && val.toLowerCase(Locale.ROOT).contains(q)) {
                matched.add(r);
            }
        }
        return matched;
    }

    public int countByUserId(int userId) throws SQLException {
        String sql = """
            SELECT COUNT(r.id)
            FROM records r
            INNER JOIN modules m ON r.module_id = m.id
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

    private RecordData mapRow(ResultSet rs) throws SQLException {
        Map<String, String> values = RecordData.parseJson(rs.getString("record_data"));
        return new RecordData(
                rs.getInt("id"),
                rs.getInt("module_id"),
                values,
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
}
