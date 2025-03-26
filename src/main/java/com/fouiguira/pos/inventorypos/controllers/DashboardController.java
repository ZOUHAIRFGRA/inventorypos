package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.Category;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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
        // Update basic metrics
        double totalSales = salesService.getSalesTotalByDate(LocalDate.now());
        long totalProducts = productService.getAllProducts().size();
        long totalUsers = userService.getAllUsers().size();
        
        totalSalesLabel.setText(String.format("$%s", df.format(totalSales)));
        totalProductsLabel.setText(String.valueOf(totalProducts));
        totalUsersLabel.setText(String.valueOf(totalUsers));
        
        // Add new metrics
        double avgTicket = salesService.getAverageTicketSize();
        avgTicketLabel.setText(String.format("$%s", df.format(avgTicket)));
        
        double growth = salesService.getSalesGrowthRate();
        growthLabel.setText(String.format("%s%%", df.format(growth)));
        growthLabel.getStyleClass().add(growth >= 0 ? "growth-label-positive" : "growth-label-negative");
    }

    private void setupSalesTrendChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");
        
        // Get last 7 days of sales
        LocalDate today = LocalDate.now();
        IntStream.range(0, 7).mapToObj(i -> today.minusDays(i))
            .forEach(date -> {
                series.getData().add(new XYChart.Data<>(
                    date.format(DateTimeFormatter.ofPattern("MM/dd")),
                    salesService.getSalesTotalByDate(date)
                ));
            });
        
        salesTrendChart.getData().add(series);
    }

    private void setupTopProductsChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Units Sold");
        
        Map<Product, Integer> topProducts = productService.getTopSellingProducts(5);
        topProducts.forEach((product, quantity) -> 
            series.getData().add(new XYChart.Data<>(product.getName(), quantity))
        );
        
        topProductsChart.getData().add(series);
    }

    private void setupCategoryDistributionChart() {
        List<Product> products = productService.getAllProducts();
        Map<String, Long> categoryCount = products.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                Collectors.counting()
            ));
        
        categoryDistributionChart.setData(
            categoryCount.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList))
        );
    }

    private void setupLowStockTable() {
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
    }

    private void loadRecentSales() {
        List<Sale> recentSales = salesService.getRecentSales(5);
        recentSales.forEach(sale -> {
            Label saleLabel = new Label(String.format("%s - $%s (%s)",
                sale.getClientName(),
                df.format(sale.getTotalPrice()),
                sale.getPaymentMethod()
            ));
            saleLabel.getStyleClass().add("recent-sale-item");
            recentSalesBox.getChildren().add(saleLabel);
        });
    }

    private void handleError() {
        totalSalesLabel.setText("Error");
        totalProductsLabel.setText("Error");
        totalUsersLabel.setText("Error");
        avgTicketLabel.setText("Error");
        growthLabel.setText("Error");
    }
}