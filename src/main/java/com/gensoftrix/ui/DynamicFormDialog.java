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
 * Modal JDialog that uses FormGenerator to render a dynamic Swing form for adding or editing a Record.
 * Validates inputs and evaluates BusinessRules before saving via ApplicationService.
 */
public class DynamicFormDialog extends JDialog {

    private final ApplicationService appService;
    private final ValidationService validationService = new ValidationService();
    private final FormGenerator formGenerator = new FormGenerator();

    private final Module module;
    private final List<FieldDefinition> fields;
    private final List<BusinessRule> rules;
    private final List<RecordData> existingRecords;
    private final Map<Integer, List<String>> referenceLookup;
    private final RecordData editingRecord;
    private final Runnable onSavedCallback;

    private FormGenerator.GeneratedForm generatedForm;

    public DynamicFormDialog(Frame owner,
                             ApplicationService appService,
                             Module module,
                             List<FieldDefinition> fields,
                             List<BusinessRule> rules,
                             List<RecordData> existingRecords,
                             Map<Integer, List<String>> referenceLookup,
                             RecordData editingRecord,
                             Runnable onSavedCallback) {
        super(owner, (editingRecord == null ? "Add Record — " : "Edit Record #" + editingRecord.getId() + " — ") + module.getName(), true);
        this.appService = appService;
        this.module = module;
        this.fields = fields;
        this.rules = rules;
        this.existingRecords = existingRecords;
        this.referenceLookup = referenceLookup;
        this.editingRecord = editingRecord;
        this.onSavedCallback = onSavedCallback;

        setSize(520, Math.min(680, 180 + fields.size() * 52));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CANVAS_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        Map<String, String> initialValues = editingRecord != null ? editingRecord.getValues() : null;
        this.generatedForm = formGenerator.buildForm(fields, initialValues, referenceLookup);

        JScrollPane scrollPane = new JScrollPane(generatedForm.getFormPanel());
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttonBar.setOpaque(false);

        JButton saveBtn = UITheme.createPrimaryButton("Save");
        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");

        buttonBar.add(clearBtn);
        buttonBar.add(cancelBtn);
        buttonBar.add(saveBtn);
        root.add(buttonBar, BorderLayout.SOUTH);

        setContentPane(root);

        clearBtn.addActionListener(e -> generatedForm.clearValues());
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> handleSave(saveBtn));
    }

    private void handleSave(JButton saveBtn) {
        Map<String, String> values = generatedForm.extractValues();
        Integer editingId = editingRecord != null ? editingRecord.getId() : null;

        try {
            validationService.processAndValidate(
                    fields,
                    rules,
                    existingRecords,
                    referenceLookup,
                    values,
                    editingId
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation & Business Rule Check",
                    JOptionPane.WARNING_MESSAGE
            );
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
                    if (onSavedCallback != null) {
                        onSavedCallback.run();
                    }
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            DynamicFormDialog.this,
                            "Database error while saving record: " + ex.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
}
