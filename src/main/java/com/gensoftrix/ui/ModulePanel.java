package com.gensoftrix.ui;

import com.gensoftrix.model.Application;
import com.gensoftrix.model.Module;
import com.gensoftrix.service.ApplicationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Allows users to manage Modules inside a selected Application (e.g., Products, Suppliers, Orders).
 */
public class ModulePanel extends JPanel {

    private final DashboardFrame dashboard;
    private final ApplicationService appService;

    private Application currentApplication;
    private List<Module> modules = new ArrayList<>();

    private final JLabel headerTitle = new JLabel("Module Builder");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Identifier", "Module Name", "Description", "Created At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public ModulePanel(DashboardFrame dashboard, ApplicationService appService) {
        this.dashboard = dashboard;
        this.appService = appService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.CANVAS_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        headerTitle.setFont(UITheme.FONT_TITLE);
        topBar.add(headerTitle, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton addBtn = UITheme.createPrimaryButton("New Module");
        JButton fieldsBtn = UITheme.createSecondaryButton("Configure Fields & Rules");
        JButton recordsBtn = UITheme.createSecondaryButton("Open Records Table");
        JButton editBtn = UITheme.createSecondaryButton("Edit");
        JButton deleteBtn = UITheme.createDangerButton("Delete");

        actions.add(addBtn);
        actions.add(fieldsBtn);
        actions.add(recordsBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);
        topBar.add(actions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        addBtn.addActionListener(e -> promptCreateModule());
        fieldsBtn.addActionListener(e -> {
            Module selected = getSelectedModule();
            if (selected != null) {
                dashboard.openFieldsForModule(currentApplication, selected);
            }
        });
        recordsBtn.addActionListener(e -> {
            Module selected = getSelectedModule();
            if (selected != null) {
                dashboard.openRecordsForModule(currentApplication, selected);
            }
        });
        editBtn.addActionListener(e -> promptEditModule());
        deleteBtn.addActionListener(e -> deleteSelectedModule());
    }

    public void setCurrentApplication(Application app) {
        this.currentApplication = app;
        headerTitle.setText("Modules — " + app.getName());
        loadModules();
    }

    public void loadModules() {
        if (currentApplication == null) return;
        SwingWorker<List<Module>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Module> doInBackground() throws Exception {
                return appService.getModules(currentApplication.getId());
            }

            @Override
            protected void done() {
                try {
                    modules = get();
                    tableModel.setRowCount(0);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    for (Module m : modules) {
                        tableModel.addRow(new Object[]{
                                m.getId(),
                                m.getModuleIdentifier(),
                                m.getName(),
                                m.getDescription(),
                                m.getCreatedAt() != null ? fmt.format(m.getCreatedAt()) : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ModulePanel.this, "Error loading modules: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private Module getSelectedModule() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= modules.size()) {
            JOptionPane.showMessageDialog(this, "Please select a module from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return modules.get(row);
    }

    private void promptCreateModule() {
        if (currentApplication == null) {
            JOptionPane.showMessageDialog(this, "Select an application first in Application Manager.", "No Application Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField nameField = new JTextField(22);
        JTextField idField = new JTextField("MOD_", 22);
        JTextArea descArea = new JTextArea(3, 22);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Module Name *:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Module Identifier *:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        int res = JOptionPane.showConfirmDialog(this, panel, "Create Module", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            SwingWorker<Module, Void> worker = new SwingWorker<>() {
                @Override
                protected Module doInBackground() throws Exception {
                    return appService.createModule(
                            currentApplication.getId(),
                            idField.getText(),
                            nameField.getText(),
                            descArea.getText()
                    );
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadModules();
                        dashboard.refreshDashboardMetrics();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ModulePanel.this,
                                ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void promptEditModule() {
        Module selected = getSelectedModule();
        if (selected == null) return;

        JTextField nameField = new JTextField(selected.getName(), 22);
        JTextField idField = new JTextField(selected.getModuleIdentifier(), 22);
        JTextArea descArea = new JTextArea(selected.getDescription(), 3, 22);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Module Name *:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Module Identifier *:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Module", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appService.updateModule(selected.getId(), idField.getText(), nameField.getText(), descArea.getText());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadModules();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ModulePanel.this, "Update error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private void deleteSelectedModule() {
        Module selected = getSelectedModule();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete module '" + selected.getName() + "' and all its field definitions and saved records?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appService.deleteModule(selected.getId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadModules();
                        dashboard.refreshDashboardMetrics();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ModulePanel.this, "Delete error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }
}
