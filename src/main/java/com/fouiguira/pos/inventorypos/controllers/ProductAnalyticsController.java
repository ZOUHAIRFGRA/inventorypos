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
package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import org.springframework.stereotype.Controller;
import java.text.DecimalFormat;
import java.util.List;

@Controller
public class ProductAnalyticsController {
    
    @FXML private TableView<Product> priceAnalysisTable;
    @FXML private TableColumn<Product, String> priceProductNameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Double> sellingPriceColumn;
    @FXML private TableColumn<Product, Double> purchasePriceColumn;
    @FXML private TableColumn<Product, String> marginColumn;
    
    @FXML private Label avgMarginLabel;
    @FXML private Label highestMarginLabel;
    @FXML private Label lowestMarginLabel;
    
    @FXML private MFXTextField searchField;
    @FXML private MFXComboBox<Category> categoryFilter;
    @FXML private MFXComboBox<String> marginFilter;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private FilteredList<Product> filteredProducts;

    public ProductAnalyticsController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        priceAnalysisTable.getStyleClass().add("price-analysis-table");
        setupFilters();
        setupTable();
        loadData();
    }

    private void setupFilters() {
        // Setup category filter
        List<Category> categories = categoryService.getAllCategories();
        categoryFilter.setItems(FXCollections.observableArrayList(categories));
        categoryFilter.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "All Categories" : category.getName();
            }
            @Override
            public Category fromString(String string) {
                return categoryFilter.getItems().stream()
                        .filter(c -> c.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Setup margin range filter
        marginFilter.setItems(FXCollections.observableArrayList(
            "All",
            "Below 10%",
            "10% - 20%",
            "20% - 30%",
            "Above 30%"
        ));
        marginFilter.selectFirst();

        // Add listeners for filters
        searchField.textProperty().addListener((obs, old, newValue) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, old, newValue) -> applyFilters());
        marginFilter.valueProperty().addListener((obs, old, newValue) -> applyFilters());
    }

    private void setupTable() {
        priceProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategory() != null ? 
                cellData.getValue().getCategory().getName() : "No Category"
            )
        );
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        
        marginColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            double profit = product.getPrice() - product.getPurchasePrice();
            double margin = calculateMargin(product);
            // Changed format to avoid the problematic string format
            String display = df.format(margin) + "% (" + df.format(profit) + " DH)";
            return new javafx.beans.property.SimpleStringProperty(display);
        });

        // Add color styling based on margin
        marginColumn.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Extract margin value for coloring (get value before the % symbol)
                    try {
                        double margin = Double.parseDouble(item.substring(0, item.indexOf('%')).replace(",", ""));
                        setStyle(getMarginColor(margin));
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        // Enable sorting
        priceProductNameColumn.setSortable(true);
        categoryColumn.setSortable(true);
        sellingPriceColumn.setSortable(true);
        purchasePriceColumn.setSortable(true);
        marginColumn.setSortable(true);

        // Set default sorting by absolute profit value (descending)
        marginColumn.setComparator((s1, s2) -> {
            if (s1 == null || s2 == null) return 0;
            try {
                // Extract margin values for comparison
                double margin1 = Double.parseDouble(s1.substring(0, s1.indexOf('%')).replace(",", ""));
                double margin2 = Double.parseDouble(s2.substring(0, s2.indexOf('%')).replace(",", ""));
                return Double.compare(margin1, margin2);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void loadData() {
        try {
            List<Product> products = productService.getAllProducts();
            // Pre-sort the list by absolute profit
            products.sort((p1, p2) -> {
                double profit1 = p1.getPrice() - p1.getPurchasePrice();
                double profit2 = p2.getPrice() - p2.getPurchasePrice();
                return Double.compare(profit2, profit1); // Descending order
            });
            
            filteredProducts = new FilteredList<>(FXCollections.observableArrayList(products));
            priceAnalysisTable.setItems(filteredProducts);
            
            // Set the sort order
            priceAnalysisTable.getSortOrder().clear();
            priceAnalysisTable.getSortOrder().add(marginColumn);
            marginColumn.setSortType(TableColumn.SortType.DESCENDING);
            
            updateSummaryMetrics(products);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        Category selectedCategory = categoryFilter.getValue();
        String selectedMarginRange = marginFilter.getValue();

        filteredProducts.setPredicate(product -> {
            boolean matchesSearch = searchText.isEmpty() || 
                                  product.getName().toLowerCase().contains(searchText);
            
            boolean matchesCategory = selectedCategory == null || 
                                    (product.getCategory() != null && 
                                     product.getCategory().getId().equals(selectedCategory.getId()));
            
            boolean matchesMargin = selectedMarginRange.equals("All") ||
                                  matchesMarginRange(calculateMargin(product), selectedMarginRange);

            return matchesSearch && matchesCategory && matchesMargin;
        });

        updateSummaryMetrics(filteredProducts);
    }

    private double calculateMargin(Product product) {
        if (product == null || product.getPurchasePrice() == 0) return 0;
        return ((product.getPrice() - product.getPurchasePrice()) / product.getPurchasePrice()) * 100;
    }

    private boolean matchesMarginRange(double margin, String range) {
        return switch (range) {
            case "Below 10%" -> margin < 10;
            case "10% - 20%" -> margin >= 10 && margin < 20;
            case "20% - 30%" -> margin >= 20 && margin < 30;
            case "Above 30%" -> margin >= 30;
            default -> true;
        };
    }

    private void updateSummaryMetrics(List<Product> products) {
        if (products.isEmpty()) {
            avgMarginLabel.setText("N/A");
            highestMarginLabel.setText("N/A");
            lowestMarginLabel.setText("N/A");
            return;
        }

        double avgMargin = products.stream()
            .mapToDouble(this::calculateMargin)
            .average()
            .orElse(0);

        double highestMargin = products.stream()
            .mapToDouble(this::calculateMargin)
            .max()
            .orElse(0);

        double lowestMargin = products.stream()
            .mapToDouble(this::calculateMargin)
            .min()
            .orElse(0);

        avgMarginLabel.setText(df.format(avgMargin) + "%");
        highestMarginLabel.setText(df.format(highestMargin) + "%");
        lowestMarginLabel.setText(df.format(lowestMargin) + "%");

        // Set colors based on values
        avgMarginLabel.setStyle(getMarginColor(avgMargin));
        highestMarginLabel.setStyle(getMarginColor(highestMargin));
        lowestMarginLabel.setStyle(getMarginColor(lowestMargin));
    }

    private String getMarginColor(double margin) {
        if (margin < 10) {
            return "-fx-text-fill: #D32F2F;"; // Red for low margins
        } else if (margin < 20) {
            return "-fx-text-fill: #FFA726;"; // Orange for medium margins
        } else {
            return "-fx-text-fill: #388E3C;"; // Green for good margins
        }
    }
}