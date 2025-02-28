package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.Date;
import java.util.List;

@Controller
public class ProductsController {

    private final ProductService productService;

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Long> colId;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Integer> colStock;
    @FXML
    private TextField productNameField, categoryField, priceField, stockField, imagePathField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ImageView productImage;
    @FXML
    private Button addButton, updateButton, deleteButton;

    @Autowired
    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(this::loadProducts);
        setupTable();
        productTable.setOnMouseClicked(event -> populateFieldsFromSelection());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
    }

    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        productTable.getItems().setAll(products);
    }

    @FXML
    private void populateFieldsFromSelection() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            productNameField.setText(selectedProduct.getName());
            categoryField.setText(selectedProduct.getCategory());
            priceField.setText(String.valueOf(selectedProduct.getPrice()));
            stockField.setText(String.valueOf(selectedProduct.getStockQuantity()));
            imagePathField.setText(selectedProduct.getImagePath());
            descriptionField.setText(selectedProduct.getDescription());

            // Load image if path is valid
            File file = new File(selectedProduct.getImagePath());
            if (file.exists()) {
                productImage.setImage(new Image(file.toURI().toString()));
            } else {
                productImage.setImage(null);
            }
        }
    }

    @FXML
    public void handleAddProduct() {
        try {
            Product product = createProductFromFields();
            productService.createProduct(product);
            showAlert(AlertType.INFORMATION, "Product added successfully!");
            clearFields();
            loadProducts();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProduct() {
        try {
            Long productId = getProductId();
            Product product = createProductFromFields();
            product.setId(productId);
            productService.updateProduct(productId, product);
            showAlert(AlertType.INFORMATION, "Product updated successfully!");
            clearFields();
            loadProducts();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Failed to update product: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteProduct() {
        try {
            Long productId = getProductId();
            productService.deleteProduct(productId);
            showAlert(AlertType.INFORMATION, "Product deleted successfully!");
            clearFields();
            loadProducts();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Failed to delete product: " + e.getMessage());
        }
    }

    private Long getProductId() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            throw new IllegalArgumentException("Please select a product to proceed.");
        }
        return selectedProduct.getId();
    }

    private Product createProductFromFields() {
        String name = productNameField.getText();
        String category = categoryField.getText();
        double price = Double.parseDouble(priceField.getText());
        int stockQuantity = Integer.parseInt(stockField.getText());
        String imagePath = imagePathField.getText();
        String description = descriptionField.getText();

        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImagePath(imagePath);
        product.setDescription(description);
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());

        return product;
    }

    private void clearFields() {
        productNameField.clear();
        categoryField.clear();
        priceField.clear();
        stockField.clear();
        imagePathField.clear();
        descriptionField.clear();
        productImage.setImage(null);
        productTable.getSelectionModel().clearSelection();
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
