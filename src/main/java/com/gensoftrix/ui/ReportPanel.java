package com.gensoftrix.ui;

import com.gensoftrix.model.Application;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.Module;
import com.gensoftrix.service.ApplicationService;
import com.gensoftrix.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Module 9: Reports and Calculations Panel.
 * Displays:
 *  - Record counts for each module in the selected application
 *  - Sum, Average, Minimum, and Maximum of a selected numeric field
 *  - Cross-field calculation (e.g., Total Inventory Value = SUM(Unit Price * Quantity))
 */
public class ReportPanel extends JPanel {

    private final ApplicationService appService;
    private final ReportService reportService = new ReportService();

    private Application currentApplication;
    private Module currentModule;
    private List<FieldDefinition> numericFields = List.of();

    private final JLabel headerTitle = new JLabel("Reports & Calculations");
    private final JComboBox<FieldDefinition> fieldSelector = new JComboBox<>();

    private final JLabel countValueLabel = new JLabel("0");
    private final JLabel sumValueLabel = new JLabel("0.00");
    private final JLabel avgValueLabel = new JLabel("0.00");
    private final JLabel minMaxValueLabel = new JLabel("0.00 / 0.00");
    private final JLabel productTotalLabel = new JLabel("N/A");

    private final DefaultTableModel moduleCountsModel = new DefaultTableModel(
            new Object[]{"Module Name", "Total Records (SQL COUNT)"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ReportPanel(ApplicationService appService) {
        this.appService = appService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.CANVAS_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Header + Field Selector
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);
        headerTitle.setFont(UITheme.FONT_TITLE);
        topBar.add(headerTitle, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        controls.add(new JLabel("Numeric Field:"));
        controls.add(fieldSelector);
        JButton calcBtn = UITheme.createPrimaryButton("Calculate Aggregates");
        controls.add(calcBtn);
        topBar.add(controls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center: KPI Aggregate Cards + Module Record Counts Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        statsGrid.setOpaque(false);
        statsGrid.add(createStatCard("Numeric Records", countValueLabel));
        statsGrid.add(createStatCard("Sum (Total)", sumValueLabel));
        statsGrid.add(createStatCard("Average (Mean)", avgValueLabel));
        statsGrid.add(createStatCard("Min / Max", minMaxValueLabel));
        statsGrid.add(createStatCard("Calculated Inventory Value", productTotalLabel));
        centerPanel.add(statsGrid, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(UITheme.CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)
        ));
        JLabel tableTitle = new JLabel("Record Counts by Module (SQL GROUP BY Aggregate)");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        JTable countsTable = new JTable(moduleCountsModel);
        UITheme.styleTable(countsTable);
        tableCard.add(new JScrollPane(countsTable), BorderLayout.CENTER);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        calcBtn.addActionListener(e -> computeSelectedFieldStats());
        fieldSelector.addActionListener(e -> computeSelectedFieldStats());
    }

    private JPanel createStatCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MUTED);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        valueLabel.setForeground(UITheme.PRIMARY_ACCENT);
        card.add(lbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void setContext(Application app, Module module) {
        this.currentApplication = app;
        this.currentModule = module;
        if (app != null && module != null) {
            headerTitle.setText("Reports & Calculations — " + app.getName() + " / " + module.getName());
        }
        loadReportMetadata();
    }

    public void loadReportMetadata() {
        if (currentApplication == null || currentModule == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> moduleCounts;
            private double crossProductTotal = 0.0;

            @Override
            protected Void doInBackground() throws Exception {
                moduleCounts = reportService.getModuleRecordCounts(currentApplication.getId());
                numericFields = reportService.getNumericFields(currentModule.getId());
                if (numericFields.size() >= 2) {
                    crossProductTotal = reportService.calculateProductSum(
                            currentModule.getId(),
                            numericFields.get(0).getFieldName(),
                            numericFields.get(1).getFieldName()
                    );
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    moduleCountsModel.setRowCount(0);
                    for (Map.Entry<String, Integer> entry : moduleCounts.entrySet()) {
                        moduleCountsModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                    }

                    fieldSelector.removeAllItems();
                    for (FieldDefinition fd : numericFields) {
                        fieldSelector.addItem(fd);
                    }

                    if (numericFields.size() >= 2) {
                        productTotalLabel.setText(String.format(Locale.US, "%.2f", crossProductTotal));
                    } else {
                        productTotalLabel.setText("N/A");
                    }

                    computeSelectedFieldStats();
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void computeSelectedFieldStats() {
        FieldDefinition selected = (FieldDefinition) fieldSelector.getSelectedItem();
        if (currentModule == null || selected == null) {
            countValueLabel.setText("0");
            sumValueLabel.setText("0.00");
            avgValueLabel.setText("0.00");
            minMaxValueLabel.setText("0.00 / 0.00");
            return;
        }

        SwingWorker<ReportService.NumericFieldStats, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportService.NumericFieldStats doInBackground() throws Exception {
                return reportService.calculateNumericFieldStats(currentModule.getId(), selected.getFieldName());
            }

            @Override
            protected void done() {
                try {
                    ReportService.NumericFieldStats stats = get();
                    countValueLabel.setText(String.valueOf(stats.getCount()));
                    sumValueLabel.setText(String.format(Locale.US, "%.2f", stats.getSum()));
                    avgValueLabel.setText(String.format(Locale.US, "%.2f", stats.getAverage()));
                    minMaxValueLabel.setText(String.format(Locale.US, "%.2f / %.2f", stats.getMin(), stats.getMax()));
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }
}
