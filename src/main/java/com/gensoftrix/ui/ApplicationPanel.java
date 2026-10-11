package com.gensoftrix.ui;

import com.gensoftrix.model.Application;
import com.gensoftrix.model.User;
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
 * Allows users to create, view, edit, delete, and open management Applications.
 */
public class ApplicationPanel extends JPanel {

    private final DashboardFrame dashboard;
    private final ApplicationService appService;
    private final User currentUser;

    private List<Application> applications = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Application Name", "Description", "Created At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public ApplicationPanel(DashboardFrame dashboard, ApplicationService appService, User currentUser) {
        this.dashboard = dashboard;
        this.appService = appService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.CANVAS_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header + Action Buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Application Manager");
        title.setFont(UITheme.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        JButton createBtn = UITheme.createPrimaryButton("New Application");
        JButton openBtn = UITheme.createSecondaryButton("Manage Modules");
        JButton editBtn = UITheme.createSecondaryButton("Edit Details");
        JButton deleteBtn = UITheme.createDangerButton("Delete");

        btnGroup.add(createBtn);
        btnGroup.add(openBtn);
        btnGroup.add(editBtn);
        btnGroup.add(deleteBtn);
        topBar.add(btnGroup, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Table Card
        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        createBtn.addActionListener(e -> promptCreateApplication());
        openBtn.addActionListener(e -> openSelectedApplication());
        editBtn.addActionListener(e -> editSelectedApplication());
        deleteBtn.addActionListener(e -> deleteSelectedApplication());

        loadApplications();
    }

    public void loadApplications() {
        SwingWorker<List<Application>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Application> doInBackground() throws Exception {
                return appService.getApplications(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    applications = get();
                    tableModel.setRowCount(0);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    for (Application app : applications) {
                        tableModel.addRow(new Object[]{
                                app.getId(),
                                app.getName(),
                                app.getDescription(),
                                app.getCreatedAt() != null ? fmt.format(app.getCreatedAt()) : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ApplicationPanel.this,
                            "Failed to load applications: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    public void promptCreateApplication() {
        JTextField nameField = new JTextField(24);
        JTextArea descArea = new JTextArea(4, 24);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Application Name *:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        int res = JOptionPane.showConfirmDialog(
                this, panel, "Create New Management Application",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            SwingWorker<Application, Void> worker = new SwingWorker<>() {
                @Override
                protected Application doInBackground() throws Exception {
                    return appService.createApplication(currentUser.getId(), name, desc);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadApplications();
                        dashboard.refreshDashboardMetrics();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ApplicationPanel.this,
                                ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(),
                                "Validation Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private Application getSelectedApplication() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= applications.size()) {
            JOptionPane.showMessageDialog(this, "Please select an application from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return applications.get(row);
    }

    private void openSelectedApplication() {
        Application selected = getSelectedApplication();
        if (selected != null) {
            dashboard.openModulesForApplication(selected);
        }
    }

    private void editSelectedApplication() {
        Application selected = getSelectedApplication();
        if (selected == null) return;

        JTextField nameField = new JTextField(selected.getName(), 24);
        JTextArea descArea = new JTextArea(selected.getDescription(), 4, 24);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Application Name *:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        int res = JOptionPane.showConfirmDialog(
                this, panel, "Edit Application",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appService.updateApplication(selected.getId(), nameField.getText(), descArea.getText());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadApplications();
                        dashboard.refreshDashboardMetrics();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ApplicationPanel.this,
                                ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(),
                                "Update Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void deleteSelectedApplication() {
        Application selected = getSelectedApplication();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete application '" + selected.getName() + "' and all of its modules, fields, and records?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appService.deleteApplication(selected.getId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadApplications();
                        dashboard.refreshDashboardMetrics();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ApplicationPanel.this,
                                "Delete failed: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }
}
