package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashierManagementController {

    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton clearButton;
    @FXML private VBox passwordResultBox;
    @FXML private MFXTextField generatedPasswordField;
    @FXML private MFXButton copyPasswordButton;

    private final UserService userService;

    @Autowired
    public CashierManagementController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        usernameField.requestFocus();
        passwordResultBox.setVisible(false);
        System.out.println("CashierManagementController initialized");
    }

    @FXML
    private void handleCopyPassword() {
        String password = generatedPasswordField.getText();
        if (password != null && !password.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(password);
            clipboard.setContent(content);
            
            // Show brief success feedback
            copyPasswordButton.setText("Copied!");
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> copyPasswordButton.setText("Copy"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @FXML
    private void handleSaveCashier() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("handleSaveCashier called with username: " + username + ", password: " + (password.isEmpty() ? "(empty)" : "(provided)"));

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username is required.");
            System.out.println("Username is empty - operation aborted");
            return;
        }

        try {
            int currentUserCount = userService.getAllUsers().size();
            System.out.println("Current number of users: " + currentUserCount);

            User cashier = new User();
            cashier.setUsername(username);
            String displayPassword = null;
            if (password.isEmpty()) {
                displayPassword = "Cashier" + System.currentTimeMillis();
                cashier.setPassword(displayPassword); // Will be hashed in service
                cashier.setTemporaryPassword(true);
                System.out.println("Generated temporary password: " + displayPassword);
            } else {
                displayPassword = password;
                cashier.setPassword(password); // Will be hashed in service
                cashier.setTemporaryPassword(false);
                System.out.println("Using provided password");
            }
            cashier.setRole(User.Role.CASHIER);

            User existingUser = userService.getUserByUsername(username);
            System.out.println("Existing user check: " + (existingUser != null ? "Found user " + existingUser.getUsername() : "No existing user"));

            if (existingUser != null) {
                if (userService.getCurrentUserRole() != User.Role.OWNER) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Only owners can reset existing cashier passwords.");
                    System.out.println("Reset denied: Current user is not OWNER");
                    return;
                }
                
                // Ask for confirmation before resetting password
                if (!showConfirmationAlert("Reset Password", 
                    "Are you sure you want to reset the password for cashier '" + username + "'?\n" +
                    (password.isEmpty() ? "A temporary password will be generated." : "The specified password will be set."))) {
                    return;
                }

                existingUser.setPassword(displayPassword);
                existingUser.setTemporaryPassword(displayPassword != null && password.isEmpty());
                userService.updateUser(existingUser.getId(), existingUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password reset successfully!");
                showPasswordResult(displayPassword);
                System.out.println("Password reset for existing user: " + username);
            } else {
                // For new cashier creation with empty password
                if (password.isEmpty()) {
                    if (!showConfirmationAlert("Create Cashier", 
                        "You are about to create a new cashier '" + username + "' with a generated temporary password.\n" +
                        "The cashier will be required to change this password on first login.\n\n" +
                        "Do you want to continue?")) {
                        return;
                    }
                } else {
                    // For new cashier with specified password
                    if (!showConfirmationAlert("Create Cashier", 
                        "You are about to create a new cashier '" + username + "' with the specified password.\n" +
                        "Do you want to continue?")) {
                        return;
                    }
                }

                System.out.println("Creating new cashier: " + username);
                userService.createUser(cashier);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cashier created successfully!");
                showPasswordResult(displayPassword);
                System.out.println("✅✅❎❎ Cashier created successfully"+cashier);
            }

            int newUserCount = userService.getAllUsers().size();
            System.out.println(" ✅✅❎❎ New number of users: " + newUserCount);

            // Don't clear the form immediately, let the user copy the password first
            usernameField.clear();
            passwordField.clear();
            usernameField.requestFocus();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save/reset cashier: " + e.getMessage());
            System.err.println("Exception in handleSaveCashier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPasswordResult(String password) {
        generatedPasswordField.setText(password);
        passwordResultBox.setVisible(true);
    }

    private boolean showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == javafx.scene.control.ButtonType.OK).isPresent();
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        System.out.println("Form cleared");
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        passwordResultBox.setVisible(false);
        usernameField.requestFocus();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}