package com.fouiguira.pos.inventorypos;

import com.fouiguira.pos.inventorypos.main.MainApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fouiguira.pos.inventorypos")
public class InventoryposApplication {
    private static final Logger logger = LoggerFactory.getLogger(InventoryposApplication.class);

    public static void main(String[] args) {
        logger.info("Application started.");
        try {
            // Launch the JavaFX application
            MainApp.main(args);
        } catch (Exception e) {
            logger.error("An error occurred: ", e);
        }
    }
}