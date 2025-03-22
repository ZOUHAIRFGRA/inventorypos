package com.fouiguira.pos.inventorypos.main;

import com.fouiguira.pos.inventorypos.InventoryposApplication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApp extends Application {

    public static ApplicationContext springContext;

    @Override
    public void init() {
        // Initialize Spring context before JavaFX start
        springContext = SpringApplication.run(InventoryposApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
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

        // Configure stage for full-screen
        stage.setScene(scene);
        stage.setTitle("Inventory POS System");
        stage.setMaximized(true);
        
        // Set minimum size to prevent too small windows
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        stage.show();
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