package com.gensoftrix.service;

import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.RecordData;

import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Dynamically builds JTable models from a module's FieldDefinitions and RecordData rows.
 * Supports client-side search across all user-defined fields.
 */
public class TableGenerator {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    public DefaultTableModel buildTableModel(List<FieldDefinition> fields,
                                             List<RecordData> records,
                                             String searchQuery) {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        for (FieldDefinition field : fields) {
            columnNames.add(field.getFieldName());
        }
        columnNames.add("Updated At");

        Vector<Vector<Object>> dataRows = new Vector<>();
        String normalizedQuery = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (RecordData record : records) {
            if (!normalizedQuery.isEmpty() && !matchesSearch(record, fields, normalizedQuery)) {
                continue;
            }

            Vector<Object> row = new Vector<>();
            row.add(record.getId());
            for (FieldDefinition field : fields) {
                row.add(record.getValue(field.getFieldName()));
            }
            row.add(record.getUpdatedAt() != null ? DATE_FMT.format(record.getUpdatedAt()) : "");
            dataRows.add(row);
        }

        return new DefaultTableModel(dataRows, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private boolean matchesSearch(RecordData record, List<FieldDefinition> fields, String query) {
        if (String.valueOf(record.getId()).contains(query)) {
            return true;
        }
        for (FieldDefinition field : fields) {
            String value = record.getValue(field.getFieldName());
            if (value != null && value.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }
}
