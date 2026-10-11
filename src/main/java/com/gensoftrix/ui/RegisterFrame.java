package com.gensoftrix.ui;

import com.gensoftrix.model.User;
import com.gensoftrix.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration window for new GenSoftrix users.
 */
public class RegisterFrame extends JFrame {

    private final AuthenticationService authService = new AuthenticationService();
    private final LoginFrame parentLoginFrame;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private final JButton registerBtn = UITheme.createPrimaryButton("Register");
    private final JButton backBtn = UITheme.createSecondaryButton("Back to Sign In");

    public RegisterFrame(LoginFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;
        setTitle("GenSoftrix — User Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 400);
        setLocationRelativeTo(parentLoginFrame);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CANVAS_BG);
        root.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel("Create GenSoftrix Account");
        title.setFont(UITheme.FONT_TITLE);
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(confirmField, gbc);

        root.add(formPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        actions.setOpaque(false);
        actions.add(backBtn);
        actions.add(registerBtn);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);

        backBtn.addActionListener(e -> {
            parentLoginFrame.setVisible(true);
            dispose();
        });

        registerBtn.addActionListener(e -> performRegistration());
    }

    private void performRegistration() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        registerBtn.setEnabled(false);
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.register(username, password, confirm);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                try {
                    User created = get();
                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Account '" + created.getUsername() + "' registered! Opening dashboard.",
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    DashboardFrame dashboard = new DashboardFrame(created);
                    dashboard.setVisible(true);
                    parentLoginFrame.dispose();
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            cause.getMessage(),
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
}
