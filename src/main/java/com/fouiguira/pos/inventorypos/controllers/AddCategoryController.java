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

public class AddCategoryController {

    @FXML
    private MFXTextField categoryNameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private MFXButton saveButton;

    @FXML
    private MFXButton cancelButton;

    private final CategoryService categoryService;
    private final Consumer<Void> refreshCallback;

    public AddCategoryController(CategoryService categoryService, Consumer<Void> refreshCallback) {
        this.categoryService = categoryService;
        this.refreshCallback = refreshCallback != null ? refreshCallback : v -> {};
    }

    @FXML
    public void initialize() {
        configureFields();
    }

    private void configureFields() {
        // No specific formatting needed for text fields
    }

    @FXML
    public void handleSaveCategory() {
        if (!validateFields()) {
            return;
        }

        try {
            Category category = createCategoryFromFields();
            categoryService.createCategory(category);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
            refreshCallback.accept(null);
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private Category createCategoryFromFields() {
        Category category = new Category();
        category.setName(categoryNameField.getText());
        category.setDescription(descriptionField.getText());
        return category;
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