package com.gensoftrix.service;

import com.gensoftrix.model.FieldDefinition;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamically generates Java Swing form components based on a module's FieldDefinitions.
 * Maps:
 *  - TEXT / EMAIL / DECIMAL / DATE -> JTextField
 *  - NUMBER -> JSpinner
 *  - BOOLEAN -> JCheckBox
 *  - DROPDOWN / REFERENCE -> JComboBox<String>
 */
public class FormGenerator {

    public static class GeneratedForm {
        private final JPanel formPanel;
        private final Map<String, JComponent> inputComponents;
        private final List<FieldDefinition> fields;

        public GeneratedForm(JPanel formPanel, Map<String, JComponent> inputComponents, List<FieldDefinition> fields) {
            this.formPanel = formPanel;
            this.inputComponents = inputComponents;
            this.fields = fields;
        }

        public JPanel getFormPanel() {
            return formPanel;
        }

        public Map<String, JComponent> getInputComponents() {
            return inputComponents;
        }

        public Map<String, String> extractValues() {
            Map<String, String> values = new LinkedHashMap<>();
            for (FieldDefinition field : fields) {
                JComponent comp = inputComponents.get(field.getFieldName());
                if (comp instanceof JTextField textField) {
                    values.put(field.getFieldName(), textField.getText().trim());
                } else if (comp instanceof JSpinner spinner) {
                    values.put(field.getFieldName(), String.valueOf(spinner.getValue()));
                } else if (comp instanceof JCheckBox checkBox) {
                    values.put(field.getFieldName(), String.valueOf(checkBox.isSelected()));
                } else if (comp instanceof JComboBox<?> comboBox) {
                    Object selected = comboBox.getSelectedItem();
                    values.put(field.getFieldName(), selected != null ? String.valueOf(selected) : "");
                }
            }
            return values;
        }

        public void clearValues() {
            for (FieldDefinition field : fields) {
                JComponent comp = inputComponents.get(field.getFieldName());
                String def = field.getDefaultValue() != null ? field.getDefaultValue() : "";
                if (comp instanceof JTextField textField) {
                    textField.setText(def);
                } else if (comp instanceof JSpinner spinner) {
                    try {
                        spinner.setValue(def.isBlank() ? 0 : Integer.parseInt(def));
                    } catch (NumberFormatException e) {
                        spinner.setValue(0);
                    }
                } else if (comp instanceof JCheckBox checkBox) {
                    checkBox.setSelected("true".equalsIgnoreCase(def));
                } else if (comp instanceof JComboBox<?> comboBox && comboBox.getItemCount() > 0) {
                    comboBox.setSelectedIndex(0);
                }
            }
        }
    }

    public GeneratedForm buildForm(List<FieldDefinition> fields,
                                   Map<String, String> initialValues,
                                   Map<Integer, List<String>> referenceLookup) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Map<String, JComponent> componentMap = new LinkedHashMap<>();
        int row = 0;

        for (FieldDefinition field : fields) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;

            String labelText = field.getFieldName() + (field.isRequired() ? " *" : "");
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            panel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;

            String currentVal = (initialValues != null && initialValues.containsKey(field.getFieldName()))
                    ? initialValues.get(field.getFieldName())
                    : (field.getDefaultValue() != null ? field.getDefaultValue() : "");

            JComponent input = createComponentForField(field, currentVal, referenceLookup);
            componentMap.put(field.getFieldName(), input);
            panel.add(input, gbc);

            row++;
        }

        return new GeneratedForm(panel, componentMap, fields);
    }

    private JComponent createComponentForField(FieldDefinition field,
                                               String currentVal,
                                               Map<Integer, List<String>> referenceLookup) {
        String type = field.getFieldType() != null ? field.getFieldType().toUpperCase() : "TEXT";

        return switch (type) {
            case "NUMBER" -> {
                int initialInt = 0;
                try {
                    if (!currentVal.isBlank()) {
                        initialInt = (int) Double.parseDouble(currentVal);
                    }
                } catch (NumberFormatException ignored) {
                }
                SpinnerNumberModel model = new SpinnerNumberModel(initialInt, -999999999, 999999999, 1);
                yield new JSpinner(model);
            }
            case "BOOLEAN" -> {
                JCheckBox checkBox = new JCheckBox("Enabled / Yes");
                checkBox.setBackground(Color.WHITE);
                checkBox.setSelected("true".equalsIgnoreCase(currentVal));
                yield checkBox;
            }
            case "DROPDOWN" -> {
                List<String> opts = field.getDropdownOptions();
                JComboBox<String> comboBox = new JComboBox<>(opts.toArray(new String[0]));
                if (!currentVal.isBlank()) {
                    comboBox.setSelectedItem(currentVal);
                }
                yield comboBox;
            }
            case "REFERENCE" -> {
                List<String> refOptions = referenceLookup != null && field.getReferenceModuleId() != null
                        ? referenceLookup.getOrDefault(field.getReferenceModuleId(), List.of())
                        : List.of();
                JComboBox<String> comboBox = new JComboBox<>(refOptions.toArray(new String[0]));
                if (!currentVal.isBlank()) {
                    comboBox.setSelectedItem(currentVal);
                }
                yield comboBox;
            }
            case "DATE" -> {
                String dateVal = currentVal.isBlank() ? LocalDate.now().toString() : currentVal;
                JTextField dateField = new JTextField(dateVal, 20);
                dateField.setToolTipText("Format: YYYY-MM-DD");
                yield dateField;
            }
            default -> {
                // Covers TEXT, EMAIL, DECIMAL
                JTextField textField = new JTextField(currentVal, 20);
                yield textField;
            }
        };
    }
}
