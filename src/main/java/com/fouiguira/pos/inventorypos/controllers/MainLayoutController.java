package com.fouiguira.pos.inventorypos.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;

import java.io.IOException;

@Component
public class MainLayoutController {

    @FXML
    private BorderPane mainLayout;

    @FXML
    private MFXButton dashboardButton,manage_cashiersButton,categoryButton, productsButton, salesHistoryButton, cashierButton, settingsButton, invoicesButton, exitButton;

    private final ApplicationContext context;
    private final UserService userService; // Now properly injected

    public MainLayoutController(ApplicationContext context, UserService userService) {
        this.context = context;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        // Load CSS if available
        String cssPath = getClass().getResource("/styles/styles.css") != null 
            ? getClass().getResource("/styles/styles.css").toExternalForm() 
            : null;
        if (cssPath != null) {
            mainLayout.getStylesheets().add(cssPath);
            System.out.println("MainLayoutController: Loaded styles.css from " + cssPath);
        } else {
            System.err.println("MainLayoutController: Warning - styles.css not found at /styles/styles.css");
        }

        // Set button actions
        dashboardButton.setOnAction(e -> loadView("AdminDashboard.fxml"));
        productsButton.setOnAction(e -> loadView("ProductView.fxml"));
        salesHistoryButton.setOnAction(e -> loadView("SalesHistoryView.fxml"));
        cashierButton.setOnAction(e -> loadView("CashierDashboard.fxml"));
        manage_cashiersButton.setOnAction(e -> loadView("manage_cashiers.fxml"));
        settingsButton.setOnAction(e -> loadView("SettingsView.fxml"));
        invoicesButton.setOnAction(e -> loadView("InvoiceView.fxml"));
        categoryButton.setOnAction(e -> loadView("CategoryView.fxml"));
        exitButton.setOnAction(this::handleExit);

        // Apply role-based access
        configureAccess();

        // Load default view based on role
        User.Role role = userService.getCurrentUserRole();
        if (role == User.Role.OWNER) { // Assuming OWNER is admin equivalent
            loadView("AdminDashboard.fxml");
        } else {
            loadView("CashierDashboard.fxml");
        }
    }

    private void configureAccess() {
        User.Role role = userService.getCurrentUserRole();
        if (role == User.Role.CASHIER || role == User.Role.STAFF) {
            dashboardButton.setDisable(true);
            productsButton.setDisable(true);
            settingsButton.setDisable(true);
            invoicesButton.setDisable(true);
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            mainLayout.setCenter(view);
            
            // Remove any margins that might affect full-screen layout
            BorderPane.setMargin(view, new Insets(0));
            BorderPane.setAlignment(view, Pos.CENTER);
            
            // Make sure the main layout itself takes full size
            mainLayout.setPrefWidth(Region.USE_COMPUTED_SIZE);
            mainLayout.setPrefHeight(Region.USE_COMPUTED_SIZE);
            mainLayout.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            
            // Force the stage to maximize if not already
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            if (!stage.isMaximized()) {
                stage.setMaximized(true);
            }
        } catch (IOException e) {
            showAlert("Error", "Could not load view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleExit(ActionEvent actionEvent) {
        System.exit(0);
    }
}