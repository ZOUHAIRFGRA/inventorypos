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
        // Load FXML from resources/view/ (changed to Login.fxml as per your requirement)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        loader.setControllerFactory(springContext::getBean);

        // Debug: Print FXML path
        System.out.println("Login.fxml path: " + getClass().getResource("/view/Login.fxml"));

        // Load the FXML and create the scene
        Scene scene = new Scene(loader.load());

        // Load CSS from resources/styles/
        String cssPath = getClass().getResource("/styles/styles.css") != null 
            ? getClass().getResource("/styles/styles.css").toExternalForm() 
            : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
            System.out.println("Styles.css path: " + cssPath);
        } else {
            System.err.println("Error: styles.css not found at /styles/styles.css");
        }

        // Set up the stage
        stage.setScene(scene);
        stage.setTitle("Inventory POS System");
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        // Close Spring context when JavaFX stops
        if (springContext != null) {
            ((ConfigurableApplicationContext) springContext).close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}