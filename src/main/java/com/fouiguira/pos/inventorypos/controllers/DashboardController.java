package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import org.springframework.stereotype.Controller;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.layout.VBox;
import java.text.DecimalFormat;
import java.util.ArrayList;

@Controller
public class DashboardController {

    @FXML private Label totalSalesLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label avgTicketLabel;
    @FXML private Label growthLabel;
    @FXML private Label dateLabel;
    
    @FXML private PieChart categoryDistributionChart;
    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private BarChart<String, Number> topProductsChart;
    
    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Number> stockLevelColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    
    @FXML private VBox recentSalesBox;

    private final SalesService salesService;
    private final ProductService productService;
    private final UserService userService;
    @SuppressWarnings("unused")
    private final CategoryService categoryService;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    
    public DashboardController(SalesService salesService, ProductService productService, 
                             UserService userService, CategoryService categoryService) {
        this.salesService = salesService;
        this.productService = productService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        updateSummaryMetrics();
        setupSalesTrendChart();
        setupTopProductsChart();
        setupCategoryDistributionChart();
        setupLowStockTable();
        loadRecentSales();
    }

    private void updateSummaryMetrics() {
        try {
            // Update basic metrics
            double totalSales = salesService.getSalesTotalByDate(LocalDate.now());
            totalSalesLabel.setText(String.format("$%s", df.format(totalSales)));
        } catch (Exception e) {
            totalSalesLabel.setText("N/A");
            totalSalesLabel.getStyleClass().add("error-label");
        }
        
        try {
            long totalProducts = productService.getAllProducts().size();
            totalProductsLabel.setText(String.valueOf(totalProducts));
        } catch (Exception e) {
            totalProductsLabel.setText("N/A");
            totalProductsLabel.getStyleClass().add("error-label");
        }
        
        try {
            long totalUsers = userService.getAllUsers().size();
            totalUsersLabel.setText(String.valueOf(totalUsers));
        } catch (Exception e) {
            totalUsersLabel.setText("N/A");
            totalUsersLabel.getStyleClass().add("error-label");
        }
        
        try {
            double avgTicket = salesService.getAverageTicketSize();
            avgTicketLabel.setText(String.format("$%s", df.format(avgTicket)));
        } catch (Exception e) {
            avgTicketLabel.setText("N/A");
            avgTicketLabel.getStyleClass().add("error-label");
        }
        
        try {
            double growth = salesService.getSalesGrowthRate();
            growthLabel.setText(String.format("%s%%", df.format(growth)));
            growthLabel.getStyleClass().add(growth >= 0 ? "growth-label-positive" : "growth-label-negative");
        } catch (Exception e) {
            growthLabel.setText("N/A");
            growthLabel.getStyleClass().add("error-label");
        }
    }

    private void setupSalesTrendChart() {
        try {
            salesTrendChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Sales");
            
            // Get last 7 days of sales
            LocalDate today = LocalDate.now();
            List<XYChart.Data<String, Number>> dataPoints = new ArrayList<>();
            
            IntStream.range(0, 7).mapToObj(i -> today.minusDays(i))
                .forEach(date -> {
                    try {
                        dataPoints.add(new XYChart.Data<>(
                            date.format(DateTimeFormatter.ofPattern("MM/dd")),
                            salesService.getSalesTotalByDate(date)
                        ));
                    } catch (Exception e) {
                        dataPoints.add(new XYChart.Data<>(
                            date.format(DateTimeFormatter.ofPattern("MM/dd")),
                            0
                        ));
                    }
                });
            
            if (dataPoints.stream().allMatch(data -> ((Number)data.getYValue()).doubleValue() == 0)) {
                // Handle empty state
                Label noDataLabel = new Label("No sales data available");
                noDataLabel.getStyleClass().add("chart-placeholder-label");
                salesTrendChart.setVisible(false);
                salesTrendChart.getParent().getChildrenUnmodifiable().add(noDataLabel);
            } else {
                salesTrendChart.setVisible(true);
                series.getData().addAll(dataPoints);
                salesTrendChart.getData().add(series);
            }
        } catch (Exception e) {
            // Handle error state
            Label errorLabel = new Label("Unable to load sales trend data");
            errorLabel.getStyleClass().add("error-label");
            salesTrendChart.setVisible(false);
            salesTrendChart.getParent().getChildrenUnmodifiable().add(errorLabel);
        }
    }

    private void setupTopProductsChart() {
        try {
            topProductsChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Units Sold");
            
            Map<Product, Integer> topProducts = productService.getTopSellingProducts(5);
            if (topProducts.isEmpty()) {
                // Handle empty state
                Label noDataLabel = new Label("No sales data available");
                noDataLabel.getStyleClass().add("chart-placeholder-label");
                topProductsChart.setVisible(false);
                topProductsChart.getParent().getChildrenUnmodifiable().add(noDataLabel);
            } else {
                topProducts.forEach((product, quantity) -> 
                    series.getData().add(new XYChart.Data<>(product.getName(), quantity))
                );
                topProductsChart.setVisible(true);
                topProductsChart.getData().add(series);
            }
        } catch (Exception e) {
            // Handle error state
            Label errorLabel = new Label("Unable to load top products data");
            errorLabel.getStyleClass().add("error-label");
            topProductsChart.setVisible(false);
            topProductsChart.getParent().getChildrenUnmodifiable().add(errorLabel);
        }
    }

    private void setupCategoryDistributionChart() {
        try {
            categoryDistributionChart.getData().clear();
            List<Product> products = productService.getAllProducts();
            Map<String, Long> categoryCount = products.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                    Collectors.counting()
                ));
            
            if (categoryCount.isEmpty()) {
                // Handle empty state
                Label noDataLabel = new Label("No category data available");
                noDataLabel.getStyleClass().add("chart-placeholder-label");
                categoryDistributionChart.setVisible(false);
                categoryDistributionChart.getParent().getChildrenUnmodifiable().add(noDataLabel);
            } else {
                categoryDistributionChart.setVisible(true);
                categoryDistributionChart.setData(
                    categoryCount.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList))
                );
            }
        } catch (Exception e) {
            // Handle error state
            Label errorLabel = new Label("Unable to load category distribution data");
            errorLabel.getStyleClass().add("error-label");
            categoryDistributionChart.setVisible(false);
            categoryDistributionChart.getParent().getChildrenUnmodifiable().add(errorLabel);
        }
    }

    private void setupLowStockTable() {
        try {
            List<Product> lowStockProducts = productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() < 5)
                .collect(Collectors.toList());
            
            productNameColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
            
            stockLevelColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStockQuantity()));
            
            categoryColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getCategory() != null ? 
                    cellData.getValue().getCategory().getName() : "No Category"
                ));
            
            lowStockTable.setItems(FXCollections.observableArrayList(lowStockProducts));
        } catch (Exception e) {
            lowStockTable.setPlaceholder(new Label("Unable to load low stock data"));
        }
    }

    private void loadRecentSales() {
        try {
            List<Sale> recentSales = salesService.getRecentSales(5);
            recentSalesBox.getChildren().clear();
            if (recentSales.isEmpty()) {
                Label noSalesLabel = new Label("No recent sales");
                noSalesLabel.getStyleClass().add("info-label");
                recentSalesBox.getChildren().add(noSalesLabel);
                return;
            }
            recentSales.forEach(sale -> {
                try {
                    Label saleLabel = new Label(String.format("%s - $%s (%s)",
                        sale.getClientName(),
                        df.format(sale.getTotalPrice()),
                        sale.getPaymentMethod()
                    ));
                    saleLabel.getStyleClass().add("recent-sale-item");
                    recentSalesBox.getChildren().add(saleLabel);
                } catch (Exception e) {
                    Label errorLabel = new Label("Error loading sale");
                    errorLabel.getStyleClass().add("error-label");
                    recentSalesBox.getChildren().add(errorLabel);
                }
            });
        } catch (Exception e) {
            Label errorLabel = new Label("Unable to load recent sales");
            errorLabel.getStyleClass().add("error-label");
            recentSalesBox.getChildren().add(errorLabel);
        }
    }
}