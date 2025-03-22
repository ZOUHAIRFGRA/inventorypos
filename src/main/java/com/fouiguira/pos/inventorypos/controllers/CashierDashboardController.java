package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.*;
import com.fouiguira.pos.inventorypos.services.interfaces.*;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class CashierDashboardController {

    @FXML private MFXTextField searchField;
    @FXML private MFXComboBox<Category> categoryComboBox;
    @FXML private GridPane productGrid;

    @FXML private TableView<SaleProduct> cartTable;
    private TableColumn<SaleProduct, Void> cartImageCol;
    private TableColumn<SaleProduct, String> cartNameCol;
    private TableColumn<SaleProduct, Integer> cartQtyCol;
    private TableColumn<SaleProduct, Double> cartPriceCol;
    @FXML private MFXTextField clientNameField;
    @FXML private Label cartTotalLabel;
    @FXML private MFXComboBox<String> paymentMethodComboBox;
    @FXML private MFXButton checkoutButton;
    @FXML private MFXButton clearCartButton;

    @FXML private VBox cartVBox;
    @FXML private HBox totalHBox;
    @FXML private Label welcomeLabel;
    @FXML private MFXButton logoutButton;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SalesService salesService;
    private final InvoiceService invoiceService;
    private final UserService userService;
    private final ApplicationContext context;

    private ObservableList<SaleProduct> cartItems = FXCollections.observableArrayList();
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final String PLACEHOLDER_IMAGE = "/images/placeholder.png";

    public CashierDashboardController(ProductService productService, CategoryService categoryService,
                                      SalesService salesService, InvoiceService invoiceService,
                                      UserService userService, ApplicationContext context) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.salesService = salesService;
        this.invoiceService = invoiceService;
        this.userService = userService;
        this.context = context;
    }

    @FXML
    public void initialize() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in.");
            handleLogout();
            return;
        }

        if (currentUser.isTemporaryPassword()) {
            loadChangePasswordView();
            return;
        }

        if (productGrid == null) {
            System.err.println("Error: productGrid is null - FXML injection failed!");
            return;
        }

        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        cartTotalLabel = new Label("$0.00");
        cartTotalLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");
        totalHBox.getChildren().add(cartTotalLabel);

        setupCartTable();
        loadCategories();
        loadProducts();
        setupPaymentMethods();

        Platform.runLater(() -> {
            System.out.println("Product grid children: " + productGrid.getChildren().size());
        });
    }

    @SuppressWarnings("unchecked")
    private void setupCartTable() {
        cartTable.getColumns().clear();

        // Image column
        cartImageCol = new TableColumn<>("Image");
        cartImageCol.setPrefWidth(60);
        cartImageCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    SaleProduct saleProduct = getTableRow().getItem();
                    String imagePath = saleProduct.getProduct().getImagePath();
                    if (imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists()) {
                        imageView.setImage(new Image(new File(imagePath).toURI().toString()));
                    } else {
                        imageView.setImage(new Image(getClass().getResourceAsStream(PLACEHOLDER_IMAGE)));
                    }
                    setGraphic(imageView);
                }
            }
        });

        // Name column
        cartNameCol = new TableColumn<>("Name");
        cartNameCol.setPrefWidth(100);
        cartNameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getProduct().getName()
            )
        );

        // Quantity column
        cartQtyCol = new TableColumn<>("Qty");
        cartQtyCol.setPrefWidth(60);
        cartQtyCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getQuantity()
            ).asObject()
        );
        cartQtyCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText(quantity.toString());
                }
            }
        });

        // Price column
        cartPriceCol = new TableColumn<>("Price");
        cartPriceCol.setPrefWidth(80);
        cartPriceCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getProduct().getPrice()
            ).asObject()
        );
        cartPriceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText("$" + df.format(price));
                }
            }
        });

        cartTable.getColumns().addAll(cartImageCol, cartNameCol, cartQtyCol, cartPriceCol);
        cartTable.setItems(cartItems);
        cartTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "All Categories" : category.getName();
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

    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        System.out.println("Loaded products: " + products);
        updateProductGrid(products);
    }

    private void updateProductGrid(List<Product> products) {
        productGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        for (Product product : products) {
            VBox productTile = createProductTile(product);
            productGrid.add(productTile, col, row);
            col++;
            if (col > 3) { // 4 products per row
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductTile(Product product) {
        VBox tile = new VBox(5);
        tile.setPrefSize(150, 150);
        tile.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-alignment: center;");
        tile.setOnMouseClicked(e -> handleAddToCart(product));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        String imagePath = product.getImagePath();
        Image image = null;

        try {
            if (imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists()) {
                image = new Image(new File(imagePath).toURI().toString());
                System.out.println("Loaded product image: " + imagePath);
            } else {
                InputStream placeholderStream = getClass().getResourceAsStream(PLACEHOLDER_IMAGE);
                if (placeholderStream != null) {
                    image = new Image(placeholderStream);
                    System.out.println("Loaded placeholder image: " + PLACEHOLDER_IMAGE);
                } else {
                    System.err.println("Placeholder image not found: " + PLACEHOLDER_IMAGE);
                    image = new Image("https://via.placeholder.com/100x100.png?text=No+Image");
                }
            }
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image for product " + product.getName() + ": " + e.getMessage());
            imageView.setImage(new Image("https://via.placeholder.com/100x100.png?text=No+Image"));
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-wrap-text: true; -fx-max-width: 130;");
        Label priceLabel = new Label("$" + df.format(product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        tile.getChildren().addAll(imageView, nameLabel, priceLabel);
        return tile;
    }

    private void setupPaymentMethods() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("Cash", "Card"));
        paymentMethodComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        List<Product> filteredProducts = selectedCategory != null 
            ? productService.getProductsByCategory(selectedCategory.getName()) 
            : productService.getAllProducts();

        if (!searchText.isEmpty()) {
            filteredProducts.removeIf(p -> !p.getName().toLowerCase().contains(searchText));
        }
        updateProductGrid(filteredProducts);
    }

    @FXML
    public void handleCategorySelection() {
        handleSearch();
    }

    private void handleAddToCart(Product selectedProduct) {
        Alert qtyAlert = new Alert(AlertType.CONFIRMATION);
        qtyAlert.setTitle("Add to Cart");
        qtyAlert.setHeaderText("Add " + selectedProduct.getName() + " to cart");
        qtyAlert.setContentText("Enter quantity (Available: " + selectedProduct.getStockQuantity() + "):");

        TextField qtyField = new TextField("1");
        qtyField.setPrefWidth(100);
        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Quantity:"), qtyField);
        qtyAlert.getDialogPane().setContent(content);

        qtyAlert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = qtyAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) {
                    showAlert(AlertType.WARNING, "Warning", "Quantity must be greater than 0!");
                    return;
                }
                if (qty > selectedProduct.getStockQuantity()) {
                    showAlert(AlertType.WARNING, "Warning", "Not enough stock! Available: " + selectedProduct.getStockQuantity());
                    return;
                }

                SaleProduct item = cartItems.stream()
                    .filter(i -> i.getProduct().getId().equals(selectedProduct.getId()))
                    .findFirst()
                    .orElse(new SaleProduct());
                if (item.getProduct() == null) {
                    item.setProduct(selectedProduct);
                    item.setQuantity(0);
                }
                int newQty = item.getQuantity() + qty;
                if (newQty <= selectedProduct.getStockQuantity()) {
                    item.setQuantity(newQty);
                    if (!cartItems.contains(item)) {
                        cartItems.add(item);
                    }
                    updateCartTotal();
                } else {
                    showAlert(AlertType.WARNING, "Warning", "Total quantity exceeds stock! Available: " + selectedProduct.getStockQuantity());
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.WARNING, "Warning", "Invalid quantity! Please enter a number.");
            }
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cart is empty!");
            return;
        }
        String clientName = clientNameField.getText().trim();
        if (clientName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a client name to complete the sale!");
            return;
        }

        Sale sale = new Sale();
        sale.setTimestamp(new Date());
        sale.setTotalPrice(cartItems.stream().mapToDouble(i -> i.getQuantity() * i.getProduct().getPrice()).sum());
        sale.setPaymentMethod(paymentMethodComboBox.getSelectionModel().getSelectedItem());
        sale.setCashier(userService.getCurrentUser());
        sale.setClientName(clientName);

        List<SaleProduct> saleProducts = new ArrayList<>();
        for (SaleProduct cartItem : cartItems) {
            SaleProduct saleProduct = new SaleProduct();
            saleProduct.setProduct(cartItem.getProduct());
            saleProduct.setQuantity(cartItem.getQuantity());
            saleProduct.setSale(sale);
            saleProducts.add(saleProduct);
        }
        sale.setProducts(saleProducts);

        try {
            Sale savedSale = salesService.createSale(sale);
            savedSale.getProducts().size();
            Invoice invoice = invoiceService.createInvoiceFromSale(savedSale.getId());
            productService.updateStockAfterSale(cartItems);
            invoiceService.generateInvoicePdf(invoice);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Checkout successful! Invoice #" + invoice.getId() + " generated.");
            clearCart();
            loadProducts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Checkout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClearCart() {
        clearCart();
    }

    @FXML
    public void handleLogout() {
        userService.logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventory POS System - Login");
            String cssPath = getClass().getResource("/styles/styles.css") != null 
                ? getClass().getResource("/styles/styles.css").toExternalForm() 
                : null;
            if (cssPath != null) {
                stage.getScene().getStylesheets().add(cssPath);
            }
            stage.show();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Logged out successfully. Contact an admin if you forget your password.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadChangePasswordView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/change_password.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent changePasswordView = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(changePasswordView));
            stage.setTitle("Change Password");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load password change view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCartTotal() {
        double total = cartItems.stream().mapToDouble(i -> i.getQuantity() * i.getProduct().getPrice()).sum();
        cartTotalLabel.setText("$" + df.format(total));
    }

    private void clearCart() {
        cartItems.clear();
        clientNameField.clear();
        updateCartTotal();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}