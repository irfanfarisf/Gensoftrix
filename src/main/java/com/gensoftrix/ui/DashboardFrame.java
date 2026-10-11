package com.gensoftrix.ui;

import com.gensoftrix.model.Application;
import com.gensoftrix.model.Module;
import com.gensoftrix.model.User;
import com.gensoftrix.service.ApplicationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Module 2: Main Dashboard using JFrame and JPanel.
 * Displays Total Applications, Total Modules, Total Records, and Recently Created Applications.
 * Provides navigation buttons for: My Applications, Module Builder, Field Builder, Records, Reports, and Logout.
 */
public class DashboardFrame extends JFrame {

    public static final String VIEW_DASHBOARD = "DASHBOARD";
    public static final String VIEW_APPLICATIONS = "APPLICATIONS";
    public static final String VIEW_MODULES = "MODULES";
    public static final String VIEW_FIELDS = "FIELDS";
    public static final String VIEW_RECORDS = "RECORDS";
    public static final String VIEW_REPORTS = "REPORTS";

    private final User currentUser;
    private final ApplicationService appService = new ApplicationService();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);

    // Dashboard summary metrics
    private final JLabel totalAppsLabel = new JLabel("0");
    private final JLabel totalModulesLabel = new JLabel("0");
    private final JLabel totalRecordsLabel = new JLabel("0");
    private final DefaultTableModel recentAppsModel = new DefaultTableModel(
            new Object[]{"ID", "Application Name", "Description", "Creation Date"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // Sub-panels
    private final ApplicationPanel applicationPanel;
    private final ModulePanel modulePanel;
    private final FieldBuilderPanel fieldBuilderPanel;
    private final RecordTablePanel recordTablePanel;
    private final ReportPanel reportPanel;

    private Application activeApp;
    private Module activeModule;

    // Back-navigation history
    private static final class NavState {
        final String view;
        final Application app;
        final Module module;

        NavState(String view, Application app, Module module) {
            this.view = view;
            this.app = app;
            this.module = module;
        }
    }

    private final Deque<NavState> backStack = new ArrayDeque<>();
    private String currentView = VIEW_DASHBOARD;
    private Application shownApp;
    private Module shownModule;
    private JButton backBtn;

    public DashboardFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("GenSoftrix – Customizable Management Software Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

        this.applicationPanel = new ApplicationPanel(this, appService, currentUser);
        this.modulePanel = new ModulePanel(this, appService);
        this.fieldBuilderPanel = new FieldBuilderPanel(this, appService);
        this.recordTablePanel = new RecordTablePanel(this, appService);
        this.reportPanel = new ReportPanel(appService);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopHeader(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);

        contentCards.add(buildDashboardOverviewPanel(), VIEW_DASHBOARD);
        contentCards.add(applicationPanel, VIEW_APPLICATIONS);
        contentCards.add(modulePanel, VIEW_MODULES);
        contentCards.add(fieldBuilderPanel, VIEW_FIELDS);
        contentCards.add(recordTablePanel, VIEW_RECORDS);
        contentCards.add(reportPanel, VIEW_REPORTS);

        root.add(contentCards, BorderLayout.CENTER);
        setContentPane(root);

        refreshDashboardMetrics();
    }

    private JPanel buildTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_ACCENT);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel logoLabel = new JLabel("GenSoftrix – Customizable Management Software Generator");
        logoLabel.setFont(UITheme.FONT_SUBTITLE);
        logoLabel.setForeground(Color.WHITE);

        backBtn = UITheme.createSecondaryButton("\u2190 Back");
        backBtn.setEnabled(false);
        backBtn.addActionListener(e -> goBack());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(backBtn);
        left.add(logoLabel);
        header.add(left, BorderLayout.WEST);

        JLabel userLabel = new JLabel("Logged in: " + currentUser.getUsername());
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(Color.WHITE);
        header.add(userLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        sidebar.add(createNavButton("Dashboard", () -> {
            refreshDashboardMetrics();
            showView(VIEW_DASHBOARD);
        }));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("My Applications", () -> {
            applicationPanel.loadApplications();
            showView(VIEW_APPLICATIONS);
        }));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("Module Builder", () -> showView(VIEW_MODULES)));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("Field Builder", () -> showView(VIEW_FIELDS)));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("Records", () -> showView(VIEW_RECORDS)));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("Reports", () -> {
            if (activeApp != null && activeModule != null) {
                reportPanel.setContext(activeApp, activeModule);
            }
            showView(VIEW_REPORTS);
        }));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(createNavButton("Logout", this::performLogout));
        return sidebar;
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private JButton createNavButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.FONT_BUTTON);
        btn.setForeground(UITheme.SIDEBAR_TEXT);
        btn.setBackground(UITheme.SIDEBAR_ACTIVE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JPanel buildDashboardOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.CANVAS_BG);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top KPI Row + Quick Actions
        JPanel topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setOpaque(false);

        JPanel kpiGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.add(createMetricCard("Total Applications Created", totalAppsLabel));
        kpiGrid.add(createMetricCard("Total Modules", totalModulesLabel));
        kpiGrid.add(createMetricCard("Total Records", totalRecordsLabel));
        topSection.add(kpiGrid, BorderLayout.CENTER);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quickActions.setOpaque(false);
        JButton newAppBtn = UITheme.createPrimaryButton("Create Application");
        newAppBtn.addActionListener(e -> {
            applicationPanel.loadApplications();
            showView(VIEW_APPLICATIONS);
            applicationPanel.promptCreateApplication();
        });
        JButton reportsBtn = UITheme.createSecondaryButton("Open Reports & Calculations");
        reportsBtn.addActionListener(e -> {
            if (activeApp != null && activeModule != null) {
                reportPanel.setContext(activeApp, activeModule);
            }
            showView(VIEW_REPORTS);
        });

        quickActions.add(newAppBtn);
        quickActions.add(reportsBtn);
        topSection.add(quickActions, BorderLayout.SOUTH);

        panel.add(topSection, BorderLayout.NORTH);

        // Recent Applications Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UITheme.CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel recentTitle = new JLabel("Recently Created Applications");
        recentTitle.setFont(UITheme.FONT_SUBTITLE);
        tableCard.add(recentTitle, BorderLayout.NORTH);

        JTable recentTable = new JTable(recentAppsModel);
        UITheme.styleTable(recentTable);
        tableCard.add(new JScrollPane(recentTable), BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(18, 20, 18, 20)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BODY);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        valueLabel.setForeground(UITheme.PRIMARY_ACCENT);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshDashboardMetrics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int appsCount;
            private int modulesCount;
            private int recordsCount;
            private List<Application> recentApps;

            @Override
            protected Void doInBackground() throws Exception {
                int userId = currentUser.getId();
                appsCount = appService.getTotalApplications(userId);
                modulesCount = appService.getTotalModules(userId);
                recordsCount = appService.getTotalRecords(userId);
                recentApps = appService.getApplications(userId);
                if (activeApp == null && !recentApps.isEmpty()) {
                    activeApp = recentApps.get(0);
                    List<Module> mods = appService.getModules(activeApp.getId());
                    if (!mods.isEmpty()) {
                        activeModule = mods.get(0);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    totalAppsLabel.setText(String.valueOf(appsCount));
                    totalModulesLabel.setText(String.valueOf(modulesCount));
                    totalRecordsLabel.setText(String.valueOf(recordsCount));

                    recentAppsModel.setRowCount(0);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    if (recentApps != null) {
                        for (Application app : recentApps) {
                            recentAppsModel.addRow(new Object[]{
                                    app.getId(),
                                    app.getName(),
                                    app.getDescription(),
                                    app.getCreatedAt() != null ? fmt.format(app.getCreatedAt()) : ""
                            });
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    public void showView(String viewName) {
        if (!viewName.equals(currentView)) {
            backStack.push(new NavState(currentView, shownApp, shownModule));
        }
        currentView = viewName;
        shownApp = activeApp;
        shownModule = activeModule;
        cardLayout.show(contentCards, viewName);
        updateBackButton();
    }

    /** Returns to the previously visited page, restoring its application/module context. */
    private void goBack() {
        if (backStack.isEmpty()) {
            return;
        }
        NavState prev = backStack.pop();
        activeApp = prev.app;
        activeModule = prev.module;

        switch (prev.view) {
            case VIEW_DASHBOARD:
                refreshDashboardMetrics();
                break;
            case VIEW_APPLICATIONS:
                applicationPanel.loadApplications();
                break;
            case VIEW_MODULES:
                if (prev.app != null) {
                    modulePanel.setCurrentApplication(prev.app);
                }
                break;
            case VIEW_FIELDS:
                if (prev.app != null && prev.module != null) {
                    fieldBuilderPanel.setContext(prev.app, prev.module);
                }
                break;
            case VIEW_RECORDS:
                if (prev.app != null && prev.module != null) {
                    recordTablePanel.setContext(prev.app, prev.module);
                }
                break;
            case VIEW_REPORTS:
                if (prev.app != null && prev.module != null) {
                    reportPanel.setContext(prev.app, prev.module);
                }
                break;
            default:
                break;
        }

        currentView = prev.view;
        shownApp = prev.app;
        shownModule = prev.module;
        cardLayout.show(contentCards, prev.view);
        updateBackButton();
    }

    private void updateBackButton() {
        if (backBtn != null) {
            backBtn.setEnabled(!backStack.isEmpty());
        }
    }

    public void openModulesForApplication(Application app) {
        this.activeApp = app;
        modulePanel.setCurrentApplication(app);
        showView(VIEW_MODULES);
    }

    public void openFieldsForModule(Application app, Module module) {
        this.activeApp = app;
        this.activeModule = module;
        fieldBuilderPanel.setContext(app, module);
        showView(VIEW_FIELDS);
    }

    public void openRecordsForModule(Application app, Module module) {
        this.activeApp = app;
        this.activeModule = module;
        recordTablePanel.setContext(app, module);
        showView(VIEW_RECORDS);
    }

    public void openReportsForModule(Application app, Module module) {
        this.activeApp = app;
        this.activeModule = module;
        reportPanel.setContext(app, module);
        showView(VIEW_REPORTS);
    }
}