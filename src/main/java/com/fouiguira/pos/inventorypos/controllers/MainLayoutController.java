package com.fouiguira.pos.inventorypos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class MainLayoutController {

    @FXML
    private BorderPane mainLayout; // Reference to the main layout

    @FXML
    private Button loadProductsButton; // Reference to the Load Products button

    @FXML
    private MenuBar menuBar; // Reference to the MenuBar if you have one

    @FXML
    public void initialize() {
        // Initialize any necessary data or state here
        loadProductsButton.setOnAction(e -> handleLoadProducts());
    }

    // Method to handle loading products when the button is clicked
    @FXML
    private void handleLoadProducts() {
        try {
            // Logic to switch to ProductView (assuming it's defined)
            loadProductView();
        } catch (Exception e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void loadProductView() {
        try {
            // Load ProductView.fxml here
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/ProductView.fxml"));
            Parent productView = loader.load();
            mainLayout.setCenter(productView); // Set the loaded view in the center of the main layout
        } catch (IOException e) {
            showAlert("Error", "Could not load product view: " + e.getMessage());
        }
    }

    // Method to handle exit action from the menu
    @FXML
    private void handleExit() {
        // Logic to exit the application
        System.exit(0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
