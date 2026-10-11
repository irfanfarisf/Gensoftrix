package com.gensoftrix;

import com.gensoftrix.config.DatabaseConnection;
import com.gensoftrix.ui.LoginFrame;

import javax.swing.*;

/**
 * Entry point for GenSoftrix – Customizable Management Software Generator.
 * Initializes the look-and-feel, verifies database connectivity, and launches the Login screen
 * on the Swing Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // Configure clean system Look and Feel with consistent Swing defaults
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default cross-platform look and feel
        }

        // Initialize database schema in a background thread before opening login frame
        SwingUtilities.invokeLater(() -> {
            DatabaseConnection.initializeSchemaAsync();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
