package com.fouiguira.pos.inventorypos.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@Component
public class MainLayoutController {

    @FXML
    private BorderPane mainLayout;

    @FXML
    private Button dashboardButton, productsButton, salesButton, settingsButton;

    private final ApplicationContext context;

    @Autowired
    public MainLayoutController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        dashboardButton.setOnAction(e -> loadView("DashboardView.fxml"));
        productsButton.setOnAction(e -> loadView("ProductView.fxml"));
        salesButton.setOnAction(e -> loadView("SalesHistoryView.fxml"));
        settingsButton.setOnAction(e -> loadView("SettingsView.fxml"));

        // Load default view (Dashboard)
        loadView("DashboardView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            loader.setControllerFactory(context::getBean); // Spring injects controllers
            Parent view = loader.load();
            mainLayout.setCenter(view);
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

    public void handleExit(ActionEvent actionEvent) {
        System.exit(0);
    }
}
