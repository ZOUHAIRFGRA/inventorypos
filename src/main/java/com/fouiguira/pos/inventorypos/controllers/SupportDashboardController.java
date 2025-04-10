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
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
public class SupportDashboardController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, User.Role> roleColumn;
    @FXML private TableColumn<User, String> lastUpdatedColumn;
    @FXML private TableColumn<User, Boolean> tempPasswordColumn;
    @FXML private MFXTextField selectedUserField;
    @FXML private MFXPasswordField newPasswordField;
    @FXML private MFXButton resetPasswordButton;
    @FXML private Label statusLabel;
    @FXML private MFXButton logoutButton;

    private final UserService userService;
    private final ApplicationContext context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SupportDashboardController(UserService userService, ApplicationContext context) {
        this.userService = userService;
        this.context = context;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        setupTableSelection();
        setupTooltips();
    }

    private void setupTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<User, User.Role>("role"));
        lastUpdatedColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(dateFormat.format(cellData.getValue().getUpdatedAt())));
        tempPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("temporaryPassword"));

        // Prevent support admin from being displayed
        userTable.setItems(FXCollections.observableArrayList(
            userService.getAllUsers().stream()
                .filter(user -> user.getRole() != User.Role.SUPPORT_ADMIN)
                .toList()
        ));
    }

    private void setupTableSelection() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUserField.setText(newSelection.getUsername());
                resetPasswordButton.setDisable(false);
            } else {
                selectedUserField.setText("");
                resetPasswordButton.setDisable(true);
            }
        });
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers().stream()
            .filter(user -> user.getRole() != User.Role.SUPPORT_ADMIN)
            .toList();
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private void setupTooltips() {
        logoutButton.setTooltip(new Tooltip("Logout from the system"));
        resetPasswordButton.setTooltip(new Tooltip("Reset the selected user's password"));
    }

    @FXML
    public void handleLogout() {
        userService.logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventory POS System - Login");
            
            String cssPath = getClass().getResource("/styles/styles.css") != null 
                ? getClass().getResource("/styles/styles.css").toExternalForm() 
                : null;
            if (cssPath != null) {
                stage.getScene().getStylesheets().add(cssPath);
            }
            
            stage.show();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Logged out successfully");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetPassword() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showStatus("Please select a user", false);
            return;
        }

        String newPassword = newPasswordField.getText().trim();
        if (newPassword.isEmpty()) {
            showStatus("Please enter a new password", false);
            return;
        }

        try {
            selectedUser.setPassword(newPassword);
            selectedUser.setTemporaryPassword(true);
            userService.updateUser(selectedUser.getId(), selectedUser);
            
            showStatus("Password reset successfully for " + selectedUser.getUsername(), true);
            newPasswordField.clear();
            loadUsers(); // Refresh the table
        } catch (Exception e) {
            showStatus("Error resetting password: " + e.getMessage(), false);
        }
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (success ? "#4caf50" : "#f44336") + ";");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
