package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductAnalyticsController {

    @FXML private BarChart<String, Number> topSellingChart;
    @FXML private PieChart categoryDistributionChart;
    
    @FXML private TableView<Product> stockLevelsTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> statusColumn;
    
    @FXML private TableView<Product> priceAnalysisTable;
    @FXML private TableColumn<Product, String> priceProductNameColumn;
    @FXML private TableColumn<Product, Double> sellingPriceColumn;
    @FXML private TableColumn<Product, Double> purchasePriceColumn;
    @FXML private TableColumn<Product, String> marginColumn;

    private final ProductService productService;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public ProductAnalyticsController(ProductService productService) {
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        setupTopSellingChart();
        setupCategoryDistribution();
        setupStockLevelsTable();
        setupPriceAnalysisTable();
        loadData();
    }

    private void setupTopSellingChart() {
        topSellingChart.setAnimated(false);
        topSellingChart.setTitle("Top 5 Selling Products");
    }

    private void setupCategoryDistribution() {
        categoryDistributionChart.setAnimated(false);
        categoryDistributionChart.setTitle("Product Category Distribution");
    }

    private void setupStockLevelsTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategory() != null ? 
                cellData.getValue().getCategory().getName() : "No Category"
            )
        );
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        statusColumn.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getStockQuantity();
            String status = stock <= 0 ? "Out of Stock" :
                          stock < 5 ? "Low Stock" : "In Stock";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    private void setupPriceAnalysisTable() {
        priceProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        marginColumn.setCellValueFactory(cellData -> {
            double margin = ((cellData.getValue().getPrice() - cellData.getValue().getPurchasePrice()) 
                          / cellData.getValue().getPurchasePrice()) * 100;
            return new javafx.beans.property.SimpleStringProperty(df.format(margin) + "%");
        });
    }

    private void loadData() {
        try {
            // Load top selling products
            Map<Product, Integer> topProducts = productService.getTopSellingProducts(5);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Units Sold");
            
            if (topProducts.isEmpty()) {
                series.getData().add(new XYChart.Data<>("No Data", 0));
            } else {
                topProducts.forEach((product, quantity) -> 
                    series.getData().add(new XYChart.Data<>(product.getName(), quantity))
                );
            }
            topSellingChart.getData().add(series);

            // Load category distribution
            List<Product> products = productService.getAllProducts();
            Map<String, Long> categoryCount = products.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                    Collectors.counting()
                ));
            
            if (categoryCount.isEmpty()) {
                categoryDistributionChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("No Data", 1)
                ));
            } else {
                categoryDistributionChart.setData(
                    categoryCount.entrySet().stream()
                        .map(entry -> new PieChart.Data(
                            entry.getKey() + " (" + entry.getValue() + ")", 
                            entry.getValue()
                        ))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList))
                );
            }

            // Load stock levels table
            stockLevelsTable.setItems(FXCollections.observableArrayList(products));
            
            // Load price analysis table
            priceAnalysisTable.setItems(FXCollections.observableArrayList(products));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}