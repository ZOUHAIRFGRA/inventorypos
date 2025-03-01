package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML
    private ComboBox<String> categoryComboBox; // ComboBox for categories
    @FXML
    private TextField searchField; // Search field to enter product name

    @Autowired
    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(this::loadProducts);
        setupTable();
        loadCategories(); // Load categories into the ComboBox
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

    private void loadCategories() {
        // For demonstration purposes, let's assume these are predefined categories
        categoryComboBox.getItems().addAll("Category 1", "Category 2", "Category 3", "Category 4");
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
            loadImageFromPath(selectedProduct.getImagePath());
        }
    }

    private void loadImageFromPath(String imagePath) {
        File file = new File(imagePath);
        if (file.exists() && file.isFile()) {
            productImage.setImage(new Image(file.toURI().toString()));
        } else {
            productImage.setImage(null); // or set a default image
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim();
        String selectedCategory = categoryComboBox.getValue();

        // If category is selected, filter by category
        List<Product> filteredProducts;
        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            filteredProducts = productService.getProductsByCategory(selectedCategory);
        } else {
            filteredProducts = productService.getAllProducts();
        }

        // If search text is provided, filter by product name as well
        if (searchText != null && !searchText.isEmpty()) {
            filteredProducts.removeIf(product -> !product.getName().toLowerCase().contains(searchText.toLowerCase()));
        }

        productTable.getItems().setAll(filteredProducts);
    }

    @FXML
    public void handleAddProduct() {
        try {
            if (validateFields()) {
                Product product = createProductFromFields();
                productService.createProduct(product);
                showAlert(Alert.AlertType.INFORMATION, "Product added successfully!");
                clearFields();
                loadProducts();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProduct() {
        try {
            if (validateFields()) {
                Long productId = getProductId();
                Product product = createProductFromFields();
                product.setId(productId);
                productService.updateProduct(productId, product);
                showAlert(Alert.AlertType.INFORMATION, "Product updated successfully!");
                clearFields();
                loadProducts();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update product: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteProduct() {
        try {
            Long productId = getProductId();
            productService.deleteProduct(productId);
            showAlert(Alert.AlertType.INFORMATION, "Product deleted successfully!");
            clearFields();
            loadProducts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to delete product: " + e.getMessage());
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

    private boolean validateFields() {
        try {
            if (productNameField.getText().isEmpty() || categoryField.getText().isEmpty() || priceField.getText().isEmpty() || stockField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please fill in all required fields.");
                return false;
            }
            // Validate numeric fields
            Double.parseDouble(priceField.getText());
            Integer.parseInt(stockField.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Please enter valid numbers for price and stock.");
            return false;
        }
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

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
