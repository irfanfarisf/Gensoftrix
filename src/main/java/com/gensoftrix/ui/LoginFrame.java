package com.gensoftrix.ui;

import com.gensoftrix.model.User;
import com.gensoftrix.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Module 1: Login System for GenSoftrix.
 * Provides Username, Password, Login button, Registration button, and Exit button.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService = new AuthenticationService();
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = UITheme.createPrimaryButton("Login");
    private final JButton registerButton = UITheme.createSecondaryButton("Registration");
    private final JButton exitButton = UITheme.createDangerButton("Exit");

    public LoginFrame() {
        setTitle("GenSoftrix — Login System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 370);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CANVAS_BG);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("GenSoftrix");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_ACCENT);
        JLabel subLabel = new JLabel("Customizable Management Software Generator");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField.setText("admin");
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField.setText("password123");
        formPanel.add(passwordField, gbc);

        root.add(formPanel, BorderLayout.CENTER);

        // Actions: Exit, Registration, Login
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        actionsPanel.setOpaque(false);
        actionsPanel.add(exitButton);
        actionsPanel.add(registerButton);
        actionsPanel.add(loginButton);
        root.add(actionsPanel, BorderLayout.SOUTH);

        setContentPane(root);

        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame(this);
            registerFrame.setVisible(true);
            setVisible(false);
        });
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        loginButton.setEnabled(false);
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    if (user != null) {
                        DashboardFrame dashboard = new DashboardFrame(user);
                        dashboard.setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Invalid username or password.",
                                "Authentication Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Login error: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
}
