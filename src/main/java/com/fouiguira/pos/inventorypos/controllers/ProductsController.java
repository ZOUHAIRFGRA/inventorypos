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
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.DoubleFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.LongFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Controller
public class ProductsController {

    @FXML
    private MFXTableView<Product> productTable;

    @FXML
    private MFXTableColumn<Product> colId;

    @FXML
    private MFXTableColumn<Product> colName;

    @FXML
    private MFXTableColumn<Product> colCategory;

    @FXML
    private MFXTableColumn<Product> colPrice;

    @FXML
    private MFXTableColumn<Product> colPurchasePrice;

    @FXML
    private MFXTableColumn<Product> colStock;

    @FXML
    private MFXTableColumn<Product> colActions;

    @FXML
    private MFXButton addButton;

    @FXML
    private MFXTextField searchField;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private MFXButton exportButton;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SaleProductService saleProductService;
    private final SalesService salesService;

    public ProductsController(ProductService productService, 
                            CategoryService categoryService,
                            SaleProductService saleProductService,
                            SalesService salesService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.saleProductService = saleProductService;
        this.salesService = salesService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadProducts();
        updateStatusBar();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        colId.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getId));
        colId.setComparator(Comparator.comparing(Product::getId));

        colName.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getName));
        colName.setComparator(Comparator.comparing(Product::getName));

        colCategory.setRowCellFactory(product -> new MFXTableRowCell<>(p -> p.getCategory() != null ? p.getCategory().getName() : "No Category"));
        colCategory.setComparator(Comparator.comparing(p -> p.getCategory() != null ? p.getCategory().getName() : "No Category"));

        colPrice.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getPrice));
        colPrice.setComparator(Comparator.comparing(Product::getPrice));

        colPurchasePrice.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getPurchasePrice));
        colPurchasePrice.setComparator(Comparator.comparing(Product::getPurchasePrice));

        colStock.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getStockQuantity));
        colStock.setComparator(Comparator.comparing(Product::getStockQuantity));

        colActions.setRowCellFactory(product -> {
            MFXTableRowCell<Product, Void> cell = new MFXTableRowCell<>(p -> null);
            cell.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);

            MFXButton editButton = new MFXButton("Edit");
            editButton.getStyleClass().add("button-edit");
            editButton.setOnAction(event -> {
                Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
                if (selectedProduct != null) {
                    openEditProductView(selectedProduct);
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
                }
            });

            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().add("button-delete");
            deleteButton.setOnAction(event -> {
                Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
                if (selectedProduct != null) {
                    handleDeleteProduct(selectedProduct);
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
                }
            });

            MFXButton historyButton = new MFXButton("History");
            historyButton.getStyleClass().add("button-history");
            historyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            historyButton.setOnAction(event -> {
                Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
                if (selectedProduct != null) {
                    openProductHistory(selectedProduct);
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to view its history.");
                }
            });

            MFXButton viewImgButton = new MFXButton("View Img");
            viewImgButton.getStyleClass().add("button-view-img");
            viewImgButton.setOnAction(event -> {
                Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
                if (selectedProduct != null) {
                    String imagePath = selectedProduct.getImagePath();
                    if (imagePath == null || imagePath.isEmpty()) {
                        imagePath = "/images/placeholder.png";
                    }
                    showEnlargedImage(imagePath);
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to view its image.");
                }
            });

            HBox actions = new HBox(5, editButton, deleteButton, historyButton, viewImgButton);
            actions.setAlignment(javafx.geometry.Pos.CENTER);
            cell.setGraphic(actions);

            return cell;
        });

        productTable.getFilters().addAll(
            new LongFilter<>("ID", Product::getId),
            new StringFilter<>("Name", Product::getName),
            new StringFilter<>("Category", p -> p.getCategory() != null ? p.getCategory().getName() : "No Category"),
            new DoubleFilter<>("Price", Product::getPrice),
            new DoubleFilter<>("Purchase Price", Product::getPurchasePrice),
            new IntegerFilter<>("Stock", Product::getStockQuantity)
        );

        productTable.setFooterVisible(true);
        productTable.autosizeColumnsOnInitialization();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterProducts(newValue);
            }
        });
    }

    private void filterProducts(String searchText) {
        if (searchText.isEmpty()) {
            loadProducts();
            return;
        }
        
        List<Product> allProducts = productService.getAllProducts();
        List<Product> filteredProducts = allProducts.stream()
            .filter(product -> 
                product.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                (product.getCategory() != null && 
                 product.getCategory().getName().toLowerCase().contains(searchText.toLowerCase())) ||
                String.valueOf(product.getPrice()).contains(searchText) ||
                String.valueOf(product.getPurchasePrice()).contains(searchText) ||
                String.valueOf(product.getStockQuantity()).contains(searchText)
            )
            .toList();
        
        productTable.setItems(FXCollections.observableArrayList(filteredProducts));
        updateStatusBar();
    }

    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        productTable.setItems(FXCollections.observableArrayList(products));
        updateStatusBar();
    }

    private void updateStatusBar() {
        List<Product> allProducts = productService.getAllProducts();
        long lowStockCount = allProducts.stream()
            .filter(p -> p.getStockQuantity() < 5)
            .count();
        
        totalProductsLabel.setText(String.format("Total Products: %d", allProducts.size()));
        
        if (lowStockCount > 0) {
            lowStockLabel.setText(String.format("Low Stock Alert: %d items", lowStockCount));
            lowStockLabel.setVisible(true);
        } else {
            lowStockLabel.setVisible(false);
        }
    }

    @FXML
    public void openAddProductView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddProductView.fxml"));
            loader.setControllerFactory(c -> new AddProductController(productService, categoryService, v -> loadProducts()));
            Parent root = loader.load();            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Product");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);  // Prevent resizing
            stage.sizeToScene();        // Set stage size to match the scene
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditProductView(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProductView.fxml"));
            loader.setControllerFactory(c -> new EditProductController(productService, categoryService, product, v -> loadProducts()));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Product");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteProduct(Product product) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Product");
        confirmation.setHeaderText("Are you sure you want to delete " + product.getName() + "?");
        confirmation.setContentText("This action cannot be undone.");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    productService.deleteProduct(product.getId());
                    loadProducts();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + e.getMessage());
                }
            }
        });
    }

    private void showEnlargedImage(String imagePath) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Enlarged Image");

        ImageView enlargedImageView = new ImageView();
        enlargedImageView.setImage(new Image(new File(imagePath).toURI().toString(), 300, 300, true, true));
        enlargedImageView.setPreserveRatio(true);

        StackPane pane = new StackPane(enlargedImageView);
        pane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10;");

        Scene scene = new Scene(pane, 320, 320);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    public void handleExport() {
        // Implementation for exporting products to CSV
        try {
            List<Product> products = productService.getAllProducts();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "products_export_" + timestamp + ".csv";
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            File file = fileChooser.showSaveDialog(productTable.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write CSV header
                    writer.println("ID,Name,Category,Price,Purchase Price,Stock,Description");
                    
                    // Write product data
                    for (Product product : products) {
                        writer.println(String.format("%d,\"%s\",\"%s\",%.2f,%.2f,%d,\"%s\"",
                            product.getId(),
                            product.getName(),
                            product.getCategory() != null ? product.getCategory().getName() : "",
                            product.getPrice(),
                            product.getPurchasePrice(),
                            product.getStockQuantity(),
                            product.getDescription() != null ? product.getDescription() : ""
                        ));
                    }
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Products exported successfully to " + file.getName());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to export products: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openProductHistory(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProductHistoryView.fxml"));
            loader.setControllerFactory(c -> new ProductHistoryController(productService, saleProductService, salesService));
            Parent root = loader.load();

            // Get the controller and pre-select the product
            ProductHistoryController controller = loader.getController();
            controller.selectProduct(product);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Product Sales History - " + product.getName());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open product history: " + e.getMessage());
        }
    }
}