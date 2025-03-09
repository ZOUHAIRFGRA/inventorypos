package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.main.MainApp;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class AdminDashboardController {

    @FXML
    private BorderPane adminLayout;

    @FXML
    private VBox contentArea;

    @FXML
    private MFXButton dashboardButton;

    @FXML
    private MFXButton productsButton;

    @FXML
    private MFXButton salesButton;

    @FXML
    private MFXButton historyButton;

    @FXML
    private MFXButton cashiersButton;

    @FXML
    private MFXButton invoicesButton;

    @FXML
    private MFXButton settingsButton;

    @FXML
    private MFXButton logoutButton;

    @FXML
    private Label messageLabel;

    private final UserService userService;

    @Autowired
    public AdminDashboardController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        // Load the default view (e.g., Overview)
        loadDashboard();
    }

    @FXML
    public void loadDashboard() {
        loadView("DashboardView.fxml"); // Placeholder for an overview dashboard
    }

    @FXML
    public void loadProducts() {
        loadView("ProductView.fxml");
    }

    @FXML
    public void loadSales() {
        loadView("SalesHistoryView.fxml"); // Adjust to a sales-specific view if needed
    }

    @FXML
    public void loadSalesHistory() {
        loadView("SalesHistoryView.fxml");
    }

    @FXML
    public void loadCashiers() {
        loadView("CashierDashboard.fxml"); // Reuse or create a specific cashier management view
    }

    @FXML
    public void loadInvoices() {
        loadView("InvoiceView.fxml");
    }

    @FXML
    public void loadSettings() {
        loadView("SettingsView.fxml");
    }

    @FXML
    public void handleLogout() {
        userService.logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            loader.setControllerFactory(MainApp.springContext::getBean);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventory POS System - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error logging out: " + e.getMessage());
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            loader.setControllerFactory(MainApp.springContext::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            messageLabel.setText("Loaded " + fxmlFile + " successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading " + fxmlFile + ": " + e.getMessage());
        }
    }

    // Optional: Add cashier method if you want it inline instead of a separate view
    @FXML
    public void handleAddCashier() {
        // This could be moved to a CashierManagementController if preferred
        String username = "newUsernameFieldValue"; // Replace with actual field if added back
        String password = "newPasswordFieldValue"; // Replace with actual field if added back
        if (!username.isEmpty() && !password.isEmpty()) {
            User newCashier = new User();
            newCashier.setUsername(username);
            newCashier.setPassword(password); // Plain text for now; hash in production
            newCashier.setRole(User.Role.CASHIER);
            try {
                userService.createUser(newCashier);
                messageLabel.setText("Cashier '" + username + "' added successfully!");
            } catch (RuntimeException e) {
                messageLabel.setText(e.getMessage());
            }
        } else {
            messageLabel.setText("Username and password cannot be empty!");
        }
    }
}