package com.gensoftrix.service;

import com.gensoftrix.dao.UserDAO;
import com.gensoftrix.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HexFormat;

/**
 * Handles user authentication and cryptographic password hashing using Java's built-in SHA-256 API.
 */
public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null;
        }
        if (verifyPassword(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public User register(String username, String password, String confirmPassword) throws SQLException {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User existing = userDAO.findByUsername(username.trim());
        if (existing != null) {
            throw new IllegalArgumentException("Username is already registered.");
        }

        String saltedHash = hashPassword(password);
        return userDAO.create(username.trim(), saltedHash);
    }

    /**
     * Generates a 16-byte random salt and returns "saltHex:sha256Hex".
     */
    public static String hashPassword(String plainPassword) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String digestHex = sha256Hex(saltHex + ":" + plainPassword);
        return saltHex + ":" + digestHex;
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        String saltHex = parts[0];
        String expectedDigest = parts[1];
        String computedDigest = sha256Hex(saltHex + ":" + plainPassword);
        return MessageDigest.isEqual(
                expectedDigest.getBytes(StandardCharsets.UTF_8),
                computedDigest.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
