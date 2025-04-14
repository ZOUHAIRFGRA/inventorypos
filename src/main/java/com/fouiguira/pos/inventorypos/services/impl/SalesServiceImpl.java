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
package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.SaleRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
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
    }    @Override
    @Transactional
    public Sale createSale(Sale sale) {
        if (sale.getProducts() == null) {
            sale.setProducts(new ArrayList<>());
        }
        // Ensure bidirectional relationship is properly set up
        sale.getProducts().forEach(sp -> {
            sp.setSale(sale);
            if (sp.getQuantity() == null) {
                sp.setQuantity(1); // Default quantity if not specified
            }
        });
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
    @Transactional(readOnly = true)
    public void printReceipt(Sale sale) {
        Sale fullSale = saleRepository.findById(sale.getId())
            .orElseThrow(() -> new RuntimeException("Sale not found with id: " + sale.getId()));
        Hibernate.initialize(fullSale.getProducts());

        Invoice invoice = invoiceService.getInvoiceBySaleId(fullSale.getId());
        if (invoice == null) {
            invoice = invoiceService.createInvoiceFromSale(fullSale.getId());
        }
        
        try {
            // Generate the PDF
            invoiceService.generateInvoicePdf(invoice);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt: " + e.getMessage());
        }
    }

    @Override
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Sale getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        Hibernate.initialize(sale.getProducts());
        return sale;
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

    @Override
    public double getAverageTicketSize() {
        List<Sale> allSales = getAllSales();
        if (allSales.isEmpty()) {
            return 0.0;
        }
        double totalSales = allSales.stream()
                .mapToDouble(Sale::getTotalPrice)
                .sum();
        return totalSales / allSales.size();
    }

    @Override
    public double getSalesGrowthRate() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        double todaySales = getSalesTotalByDate(today);
        double yesterdaySales = getSalesTotalByDate(yesterday);
        
        if (yesterdaySales == 0) {
            return todaySales > 0 ? 100.0 : 0.0;
        }
        
        return ((todaySales - yesterdaySales) / yesterdaySales) * 100.0;
    }
}