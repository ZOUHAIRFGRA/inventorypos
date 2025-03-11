package com.fouiguira.pos.inventorypos.controllers;

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
    private MFXButton categoryButton;



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

    public AdminDashboardController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        loadDashboard();
    }

    @FXML
    public void loadDashboard() { loadView("DashboardView.fxml"); }

    @FXML
    public void loadProducts() { loadView("ProductView.fxml"); }
    @FXML
    public void loadCategories() { loadView("CategoryView.fxml"); }

    @FXML
    public void loadSales() { loadView("SalesHistoryView.fxml"); }

    @FXML
    public void loadSalesHistory() { loadView("SalesHistoryView.fxml"); }

    @FXML
    public void loadCashiers() { loadView("CashierDashboard.fxml"); }

    @FXML
    public void loadSettings() { loadView("SettingsView.fxml"); }

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
            stage.setMaximized(true);
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
}