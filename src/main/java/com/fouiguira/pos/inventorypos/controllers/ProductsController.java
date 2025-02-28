package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;

@Controller
public class ProductsController {

    private final ProductService productService;

    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField imagePathField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    @Autowired
    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        loadProducts();
        productComboBox.setOnAction(e -> populateFieldsFromSelection());
    }

    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        productComboBox.getItems().setAll(products);
    }

    @FXML
    private void populateFieldsFromSelection() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            productNameField.setText(selectedProduct.getName());
            categoryField.setText(selectedProduct.getCategory());
            priceField.setText(String.valueOf(selectedProduct.getPrice()));
            stockField.setText(String.valueOf(selectedProduct.getStockQuantity()));
            imagePathField.setText(selectedProduct.getImagePath());
            descriptionField.setText(selectedProduct.getDescription());
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
        } catch (IllegalArgumentException e) {
            showAlert(AlertType.ERROR, e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProduct() {
        try {
            Long productId = getProductId();
            Product product = createProductFromFields();
            productService.updateProduct(productId, product);
            showAlert(AlertType.INFORMATION, "Product updated successfully!");
            clearFields();
            loadProducts();
        } catch (IllegalArgumentException e) {
            showAlert(AlertType.ERROR, e.getMessage());
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
        } catch (IllegalArgumentException e) {
            showAlert(AlertType.ERROR, e.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Failed to delete product: " + e.getMessage());
        }
    }

    private Long getProductId() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            throw new IllegalArgumentException("Please select a product to proceed.");
        }
        return selectedProduct.getId();
    }

    private Product createProductFromFields() {
        String name = productNameField.getText();
        String category = categoryField.getText();
        double price = parseDouble(priceField.getText(), "Price");
        int stockQuantity = parseInteger(stockField.getText(), "Stock Quantity");
        String imagePath = imagePathField.getText();
        String description = descriptionField.getText();

        if (name.isEmpty() || category.isEmpty()) {
            throw new IllegalArgumentException("Name and category cannot be empty");
        }

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

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer.");
        }
    }

    private void clearFields() {
        productNameField.clear();
        categoryField.clear();
        priceField.clear();
        stockField.clear();
        imagePathField.clear();
        descriptionField.clear();
        productComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
