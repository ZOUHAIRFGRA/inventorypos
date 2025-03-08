package com.fouiguira.pos.inventorypos.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainLayoutController {

    @FXML
    private BorderPane mainLayout;

    @FXML
    private MFXButton dashboardButton, productsButton;

    private final ApplicationContext context;

    @Autowired
    public MainLayoutController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        // Load CSS if available, otherwise log a warning
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
        dashboardButton.setOnAction(e -> loadView("DashboardView.fxml"));
        productsButton.setOnAction(e -> loadView("ProductView.fxml"));
        loadView("DashboardView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            loader.setControllerFactory(context::getBean);
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

    @FXML
    public void handleExit(ActionEvent actionEvent) {
        System.exit(0);
    }
}