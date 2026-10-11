package com.gensoftrix.model;

import java.sql.Timestamp;

/**
 * Represents a registered GenSoftrix user.
 * Extends BaseEntity to demonstrate OOP Inheritance and Polymorphism.
 */
public class User extends BaseEntity {
    private String username;
    private String passwordHash;

    public User() {
        super();
    }

    public User(String username, String passwordHash) {
        super();
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public User(int id, String username, String passwordHash, Timestamp createdAt) {
        super(id, createdAt);
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String getDisplaySummary() {
        return "User #" + getId() + ": " + username;
    }
}
