package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Controller
public class SalesController {

    private final SalesService salesService;
    private final SaleProductService saleProductService;
    private final UserService userService;

    @FXML
    private TableView<Sale> salesTable;
    @FXML
    private TableColumn<Sale, Long> colId;
    @FXML
    private TableColumn<Sale, String> colClientName;
    @FXML
    private TableColumn<Sale, Double> colTotalPrice;
    @FXML
    private TableColumn<Sale, Date> colTimestamp;
    @FXML
    private TableColumn<Sale, String> colCashier;
    @FXML
    private TableColumn<Sale, String> colProducts;
    @FXML
    private TableColumn<Sale, String> colPaymentMethod;

    @FXML
    private TextField filterClientNameField;
    @FXML
    private ComboBox<User> filterCashierComboBox;
    @FXML
    private DatePicker filterDatePicker;

    @FXML
    private TextField clientNameField;
    @FXML
    private TextField totalPriceField;
    @FXML
    private ComboBox<User> cashierComboBox;
    @FXML
    private TextArea productsField;
    @FXML
    private TextField paymentMethodField;

    @FXML
    private Button deleteButton;
    @FXML
    private Button printButton;

    
    public SalesController(SalesService salesService, SaleProductService saleProductService, UserService userService) {
        this.salesService = salesService;
        this.saleProductService = saleProductService;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupTable();
            setupFilters();
            loadSales();
        });
        salesTable.setOnMouseClicked(event -> populateFieldsFromSelection());
        deleteButton.disableProperty().bind(salesTable.getSelectionModel().selectedItemProperty().isNull());
        printButton.disableProperty().bind(salesTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientName.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colCashier.setCellValueFactory(cellData -> {
            User cashier = cellData.getValue().getCashier();
            return cashier != null ? new SimpleStringProperty(cashier.getUsername()) : null;
        });
        colProducts.setCellValueFactory(cellData -> {
            List<SaleProduct> saleProducts = saleProductService.getSaleProductsBySale(cellData.getValue());
            String products = saleProducts.stream()
                    .map(sp -> sp.getProduct().getName() + " (Qty: " + sp.getQuantity() + ")")
                    .reduce("", (a, b) -> a + (a.isEmpty() ? "" : ", ") + b);
            return new SimpleStringProperty(products);
        });
        colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
    }

    private void setupFilters() {
        List<User> cashiers = userService.getAllUsers();
        filterCashierComboBox.setItems(FXCollections.observableArrayList(cashiers));
        filterCashierComboBox.setPromptText("All Cashiers");
        filterCashierComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterSales());
        filterClientNameField.textProperty().addListener((obs, oldVal, newVal) -> filterSales());
        filterDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> filterSales());

        cashierComboBox.setItems(FXCollections.observableArrayList(cashiers));
    }

    private void loadSales() {
        List<Sale> sales = salesService.getAllSales();
        salesTable.setItems(FXCollections.observableArrayList(sales));
    }

    @FXML
    private void populateFieldsFromSelection() {
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale != null) {
            clientNameField.setText(selectedSale.getClientName() != null ? selectedSale.getClientName() : "");
            totalPriceField.setText(String.format("%.2f", selectedSale.getTotalPrice()));
            cashierComboBox.setValue(selectedSale.getCashier());
            List<SaleProduct> saleProducts = saleProductService.getSaleProductsBySale(selectedSale);
            String products = saleProducts.stream()
                    .map(sp -> sp.getProduct().getName() + " (Qty: " + sp.getQuantity() + ")")
                    .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "\n") + b);
            productsField.setText(products);
            paymentMethodField.setText(selectedSale.getPaymentMethod());
        } else {
            clearFields();
        }
    }

    @FXML
    public void handleDeleteSale() {
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Sale Selected", "Please select a sale to delete.");
            return;
        }

        try {
            salesService.deleteSale(selectedSale.getId());
            showAlert(Alert.AlertType.INFORMATION, "Sale Deleted", "Sale deleted successfully!");
            loadSales();
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sale: " + e.getMessage());
        }
    }

    @FXML
    public void handlePrintReceipt() {
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Sale Selected", "Please select a sale to print.");
            return;
        }

        try {
            salesService.printReceipt(selectedSale);
            showAlert(Alert.AlertType.INFORMATION, "Receipt Printed", "Receipt generated successfully! Check the generated PDF.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to print receipt: " + e.getMessage());
        }
    }

    @FXML
    private void filterSales() {
        User selectedCashier = filterCashierComboBox.getValue();
        String clientName = filterClientNameField.getText().trim();
        LocalDate selectedDate = filterDatePicker.getValue();

        List<Sale> filteredSales = salesService.getAllSales();

        if (selectedCashier != null) {
            filteredSales = salesService.getSalesByCashier(selectedCashier);
        }
        if (!clientName.isEmpty()) {
            filteredSales = salesService.getSalesByClientName(clientName);
        }
        if (selectedDate != null) {
            filteredSales = salesService.getSalesByDate(selectedDate);
        }

        salesTable.setItems(FXCollections.observableArrayList(filteredSales));
    }
   
    private void clearFields() {
        clientNameField.clear();
        totalPriceField.clear();
        cashierComboBox.setValue(null);
        productsField.clear();
        paymentMethodField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}