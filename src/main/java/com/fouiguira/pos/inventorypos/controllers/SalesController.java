package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;

@Controller
public class SalesController {

    private final SaleService saleService;

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
    private TextField clientNameField, totalPriceField;
    @FXML
    private ComboBox<User> cashierComboBox;
    @FXML
    private Button deleteButton;

    @Autowired
    public SalesController(SaleService saleService) {
        this.saleService = saleService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(this::loadSales);
        setupTable();
        salesTable.setOnMouseClicked(event -> populateFieldsFromSelection());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientName.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colCashier.setCellValueFactory(data ->
                data.getValue().getCashier() != null ?
                        new javafx.beans.property.SimpleStringProperty(data.getValue().getCashier().getUsername()) : null
        );
    }

    private void loadSales() {
        List<Sale> sales = saleService.getAllSales();
        salesTable.getItems().setAll(sales);
    }

    @FXML
    private void populateFieldsFromSelection() {
        Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
        if (selectedSale != null) {
            clientNameField.setText(selectedSale.getClientName());
            totalPriceField.setText(String.valueOf(selectedSale.getTotalPrice()));
            cashierComboBox.setValue(selectedSale.getCashier());
        }
    }

    @FXML
    public void handleDeleteSale() {
        try {
            Sale selectedSale = salesTable.getSelectionModel().getSelectedItem();
            if (selectedSale == null) {
                showAlert(AlertType.WARNING, "No Sale Selected", "Please select a sale to delete.");
                return;
            }

            saleService.deleteSale(selectedSale.getId());
            showAlert(AlertType.INFORMATION, "Sale Deleted", "Sale deleted successfully!");
            loadSales();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Failed to delete sale: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
