package com.fouiguira.pos.inventorypos.main;

import com.fouiguira.pos.inventorypos.InventoryposApplication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class MainApp extends Application {

    private static ApplicationContext springContext;

    @Override
    public void start(Stage stage) throws Exception {
        springContext = SpringApplication.run(InventoryposApplication.class);

        // Load FXML from resources/view/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainLayout.fxml"));
        loader.setControllerFactory(springContext::getBean);

        // Debug: Print FXML path
        System.out.println("MainLayout.fxml path: " + getClass().getResource("/view/MainLayout.fxml"));

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

    public static void main(String[] args) {
        launch(args);
    }
}