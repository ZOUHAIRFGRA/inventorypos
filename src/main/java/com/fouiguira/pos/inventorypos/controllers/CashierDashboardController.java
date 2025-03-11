package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
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
    private MFXTableColumn<SaleProduct> cartActionCol;
    @FXML
    private MFXTextField clientNameField;
    @FXML
    private Label cartTotalLabel;
    @FXML
    private MFXComboBox<String> paymentMethodComboBox;
    @FXML
    private MFXButton checkoutButton;
    @FXML
    private MFXButton clearCartButton;

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
            clientNameField.setFloatingText("Client Name (Required)");
        });
    }

    @SuppressWarnings("unchecked")
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
                    imageView.setImage(file.exists() ? new Image(file.toURI().toString(), 50, 50, true, true) : null);
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
                        showAlert(Alert.AlertType.WARNING, "Invalid quantity! Must be > 0 and ≤ stock (" + p.getStockQuantity() + ")");
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
            removeButton.getStyleClass().add("button-danger");
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
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a product to add to cart!");
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
            showAlert(Alert.AlertType.WARNING, "Not enough stock available! Max: " + selectedProduct.getStockQuantity());
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }
        String clientName = clientNameField.getText().trim();
        if (clientName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter a client name to complete the sale!");
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
            // Ensure the products collection is initialized
            savedSale.getProducts().size();
            Invoice invoice = invoiceService.createInvoiceFromSale(savedSale.getId());
            productService.updateStockAfterSale(cartItems);
            invoiceService.generateInvoicePdf(invoice);
            showAlert(Alert.AlertType.INFORMATION, "Checkout successful! Invoice #" + invoice.getId() + " generated.");
            clearCart();
            loadSalesHistory();
            loadProducts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Checkout failed: " + e.getMessage());
            e.printStackTrace(); // Log for debugging
        }
    }

    @FXML
    public void handlePrintReceipt() {
        Sale selectedSale = salesHistoryTable.getSelectionModel().getSelectedValue();
        if (selectedSale != null) {
            try {
                salesService.printReceipt(selectedSale);
                showAlert(Alert.AlertType.INFORMATION, "Receipt printed successfully! Check the generated PDF.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to print receipt: " + e.getMessage());
                e.printStackTrace(); // Log for debugging
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a sale to print receipt!");
        }
    }
    @FXML
    public void handleClearCart() {
        clearCart();
    }

    // @FXML
    // public void handlePrintReceipt() {
    //     Sale selectedSale = salesHistoryTable.getSelectionModel().getSelectedValue();
    //     if (selectedSale != null) {
    //         try {
    //             salesService.printReceipt(selectedSale);
    //             showAlert(Alert.AlertType.INFORMATION, "Receipt printed successfully! Check the generated PDF.");
    //         } catch (Exception e) {
    //             showAlert(Alert.AlertType.ERROR, "Failed to print receipt: " + e.getMessage());
    //         }
    //     } else {
    //         showAlert(Alert.AlertType.WARNING, "Please select a sale to print receipt!");
    //     }
    // }

    @FXML
    public void handleLogout() {
        userService.logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            loader.setControllerFactory(c -> new LoginController(userService)); // Adjust if LoginController differs
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventory POS System - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error logging out: " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}