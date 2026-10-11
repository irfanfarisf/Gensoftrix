package com.gensoftrix.service;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.dao.FieldDAO;
import com.gensoftrix.dao.RecordDAO;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.RecordData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 9: Reports and Calculations Service.
 * Uses SQL aggregate functions (COUNT, GROUP BY) and Java numeric aggregation
 * to compute module record counts, Sum, Average, Minimum, Maximum, and Inventory Value
 * (Price * Quantity).
 */
public class ReportService {

    private final RecordDAO recordDAO = new RecordDAO();
    private final FieldDAO fieldDAO = new FieldDAO();

    public static class NumericFieldStats {
        private final String fieldName;
        private final int count;
        private final double sum;
        private final double average;
        private final double min;
        private final double max;

        public NumericFieldStats(String fieldName, int count, double sum, double average, double min, double max) {
            this.fieldName = fieldName;
            this.count = count;
            this.sum = sum;
            this.average = average;
            this.min = min;
            this.max = max;
        }

        public String getFieldName() { return fieldName; }
        public int getCount() { return count; }
        public double getSum() { return sum; }
        public double getAverage() { return average; }
        public double getMin() { return min; }
        public double getMax() { return max; }
    }

    /**
     * Uses SQL COUNT() and GROUP BY to retrieve record counts per module for an application.
     */
    public Map<String, Integer> getModuleRecordCounts(int applicationId) throws SQLException {
        String sql = """
            SELECT m.name AS module_name, COUNT(r.id) AS record_count
            FROM modules m
            LEFT JOIN records r ON m.id = r.module_id
            WHERE m.application_id = ?
            GROUP BY m.id, m.name
            ORDER BY m.id ASC
        """;
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("module_name"), rs.getInt("record_count"));
                }
            }
        }
        return counts;
    }

    /**
     * Computes Sum, Average, Minimum, and Maximum for a selected numeric or decimal field in a module.
     */
    public NumericFieldStats calculateNumericFieldStats(int moduleId, String fieldName) throws SQLException {
        List<RecordData> records = recordDAO.findByModuleId(moduleId);
        List<Double> values = new ArrayList<>();

        for (RecordData record : records) {
            String raw = record.getValue(fieldName);
            if (raw != null && !raw.isBlank()) {
                try {
                    values.add(Double.parseDouble(raw.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (values.isEmpty()) {
            return new NumericFieldStats(fieldName, 0, 0.0, 0.0, 0.0, 0.0);
        }

        double sum = 0.0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (double v : values) {
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }
        double avg = sum / values.size();
        return new NumericFieldStats(fieldName, values.size(), sum, avg, min, max);
    }

    /**
     * Calculates cross-field product sum (e.g., Total Inventory Value = SUM(Price * Quantity)).
     */
    public double calculateProductSum(int moduleId, String firstField, String secondField) throws SQLException {
        List<RecordData> records = recordDAO.findByModuleId(moduleId);
        double total = 0.0;
        for (RecordData record : records) {
            try {
                double a = Double.parseDouble(record.getValue(firstField).trim());
                double b = Double.parseDouble(record.getValue(secondField).trim());
                total += (a * b);
            } catch (Exception ignored) {
            }
        }
        return total;
    }

    /**
     * Returns all numeric/decimal FieldDefinitions for a module.
     */
    public List<FieldDefinition> getNumericFields(int moduleId) throws SQLException {
        List<FieldDefinition> all = fieldDAO.findByModuleId(moduleId);
        List<FieldDefinition> numeric = new ArrayList<>();
        for (FieldDefinition f : all) {
            if ("NUMBER".equalsIgnoreCase(f.getFieldType()) || "DECIMAL".equalsIgnoreCase(f.getFieldType())) {
                numeric.add(f);
            }
        }
        return numeric;
    }
}
