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
    void printReceipt(Sale sale);
    void deleteSale(Long id);
    Sale getSaleById(Long id);
    List<Sale> getSalesByCashier(User cashier);
    List<Sale> getSalesByClientName(String clientName);
    List<Sale> getSalesByPaymentMethod(String paymentMethod);
    
    // New methods for dashboard
    double getAverageTicketSize();
    double getSalesGrowthRate();
}