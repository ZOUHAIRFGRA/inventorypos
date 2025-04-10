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

import com.fouiguira.pos.inventorypos.entities.BusinessSettings;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.main.MainApp;
import com.fouiguira.pos.inventorypos.services.interfaces.BusinessSettingsService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.InputStream;
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
    @FXML
    private Label businessNameLabel;
    @FXML
    private Label welcomeMessageLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Label businessAddressLabel;
    @FXML
    private Label businessPhoneLabel;
    @FXML
    private Label businessEmailLabel;

    private final UserService userService;
    private final BusinessSettingsService settingsService;

    public LoginController(UserService userService, BusinessSettingsService settingsService) {
        this.userService = userService;
        this.settingsService = settingsService;
    }

    @FXML
    public void initialize() {
        try {
            BusinessSettings settings = settingsService.getSettings();

            // Set business name with placeholder
            String businessName = settings.getBusinessName();
            if (businessName == null || businessName.trim().isEmpty()) {
                businessName = "Welcome to Business Management System";
            }
            businessNameLabel.setText(businessName);
            welcomeMessageLabel.setText("Welcome to " + businessName);

            // Set address with placeholder
            String address = settings.getAddress();
            businessAddressLabel.setText(address != null && !address.trim().isEmpty()
                    ? address
                    : "Address not configured");

            // Set phone with placeholder
            String phone = settings.getPhone();
            businessPhoneLabel.setText(phone != null && !phone.trim().isEmpty()
                    ? phone
                    : "Phone not configured");

            // Set email with placeholder
            String email = settings.getEmail();
            businessEmailLabel.setText(email != null && !email.trim().isEmpty()
                    ? email
                    : "Email not configured");

            // Load logo image
            String logoPath = settings.getLogoPath();
            try {
                Image logo;
                if (logoPath != null && !logoPath.isEmpty() && new File(logoPath).exists()) {
                    logo = new Image(new File(logoPath).toURI().toString());
                } else {
                    // Load default logo from resources
                    InputStream is = getClass().getResourceAsStream("/images/icon.png");
                    if (is != null) {
                        logo = new Image(is);
                        is.close();
                    } else {
                        throw new IOException("Default logo not found");
                    }
                }
                logoImageView.setImage(logo);
                logoImageView.setFitWidth(150);
                logoImageView.setFitHeight(150);
                logoImageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
            }

            // Style placeholder text with italic and lighter color
            if (address == null || address.trim().isEmpty()) {
                businessAddressLabel.setStyle(
                        businessAddressLabel.getStyle() + "; -fx-font-style: italic; -fx-text-fill: #95a5a6;");
            }
            if (phone == null || phone.trim().isEmpty()) {
                businessPhoneLabel
                        .setStyle(businessPhoneLabel.getStyle() + "; -fx-font-style: italic; -fx-text-fill: #95a5a6;");
            }
            if (email == null || email.trim().isEmpty()) {
                businessEmailLabel
                        .setStyle(businessEmailLabel.getStyle() + "; -fx-font-style: italic; -fx-text-fill: #95a5a6;");
            }

        } catch (Exception e) {
            System.err.println("Error loading business settings: " + e.getMessage());
            // Set default values if settings fail to load
            businessNameLabel.setText("Business Management System");
            welcomeMessageLabel.setText("Welcome to Business Management System");
            businessAddressLabel.setText("Address not configured");
            businessPhoneLabel.setText("Phone not configured");
            businessEmailLabel.setText("Email not configured");

            // Style all placeholders
            businessAddressLabel
                    .setStyle(businessAddressLabel.getStyle() + "; -fx-font-style: italic; -fx-text-fill: #95a5a6;");
            businessPhoneLabel
                    .setStyle(businessPhoneLabel.getStyle() + "; -fx-font-style: italic; -fx-text-fill: #95a5a6;");
            businessEmailLabel
                    .setStyle(businessEmailLabel.getStyle() + "; -fx-font-style: italic; -fx-text-fill: #95a5a6;");
        }
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
                } else if (user.getRole() == User.Role.SUPPORT_ADMIN) {
                    fxmlPath = "/view/SupportDashboard.fxml";
                    title = "Support Administrator Dashboard";
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