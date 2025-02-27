package com.fouiguira.pos.inventorypos.services.interfaces;
import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Sale;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    Invoice getInvoiceById(Long id);
    List<Invoice> getAllInvoices();
    List<Invoice> getInvoicesBySale(Sale sale);
    void deleteInvoice(Long id);
}

