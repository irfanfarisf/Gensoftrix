package com.gensoftrix.model;

import java.sql.Timestamp;

/**
 * Represents a customized management application created by a user
 * (e.g., Inventory Management, Student Management, Employee Management).
 */
public class Application extends BaseEntity {
    private int userId;
    private String name;
    private String description;

    public Application() {
        super();
    }

    public Application(int userId, String name, String description) {
        super();
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    public Application(int id, int userId, String name, String description, Timestamp createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
        return name + " (App #" + getId() + ")";
    }

    @Override
    public String toString() {
        return name;
    }
}
