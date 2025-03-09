package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Invoice;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    List<Invoice> getInvoicesByDate(LocalDate date);
    List<Invoice> getPendingInvoices();
    int getPendingInvoicesCount();
    void updateInvoiceStatus(Long invoiceId, Invoice.InvoiceStatus status);
    void generateInvoicePdf(Invoice invoice); // For printing
}