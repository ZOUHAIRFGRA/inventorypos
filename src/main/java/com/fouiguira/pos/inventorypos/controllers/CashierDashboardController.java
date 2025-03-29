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
import java.util.concurrent.atomic.AtomicInteger;

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
    @FXML private Label cartItemCountLabel;

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
        cartTotalLabel = new Label("0.00DH");
        cartTotalLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");
        totalHBox.getChildren().add(cartTotalLabel);

        setupKeyboardShortcuts();
        setupCartTable();
        loadCategories();
        loadProducts();
        setupPaymentMethods();

        Platform.runLater(() -> {
            System.out.println("Product grid children: " + productGrid.getChildren().size());
        });
    }

    private void setupKeyboardShortcuts() {
        Scene scene = productGrid.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(event -> {
                if (event.isControlDown()) {
                    switch (event.getCode()) {
                        case F -> handleSearch();
                        case C -> handleClearCart();
                        case ENTER -> handleCheckout();
                        case L -> handleLogout();
                        default -> { /* Do nothing */ }
                    }
                }
            });
        } else {
            Platform.runLater(this::setupKeyboardShortcuts);
        }
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

        // Quantity column with adjustment buttons
        cartQtyCol = new TableColumn<>("Quantity");
        cartQtyCol.setPrefWidth(120);
        cartQtyCol.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox(5);
            private final MFXButton minusBtn = new MFXButton();
            private final Label qtyLabel = new Label();
            private final MFXButton plusBtn = new MFXButton();
            
            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                String btnStyle = "-fx-min-width: 24px; -fx-min-height: 24px; -fx-max-width: 24px; -fx-max-height: 24px;";
                
                // Add minus icon
                Label minusIcon = new Label("−");
                minusIcon.setStyle("-fx-font-family: 'System'; -fx-font-weight: bold;");
                minusBtn.setGraphic(minusIcon);
                minusBtn.setStyle(btnStyle + "-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-background-radius: 12;");
                
                // Add plus icon
                Label plusIcon = new Label("+");
                plusIcon.setStyle("-fx-font-family: 'System'; -fx-font-weight: bold;");
                plusBtn.setGraphic(plusIcon);
                plusBtn.setStyle(btnStyle + "-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-background-radius: 12;");
                
                qtyLabel.setStyle("-fx-min-width: 30px; -fx-alignment: center; -fx-font-size: 13px;");
                
                minusBtn.setOnAction(e -> {
                    SaleProduct item = getTableRow().getItem();
                    if (item != null && item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        updateItem(item.getQuantity(), false);
                        updateCartTotal();
                    }
                });
                
                plusBtn.setOnAction(e -> {
                    SaleProduct item = getTableRow().getItem();
                    if (item != null && item.getQuantity() < item.getProduct().getStockQuantity()) {
                        item.setQuantity(item.getQuantity() + 1);
                        updateItem(item.getQuantity(), false);
                        updateCartTotal();
                    }
                });
                
                box.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
            }
            
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setGraphic(null);
                } else {
                    qtyLabel.setText(quantity.toString());
                    SaleProduct item = getTableRow().getItem();
                    if (item != null) {
                        minusBtn.setDisable(quantity <= 1);
                        plusBtn.setDisable(quantity >= item.getProduct().getStockQuantity());
                    }
                    setGraphic(box);
                }
            }
        });
        cartQtyCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getQuantity()
            ).asObject()
        );

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
                    setText(df.format(price) + "DH");
                }
            }
        });

        // Remove button column
        TableColumn<SaleProduct, Void> removeCol = new TableColumn<>("");
        removeCol.setPrefWidth(50);
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final MFXButton removeButton = new MFXButton("X");
            {
                removeButton.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-size: 10px; -fx-min-width: 24px; -fx-min-height: 24px; -fx-max-width: 24px; -fx-max-height: 24px; -fx-background-radius: 12;");
                removeButton.setOnAction(e -> {
                    SaleProduct item = getTableRow().getItem();
                    if (item != null) {
                        cartItems.remove(item);
                        updateCartTotal();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });

        cartTable.getColumns().addAll(cartImageCol, cartNameCol, cartQtyCol, cartPriceCol, removeCol);
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
        
        // Add warning style for low stock
        String baseStyle = "-fx-background-color: #FFFFFF; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-alignment: center;";
        if (product.getStockQuantity() < 5) {
            tile.setStyle(baseStyle + "; -fx-border-color: #FFA726; -fx-effect: dropshadow(three-pass-box, #FFA726, 5, 0, 0, 0);");
        } else {
            tile.setStyle(baseStyle + "; -fx-border-color: #E0E0E0;");
        }
        
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
        Label priceLabel = new Label( df.format(product.getPrice()) + " DH");
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        // Add stock warning icon for low stock
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(javafx.geometry.Pos.CENTER);
        Label stockLabel = new Label("Available: " + product.getStockQuantity());
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        if (product.getStockQuantity() < 5) {
            Label warningLabel = new Label("⚠");
            warningLabel.setStyle("-fx-text-fill: #FFA726; -fx-font-size: 12px;");
            stockBox.getChildren().addAll(warningLabel, stockLabel);
        } else {
            stockBox.getChildren().add(stockLabel);
        }

        tile.getChildren().addAll(imageView, nameLabel, priceLabel, stockBox);
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
        Dialog<Integer> qtyDialog = new Dialog<>();
        qtyDialog.setTitle("Add to Cart");
        qtyDialog.setHeaderText("Add " + selectedProduct.getName() + " to cart");

        // Create custom content
        VBox content = new VBox(10);
        content.setAlignment(javafx.geometry.Pos.CENTER);

        // Quantity selection with + and - buttons
        HBox qtyBox = new HBox(10);
        qtyBox.setAlignment(javafx.geometry.Pos.CENTER);

        MFXButton minusBtn = new MFXButton();
        Label minusIcon = new Label("−");
        minusIcon.setStyle("-fx-font-family: 'System'; -fx-font-weight: bold;");
        minusBtn.setGraphic(minusIcon);
        
        Label qtyLabel = new Label("1");
        qtyLabel.setStyle("-fx-min-width: 50px; -fx-alignment: center; -fx-font-size: 16px;");
        
        MFXButton plusBtn = new MFXButton();
        Label plusIcon = new Label("+");
        plusIcon.setStyle("-fx-font-family: 'System'; -fx-font-weight: bold;");
        plusBtn.setGraphic(plusIcon);

        String btnStyle = "-fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px; " +
                         "-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-background-radius: 15;";
        minusBtn.setStyle(btnStyle);
        plusBtn.setStyle(btnStyle);

        TextField qtyField = new TextField("1");
        qtyField.setPrefWidth(50);
        qtyField.setStyle("-fx-alignment: center;");
        qtyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                qtyField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            try {
                int qty = Integer.parseInt(qtyField.getText());
                minusBtn.setDisable(qty <= 1);
                plusBtn.setDisable(qty >= selectedProduct.getStockQuantity());
                qtyLabel.setText(String.valueOf(qty));
            } catch (NumberFormatException e) {
                qtyField.setText("1");
            }
        });

        AtomicInteger quantity = new AtomicInteger(1);
        minusBtn.setOnAction(e -> {
            int qty = quantity.get();
            if (qty > 1) {
                quantity.decrementAndGet();
                qtyField.setText(String.valueOf(quantity.get()));
                qtyLabel.setText(String.valueOf(quantity.get()));
            }
        });

        plusBtn.setOnAction(e -> {
            int qty = quantity.get();
            if (qty < selectedProduct.getStockQuantity()) {
                quantity.incrementAndGet();
                qtyField.setText(String.valueOf(quantity.get()));
                qtyLabel.setText(String.valueOf(quantity.get()));
            }
        });

        qtyBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        Label stockLabel = new Label("Available: " + selectedProduct.getStockQuantity());
        stockLabel.setStyle("-fx-text-fill: #666666;");

        content.getChildren().addAll(
            new Label("Quantity:"),
            qtyBox,
            stockLabel
        );

        // Set the custom content
        qtyDialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType confirmButtonType = new ButtonType("Add to Cart", ButtonBar.ButtonData.OK_DONE);
        qtyDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Convert the result
        qtyDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    return quantity.get();
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<Integer> result = qtyDialog.showAndWait();
        result.ifPresent(qty -> {
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
                showAlert(AlertType.INFORMATION, "Success", "Added " + qty + " x " + selectedProduct.getName() + " to cart");
            } else {
                showAlert(AlertType.WARNING, "Warning", "Total quantity exceeds stock! Available: " + selectedProduct.getStockQuantity());
            }
        });
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
        int totalItems = cartItems.stream().mapToInt(SaleProduct::getQuantity).sum();
        cartTotalLabel.setText(String.format("%.2f DH", total));
        cartItemCountLabel.setText(String.format("%d %s (%d unique)", 
            totalItems,
            totalItems != 1 ? "items" : "item",
            cartItems.size()
        ));

        // Update button states
        checkoutButton.setDisable(cartItems.isEmpty());
        clearCartButton.setDisable(cartItems.isEmpty());

        // Add tooltips for keyboard shortcuts
        checkoutButton.setTooltip(new Tooltip("Complete sale (Ctrl+Enter)"));
        clearCartButton.setTooltip(new Tooltip("Clear cart (Ctrl+C)"));
        searchField.setTooltip(new Tooltip("Search products (Ctrl+F)"));
        logoutButton.setTooltip(new Tooltip("Logout (Ctrl+L)"));
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