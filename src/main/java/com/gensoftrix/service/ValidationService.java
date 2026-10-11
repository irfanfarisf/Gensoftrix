package com.gensoftrix.service;

import com.gensoftrix.model.BusinessRule;
import com.gensoftrix.model.FieldDefinition;
import com.gensoftrix.model.RecordData;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Evaluates intrinsic field constraints, module reference integrity, and configured BusinessRules
 * before saving a record to MySQL. Implements the Validator interface (OOP Abstraction).
 */
public class ValidationService implements Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void processAndValidate(List<FieldDefinition> fields,
                                   List<BusinessRule> rules,
                                   List<RecordData> existingRecords,
                                   Map<Integer, List<String>> referenceOptions,
                                   Map<String, String> recordValues,
                                   Integer editingRecordId) {

        // 1. Apply DEFAULT_VALUE rules and field default values when empty
        for (FieldDefinition field : fields) {
            String current = recordValues.getOrDefault(field.getFieldName(), "").trim();
            if (current.isEmpty() && field.getDefaultValue() != null && !field.getDefaultValue().isBlank()) {
                recordValues.put(field.getFieldName(), field.getDefaultValue().trim());
            }
        }

        for (BusinessRule rule : rules) {
            if (!rule.isEnabled() || rule.getFieldName() == null) {
                continue;
            }
            if ("DEFAULT_VALUE".equalsIgnoreCase(rule.getRuleType())) {
                String current = recordValues.getOrDefault(rule.getFieldName(), "").trim();
                if (current.isEmpty()) {
                    recordValues.put(rule.getFieldName(), rule.getRuleValue());
                }
            }
        }

        // 2. Apply FORMULA rules (e.g., "Unit Price * Order Qty" or "Price * Quantity")
        for (BusinessRule rule : rules) {
            if (!rule.isEnabled() || rule.getFieldName() == null) {
                continue;
            }
            if ("FORMULA".equalsIgnoreCase(rule.getRuleType())) {
                String calculated = evaluateSimpleFormula(rule.getRuleValue(), recordValues);
                if (calculated != null) {
                    recordValues.put(rule.getFieldName(), calculated);
                }
            }
        }

        // 3. Validate intrinsic field requirements & formats
        for (FieldDefinition field : fields) {
            String name = field.getFieldName();
            String val = recordValues.getOrDefault(name, "").trim();
            String type = field.getFieldType() != null ? field.getFieldType().toUpperCase(Locale.ROOT) : "TEXT";

            if (field.isRequired() && val.isEmpty()) {
                throw new IllegalArgumentException("Field '" + name + "' is required.");
            }
            if (val.isEmpty()) {
                continue;
            }

            switch (type) {
                case "NUMBER" -> {
                    try {
                        Long.parseLong(val);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Field '" + name + "' must be a valid whole number.");
                    }
                }
                case "DECIMAL" -> {
                    try {
                        Double.parseDouble(val);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Field '" + name + "' must be a valid decimal number.");
                    }
                }
                case "DATE" -> {
                    try {
                        LocalDate.parse(val);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Field '" + name + "' must be a valid date (YYYY-MM-DD).");
                    }
                }
                case "EMAIL" -> {
                    if (!EMAIL_PATTERN.matcher(val).matches()) {
                        throw new IllegalArgumentException("Field '" + name + "' must be a valid email address.");
                    }
                }
                case "REFERENCE" -> {
                    if (field.getReferenceModuleId() != null && referenceOptions != null) {
                        List<String> validTargets = referenceOptions.getOrDefault(field.getReferenceModuleId(), List.of());
                        if (!validTargets.isEmpty() && !validTargets.contains(val)) {
                            throw new IllegalArgumentException("Field '" + name + "' references an invalid record.");
                        }
                    }
                }
            }
        }

        // 4. Evaluate configured BusinessRules
        for (BusinessRule rule : rules) {
            if (!rule.isEnabled() || rule.getFieldName() == null) {
                continue;
            }
            String fieldName = rule.getFieldName();
            String rawVal = recordValues.getOrDefault(fieldName, "").trim();
            String rType = rule.getRuleType().toUpperCase(Locale.ROOT);

            switch (rType) {
                case "REQUIRED" -> {
                    if (rawVal.isEmpty()) {
                        throw new IllegalArgumentException("Business Rule Violation: '" + fieldName + "' cannot be empty.");
                    }
                }
                case "MIN_VALUE" -> {
                    if (!rawVal.isEmpty()) {
                        double num = parseDoubleSafe(rawVal, fieldName);
                        double min = parseDoubleSafe(rule.getRuleValue(), "MIN_VALUE rule");
                        if (num < min) {
                            throw new IllegalArgumentException(
                                    "Business Rule Violation: '" + fieldName + "' (" + num + ") cannot be less than " + rule.getRuleValue() + "."
                            );
                        }
                    }
                }
                case "MAX_VALUE" -> {
                    if (!rawVal.isEmpty()) {
                        double num = parseDoubleSafe(rawVal, fieldName);
                        double max = parseDoubleSafe(rule.getRuleValue(), "MAX_VALUE rule");
                        if (num > max) {
                            throw new IllegalArgumentException(
                                    "Business Rule Violation: '" + fieldName + "' (" + num + ") must not exceed " + rule.getRuleValue() + "."
                            );
                        }
                    }
                }
                case "UNIQUE" -> {
                    if (!rawVal.isEmpty() && existingRecords != null) {
                        for (RecordData existing : existingRecords) {
                            if (editingRecordId != null && existing.getId() == editingRecordId) {
                                continue;
                            }
                            String existingVal = existing.getValue(fieldName).trim();
                            if (existingVal.equalsIgnoreCase(rawVal)) {
                                throw new IllegalArgumentException(
                                        "Business Rule Violation: Duplicate value '" + rawVal + "' for unique field '" + fieldName + "'."
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private double parseDoubleSafe(String value, String label) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value for " + label + ": " + value);
        }
    }

    public String evaluateSimpleFormula(String formula, Map<String, String> values) {
        if (formula == null || formula.isBlank()) {
            return null;
        }
        String[] operators = {"\\*", "/", "\\+", "-"};
        char[] opSymbols = {'*', '/', '+', '-'};

        for (int i = 0; i < operators.length; i++) {
            String opRegex = operators[i];
            char symbol = opSymbols[i];
            String[] parts = formula.split(opRegex, 2);
            if (parts.length == 2) {
                double left = resolveOperand(parts[0].trim(), values);
                double right = resolveOperand(parts[1].trim(), values);
                double result = switch (symbol) {
                    case '*' -> left * right;
                    case '/' -> right != 0 ? left / right : 0.0;
                    case '+' -> left + right;
                    case '-' -> left - right;
                    default -> 0.0;
                };
                return String.format(Locale.US, "%.2f", result);
            }
        }
        return null;
    }

    private double resolveOperand(String token, Map<String, String> values) {
        if (values.containsKey(token)) {
            String raw = values.get(token);
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
