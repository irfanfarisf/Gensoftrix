package com.gensoftrix.model;

import java.sql.Timestamp;

/**
 * Abstract base class for all persistent entities in GenSoftrix.
 * Demonstrates OOP Inheritance, Encapsulation, Constructors, and Polymorphism.
 */
public abstract class BaseEntity {
    private int id;
    private Timestamp createdAt;

    protected BaseEntity() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    protected BaseEntity(int id, Timestamp createdAt) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Polymorphic method overridden by subclasses to return a human-readable entity summary.
     */
    public abstract String getDisplaySummary();
}
