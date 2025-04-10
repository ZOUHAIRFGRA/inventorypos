/*
 * Inventory POS System
 * Copyright (c) 2025 ZOUHAIR FOUIGUIRA. All rights reserved.
 *
 * Licensed under Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International
 * You may not use this file except in compliance with the License.
 *
 * @author ZOUHAIR FOUIGUIRA
 * @version 1.0
 * @since 2025-02-24
 */
package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import javafx.util.StringConverter;

@Controller
public class SalesHistoryController {

    @FXML
    private MFXTableView<Sale> salesTable;

    @FXML
    private MFXTableColumn<Sale> colId;

    @FXML
    private MFXTableColumn<Sale> colCashier;

    @FXML
    private MFXTableColumn<Sale> colClient;

    @FXML
    private MFXTableColumn<Sale> colProducts;

    @FXML
    private MFXTableColumn<Sale> colTotalPrice;

    @FXML
    private MFXTableColumn<Sale> colPaymentMethod;

    @FXML
    private MFXTableColumn<Sale> colTimestamp;

    @FXML
    private MFXTableColumn<Sale> colActions;

    @FXML
    private MFXComboBox<User> cashierFilterComboBox;

    @FXML
    private MFXTextField clientFilterField;

    private final SalesService salesService;
    private final SaleProductService saleProductService;
    private final UserService userService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SalesHistoryController(SalesService salesService, SaleProductService saleProductService, UserService userService) {
        this.salesService = salesService;
        this.saleProductService = saleProductService;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadSales();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        colId.setRowCellFactory(sale -> {
            MFXTableRowCell<Sale, Long> cell = new MFXTableRowCell<>(Sale::getId);
            cell.setStyle("-fx-font-weight: bold;");
            return cell;
        });
        colId.setComparator(Comparator.comparing(Sale::getId));

        colCashier.setRowCellFactory(sale -> {
            MFXTableRowCell<Sale, String> cell = new MFXTableRowCell<>(s -> s.getCashier().getUsername());
            cell.setStyle("-fx-text-fill: #2196F3;");
            return cell;
        });
        colCashier.setComparator(Comparator.comparing(s -> s.getCashier().getUsername()));

        colClient.setRowCellFactory(sale -> 
            new MFXTableRowCell<Sale, String>(Sale::getClientName));
        colClient.setComparator(Comparator.comparing(Sale::getClientName, Comparator.nullsLast(Comparator.naturalOrder())));

        colProducts.setRowCellFactory(sale -> {
            MFXTableRowCell<Sale, String> cell = new MFXTableRowCell<>(s -> {
                List<SaleProduct> saleProducts = saleProductService.getSaleProductsBySale((Sale)s);
                return formatProductsList(saleProducts);
            });
            cell.setWrapText(true);
            cell.setStyle("-fx-padding: 8 5; -fx-text-fill: #666666;");
            return cell;
        });

        colTotalPrice.setRowCellFactory(sale -> {
            MFXTableRowCell<Sale, String> cell = new MFXTableRowCell<>(s -> 
                String.format("%.2f DH", ((Sale)s).getTotalPrice()));
            cell.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            return cell;
        });
        colTotalPrice.setComparator(Comparator.comparing(Sale::getTotalPrice));

        colPaymentMethod.setRowCellFactory(sale -> 
            new MFXTableRowCell<Sale, String>(Sale::getPaymentMethod));
        colPaymentMethod.setComparator(Comparator.comparing(Sale::getPaymentMethod));

        colTimestamp.setRowCellFactory(sale -> 
            new MFXTableRowCell<Sale, String>(s -> 
                dateFormat.format(((Sale)s).getTimestamp())));
        colTimestamp.setComparator(Comparator.comparing(Sale::getTimestamp));

        colActions.setRowCellFactory(sale -> {
            MFXTableRowCell<Sale, String> cell = new MFXTableRowCell<>((s) -> "");
            HBox actions = new HBox(5);
            actions.setAlignment(Pos.CENTER);

            MFXButton printBtn = new MFXButton();
            printBtn.setText("Print");
            printBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
            printBtn.setOnAction(e -> handlePrintReceipt((Sale)sale));

            MFXButton deleteBtn = new MFXButton();
            deleteBtn.setText("Delete");
            deleteBtn.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
            deleteBtn.setOnAction(e -> handleDeleteSale((Sale)sale));

            actions.getChildren().addAll(printBtn, deleteBtn);
            cell.setGraphic(actions);
            return cell;
        });

        salesTable.getFilters().addAll(
            new StringFilter<>("Cashier", s -> s.getCashier().getUsername()),
            new StringFilter<>("Client", Sale::getClientName),
            new StringFilter<>("Payment Method", Sale::getPaymentMethod)
        );

        salesTable.setFooterVisible(true);
        salesTable.autosizeColumnsOnInitialization();
    }

    private String formatProductsList(List<SaleProduct> saleProducts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < saleProducts.size(); i++) {
            SaleProduct sp = saleProducts.get(i);
            sb.append(sp.getProduct().getName())
              .append(" (×")
              .append(sp.getQuantity())
              .append(")");
            
            if (i < saleProducts.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private void setupFilters() {
        List<User> cashiers = userService.getAllUsers();
        cashierFilterComboBox.setItems(FXCollections.observableArrayList(cashiers));
        cashierFilterComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "All Cashiers" : user.getUsername();
            }

            @Override
            public User fromString(String string) {
                return cashierFilterComboBox.getItems().stream()
                        .filter(u -> u.getUsername().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        cashierFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterSales());
        clientFilterField.textProperty().addListener((obs, oldVal, newVal) -> filterSales());
    }

    private void loadSales() {
        List<Sale> sales = salesService.getAllSales();
        salesTable.setItems(FXCollections.observableArrayList(sales));
    }

    @FXML
    private void filterSales() {
        User selectedCashier = cashierFilterComboBox.getValue();
        String clientName = clientFilterField.getText().trim();

        List<Sale> filteredSales = salesService.getAllSales();

        if (selectedCashier != null) {
            filteredSales = salesService.getSalesByCashier(selectedCashier);
        }
        if (!clientName.isEmpty()) {
            filteredSales = salesService.getSalesByClientName(clientName);
        }

        salesTable.setItems(FXCollections.observableArrayList(filteredSales));
    }

    private void handlePrintReceipt(Sale sale) {
        try {
            salesService.printReceipt(sale);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Receipt generated and opened!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate receipt: " + e.getMessage());
        }
    }

    private void handleDeleteSale(Sale sale) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Sale");
        confirm.setHeaderText("Delete Sale #" + sale.getId());
        confirm.setContentText("Are you sure you want to delete this sale? This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    salesService.deleteSale(sale.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sale deleted successfully!");
                    loadSales();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sale: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}