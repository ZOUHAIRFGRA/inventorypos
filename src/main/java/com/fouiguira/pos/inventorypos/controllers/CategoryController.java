package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class CategoryController {

    @FXML
    private MFXComboBox<Category> categoryComboBox;

    @FXML
    private MFXButton addCategoryButton, editCategoryButton, deleteCategoryButton;

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(this::loadCategories);
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "Select Category";
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

    @FXML
    public void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter new category name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                Category category = new Category();
                category.setName(name);
                categoryService.createCategory(category);
                showAlert(Alert.AlertType.INFORMATION, "Category added successfully!");
                loadCategories();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to add category: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleEditCategory() {
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a category to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedCategory.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit category name:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                selectedCategory.setName(name);
                categoryService.updateCategory(selectedCategory);
                showAlert(Alert.AlertType.INFORMATION, "Category updated successfully!");
                loadCategories();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to update category: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleDeleteCategory() {
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a category to delete.");
            return;
        }

        try {
            categoryService.deleteCategory(selectedCategory.getId());
            showAlert(Alert.AlertType.INFORMATION, "Category deleted successfully!");
            loadCategories();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to delete category: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}