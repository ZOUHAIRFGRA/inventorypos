package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashierManagementController {

    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton clearButton;

    private final UserService userService;

    @Autowired
    public CashierManagementController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        usernameField.requestFocus();
        System.out.println("CashierManagementController initialized");
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
                existingUser.setPassword(displayPassword);
                existingUser.setTemporaryPassword(displayPassword != null && password.isEmpty());
                userService.updateUser(existingUser.getId(), existingUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cashier " + username + " password reset to: " + displayPassword + ". Share this with the cashier.");
                System.out.println("Password reset for existing user: " + username);
            } else {
                System.out.println("Creating new cashier: " + username);
                userService.createUser(cashier);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cashier " + username + " created successfully!");
                System.out.println("✅✅❎❎ Cashier created successfully"+cashier);
            }

            int newUserCount = userService.getAllUsers().size();
            System.out.println(" ✅✅❎❎ New number of users: " + newUserCount);

            clearForm();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save/reset cashier: " + e.getMessage());
            System.err.println("Exception in handleSaveCashier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        System.out.println("Form cleared");
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
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