package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;

import java.text.DecimalFormat;
import java.util.List;

@Controller
public class ProductAnalyticsController {
    
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
        setupPriceAnalysisTable();
        loadData();
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
            List<Product> products = productService.getAllProducts();
            priceAnalysisTable.setItems(FXCollections.observableArrayList(products));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}