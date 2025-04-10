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

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class AddProductController {

    @FXML private MFXTextField productNameField;
    @FXML private MFXComboBox<Category> categoryComboBox;
    @FXML private MFXTextField priceField;
    @FXML private MFXTextField purchasePriceField;
    @FXML private MFXTextField stockField;
    @FXML private MFXTextField imagePathField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView productImage;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton cancelButton;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final Consumer<Void> refreshCallback;

    public AddProductController(ProductService productService, CategoryService categoryService, Consumer<Void> refreshCallback) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.refreshCallback = refreshCallback;
    }

    @FXML
    public void initialize() {
        loadCategories();
        configureFields();
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "Select Category" : category.getName();
            }

            @Override
            public Category fromString(String string) {
                return categoryComboBox.getItems().stream()
                        .filter(c -> c.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void configureFields() {
        // Only allow numbers and decimal points for price fields
        priceField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> 
            change.getControlNewText().matches("^\\d*\\.?\\d*$") ? change : null));
        purchasePriceField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> 
            change.getControlNewText().matches("^\\d*\\.?\\d*$") ? change : null));
        stockField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> 
            change.getControlNewText().matches("^\\d*$") ? change : null));
        imagePathField.setEditable(false);
    }

    @FXML
    public void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(productImage.getScene().getWindow());
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            imagePathField.setText(imagePath);
            productImage.setImage(new Image(selectedFile.toURI().toString(), 150, 150, true, true));
        }
    }

    @FXML
    public void handleSaveProduct() {
        if (!validateFields()) {
            return;
        }

        try {
            Product product = createProductFromFields();
            productService.createProduct(product);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
            refreshCallback.accept(null);
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private Product createProductFromFields() {
        Product product = new Product();
        product.setName(productNameField.getText());
        product.setCategory(categoryComboBox.getValue());
        
        String priceText = priceField.getText().trim();
        String purchasePriceText = purchasePriceField.getText().trim();
        
        try {
            product.setPrice(Double.parseDouble(priceText));
            product.setPurchasePrice(Double.parseDouble(purchasePriceText));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format");
        }
        
        int initialStock = Integer.parseInt(stockField.getText().trim());
        product.setStockQuantity(initialStock);
        product.setInitialStock(initialStock);
        product.setImagePath(imagePathField.getText());
        product.setDescription(descriptionField.getText());
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        return product;
    }

    private boolean validateFields() {
        if (productNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Product name is required.");
            return false;
        }
        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a category.");
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Selling price is required.");
            return false;
        }
        if (purchasePriceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Purchase price is required.");
            return false;
        }
        if (stockField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Stock quantity is required.");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            double purchasePrice = Double.parseDouble(purchasePriceField.getText().trim());
            if (price <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Selling price must be greater than 0.");
                return false;
            }
            if (purchasePrice <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Purchase price must be greater than 0.");
                return false;
            }
            if (purchasePrice >= price) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Purchase price must be less than selling price.");
                return false;
            }
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Stock quantity cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price, purchase price and stock must be valid numbers.");
            return false;
        }
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}