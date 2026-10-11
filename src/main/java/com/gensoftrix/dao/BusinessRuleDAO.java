package com.gensoftrix.dao;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.model.BusinessRule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the "business_rules" table.
 */
public class BusinessRuleDAO {

    public List<BusinessRule> findByModuleId(int moduleId) throws SQLException {
        String sql = """
            SELECT br.id, br.module_id, br.field_id, f.field_name, br.rule_type, br.rule_value, br.enabled
            FROM business_rules br
            LEFT JOIN fields f ON br.field_id = f.id
            WHERE br.module_id = ?
            ORDER BY br.id ASC
        """;
        List<BusinessRule> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int fId = rs.getInt("field_id");
                    Integer fieldId = rs.wasNull() ? null : fId;
                    list.add(new BusinessRule(
                            rs.getInt("id"),
                            rs.getInt("module_id"),
                            fieldId,
                            rs.getString("field_name"),
                            rs.getString("rule_type"),
                            rs.getString("rule_value"),
                            rs.getBoolean("enabled")
                    ));
                }
            }
        }
        return list;
    }

    public BusinessRule create(BusinessRule rule) throws SQLException {
        String sql = "INSERT INTO business_rules (module_id, field_id, rule_type, rule_value, enabled) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rule.getModuleId());
            if (rule.getFieldId() != null) {
                ps.setInt(2, rule.getFieldId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, rule.getRuleType());
            ps.setString(4, rule.getRuleValue());
            ps.setBoolean(5, rule.isEnabled());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    rule.setId(keys.getInt(1));
                    return rule;
                }
            }
        }
        throw new SQLException("Failed to create business rule.");
    }

    public void setEnabled(int ruleId, boolean enabled) throws SQLException {
        String sql = "UPDATE business_rules SET enabled = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setInt(2, ruleId);
            ps.executeUpdate();
        }
    }

    public void delete(int ruleId) throws SQLException {
        String sql = "DELETE FROM business_rules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ruleId);
            ps.executeUpdate();
        }
    }
}
