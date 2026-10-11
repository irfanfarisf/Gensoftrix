package com.gensoftrix.model;

/**
 * Represents a configurable validation or calculation rule on a Module and Field.
 * Supported rule types: REQUIRED, MIN_VALUE, MAX_VALUE, UNIQUE, DEFAULT_VALUE, FORMULA.
 */
public class BusinessRule extends BaseEntity {
    private int moduleId;
    private Integer fieldId;
    private String fieldName;
    private String ruleType;
    private String ruleValue;
    private boolean enabled;

    public BusinessRule() {
        super();
    }

    public BusinessRule(int moduleId, Integer fieldId, String fieldName, String ruleType, String ruleValue) {
        super();
        this.moduleId = moduleId;
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.enabled = true;
    }

    public BusinessRule(int id, int moduleId, Integer fieldId, String fieldName,
                        String ruleType, String ruleValue, boolean enabled) {
        super(id, null);
        this.moduleId = moduleId;
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.enabled = enabled;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public Integer getFieldId() {
        return fieldId;
    }

    public void setFieldId(Integer fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getDisplaySummary() {
        return fieldName + " [" + ruleType + ": " + ruleValue + "]";
    }
}
