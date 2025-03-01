package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.SaleRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleServiceImpl implements SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Override
    public Sale createSale(Sale sale) {
        // Ensure all necessary fields are set before saving
        return saleRepository.save(sale);
    }



    @Override
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id).orElse(null);
    }

    @Override
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> getSalesByCashier(User cashier) {
        return saleRepository.findByCashier(cashier);
    }

    @Override
    public Sale updateSale(Long id, Sale sale) {
        Sale existingSale = saleRepository.findById(id).orElse(null);
        if (existingSale != null) {
            existingSale.setTotalPrice(sale.getTotalPrice()); // Update total price
            existingSale.setProducts(sale.getProducts()); // Update list of products
            existingSale.setClientName(sale.getClientName()); // Update client name if present
            return saleRepository.save(existingSale); // Save the updated sale
        }
        return null; // Sale not found
    }

    @Override
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    @Override
    public double getTotalSalesToday() {
        return saleRepository.findTotalSalesToday() != null ? saleRepository.findTotalSalesToday() : 0.0;
    }

    @Override
    public double getTotalRevenue() {
        return saleRepository.findTotalRevenue() != null ? saleRepository.findTotalRevenue() : 0.0;
    }

    @Override
    public double getPendingPayments() {
        return saleRepository.findPendingPayments() != null ? saleRepository.findPendingPayments() : 0.0;
    }

    @Override
    public List<Sale> getLast10Sales() {
        return saleRepository.findTop10ByOrderByTimestampDesc();
    }
}
