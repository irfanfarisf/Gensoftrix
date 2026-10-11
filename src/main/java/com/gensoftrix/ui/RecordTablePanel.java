package com.gensoftrix.ui;

import com.gensoftrix.model.Application;
import com.gensoftrix.model.BusinessRule;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.Module;
import com.gensoftrix.model.RecordData;
import com.gensoftrix.service.ApplicationService;
import com.gensoftrix.service.TableGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays dynamic module records in a JTable generated from FieldDefinitions.
 * Supports Add, View, Edit, Delete, Refresh, and live Search.
 */
public class RecordTablePanel extends JPanel {

    private final DashboardFrame dashboard;
    private final ApplicationService appService;
    private final TableGenerator tableGenerator = new TableGenerator();

    private Application currentApplication;
    private Module currentModule;
    private List<FieldDefinition> fields = new ArrayList<>();
    private List<BusinessRule> rules = new ArrayList<>();
    private List<RecordData> records = new ArrayList<>();
    private Map<Integer, List<String>> referenceLookup = new HashMap<>();

    private final JLabel headerTitle = new JLabel("Dynamic Records Table");
    private final JTextField searchField = new JTextField(20);
    private final JTable table = new JTable();

    public RecordTablePanel(DashboardFrame dashboard, ApplicationService appService) {
        this.dashboard = dashboard;
        this.appService = appService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.CANVAS_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout(0, 12));
        topBar.setOpaque(false);

        headerTitle.setFont(UITheme.FONT_TITLE);
        topBar.add(headerTitle, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);

        controls.add(new JLabel("Search:"));
        controls.add(searchField);

        JButton addBtn = UITheme.createPrimaryButton("Add Record");
        JButton viewBtn = UITheme.createSecondaryButton("View");
        JButton editBtn = UITheme.createSecondaryButton("Edit");
        JButton deleteBtn = UITheme.createDangerButton("Delete");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        JButton configureFieldsBtn = UITheme.createSecondaryButton("Fields & Rules");

        controls.add(addBtn);
        controls.add(viewBtn);
        controls.add(editBtn);
        controls.add(deleteBtn);
        controls.add(refreshBtn);
        controls.add(configureFieldsBtn);

        topBar.add(controls, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        addBtn.addActionListener(e -> openFormDialog(null));
        viewBtn.addActionListener(e -> viewSelectedRecord());
        editBtn.addActionListener(e -> {
            RecordData selected = getSelectedRecord();
            if (selected != null) {
                openFormDialog(selected);
            }
        });
        deleteBtn.addActionListener(e -> deleteSelectedRecord());
        refreshBtn.addActionListener(e -> loadModuleRecords());
        configureFieldsBtn.addActionListener(e -> {
            if (currentApplication != null && currentModule != null) {
                dashboard.openFieldsForModule(currentApplication, currentModule);
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuildTableModel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuildTableModel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuildTableModel();
            }
        });
    }

    public void setContext(Application app, Module module) {
        this.currentApplication = app;
        this.currentModule = module;
        headerTitle.setText(module.getName() + " Records (" + app.getName() + ")");
        loadModuleRecords();
    }

    public void loadModuleRecords() {
        if (currentModule == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                fields = appService.getFields(currentModule.getId());
                rules = appService.getBusinessRules(currentModule.getId());
                records = appService.getRecords(currentModule.getId());

                referenceLookup.clear();
                for (FieldDefinition fd : fields) {
                    if ("REFERENCE".equalsIgnoreCase(fd.getFieldType()) && fd.getReferenceModuleId() != null) {
                        int targetModId = fd.getReferenceModuleId();
                        referenceLookup.put(targetModId, appService.getReferenceOptions(targetModId));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    rebuildTableModel();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RecordTablePanel.this, "Failed to load records: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void rebuildTableModel() {
        DefaultTableModel model = tableGenerator.buildTableModel(fields, records, searchField.getText());
        table.setModel(model);
        UITheme.styleTable(table);
    }

    private RecordData getSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a record from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Object idObj = table.getValueAt(viewRow, 0);
        if (idObj instanceof Integer recordId) {
            for (RecordData r : records) {
                if (r.getId() == recordId) {
                    return r;
                }
            }
        }
        return null;
    }

    private void openFormDialog(RecordData editingRecord) {
        if (currentModule == null) return;
        if (fields.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please define fields for this module first in the Field Builder.");
            return;
        }
        DynamicFormDialog dialog = new DynamicFormDialog(
                dashboard,
                appService,
                currentModule,
                fields,
                rules,
                records,
                referenceLookup,
                editingRecord,
                () -> {
                    loadModuleRecords();
                    dashboard.refreshDashboardMetrics();
                }
        );
        dialog.setVisible(true);
    }

    private void viewSelectedRecord() {
        RecordData record = getSelectedRecord();
        if (record == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Record ID: #").append(record.getId()).append("\n");
        sb.append("----------------------------------------\n");
        for (FieldDefinition f : fields) {
            sb.append(f.getFieldName()).append(": ")
              .append(record.getValue(f.getFieldName())).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 12, 36);
        area.setEditable(false);
        area.setFont(UITheme.FONT_MONO);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Record Details — #" + record.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedRecord() {
        RecordData record = getSelectedRecord();
        if (record == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete Record #" + record.getId() + " from " + currentModule.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appService.deleteRecord(record.getId());
                    return null;
                }

                @Override
                protected void done() {
                    loadModuleRecords();
                    dashboard.refreshDashboardMetrics();
                }
            };
            worker.execute();
        }
    }
}