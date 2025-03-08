package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Controller
public class DashboardController {

    @FXML
    private Label totalSalesTodayLabel, totalRevenueLabel, pendingPaymentsLabel;

    @FXML
    private TableView<Sale> recentSalesTable;
    @FXML
    private TableColumn<Sale, Long> colSaleId;
    @FXML
    private TableColumn<Sale, Double> colAmount;
    @FXML
    private TableColumn<Sale, String> colCashier;
    @FXML
    private TableColumn<Sale, String> colDate;

    @FXML
    private ListView<String> lowStockList;

    private final SaleService saleService;
    private final ProductService productService;

    @Autowired
    public DashboardController(SaleService saleService, ProductService productService) {
        this.saleService = saleService;
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(this::loadDashboardData);
    }

    private void loadDashboardData() {
        totalSalesTodayLabel.setText(String.format("$%.2f", saleService.getTotalSalesToday()));
        totalRevenueLabel.setText(String.format("$%.2f", saleService.getTotalRevenue()));
        pendingPaymentsLabel.setText(String.format("$%.2f", saleService.getPendingPayments()));

        loadRecentSales();
        loadLowStockAlerts();
    }

    private void loadRecentSales() {
        List<Sale> recentSales = saleService.getLast10Sales();

        colSaleId.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        colAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());
        colCashier.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCashier() != null ? cellData.getValue().getCashier().getUsername() : "N/A"));
        colDate.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getTimestamp();
            String formattedDate = "N/A";
            if (date != null) {
                LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            return new SimpleStringProperty(formattedDate);
        });

        recentSalesTable.getItems().setAll(recentSales);
    }

    private void loadLowStockAlerts() {
        List<Product> lowStockProducts = productService.getLowStockProducts(5);
        lowStockList.getItems().clear();
        for (Product product : lowStockProducts) {
            lowStockList.getItems().add(product.getName() + " (Stock: " + product.getStockQuantity() + ")");
        }
    }
}