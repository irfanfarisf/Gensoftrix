package com.gensoftrix.model;

import java.sql.Timestamp;

/**
 * Represents a functional module inside a management Application
 * (e.g., Products, Suppliers, Orders).
 */
public class Module extends BaseEntity {
    private int applicationId;
    private String moduleIdentifier;
    private String name;
    private String description;

    public Module() {
        super();
    }

    public Module(int applicationId, String moduleIdentifier, String name, String description) {
        super();
        this.applicationId = applicationId;
        this.moduleIdentifier = moduleIdentifier;
        this.name = name;
        this.description = description;
    }

    public Module(int id, int applicationId, String moduleIdentifier, String name, String description, Timestamp createdAt) {
        super(id, createdAt);
        this.applicationId = applicationId;
        this.moduleIdentifier = moduleIdentifier;
        this.name = name;
        this.description = description;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getModuleIdentifier() {
        return moduleIdentifier;
    }

    public void setModuleIdentifier(String moduleIdentifier) {
        this.moduleIdentifier = moduleIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDisplaySummary() {
        return name + " [" + moduleIdentifier + "]";
    }

    @Override
    public String toString() {
        return name + " (" + moduleIdentifier + ")";
    }
}
