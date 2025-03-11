package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;

import java.time.LocalDate;
import java.util.List;

public interface SalesService {
    Sale createSale(Sale sale);
    List<Sale> getAllSales();
    List<Sale> getRecentSales(int limit);
    List<Sale> getSalesByDate(LocalDate date);
    double getSalesTotalByDate(LocalDate date);
    void printReceipt(Sale sale); // For printing
    void deleteSale(Long id);
    Sale getSaleById(Long id);
    List<Sale> getSalesByCashier(User cashier);
    List<Sale> getSalesByClientName(String clientName);
    List<Sale> getSalesByPaymentMethod(String paymentMethod);
}