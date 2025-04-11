/*
 * Inventory POS System
 * Copyright (c) 2025 ZOUHAIR FOUIGUIRA. All rights reserved.
 *
 * Licensed under Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International
 * You may not use this file except in compliance with the License.
 *
 * @author ZOUHAIR FOUIGUIRA
 * @version 1.0
 * @since 2025-02-24
 */
package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChangePasswordController {

    @FXML private MFXPasswordField currentPasswordField;
    @FXML private MFXPasswordField newPasswordField;
    @FXML private MFXPasswordField confirmPasswordField;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton cancelButton;

    private final UserService userService;
    private final ApplicationContext context;

    public ChangePasswordController(UserService userService, ApplicationContext context) {
        this.userService = userService;
        this.context = context;
    }

    @FXML
    public void initialize() {
        currentPasswordField.requestFocus();
    }

    @FXML
    private void handleSavePassword() {
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password and confirmation do not match.");
            return;
        }

        try {
            User currentUser = userService.getCurrentUser();
            User authenticatedUser = userService.authenticate(currentUser.getUsername(), currentPassword);
            if (currentUser == null || authenticatedUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect.");
                return;
            }

            currentUser.setPassword(newPassword);
            currentUser.setTemporaryPassword(false);
            userService.updateUser(currentUser.getId(), currentUser);
            
            // Re-authenticate with new password to update the session
            userService.authenticate(currentUser.getUsername(), newPassword);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
            
            // Navigate to appropriate dashboard based on user role
            Stage stage = (Stage) saveButton.getScene().getWindow();
            String fxmlPath;
            String title;
            
            if (currentUser.getRole() == User.Role.OWNER) {
                fxmlPath = "/view/AdminDashboard.fxml";
                title = "Admin Dashboard";
            } else if (currentUser.getRole() == User.Role.CASHIER) {
                fxmlPath = "/view/CashierDashboard.fxml";
                title = "Cashier Dashboard";
            } else if (currentUser.getRole() == User.Role.SUPPORT_ADMIN) {
                fxmlPath = "/view/SupportDashboard.fxml";
                title = "Support Administrator Dashboard";
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Unsupported user role");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Add stylesheet
            String cssPath = getClass().getResource("/styles/styles.css") != null
                ? getClass().getResource("/styles/styles.css").toExternalForm()
                : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }
            
            stage.setScene(scene);
            stage.setTitle(title);
            if (currentUser.getRole() == User.Role.OWNER) {
                stage.setMaximized(true);
            }
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        userService.logout();
        returnToLogin();
    }

    private void returnToLogin() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Add stylesheet
            String cssPath = getClass().getResource("/styles/styles.css") != null
                ? getClass().getResource("/styles/styles.css").toExternalForm()
                : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }
            
            stage.setScene(scene);
            stage.setTitle("Inventory POS System - Login");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}