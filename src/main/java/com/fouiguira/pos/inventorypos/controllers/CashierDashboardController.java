package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CashierDashboardController {

    @FXML
    private MFXTextField searchField;
    @FXML
    private MFXComboBox<Category> categoryComboBox;
    @FXML
    private MFXTableView<Product> productTable;
    private MFXTableColumn<Product> prodIdCol;
    private MFXTableColumn<Product> prodNameCol;
    private MFXTableColumn<Product> prodPriceCol;
    private MFXTableColumn<Product> prodStockCol;
    private MFXTableColumn<Product> prodImageCol;
    @FXML
    private MFXButton addToCartButton;

    @FXML
    private MFXTableView<SaleProduct> cartTable;
    private MFXTableColumn<SaleProduct> cartNameCol;
    private MFXTableColumn<SaleProduct> cartQtyCol;
    private MFXTableColumn<SaleProduct> cartPriceCol;
    private MFXTableColumn<SaleProduct> cartTotalCol;
    @FXML
    private Label cartTotalLabel;
    @FXML
    private MFXComboBox<String> paymentMethodComboBox;
    @FXML
    private MFXButton checkoutButton, clearCartButton;

    @FXML
    private MFXTableView<Sale> salesHistoryTable;
    private MFXTableColumn<Sale> salesIdCol;
    private MFXTableColumn<Sale> salesDateCol;
    private MFXTableColumn<Sale> salesTotalCol;
    private MFXTableColumn<Sale> salesMethodCol;
    @FXML
    private MFXButton printReceiptButton;

    @FXML
    private VBox cartVBox;
    @FXML
    private HBox totalHBox;

    @FXML
    private MFXButton logoutButton;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SalesService salesService;
    private final InvoiceService invoiceService;
    private final UserService userService;

    private ObservableList<SaleProduct> cartItems = FXCollections.observableArrayList();
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    @Autowired
    public CashierDashboardController(ProductService productService, CategoryService categoryService,
                                      SalesService salesService, InvoiceService invoiceService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.salesService = salesService;
        this.invoiceService = invoiceService;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            cartTotalLabel = new Label("$0.00");
            cartTotalLabel.setStyle("-fx-font-size: 16px;");
            totalHBox.getChildren().add(cartTotalLabel);

            setupTables();
            loadCategories();
            loadProducts();
            loadSalesHistory();
            setupPaymentMethods();
            searchField.setFloatingText("Search Products");
        });
    }

    private void setupTables() {
        // Product Table
        prodIdCol = new MFXTableColumn<>("ID", true);
        prodNameCol = new MFXTableColumn<>("Name", true);
        prodPriceCol = new MFXTableColumn<>("Price", true);
        prodStockCol = new MFXTableColumn<>("Stock", true);
        prodImageCol = new MFXTableColumn<>("Image", true);

        prodIdCol.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getId));
        prodNameCol.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getName));
        prodPriceCol.setRowCellFactory(product -> new MFXTableRowCell<>(p -> "$" + df.format(p.getPrice())));
        prodStockCol.setRowCellFactory(product -> new MFXTableRowCell<>(Product::getStockQuantity));
        prodImageCol.setRowCellFactory(product -> {
            MFXTableRowCell<Product, String> cell = new MFXTableRowCell<>(Product::getImagePath);
            ImageView imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            cell.setGraphic(imageView);
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
        productTable.getTableColumns().addAll(prodIdCol, prodNameCol, prodPriceCol, prodStockCol, prodImageCol);
        productTable.getSelectionModel().setAllowsMultipleSelection(false);

        // Cart Table
        cartNameCol = new MFXTableColumn<>("Name", true);
        cartQtyCol = new MFXTableColumn<>("Qty", true);
        cartPriceCol = new MFXTableColumn<>("Price", true);
        cartTotalCol = new MFXTableColumn<>("Total", true);

        cartNameCol.setRowCellFactory(item -> new MFXTableRowCell<>(i -> i.getProduct().getName()));
        cartQtyCol.setRowCellFactory(item -> new MFXTableRowCell<>(SaleProduct::getQuantity));
        cartPriceCol.setRowCellFactory(item -> new MFXTableRowCell<>(i -> "$" + df.format(i.getProduct().getPrice())));
        cartTotalCol.setRowCellFactory(item -> new MFXTableRowCell<>(i -> "$" + df.format(i.getQuantity() * i.getProduct().getPrice())));
        cartTable.getTableColumns().addAll(cartNameCol, cartQtyCol, cartPriceCol, cartTotalCol);
        cartTable.setItems(cartItems);
        cartTable.getSelectionModel().setAllowsMultipleSelection(false);

        // Sales History Table
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
        productTable.setItems(FXCollections.observableArrayList(products));
    }

    private void loadSalesHistory() {
        List<Sale> recentSales = salesService.getRecentSales(5);
        salesHistoryTable.setItems(FXCollections.observableArrayList(recentSales));
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
        Product selectedProduct = productTable.getSelectionModel().getSelectedValue();
        if (selectedProduct != null) {
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
                showAlert(Alert.AlertType.WARNING, "Not enough stock available!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a product to add to cart!");
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }
        Sale sale = new Sale();
        sale.setTimestamp(new Date());
        sale.setProducts(new ArrayList<>(cartItems));
        sale.setTotalPrice(cartItems.stream().mapToDouble(i -> i.getQuantity() * i.getProduct().getPrice()).sum());
        sale.setPaymentMethod(paymentMethodComboBox.getSelectionModel().getSelectedItem());
        sale.setCashier(userService.getCurrentUser()); // Use the logged-in cashier

        Sale savedSale = salesService.createSale(sale);
        Invoice invoice = new Invoice();
        invoice.setSale(savedSale);
        invoice.setTotalAmount(savedSale.getTotalPrice());
        invoice.setStatus("Cash".equals(sale.getPaymentMethod()) ? Invoice.InvoiceStatus.PAID : Invoice.InvoiceStatus.PENDING);
        invoiceService.createInvoice(invoice);

        productService.updateStockAfterSale(cartItems);
        showAlert(Alert.AlertType.INFORMATION, "Checkout successful! Invoice generated.");
        clearCart();
        loadSalesHistory();
    }

    @FXML
    public void handleClearCart() {
        clearCart();
    }

    @FXML
    public void handlePrintReceipt() {
        Sale selectedSale = salesHistoryTable.getSelectionModel().getSelectedValue();
        if (selectedSale != null) {
            salesService.printReceipt(selectedSale);
            showAlert(Alert.AlertType.INFORMATION, "Receipt printed successfully!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a sale to print receipt!");
        }
    }

    @FXML
    public void handleLogout() {
        userService.logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error logging out!");
        }
    }

    private void updateCartTotal() {
        double total = cartItems.stream().mapToDouble(i -> i.getQuantity() * i.getProduct().getPrice()).sum();
        cartTotalLabel.setText("$" + df.format(total));
    }

    private void clearCart() {
        cartItems.clear();
        updateCartTotal();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}