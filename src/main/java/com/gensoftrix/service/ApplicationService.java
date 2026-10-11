package com.gensoftrix.service;

import com.gensoftrix.dao.*;
import com.gensoftrix.model.*;
import com.gensoftrix.model.Module;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Coordinates business operations across Applications, Modules, Fields, Records, and Business Rules.
 */
public class ApplicationService {

    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final FieldDAO fieldDAO = new FieldDAO();
    private final RecordDAO recordDAO = new RecordDAO();
    private final BusinessRuleDAO businessRuleDAO = new BusinessRuleDAO();

    public List<Application> getApplications(int userId) throws SQLException {
        return applicationDAO.findByUserId(userId);
    }

    public Application createApplication(int userId, String name, String description) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Application name is required.");
        }
        return applicationDAO.create(userId, name.trim(), description != null ? description.trim() : "");
    }

    public void updateApplication(int id, String name, String description) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Application name is required.");
        }
        applicationDAO.update(id, name.trim(), description != null ? description.trim() : "");
    }

    public void deleteApplication(int id) throws SQLException {
        applicationDAO.delete(id);
    }

    public List<Module> getModules(int applicationId) throws SQLException {
        return moduleDAO.findByApplicationId(applicationId);
    }

    public Module createModule(int applicationId, String identifier, String name, String description) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Module name is required.");
        }
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Module identifier is required.");
        }
        return moduleDAO.create(applicationId, identifier.trim().toUpperCase(), name.trim(), description != null ? description.trim() : "");
    }

    public void updateModule(int id, String identifier, String name, String description) throws SQLException {
        moduleDAO.update(id, identifier.trim().toUpperCase(), name.trim(), description != null ? description.trim() : "");
    }

    public void deleteModule(int id) throws SQLException {
        moduleDAO.delete(id);
    }

    public List<FieldDefinition> getFields(int moduleId) throws SQLException {
        return fieldDAO.findByModuleId(moduleId);
    }

    public FieldDefinition createField(FieldDefinition field) throws SQLException {
        if (field.getFieldName() == null || field.getFieldName().isBlank()) {
            throw new IllegalArgumentException("Field name is required.");
        }
        return fieldDAO.create(field);
    }

    public void updateField(FieldDefinition field) throws SQLException {
        fieldDAO.update(field);
    }

    public void deleteField(int fieldId) throws SQLException {
        fieldDAO.delete(fieldId);
    }

    public List<RecordData> getRecords(int moduleId) throws SQLException {
        return recordDAO.findByModuleId(moduleId);
    }

    public RecordData createRecord(int moduleId, Map<String, String> values) throws SQLException {
        return recordDAO.create(moduleId, values);
    }

    public void updateRecord(int recordId, Map<String, String> values) throws SQLException {
        recordDAO.update(recordId, values);
    }

    public void deleteRecord(int recordId) throws SQLException {
        recordDAO.delete(recordId);
    }

    public List<BusinessRule> getBusinessRules(int moduleId) throws SQLException {
        return businessRuleDAO.findByModuleId(moduleId);
    }

    public BusinessRule createBusinessRule(BusinessRule rule) throws SQLException {
        return businessRuleDAO.create(rule);
    }

    public void toggleBusinessRule(int ruleId, boolean enabled) throws SQLException {
        businessRuleDAO.setEnabled(ruleId, enabled);
    }

    public void deleteBusinessRule(int ruleId) throws SQLException {
        businessRuleDAO.delete(ruleId);
    }

    /**
     * Resolves display labels from a referenced module for REFERENCE fields.
     */
    public List<String> getReferenceOptions(int targetModuleId) throws SQLException {
        List<FieldDefinition> targetFields = fieldDAO.findByModuleId(targetModuleId);
        List<RecordData> targetRecords = recordDAO.findByModuleId(targetModuleId);
        String primaryField = targetFields.isEmpty() ? null : targetFields.get(0).getFieldName();

        List<String> options = new ArrayList<>();
        for (RecordData record : targetRecords) {
            if (primaryField != null && !record.getValue(primaryField).isBlank()) {
                options.add(record.getValue(primaryField));
            } else {
                options.add("Record #" + record.getId());
            }
        }
        return options;
    }

    public int getTotalApplications(int userId) throws SQLException {
        return applicationDAO.countByUserId(userId);
    }

    public int getTotalModules(int userId) throws SQLException {
        return moduleDAO.countByUserId(userId);
    }

    public int getTotalRecords(int userId) throws SQLException {
        return recordDAO.countByUserId(userId);
    }
}
