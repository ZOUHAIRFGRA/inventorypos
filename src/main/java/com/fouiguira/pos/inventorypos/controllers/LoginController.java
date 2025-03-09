package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.main.MainApp;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class LoginController {

    @FXML
    private MFXTextField usernameField;

    @FXML
    private MFXPasswordField passwordField;

    @FXML
    private MFXButton loginButton;

    @FXML
    private Label messageLabel;

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        User user = userService.authenticate(username, password);
        if (user != null) {
            try {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                String fxmlPath;
                String title;
                if (user.getRole() == User.Role.OWNER) {
                    fxmlPath = "/view/AdminDashboard.fxml";
                    title = "Admin Dashboard";
                } else if (user.getRole() == User.Role.CASHIER) {
                    fxmlPath = "/view/CashierDashboard.fxml";
                    title = "Cashier Dashboard";
                } else {
                    messageLabel.setText("Role not supported!");
                    return;
                }

                // Debug: Check if FXML resource is found
                java.net.URL resource = getClass().getResource(fxmlPath);
                if (resource == null) {
                    messageLabel.setText("Error: " + fxmlPath + " not found!");
                    System.err.println("FXML file not found: " + fxmlPath);
                    return;
                }

                // Load FXML using MainApp's springContext
                FXMLLoader loader = new FXMLLoader(resource);
                loader.setControllerFactory(MainApp.springContext::getBean);
                Parent root = loader.load();

                // Create new scene and apply stylesheet
                Scene scene = new Scene(root);
                String cssPath = getClass().getResource("/styles/styles.css") != null 
                    ? getClass().getResource("/styles/styles.css").toExternalForm() 
                    : null;
                if (cssPath != null) {
                    scene.getStylesheets().add(cssPath);
                    System.out.println("LoginController: Applied styles.css to " + fxmlPath + " - " + cssPath);
                } else {
                    System.err.println("LoginController: Warning - styles.css not found at /styles/styles.css");
                }

                // Set the scene and show the stage
                stage.setScene(scene);
                stage.setTitle(title);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Error loading dashboard: " + e.getMessage());
            }
        } else {
            messageLabel.setText("Invalid username or password!");
        }
    }
}