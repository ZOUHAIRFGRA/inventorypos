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
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class EditCategoryController {

    @FXML
    private MFXTextField categoryNameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private MFXButton saveButton;

    @FXML
    private MFXButton cancelButton;

    private final CategoryService categoryService;
    private final Category categoryToEdit;
    private final Consumer<Void> refreshCallback;

    public EditCategoryController(CategoryService categoryService, Category categoryToEdit, Consumer<Void> refreshCallback) {
        this.categoryService = categoryService;
        this.categoryToEdit = categoryToEdit;
        this.refreshCallback = refreshCallback != null ? refreshCallback : v -> {};
    }

    @FXML
    public void initialize() {
        configureFields();
        populateFields();
    }

    private void configureFields() {
        // No specific formatting needed for text fields
    }

    private void populateFields() {
        categoryNameField.setText(categoryToEdit.getName());
        descriptionField.setText(categoryToEdit.getDescription());
    }

    @FXML
    public void handleSaveCategory() {
        if (!validateFields()) {
            return;
        }

        try {
            Category updatedCategory = createUpdatedCategory();
            categoryService.updateCategory(categoryToEdit.getId(), updatedCategory);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
            refreshCallback.accept(null);
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private Category createUpdatedCategory() {
        Category updatedCategory = new Category();
        updatedCategory.setId(categoryToEdit.getId());
        updatedCategory.setName(categoryNameField.getText());
        updatedCategory.setDescription(descriptionField.getText());
        return updatedCategory;
    }

    private boolean validateFields() {
        if (categoryNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Category name is required.");
            return false;
        }
        return true; // Description is optional
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