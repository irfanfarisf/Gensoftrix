package com.gensoftrix.ui;

import com.gensoftrix.model.Application;
import com.gensoftrix.model.BusinessRule;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.Module;
import com.gensoftrix.service.ApplicationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows users to define custom Fields (TEXT, NUMBER, DECIMAL, DATE, EMAIL, BOOLEAN, DROPDOWN, REFERENCE)
 * and Business Rules (REQUIRED, MIN_VALUE, MAX_VALUE, UNIQUE, DEFAULT_VALUE, FORMULA) for a Module.
 */
public class FieldBuilderPanel extends JPanel {

    private static final String[] FIELD_TYPES = {
            "TEXT", "NUMBER", "DECIMAL", "DATE", "EMAIL", "BOOLEAN", "DROPDOWN", "REFERENCE"
    };
    private static final String[] RULE_TYPES = {
            "REQUIRED", "MIN_VALUE", "MAX_VALUE", "UNIQUE", "DEFAULT_VALUE", "FORMULA"
    };

    private final DashboardFrame dashboard;
    private final ApplicationService appService;

    private Application currentApplication;
    private Module currentModule;
    private List<FieldDefinition> fields = new ArrayList<>();
    private List<BusinessRule> rules = new ArrayList<>();
    private List<Module> siblingModules = new ArrayList<>();

    private final JLabel headerLabel = new JLabel("Field & Business Rule Builder");

    private final DefaultTableModel fieldsTableModel = new DefaultTableModel(
            new Object[]{"Order", "Field Name", "Type", "Required", "Default Value", "Options / Reference"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable fieldsTable = new JTable(fieldsTableModel);

    private final DefaultTableModel rulesTableModel = new DefaultTableModel(
            new Object[]{"ID", "Target Field", "Rule Type", "Rule Value / Formula", "Enabled"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable rulesTable = new JTable(rulesTableModel);

    public FieldBuilderPanel(DashboardFrame dashboard, ApplicationService appService) {
        this.dashboard = dashboard;
        this.appService = appService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.CANVAS_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        headerLabel.setFont(UITheme.FONT_TITLE);
        topBar.add(headerLabel, BorderLayout.WEST);

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navButtons.setOpaque(false);
        JButton openRecordsBtn = UITheme.createPrimaryButton("Open Dynamic Table & Form");
        openRecordsBtn.addActionListener(e -> {
            if (currentApplication != null && currentModule != null) {
                dashboard.openRecordsForModule(currentApplication, currentModule);
            }
        });
        navButtons.add(openRecordsBtn);
        topBar.add(navButtons, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Split Pane: Top = Fields, Bottom = Business Rules
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildFieldsSection(), buildRulesSection());
        splitPane.setResizeWeight(0.58);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel buildFieldsSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JLabel lbl = new JLabel("Module Fields Metadata");
        lbl.setFont(UITheme.FONT_SUBTITLE);
        bar.add(lbl, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        JButton addFieldBtn = UITheme.createPrimaryButton("Add Field");
        JButton deleteFieldBtn = UITheme.createDangerButton("Remove Field");
        btns.add(addFieldBtn);
        btns.add(deleteFieldBtn);
        bar.add(btns, BorderLayout.EAST);
        panel.add(bar, BorderLayout.NORTH);

        UITheme.styleTable(fieldsTable);
        panel.add(new JScrollPane(fieldsTable), BorderLayout.CENTER);

        addFieldBtn.addActionListener(e -> promptAddField());
        deleteFieldBtn.addActionListener(e -> deleteSelectedField());
        return panel;
    }

    private JPanel buildRulesSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JLabel lbl = new JLabel("Module Business Rules (Validation & Formula Calculation)");
        lbl.setFont(UITheme.FONT_SUBTITLE);
        bar.add(lbl, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        JButton addRuleBtn = UITheme.createSecondaryButton("Add Business Rule");
        JButton toggleRuleBtn = UITheme.createSecondaryButton("Toggle Enabled");
        JButton deleteRuleBtn = UITheme.createDangerButton("Delete Rule");
        btns.add(addRuleBtn);
        btns.add(toggleRuleBtn);
        btns.add(deleteRuleBtn);
        bar.add(btns, BorderLayout.EAST);
        panel.add(bar, BorderLayout.NORTH);

        UITheme.styleTable(rulesTable);
        panel.add(new JScrollPane(rulesTable), BorderLayout.CENTER);

        addRuleBtn.addActionListener(e -> promptAddRule());
        toggleRuleBtn.addActionListener(e -> toggleSelectedRule());
        deleteRuleBtn.addActionListener(e -> deleteSelectedRule());
        return panel;
    }

    public void setContext(Application app, Module module) {
        this.currentApplication = app;
        this.currentModule = module;
        headerLabel.setText("Field & Rule Builder — " + app.getName() + " / " + module.getName());
        loadMetadata();
    }

    public void loadMetadata() {
        if (currentModule == null || currentApplication == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                fields = appService.getFields(currentModule.getId());
                rules = appService.getBusinessRules(currentModule.getId());
                siblingModules = appService.getModules(currentApplication.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    fieldsTableModel.setRowCount(0);
                    for (FieldDefinition f : fields) {
                        String extra = "";
                        if ("DROPDOWN".equalsIgnoreCase(f.getFieldType())) {
                            extra = f.getOptionsCsv() != null ? f.getOptionsCsv() : "";
                        } else if ("REFERENCE".equalsIgnoreCase(f.getFieldType()) && f.getReferenceModuleId() != null) {
                            extra = "Ref Module #" + f.getReferenceModuleId();
                        }
                        fieldsTableModel.addRow(new Object[]{
                                f.getDisplayOrder(),
                                f.getFieldName(),
                                f.getFieldType(),
                                f.isRequired() ? "Required" : "Optional",
                                f.getDefaultValue() != null ? f.getDefaultValue() : "",
                                extra
                        });
                    }

                    rulesTableModel.setRowCount(0);
                    for (BusinessRule r : rules) {
                        rulesTableModel.addRow(new Object[]{
                                r.getId(),
                                r.getFieldName() != null ? r.getFieldName() : "Module-level",
                                r.getRuleType(),
                                r.getRuleValue(),
                                r.isEnabled() ? "Active" : "Disabled"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FieldBuilderPanel.this, "Failed to load fields: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void promptAddField() {
        if (currentModule == null) return;

        JTextField nameField = new JTextField(18);
        JComboBox<String> typeBox = new JComboBox<>(FIELD_TYPES);
        JCheckBox requiredCheck = new JCheckBox("Required Field");
        JTextField defaultField = new JTextField(18);
        JTextField optionsField = new JTextField(18);
        JComboBox<Module> refModuleBox = new JComboBox<>(siblingModules.toArray(new Module[0]));
        JSpinner orderSpinner = new JSpinner(new SpinnerNumberModel(fields.size() + 1, 1, 100, 1));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Field Name *:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Field Type *:"), gbc);
        gbc.gridx = 1; panel.add(typeBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Validation:"), gbc);
        gbc.gridx = 1; panel.add(requiredCheck, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Default Value:"), gbc);
        gbc.gridx = 1; panel.add(defaultField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Dropdown Options (CSV):"), gbc);
        gbc.gridx = 1; panel.add(optionsField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Referenced Module (if REFERENCE):"), gbc);
        gbc.gridx = 1; panel.add(refModuleBox, gbc);

        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Display Order:"), gbc);
        gbc.gridx = 1; panel.add(orderSpinner, gbc);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Field Definition", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String selectedType = (String) typeBox.getSelectedItem();
            Module refMod = (Module) refModuleBox.getSelectedItem();
            Integer refModId = ("REFERENCE".equals(selectedType) && refMod != null) ? refMod.getId() : null;

            FieldDefinition fd = new FieldDefinition(
                    0,
                    currentModule.getId(),
                    nameField.getText().trim(),
                    selectedType,
                    requiredCheck.isSelected(),
                    defaultField.getText().trim(),
                    optionsField.getText().trim(),
                    refModId,
                    (Integer) orderSpinner.getValue()
            );

            SwingWorker<FieldDefinition, Void> worker = new SwingWorker<>() {
                @Override
                protected FieldDefinition doInBackground() throws Exception {
                    return appService.createField(fd);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadMetadata();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(FieldBuilderPanel.this, "Error adding field: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private void deleteSelectedField() {
        int row = fieldsTable.getSelectedRow();
        if (row < 0 || row >= fields.size()) {
            JOptionPane.showMessageDialog(this, "Select a field to remove.");
            return;
        }
        FieldDefinition fd = fields.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove field '" + fd.getFieldName() + "'?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appService.deleteField(fd.getId());
                    return null;
                }

                @Override
                protected void done() {
                    loadMetadata();
                }
            };
            worker.execute();
        }
    }

    private void promptAddRule() {
        if (currentModule == null || fields.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Define at least one field before adding business rules.");
            return;
        }
        JComboBox<FieldDefinition> fieldBox = new JComboBox<>(fields.toArray(new FieldDefinition[0]));
        JComboBox<String> ruleTypeBox = new JComboBox<>(RULE_TYPES);
        JTextField ruleValueField = new JTextField(20);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Target Field *:"), gbc);
        gbc.gridx = 1; panel.add(fieldBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Rule Type *:"), gbc);
        gbc.gridx = 1; panel.add(ruleTypeBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Threshold / Formula *:"), gbc);
        gbc.gridx = 1; panel.add(ruleValueField, gbc);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Business Rule", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            FieldDefinition targetField = (FieldDefinition) fieldBox.getSelectedItem();
            String type = (String) ruleTypeBox.getSelectedItem();
            String val = ruleValueField.getText().trim();

            BusinessRule rule = new BusinessRule(
                    0,
                    currentModule.getId(),
                    targetField != null ? targetField.getId() : null,
                    targetField != null ? targetField.getFieldName() : null,
                    type,
                    val.isEmpty() ? "true" : val,
                    true
            );

            SwingWorker<BusinessRule, Void> worker = new SwingWorker<>() {
                @Override
                protected BusinessRule doInBackground() throws Exception {
                    return appService.createBusinessRule(rule);
                }

                @Override
                protected void done() {
                    loadMetadata();
                }
            };
            worker.execute();
        }
    }

    private void toggleSelectedRule() {
        int row = rulesTable.getSelectedRow();
        if (row < 0 || row >= rules.size()) return;
        BusinessRule rule = rules.get(row);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                appService.toggleBusinessRule(rule.getId(), !rule.isEnabled());
                return null;
            }

            @Override
            protected void done() {
                loadMetadata();
            }
        };
        worker.execute();
    }

    private void deleteSelectedRule() {
        int row = rulesTable.getSelectedRow();
        if (row < 0 || row >= rules.size()) return;
        BusinessRule rule = rules.get(row);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                appService.deleteBusinessRule(rule.getId());
                return null;
            }

            @Override
            protected void done() {
                loadMetadata();
            }
        };
        worker.execute();
    }
}
