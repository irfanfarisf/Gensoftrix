package com.gensoftrix.ui;

import com.gensoftrix.model.BusinessRule;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.Module;
import com.gensoftrix.model.RecordData;
import com.gensoftrix.service.ApplicationService;
import com.gensoftrix.service.FormGenerator;
import com.gensoftrix.service.ValidationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Module 6: Embeddable Dynamic Form JPanel that generates Swing input controls
 * (JTextField, JComboBox, JCheckBox) from stored FieldDefinitions and provides
 * Save and Clear buttons.
 */
public class DynamicFormPanel extends JPanel {

    private final ApplicationService appService;
    private final ValidationService validationService = new ValidationService();
    private final FormGenerator formGenerator = new FormGenerator();

    private Module module;
    private List<FieldDefinition> fields;
    private List<BusinessRule> rules;
    private List<RecordData> existingRecords;
    private Map<Integer, List<String>> referenceLookup;
    private RecordData editingRecord;
    private Runnable onSavedCallback;

    private FormGenerator.GeneratedForm generatedForm;
    private final JPanel formContainer = new JPanel(new BorderLayout());

    public DynamicFormPanel(ApplicationService appService) {
        this.appService = appService;
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)
        ));

        add(formContainer, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);
        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        JButton saveBtn = UITheme.createPrimaryButton("Save");
        buttonBar.add(clearBtn);
        buttonBar.add(saveBtn);
        add(buttonBar, BorderLayout.SOUTH);

        clearBtn.addActionListener(e -> {
            if (generatedForm != null) {
                generatedForm.clearValues();
            }
        });
        saveBtn.addActionListener(e -> saveCurrentForm(saveBtn));
    }

    public void bindModule(Module module,
                           List<FieldDefinition> fields,
                           List<BusinessRule> rules,
                           List<RecordData> existingRecords,
                           Map<Integer, List<String>> referenceLookup,
                           RecordData editingRecord,
                           Runnable onSavedCallback) {
        this.module = module;
        this.fields = fields;
        this.rules = rules;
        this.existingRecords = existingRecords;
        this.referenceLookup = referenceLookup;
        this.editingRecord = editingRecord;
        this.onSavedCallback = onSavedCallback;

        formContainer.removeAll();
        Map<String, String> initVals = editingRecord != null ? editingRecord.getValues() : null;
        this.generatedForm = formGenerator.buildForm(fields, initVals, referenceLookup);
        formContainer.add(new JScrollPane(generatedForm.getFormPanel()), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void saveCurrentForm(JButton saveBtn) {
        if (module == null || generatedForm == null) return;
        Map<String, String> values = generatedForm.extractValues();
        Integer editingId = editingRecord != null ? editingRecord.getId() : null;

        try {
            validationService.processAndValidate(fields, rules, existingRecords, referenceLookup, values, editingId);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveBtn.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (editingRecord == null) {
                    appService.createRecord(module.getId(), values);
                } else {
                    appService.updateRecord(editingRecord.getId(), values);
                }
                return null;
            }

            @Override
            protected void done() {
                saveBtn.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(DynamicFormPanel.this, "Record saved to MySQL successfully.");
                    if (onSavedCallback != null) {
                        onSavedCallback.run();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DynamicFormPanel.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
