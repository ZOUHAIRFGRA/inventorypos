package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;

@Controller
public class InvoiceController {

    @FXML
    private MFXDatePicker datePicker;
    @FXML
    private MFXButton clearFilterButton, generatePdfButton;

    @FXML
    private MFXTableView<Invoice> invoiceTable;
    @FXML
    private MFXTableColumn<Invoice> invoiceIdCol, saleIdCol, timestampCol, totalAmountCol, statusCol;

    private final InvoiceService invoiceService;

    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupTable();
            loadInvoices();
        });
    }

    private void setupTable() {
        invoiceIdCol.setRowCellFactory(invoice -> new MFXTableRowCell<>(Invoice::getId));
        saleIdCol.setRowCellFactory(invoice -> new MFXTableRowCell<>(i -> i.getSale().getId()));
        timestampCol.setRowCellFactory(invoice -> new MFXTableRowCell<>(i -> i.getTimestamp().toString()));
        totalAmountCol.setRowCellFactory(invoice -> new MFXTableRowCell<>(i -> df.format(i.getTotalAmount()) + " DH"));
        statusCol.setRowCellFactory(invoice -> new MFXTableRowCell<>(i -> i.getStatus().toString()));
        invoiceTable.getSelectionModel().setAllowsMultipleSelection(false);
    }

    private void loadInvoices() {
        List<Invoice> invoices = invoiceService.getInvoicesByDate(null); // Null for all invoices
        invoiceTable.setItems(FXCollections.observableArrayList(invoices));
    }

    @FXML
    public void handleDateFilter() {
        LocalDate selectedDate = datePicker.getValue();
        List<Invoice> filteredInvoices = selectedDate != null 
            ? invoiceService.getInvoicesByDate(selectedDate) 
            : invoiceService.getInvoicesByDate(null); // Null for all invoices
        invoiceTable.setItems(FXCollections.observableArrayList(filteredInvoices));
    }

    @FXML
    public void handleClearFilter() {
        datePicker.clear();
        loadInvoices();
    }

    @FXML
    public void handleGeneratePdf() {
        Invoice selectedInvoice = invoiceTable.getSelectionModel().getSelectedValue();
        if (selectedInvoice != null) {
            try {
                invoiceService.generateInvoicePdf(selectedInvoice);
                showAlert(Alert.AlertType.INFORMATION, "Invoice PDF generated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to generate PDF: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select an invoice to generate PDF!");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}