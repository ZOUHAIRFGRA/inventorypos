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
        // Start Spring Boot backend
        springContext = SpringApplication.run(InventoryposApplication.class);

        // Load JavaFX UI from FXML
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/MainLayout.fxml"));
        loader.setControllerFactory(springContext::getBean); // Let Spring inject controllers
        System.out.println(getClass().getClassLoader().getResource("view/MainLayout.fxml")
);

        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Inventory POS System");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
