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