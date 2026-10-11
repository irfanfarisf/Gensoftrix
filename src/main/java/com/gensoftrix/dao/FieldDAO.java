package com.gensoftrix.dao;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.model.FieldDefinition;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the "fields" table.
 */
public class FieldDAO {

    public List<FieldDefinition> findByModuleId(int moduleId) throws SQLException {
        String sql = """
            SELECT id, module_id, field_name, field_type, required, default_value,
                   options_csv, reference_module_id, display_order
            FROM fields
            WHERE module_id = ?
            ORDER BY display_order ASC, id ASC
        """;
        List<FieldDefinition> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int refMod = rs.getInt("reference_module_id");
                    Integer referenceModuleId = rs.wasNull() ? null : refMod;
                    list.add(new FieldDefinition(
                            rs.getInt("id"),
                            rs.getInt("module_id"),
                            rs.getString("field_name"),
                            rs.getString("field_type"),
                            rs.getBoolean("required"),
                            rs.getString("default_value"),
                            rs.getString("options_csv"),
                            referenceModuleId,
                            rs.getInt("display_order")
                    ));
                }
            }
        }
        return list;
    }

    public FieldDefinition create(FieldDefinition field) throws SQLException {
        String sql = """
            INSERT INTO fields (module_id, field_name, field_type, required, default_value, options_csv, reference_module_id, display_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, field.getModuleId());
            ps.setString(2, field.getFieldName());
            ps.setString(3, field.getFieldType());
            ps.setBoolean(4, field.isRequired());
            ps.setString(5, field.getDefaultValue());
            ps.setString(6, field.getOptionsCsv());
            if (field.getReferenceModuleId() != null) {
                ps.setInt(7, field.getReferenceModuleId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, field.getDisplayOrder());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    field.setId(keys.getInt(1));
                    return field;
                }
            }
        }
        throw new SQLException("Failed to create field definition.");
    }

    public void update(FieldDefinition field) throws SQLException {
        String sql = """
            UPDATE fields
            SET field_name = ?, field_type = ?, required = ?, default_value = ?,
                options_csv = ?, reference_module_id = ?, display_order = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, field.getFieldName());
            ps.setString(2, field.getFieldType());
            ps.setBoolean(3, field.isRequired());
            ps.setString(4, field.getDefaultValue());
            ps.setString(5, field.getOptionsCsv());
            if (field.getReferenceModuleId() != null) {
                ps.setInt(6, field.getReferenceModuleId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, field.getDisplayOrder());
            ps.setInt(8, field.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int fieldId) throws SQLException {
        String sql = "DELETE FROM fields WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fieldId);
            ps.executeUpdate();
        }
    }
}
