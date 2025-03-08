package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ProductsController {

    private final ProductService productService;
    private final CategoryService categoryService;

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
    private MFXTableColumn<Product> colStock;
    @FXML
    private MFXTableColumn<Product> colImage;
    @FXML
    private MFXTextField productNameField;
    @FXML
    private MFXTextField priceField;
    @FXML
    private MFXTextField stockField;
    @FXML
    private MFXTextField imagePathField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ImageView productImage;
    @FXML
    private MFXButton addButton;
    @FXML
    private MFXButton updateButton;
    @FXML
    private MFXButton deleteButton;
    @FXML
    private MFXComboBox<Category> categoryComboBox;
    @FXML
    private MFXTextField searchField;

    @Autowired
    public ProductsController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loadProducts();
            searchField.setFloatingText("Search");
            productNameField.setFloatingText("Product Name");
            priceField.setFloatingText("Price");
            stockField.setFloatingText("Stock Quantity");
            imagePathField.setFloatingText("Image Path");
        });
        setupTable();
        loadCategories();
        productTable.getSelectionModel().selectionProperty().addListener((obs, oldSel, newSel) -> populateFieldsFromSelection());
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

    private void setupTable() {
        // Configure MFXTableColumns
        colId.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getId));
        colName.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getName));
        colCategory.setRowCellFactory(product -> new MFXTableRowCell<>(p -> p.getCategory() != null ? p.getCategory().getName() : "No Category"));
        colPrice.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getPrice));
        colStock.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getStockQuantity));
    
        // Custom cell for image column
        colImage.setRowCellFactory(product -> {
            MFXTableRowCell<Product, String> cell = new MFXTableRowCell<>(Product::getImagePath);
            ImageView imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            cell.setGraphic(imageView); // Set ImageView as the graphic
            cell.textProperty().addListener((obs, oldText, newText) -> {
                if (newText != null && !newText.isEmpty()) {
                    File file = new File(newText);
                    if (file.exists()) {
                        imageView.setImage(new Image(file.toURI().toString(), 50, 50, true, true));
                    } else {
                        imageView.setImage(null);
                    }
                } else {
                    imageView.setImage(null);
                }
            });
            return cell;
        });
    
        // Enable selection
        productTable.getSelectionModel().setAllowsMultipleSelection(false);
    }
    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        productTable.setItems(FXCollections.observableArrayList(products));
    }

    @FXML
    private void populateFieldsFromSelection() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
        if (selectedProduct != null) {
            productNameField.setText(selectedProduct.getName());
            categoryComboBox.getSelectionModel().selectItem(selectedProduct.getCategory());
            priceField.setText(String.valueOf(selectedProduct.getPrice()));
            stockField.setText(String.valueOf(selectedProduct.getStockQuantity()));
            imagePathField.setText(selectedProduct.getImagePath());
            descriptionField.setText(selectedProduct.getDescription());
            loadImageFromPath(selectedProduct.getImagePath());
        }
    }

    private void loadImageFromPath(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists() && file.isFile()) {
                productImage.setImage(new Image(file.toURI().toString()));
            } else {
                productImage.setImage(null);
            }
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        List<Product> filteredProducts;

        if (selectedCategory != null) {
            filteredProducts = productService.getProductsByCategory(selectedCategory.getName());
        } else {
            filteredProducts = productService.getAllProducts();
        }

        if (!searchText.isEmpty()) {
            filteredProducts.removeIf(product -> !product.getName().toLowerCase().contains(searchText));
        }

        productTable.setItems(FXCollections.observableArrayList(filteredProducts));
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            imagePathField.setText(imagePath);
            loadImageFromPath(imagePath);
        }
    }

    @FXML
    public void handleCategorySelection() {
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        System.out.println("Selected Category: " + selectedCategory);

        if (selectedCategory != null) {
            List<Product> filteredProducts = productService.getProductsByCategory(selectedCategory.getName());
            productTable.setItems(FXCollections.observableArrayList(filteredProducts));
        } else {
            loadProducts();
        }
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
        Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
        if (selectedProduct == null) {
            throw new IllegalArgumentException("Please select a product to proceed.");
        }
        return selectedProduct.getId();
    }

    private Product createProductFromFields() {
        String name = productNameField.getText();
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a category.");
            throw new IllegalArgumentException("Category is required.");
        }

        double price = Double.parseDouble(priceField.getText());
        int stockQuantity = Integer.parseInt(stockField.getText());
        String imagePath = imagePathField.getText();
        String description = descriptionField.getText();

        Product product = new Product();
        product.setName(name);
        product.setCategory(selectedCategory);
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
            if (productNameField.getText().isEmpty() || categoryComboBox.getSelectionModel().getSelectedItem() == null ||
                    priceField.getText().isEmpty() || stockField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please fill in all required fields.");
                return false;
            }
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
        categoryComboBox.getSelectionModel().clearSelection();
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