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
import javafx.application.Platform;
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
import java.util.Locale;
import java.util.function.Consumer;

public class EditProductController {

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
    private final Product productToEdit;
    private final Consumer<Void> refreshCallback;

    public EditProductController(ProductService productService, CategoryService categoryService, Product productToEdit, Consumer<Void> refreshCallback) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productToEdit = productToEdit;
        this.refreshCallback = refreshCallback;
    }

    @FXML
    public void initialize() {
        loadCategories();
        // Move everything into Platform.runLater to ensure proper initialization order
        Platform.runLater(() -> {
            if (productToEdit != null) {
                populateFields();
                setupValidation();  // Setup validation after fields are populated
            }
        });
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
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                return categoryComboBox.getItems().stream()
                        .filter(c -> c.getName().equals(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void populateFields() {
        // First populate the category
        if (productToEdit.getCategory() != null) {
            for (Category category : categoryComboBox.getItems()) {
                if (category.getId().equals(productToEdit.getCategory().getId())) {
                    categoryComboBox.setValue(category);
                    break;
                }
            }
        }
        
        // Then populate other fields with explicit Locale.US for decimal format
        productNameField.setText(productToEdit.getName());
        priceField.setText(String.format(Locale.US, "%.2f", productToEdit.getPrice()));
        purchasePriceField.setText(String.format(Locale.US, "%.2f", productToEdit.getPurchasePrice()));
        stockField.setText(String.valueOf(productToEdit.getStockQuantity()));
        imagePathField.setText(productToEdit.getImagePath());
        descriptionField.setText(productToEdit.getDescription());

        // Load image if it exists
        if (productToEdit.getImagePath() != null && !productToEdit.getImagePath().isEmpty()) {
            File file = new File(productToEdit.getImagePath());
            if (file.exists()) {
                productImage.setImage(new Image(file.toURI().toString(), 150, 150, true, true));
            }
        }
    }

    private void setupValidation() {
        // Set up text formatters that don't interfere with existing values
        priceField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            return newText.matches("^\\d*\\.?\\d*$") ? change : null;
        }));
        
        purchasePriceField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            return newText.matches("^\\d*\\.?\\d*$") ? change : null;
        }));
        
        stockField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            return newText.matches("^\\d*$") ? change : null;
        }));
        
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
            Product updatedProduct = createUpdatedProduct();
            productService.updateProduct(productToEdit.getId(), updatedProduct);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
            refreshCallback.accept(null);
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private Product createUpdatedProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setId(productToEdit.getId());
        updatedProduct.setName(productNameField.getText());
        updatedProduct.setCategory(categoryComboBox.getValue());
        updatedProduct.setPrice(Double.parseDouble(priceField.getText()));
        updatedProduct.setPurchasePrice(Double.parseDouble(purchasePriceField.getText()));
        updatedProduct.setStockQuantity(Integer.parseInt(stockField.getText()));
        updatedProduct.setInitialStock(productToEdit.getInitialStock()); // Preserve initial stock
        updatedProduct.setImagePath(imagePathField.getText());
        updatedProduct.setDescription(descriptionField.getText());
        updatedProduct.setUpdatedAt(new Date());
        updatedProduct.setCreatedAt(productToEdit.getCreatedAt());
        return updatedProduct;
    }

    private boolean validateFields() {
        if (productNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Product name is required.");
            return false;
        }
        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a category.");
            return false;
        }
        if (priceField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Price is required.");
            return false;
        }
        if (purchasePriceField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Purchase price is required.");
            return false;
        }
        if (stockField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Stock quantity is required.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText());
            double purchasePrice = Double.parseDouble(purchasePriceField.getText());
            if (purchasePrice >= price) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Purchase price must be less than selling price.");
                return false;
            }
            Integer.parseInt(stockField.getText());
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