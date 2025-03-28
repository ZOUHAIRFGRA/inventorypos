package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class CashierManagementController {

    @FXML private TableView<User> cashiersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> lastLoginColumn;
    @FXML private TableColumn<User, String> statusColumn;
    
    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton clearButton;
    @FXML private VBox passwordResultBox;
    @FXML private MFXTextField generatedPasswordField;
    @FXML private MFXButton copyPasswordButton;

    private final UserService userService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User selectedUser;

    public CashierManagementController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupTableSelection();
        loadCashiers();
        usernameField.requestFocus();
        passwordResultBox.setVisible(false);
    }

    private void setupTable() {
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        lastLoginColumn.setCellValueFactory(data -> {
            if (data.getValue().getUpdatedAt() != null) {
                return new SimpleStringProperty(dateFormat.format(data.getValue().getUpdatedAt()));
            }
            return new SimpleStringProperty("Never");
        });
        statusColumn.setCellValueFactory(data -> {
            User user = data.getValue();
            String status = user.isTemporaryPassword() ? "Temporary Password" : "Active";
            return new SimpleStringProperty(status);
        });

        
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Temporary Password")) {
                        setStyle("-fx-text-fill: #FFA000;"); 
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); 
                    }
                }
            }
        });
    }

    private void setupTableSelection() {
        cashiersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedUser = newSelection;
            if (newSelection != null) {
                usernameField.setText(newSelection.getUsername());
                passwordField.clear(); 
                saveButton.setText("Update");
            } else {
                clearForm();
                saveButton.setText("Save");
            }
        });
    }

    private void loadCashiers() {
        List<User> cashiers = userService.getAllUsers().stream()
            .filter(user -> user.getRole() == User.Role.CASHIER)
            .toList();
        cashiersTable.setItems(FXCollections.observableArrayList(cashiers));
    }

    @FXML
    private void handleSaveCashier() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username is required.");
            return;
        }

        try {
            if (selectedUser != null) {
                
                if (!showConfirmationAlert("Update Cashier", 
                    "Are you sure you want to update cashier '" + username + "'?" +
                    (password.isEmpty() ? "\nNo password change will be made." : "\nThe password will be updated."))) {
                    return;
                }

                selectedUser.setUsername(username);
                if (!password.isEmpty()) {
                    selectedUser.setPassword(password);
                    selectedUser.setTemporaryPassword(false);
                }
                userService.updateUser(selectedUser.getId(), selectedUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cashier updated successfully!");
            } else {
                
                User cashier = new User();
                cashier.setUsername(username);
                String displayPassword;
                
                if (password.isEmpty()) {
                    displayPassword = generateRandomPassword();
                    cashier.setPassword(displayPassword);
                    cashier.setTemporaryPassword(true);
                } else {
                    displayPassword = password;
                    cashier.setPassword(password);
                    cashier.setTemporaryPassword(false);
                }
                cashier.setRole(User.Role.CASHIER);

                if (!showConfirmationAlert("Create Cashier", 
                    "You are about to create a new cashier '" + username + "'" +
                    (password.isEmpty() ? " with a generated temporary password." : " with the specified password.") +
                    "\nDo you want to continue?")) {
                    return;
                }

                userService.createUser(cashier);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Cashier created successfully!");
                showPasswordResult(displayPassword);
            }

            loadCashiers(); 
            if (selectedUser == null) {
                clearForm(); 
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save/update cashier: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        return "Cashier" + System.currentTimeMillis();
    }

    @FXML
    private void handleCopyPassword() {
        String password = generatedPasswordField.getText();
        if (password != null && !password.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(password);
            clipboard.setContent(content);
            
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
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        passwordResultBox.setVisible(false);
        selectedUser = null;
        saveButton.setText("Save");
        cashiersTable.getSelectionModel().clearSelection();
        usernameField.requestFocus();
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
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}