package com.gensoftrix.model;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a dynamic data record belonging to a Module.
 * Values are serialized to and from a JSON payload in MySQL.
 */
public class RecordData extends BaseEntity {
    private int moduleId;
    private Map<String, String> values;
    private Timestamp updatedAt;

    public RecordData() {
        super();
        this.values = new LinkedHashMap<>();
    }

    public RecordData(int moduleId, Map<String, String> values) {
        super();
        this.moduleId = moduleId;
        this.values = values != null ? new LinkedHashMap<>(values) : new LinkedHashMap<>();
        this.updatedAt = getCreatedAt();
    }

    public RecordData(int id, int moduleId, Map<String, String> values, Timestamp createdAt, Timestamp updatedAt) {
        super(id, createdAt);
        this.moduleId = moduleId;
        this.values = values != null ? new LinkedHashMap<>(values) : new LinkedHashMap<>();
        this.updatedAt = updatedAt;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values != null ? new LinkedHashMap<>(values) : new LinkedHashMap<>();
    }

    public String getValue(String fieldName) {
        return values.getOrDefault(fieldName, "");
    }

    public void setValue(String fieldName, String value) {
        values.put(fieldName, value != null ? value : "");
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String toJsonString() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            json.put(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }
        return json.toString();
    }

    public static Map<String, String> parseJson(String jsonPayload) {
        Map<String, String> map = new LinkedHashMap<>();
        if (jsonPayload == null || jsonPayload.isBlank()) {
            return map;
        }
        JSONObject json = new JSONObject(jsonPayload);
        for (String key : json.keySet()) {
            Object val = json.opt(key);
            map.put(key, val != null ? String.valueOf(val) : "");
        }
        return map;
    }

    @Override
    public String getDisplaySummary() {
        return "Record #" + getId() + " (Module #" + moduleId + ")";
    }
}
