package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.util.StringConverter;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

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
    private MFXComboBox<User> cashierFilterComboBox;

    @FXML
    private MFXTextField clientFilterField;

    private final SalesService salesService;
    private final SaleProductService saleProductService;
    private final UserService userService;

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
        colId.setRowCellFactory(sale -> new MFXTableRowCell<>(Sale::getId));
        colId.setComparator(Comparator.comparing(Sale::getId));

        colCashier.setRowCellFactory(sale -> new MFXTableRowCell<>(s -> ((Sale) s).getCashier().getUsername()));
        colCashier.setComparator(Comparator.comparing(s -> ((Sale) s).getCashier().getUsername()));

        colClient.setRowCellFactory(sale -> new MFXTableRowCell<>(Sale::getClientName));
        colClient.setComparator(Comparator.comparing(Sale::getClientName, Comparator.nullsLast(Comparator.naturalOrder())));

        colProducts.setRowCellFactory(sale -> new MFXTableRowCell<>(s -> {
            List<SaleProduct> saleProducts = saleProductService.getSaleProductsBySale((Sale) s);
            return saleProducts.stream()
                    .map(sp -> sp.getProduct().getName() + " (Qty: " + sp.getQuantity() + ")")
                    .reduce("", (a, b) -> a + (a.isEmpty() ? "" : ", ") + b);
        }));

        colTotalPrice.setRowCellFactory(sale -> new MFXTableRowCell<>(Sale::getTotalPrice));
        colTotalPrice.setComparator(Comparator.comparing(Sale::getTotalPrice));

        colPaymentMethod.setRowCellFactory(sale -> new MFXTableRowCell<>(Sale::getPaymentMethod));
        colPaymentMethod.setComparator(Comparator.comparing(Sale::getPaymentMethod));

        colTimestamp.setRowCellFactory(sale -> new MFXTableRowCell<>(s -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(((Sale) s).getTimestamp())));
        colTimestamp.setComparator(Comparator.comparing(Sale::getTimestamp));

        salesTable.getFilters().addAll(
            new StringFilter<>("Cashier", s -> ((Sale) s).getCashier().getUsername()),
            new StringFilter<>("Client", Sale::getClientName),
            new StringFilter<>("Payment Method", Sale::getPaymentMethod)
        );

        salesTable.setFooterVisible(true);
        salesTable.autosizeColumnsOnInitialization();
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
}