package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.SaleRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class SalesServiceImpl implements SalesService {

    private final SaleRepository saleRepository;
    private InvoiceService invoiceService;

    public SalesServiceImpl(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Autowired
    @Lazy
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public Sale createSale(Sale sale) {
        sale.getProducts().forEach(sp -> sp.setSale(sale)); // Set bidirectional relationship
        return saleRepository.save(sale);
    }

    @Override
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> getRecentSales(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        return saleRepository.findByOrderByTimestampDesc(pageable);
    }

    @Override
    public List<Sale> getSalesByDate(LocalDate date) {
        Date start = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return saleRepository.findByTimestampBetween(start, end);
    }

    @Override
    public double getSalesTotalByDate(LocalDate date) {
        return getSalesByDate(date).stream()
                .mapToDouble(Sale::getTotalPrice)
                .sum();
    }

    @Override
    public void printReceipt(Sale sale) {
        Invoice invoice = invoiceService.getInvoiceBySaleId(sale.getId());
        if (invoice == null) {
            invoice = invoiceService.createInvoiceFromSale(sale.getId());
        }
        invoiceService.generateInvoicePdf(invoice);
    }

    @Override
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    @Override
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
    }

    @Override
    public List<Sale> getSalesByCashier(User cashier) {
        return saleRepository.findByCashier(cashier);
    }

    @Override
    public List<Sale> getSalesByClientName(String clientName) {
        return saleRepository.findByClientNameContainingIgnoreCase(clientName);
    }

    @Override
    public List<Sale> getSalesByPaymentMethod(String paymentMethod) {
        return saleRepository.findByPaymentMethod(paymentMethod);
    }
}