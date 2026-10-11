package com.gensoftrix.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a field belonging to a Module.
 * Supported field types: TEXT, NUMBER, DECIMAL, DATE, EMAIL, BOOLEAN, DROPDOWN, REFERENCE.
 * Extends BaseEntity and provides polymorphic Swing component mapping.
 */
public class FieldDefinition extends BaseEntity {
    private int moduleId;
    private String fieldName;
    private String fieldType;
    private boolean required;
    private String defaultValue;
    private String optionsCsv;
    private Integer referenceModuleId;
    private int displayOrder;

    public FieldDefinition() {
        super();
    }

    public FieldDefinition(int moduleId, String fieldName, String fieldType, boolean required, String defaultValue) {
        super();
        this.moduleId = moduleId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = required;
        this.defaultValue = defaultValue;
        this.displayOrder = 1;
    }

    public FieldDefinition(int id, int moduleId, String fieldName, String fieldType,
                           boolean required, String defaultValue, String optionsCsv,
                           Integer referenceModuleId, int displayOrder) {
        super(id, null);
        this.moduleId = moduleId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = required;
        this.defaultValue = defaultValue;
        this.optionsCsv = optionsCsv;
        this.referenceModuleId = referenceModuleId;
        this.displayOrder = displayOrder;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOptionsCsv() {
        return optionsCsv;
    }

    public void setOptionsCsv(String optionsCsv) {
        this.optionsCsv = optionsCsv;
    }

    public Integer getReferenceModuleId() {
        return referenceModuleId;
    }

    public void setReferenceModuleId(Integer referenceModuleId) {
        this.referenceModuleId = referenceModuleId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<String> getDropdownOptions() {
        if (optionsCsv == null || optionsCsv.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(optionsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public String getSwingComponentName() {
        if (fieldType == null) return "JTextField";
        return switch (fieldType.toUpperCase()) {
            case "BOOLEAN" -> "JCheckBox";
            case "DROPDOWN", "REFERENCE" -> "JComboBox";
            default -> "JTextField";
        };
    }

    @Override
    public String getDisplaySummary() {
        return fieldName + " (" + fieldType + (required ? ", Required" : ", Optional") + ")";
    }

    @Override
    public String toString() {
        return fieldName + " [" + fieldType + "]";
    }
}
