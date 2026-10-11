package com.gensoftrix.service;

import com.gensoftrix.model.BusinessRule;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.RecordData;

import java.util.List;
import java.util.Map;

/**
 * Interface defining validation operations for dynamic module records.
 * Demonstrates OOP Abstraction in the service layer.
 */
public interface Validator {

    void processAndValidate(
            List<FieldDefinition> fields,
            List<BusinessRule> rules,
            List<RecordData> existingRecords,
            Map<Integer, List<String>> referenceOptions,
            Map<String, String> recordValues,
            Integer editingRecordId
    ) throws IllegalArgumentException;
}
