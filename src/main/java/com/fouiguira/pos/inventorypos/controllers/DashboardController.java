package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class DashboardController {

    @FXML
    private Label totalSalesLabel;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label totalUsersLabel;

    private final SalesService salesService;
    private final ProductService productService;
    private final UserService userService;

    
    public DashboardController(SalesService salesService, ProductService productService, UserService userService) {
        this.salesService = salesService;
        this.productService = productService;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        try {
            // Populate summary data
            double totalSales = salesService.getSalesTotalByDate(LocalDate.now());
            long totalProducts = productService.getAllProducts().size();
            long totalUsers = userService.getAllUsers().size();

            totalSalesLabel.setText(String.format("$%.2f", totalSales));
            totalProductsLabel.setText(String.valueOf(totalProducts));
            totalUsersLabel.setText(String.valueOf(totalUsers));
        } catch (Exception e) {
            e.printStackTrace();
            totalSalesLabel.setText("Error");
            totalProductsLabel.setText("Error");
            totalUsersLabel.setText("Error");
        }
    }
}