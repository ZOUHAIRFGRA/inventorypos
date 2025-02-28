package com.fouiguira.pos.inventorypos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

@Component
public class SalesHistoryController {

    @FXML
    private TableView<?> salesTable; // To be replaced with actual sales data

    @FXML
    public void initialize() {
        // Load sales data logic (if needed)
    }
}
