package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalesController {

    @Autowired
    private SaleService saleService;

    public Sale createSale(Sale sale) {
        return saleService.createSale(sale);
    }

    public Sale getSaleById(Long id) {
        return saleService.getSaleById(id);
    }

    public List<Sale> getAllSales() {
        return saleService.getAllSales();
    }

    public List<Sale> getSalesByCashier(User cashier) {
        return saleService.getSalesByCashier(cashier);
    }

    public Sale updateSale(Long id, Sale sale) {
        return saleService.updateSale(id, sale);
    }

    public void deleteSale(Long id) {
        saleService.deleteSale(id);
    }
}
