package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    public Invoice createInvoice(Invoice invoice) {
        return invoiceService.createInvoice(invoice);
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceService.getInvoiceById(id);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    public List<Invoice> getInvoicesBySale(Sale saleId) {
        return invoiceService.getInvoicesBySale(saleId);
    }

    public void deleteInvoice(Long id) {
        invoiceService.deleteInvoice(id);
    }
}
