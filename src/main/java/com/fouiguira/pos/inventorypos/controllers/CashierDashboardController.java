package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.*;
import com.fouiguira.pos.inventorypos.services.interfaces.*;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CashierDashboardController {

    @FXML private MFXTextField searchField;
    @FXML private MFXComboBox<Category> categoryComboBox;
    @FXML private TableView<Product> productTable;
    private TableColumn<Product, Long> prodIdCol;
    private TableColumn<Product, String> prodNameCol;
    private TableColumn<Product, Double> prodPriceCol;
    private TableColumn<Product, Integer> prodStockCol;
    @FXML private MFXButton addToCartButton;

    @FXML private MFXTableView<SaleProduct> cartTable;
    private MFXTableColumn<SaleProduct> cartNameCol;
    private MFXTableColumn<SaleProduct> cartQtyCol;
    private MFXTableColumn<SaleProduct> cartPriceCol;
    private MFXTableColumn<SaleProduct> cartTotalCol;
    private MFXTableColumn<SaleProduct> cartActionCol;
    @FXML private MFXTextField clientNameField;
    @FXML private Label cartTotalLabel;
    @FXML private MFXComboBox<String> paymentMethodComboBox;
    @FXML private MFXButton checkoutButton;
    @FXML private MFXButton clearCartButton;

    @FXML private MFXTableView<Sale> salesHistoryTable;
    private MFXTableColumn<Sale> salesIdCol;
    private MFXTableColumn<Sale> salesDateCol;
    private MFXTableColumn<Sale> salesTotalCol;
    private MFXTableColumn<Sale> salesMethodCol;
    @FXML private MFXButton printReceiptButton;

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

    @Autowired
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

        // Check if productTable is injected
        if (productTable == null) {
            System.err.println("Error: productTable is null - FXML injection failed!");
            return;
        }

        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        cartTotalLabel = new Label("$0.00");
        cartTotalLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");
        totalHBox.getChildren().add(cartTotalLabel);

        setupTables();
        loadCategories();
        loadProducts();
        loadSalesHistory();
        setupPaymentMethods();

        // Debug table state
        Platform.runLater(() -> {
            System.out.println("Table visible: " + productTable.isVisible());
            System.out.println("Table items size: " + (productTable.getItems() != null ? productTable.getItems().size() : "null"));
            System.out.println("Table columns: " + productTable.getColumns());
        });
    }

    @SuppressWarnings("unchecked")
    private void setupTables() {
        // Product Table
        productTable.getColumns().clear();
        prodIdCol = new TableColumn<>("ID");
        prodNameCol = new TableColumn<>("Name");
        prodPriceCol = new TableColumn<>("Price");
        prodStockCol = new TableColumn<>("Stock");

        prodIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        prodNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        prodPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        prodStockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        prodPriceCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
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

        productTable.getColumns().addAll(prodIdCol, prodNameCol, prodPriceCol, prodStockCol);
        productTable.getSelectionModel().setCellSelectionEnabled(false);
        System.out.println("Product table columns set: " + productTable.getColumns());

        // Cart Table (unchanged)
        cartTable.getTableColumns().clear();
        cartNameCol = new MFXTableColumn<>("Name", true);
        cartQtyCol = new MFXTableColumn<>("Qty", true);
        cartPriceCol = new MFXTableColumn<>("Price", true);
        cartTotalCol = new MFXTableColumn<>("Total", true);
        cartActionCol = new MFXTableColumn<>("Action", false);

        cartNameCol.setRowCellFactory(item -> new MFXTableRowCell<>(i -> i.getProduct().getName()));
        cartQtyCol.setRowCellFactory(item -> {
            MFXTableRowCell<SaleProduct, Integer> cell = new MFXTableRowCell<>(SaleProduct::getQuantity);
            MFXTextField qtyField = new MFXTextField(String.valueOf(item.getQuantity()));
            qtyField.setPrefWidth(50);
            qtyField.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    int newQty = Integer.parseInt(newVal);
                    Product p = item.getProduct();
                    if (newQty <= p.getStockQuantity() && newQty > 0) {
                        item.setQuantity(newQty);
                        updateCartTotal();
                        cartTable.update();
                    } else {
                        qtyField.setText(String.valueOf(item.getQuantity()));
                        showAlert(Alert.AlertType.WARNING, "Warning", "Invalid quantity! Must be > 0 and ≤ stock (" + p.getStockQuantity() + ")");
                    }
                } catch (NumberFormatException e) {
                    qtyField.setText(String.valueOf(item.getQuantity()));
                }
            });
            cell.setGraphic(qtyField);
            return cell;
        });
        cartPriceCol.setRowCellFactory(item -> new MFXTableRowCell<>(i -> "$" + df.format(i.getProduct().getPrice())));
        cartTotalCol.setRowCellFactory(item -> new MFXTableRowCell<>(i -> "$" + df.format(i.getQuantity() * i.getProduct().getPrice())));
        cartActionCol.setRowCellFactory(item -> {
            MFXTableRowCell<SaleProduct, Void> cell = new MFXTableRowCell<>(i -> null);
            MFXButton removeButton = new MFXButton("Remove");
            removeButton.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10 5 10; -fx-background-radius: 3;");
            removeButton.setOnAction(e -> {
                cartItems.remove(item);
                updateCartTotal();
            });
            cell.setGraphic(removeButton);
            return cell;
        });
        cartTable.getTableColumns().addAll(cartNameCol, cartQtyCol, cartPriceCol, cartTotalCol, cartActionCol);
        cartTable.setItems(cartItems);
        cartTable.getSelectionModel().setAllowsMultipleSelection(false);

        // Sales History Table (unchanged)
        salesHistoryTable.getTableColumns().clear();
        salesIdCol = new MFXTableColumn<>("ID", true);
        salesDateCol = new MFXTableColumn<>("Date", true);
        salesTotalCol = new MFXTableColumn<>("Total", true);
        salesMethodCol = new MFXTableColumn<>("Method", true);

        salesIdCol.setRowCellFactory(sale -> new MFXTableRowCell<>(Sale::getId));
        salesDateCol.setRowCellFactory(sale -> new MFXTableRowCell<>(s -> s.getTimestamp().toString()));
        salesTotalCol.setRowCellFactory(sale -> new MFXTableRowCell<>(s -> "$" + df.format(s.getTotalPrice())));
        salesMethodCol.setRowCellFactory(sale -> new MFXTableRowCell<>(Sale::getPaymentMethod));
        salesHistoryTable.getTableColumns().addAll(salesIdCol, salesDateCol, salesTotalCol, salesMethodCol);
        salesHistoryTable.getSelectionModel().setAllowsMultipleSelection(false);
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
        ObservableList<Product> productList = FXCollections.observableArrayList(products);
        productTable.setItems(productList);
        System.out.println("Product table items set: " + productTable.getItems());
    }

    private void loadSalesHistory() {
        List<Sale> recentSales = salesService.getRecentSales(5);
        salesHistoryTable.setItems(FXCollections.observableArrayList(recentSales));
        salesHistoryTable.update();
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
        productTable.setItems(FXCollections.observableArrayList(filteredProducts));
    }

    @FXML
    public void handleCategorySelection() {
        handleSearch();
    }

    @FXML
    public void handleAddToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product to add to cart!");
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
        if (item.getQuantity() < selectedProduct.getStockQuantity()) {
            item.setQuantity(item.getQuantity() + 1);
            if (!cartItems.contains(item)) {
                cartItems.add(item);
            }
            updateCartTotal();
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Not enough stock available! Max: " + selectedProduct.getStockQuantity());
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
            loadSalesHistory();
            loadProducts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Checkout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePrintReceipt() {
        Sale selectedSale = salesHistoryTable.getSelectionModel().getSelectedValue();
        if (selectedSale != null) {
            try {
                salesService.printReceipt(selectedSale);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Receipt printed successfully! Check the generated PDF.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to print receipt: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a sale to print receipt!");
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