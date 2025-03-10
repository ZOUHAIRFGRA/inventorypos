package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.LongFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Controller
public class CategoryController {

    @FXML
    private MFXTableView<Category> categoryTable;

    @FXML
    private MFXTableColumn<Category> colId;

    @FXML
    private MFXTableColumn<Category> colName;

    @FXML
    private MFXTableColumn<Category> colDescription;

    @FXML
    private MFXTableColumn<Category> colActions;

    @FXML
    private MFXButton addButton;

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadCategories();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        colId.setRowCellFactory(category -> new MFXTableRowCell<>(Category::getId));
        colId.setComparator(Comparator.comparing(Category::getId));

        colName.setRowCellFactory(category -> new MFXTableRowCell<>(Category::getName));
        colName.setComparator(Comparator.comparing(Category::getName));

        colDescription.setRowCellFactory(category -> new MFXTableRowCell<>(c -> c.getDescription() != null ? c.getDescription() : "No description"));
        colDescription.setComparator(Comparator.comparing(Category::getDescription, Comparator.nullsLast(Comparator.naturalOrder())));

        colActions.setRowCellFactory(category -> {
            MFXTableRowCell<Category, Void> cell = new MFXTableRowCell<>(c -> null);
            cell.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);

            MFXButton editButton = new MFXButton("Edit");
            editButton.getStyleClass().add("button-edit");
            editButton.setOnAction(event -> openEditCategoryView(category));

            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().add("button-delete");
            deleteButton.setOnAction(event -> handleDeleteCategory(category));

            HBox actions = new HBox(10, editButton, deleteButton);
            actions.setAlignment(javafx.geometry.Pos.CENTER);
            cell.setGraphic(actions);

            return cell;
        });

        categoryTable.getFilters().addAll(
            new LongFilter<>("ID", Category::getId),
            new StringFilter<>("Name", Category::getName),
            new StringFilter<>("Description", Category::getDescription) // Added filter for description
        );

        categoryTable.setFooterVisible(true);
        categoryTable.autosizeColumnsOnInitialization();
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryTable.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    public void openAddCategoryView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddCategoryView.fxml"));
            loader.setControllerFactory(c -> new AddCategoryController(categoryService, v -> loadCategories()));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Category");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditCategoryView(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditCategoryView.fxml"));
            loader.setControllerFactory(c -> new EditCategoryController(categoryService, category, v -> loadCategories()));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Category");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteCategory(Category category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Category");
        confirmation.setHeaderText("Are you sure you want to delete " + category.getName() + "?");
        confirmation.setContentText("This action cannot be undone.");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    categoryService.deleteCategory(category.getId());
                    loadCategories();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}