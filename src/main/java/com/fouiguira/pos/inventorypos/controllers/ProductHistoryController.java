/*
 * Inventory POS System
 * Copyright (c) 2025 ZOUHAIR FOUIGUIRA. All rights reserved.
 *
 * Licensed under Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International
 * You may not use this file except in compliance with the License.
 *
 * @author ZOUHAIR FOUIGUIRA
 * @version 1.0
 * @since 2025-04-14
 */
package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;

import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import org.springframework.stereotype.Controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductHistoryController {

    @FXML private MFXComboBox<Product> productComboBox;
    @FXML private MFXDatePicker startDatePicker;
    @FXML private MFXDatePicker endDatePicker;
    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private Label totalSoldLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label avgUnitsLabel;
    @FXML private MFXTableView<SaleProduct> historyTable;
    @FXML private MFXTableColumn<SaleProduct> colDate;
    @FXML private MFXTableColumn<SaleProduct> colQuantity;
    @FXML private MFXTableColumn<SaleProduct> colClient;
    @FXML private MFXTableColumn<SaleProduct> colCashier;
    @FXML private MFXTableColumn<SaleProduct> colUnitPrice;
    @FXML private MFXTableColumn<SaleProduct> colTotalPrice;

    private final ProductService productService;
    private final SaleProductService saleProductService;
    @SuppressWarnings("unused")
    private final SalesService salesService;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ProductHistoryController(ProductService productService, 
                                  SaleProductService saleProductService,
                                  SalesService salesService) {
        this.productService = productService;
        this.saleProductService = saleProductService;
        this.salesService = salesService;
    }

    @FXML
    public void initialize() {
        setupProductComboBox();
        setupDatePickers();
        setupTable();
        setupListeners();
    }

    private void setupProductComboBox() {
        List<Product> products = productService.getAllProducts();
        productComboBox.setItems(FXCollections.observableArrayList(products));
        productComboBox.setConverter(new javafx.util.StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                return product == null ? "" : product.getName();
            }

            @Override
            public Product fromString(String string) {
                return productComboBox.getItems().stream()
                        .filter(p -> p.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void setupDatePickers() {
        // Default to last 30 days
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }

    private void setupTable() {
        colDate.setRowCellFactory(sp -> {
            MFXTableRowCell<SaleProduct, String> cell = new MFXTableRowCell<>(
                item -> dateFormat.format(item.getSale().getTimestamp())
            );
            return cell;
        });

        colQuantity.setRowCellFactory(sp -> 
            new MFXTableRowCell<>(SaleProduct::getQuantity));

        colClient.setRowCellFactory(sp -> 
            new MFXTableRowCell<>(item -> item.getSale().getClientName()));

        colCashier.setRowCellFactory(sp -> 
            new MFXTableRowCell<>(item -> item.getSale().getCashier().getUsername()));

        colUnitPrice.setRowCellFactory(sp -> {
            MFXTableRowCell<SaleProduct, String> cell = new MFXTableRowCell<>(
                item -> df.format(item.getProduct().getPrice()) + " DH"
            );
            return cell;
        });

        colTotalPrice.setRowCellFactory(sp -> {
            MFXTableRowCell<SaleProduct, String> cell = new MFXTableRowCell<>(
                item -> df.format(item.getQuantity() * item.getProduct().getPrice()) + " DH"
            );
            cell.setStyle("-fx-font-weight: bold;");
            return cell;
        });
    }

    private void setupListeners() {
        productComboBox.valueProperty().addListener((obs, old, newVal) -> updateHistory());
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> updateHistory());
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> updateHistory());
    }

    public void selectProduct(Product product) {
        Platform.runLater(() -> {
            productComboBox.setValue(product);
            // Set default date range to last 30 days
            endDatePicker.setValue(LocalDate.now());
            startDatePicker.setValue(LocalDate.now().minusMonths(1));
            updateHistory();
        });
    }

    private void updateHistory() {
        Product selectedProduct = productComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedProduct == null || startDate == null || endDate == null) {
            return;
        }

        List<SaleProduct> history = saleProductService.getSaleProductsByProduct(selectedProduct)
            .stream()
            .filter(sp -> {
                Date saleDate = sp.getSale().getTimestamp();
                LocalDate localSaleDate = saleDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                return !localSaleDate.isBefore(startDate) && !localSaleDate.isAfter(endDate);
            })
            .sorted(Comparator.comparing(sp -> sp.getSale().getTimestamp()))
            .collect(Collectors.toList());

        updateTable(history);
        updateChart(history);
        updateMetrics(history);
    }

    private void updateTable(List<SaleProduct> history) {
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    private void updateChart(List<SaleProduct> history) {
        salesTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Units Sold");

        // Group sales by date and sum quantities
        Map<LocalDate, Integer> dailySales = history.stream()
            .collect(Collectors.groupingBy(
                sp -> sp.getSale().getTimestamp().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                Collectors.summingInt(SaleProduct::getQuantity)
            ));

        // Sort by date and add to chart
        dailySales.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String date = entry.getKey().format(DateTimeFormatter.ofPattern("MM/dd"));
                series.getData().add(new XYChart.Data<>(date, entry.getValue()));
            });

        salesTrendChart.getData().add(series);
    }

    private void updateMetrics(List<SaleProduct> history) {
        int totalUnits = history.stream()
            .mapToInt(SaleProduct::getQuantity)
            .sum();

        double totalRevenue = history.stream()
            .mapToDouble(sp -> sp.getQuantity() * sp.getProduct().getPrice())
            .sum();

        double avgUnits = history.isEmpty() ? 0 : 
            (double) totalUnits / history.stream()
                .map(sp -> sp.getSale().getId())
                .distinct()
                .count();

        totalSoldLabel.setText(String.valueOf(totalUnits));
        totalRevenueLabel.setText(df.format(totalRevenue) + " DH");
        avgUnitsLabel.setText(df.format(avgUnits));
    }
}
