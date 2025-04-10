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
package com.fouiguira.pos.inventorypos.main;

import com.fouiguira.pos.inventorypos.InventoryposApplication;
import com.fouiguira.pos.inventorypos.components.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.concurrent.CountDownLatch;
import java.io.InputStream;

public class MainApp extends Application {

    public static ApplicationContext springContext;
    private SplashScreen splashScreen;
    private final CountDownLatch splashScreenLatch = new CountDownLatch(1);

    @Override
    public void init() throws Exception {
        // Create and show splash screen on FX thread and wait for it to be ready
        Platform.runLater(() -> {
            try {
                splashScreen = new SplashScreen();
                splashScreen.show();
                splashScreenLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Wait for splash screen to be created
        splashScreenLatch.await();

        // Now we can safely use the splash screen
        Platform.runLater(() -> splashScreen.updateStatus("Initializing application..."));
        
        // Initialize Spring context
        springContext = SpringApplication.run(InventoryposApplication.class);
        
        Platform.runLater(() -> splashScreen.updateProgress(0.5));
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Platform.runLater(() -> splashScreen.updateStatus("Loading interface..."));
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Scene scene = new Scene(loader.load());
            
            // Load CSS
            String cssPath = getClass().getResource("/styles/styles.css") != null 
                ? getClass().getResource("/styles/styles.css").toExternalForm() 
                : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }

            // Set application icon
            try (InputStream iconStream = getClass().getResourceAsStream("/images/icon.png")) {
                if (iconStream != null) {
                    stage.getIcons().add(new Image(iconStream));
                } else {
                    System.err.println("Could not load application icon");
                }
            }

            // Configure stage
            stage.setScene(scene);
            stage.setTitle("Inventory POS System");
            stage.setMaximized(true);
            stage.setMinWidth(1024);
            stage.setMinHeight(768);
            
            Platform.runLater(() -> {
                splashScreen.updateProgress(1.0);
                splashScreen.updateStatus("Ready!");
            });

            // Small delay to show "Ready!" message
            Thread.sleep(800);
            
            Platform.runLater(() -> {
                splashScreen.hide();
                stage.show();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            if (splashScreen != null) {
                Platform.runLater(() -> {
                    splashScreen.updateStatus("Error: " + e.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    splashScreen.hide();
                });
            }
            throw e;
        }
    }

    @Override
    public void stop() {
        if (springContext != null) {
            ((ConfigurableApplicationContext) springContext).close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}