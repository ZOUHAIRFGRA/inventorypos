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
package com.fouiguira.pos.inventorypos.components;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen {
    private Stage stage;
    private ProgressBar progressBar;
    private Label statusLabel;

    public SplashScreen() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        createSplashScreen();
    }

    private void createSplashScreen() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Load application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            root.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Could not load splash screen icon: " + e.getMessage());
        }

        Label titleLabel = new Label("Inventory POS System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);

        statusLabel = new Label("Starting...");
        statusLabel.setStyle("-fx-font-size: 14px;");

        root.getChildren().addAll(titleLabel, progressBar, statusLabel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(300);
        stage.centerOnScreen();
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.close();
    }

    public void updateProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
    }
}