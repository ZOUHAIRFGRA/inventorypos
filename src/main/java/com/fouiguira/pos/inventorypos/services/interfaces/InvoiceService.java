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

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.User;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    Invoice createInvoiceFromSale(Long saleId);
    Invoice getInvoiceById(Long invoiceId);
    List<Invoice> getInvoicesByDate(LocalDate date);
    List<Invoice> getPendingInvoices();
    int getPendingInvoicesCount();
    void updateInvoiceStatus(Long invoiceId, Invoice.InvoiceStatus status);
    List<Invoice> getInvoicesByClientName(String clientName);
    List<Invoice> getInvoicesByCashier(User cashier);
    Invoice getInvoiceBySaleId(Long saleId);
    void generateInvoicePdf(Invoice invoice);
}