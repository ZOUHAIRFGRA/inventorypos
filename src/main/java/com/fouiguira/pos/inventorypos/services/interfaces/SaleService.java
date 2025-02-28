package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;

import java.util.List;


public interface SaleService {
    Sale createSale(Sale sale);
    Sale getSaleById(Long id);
    List<Sale> getAllSales();
    List<Sale> getSalesByCashier(User cashier);
    Sale updateSale(Long id, Sale sale);
    void deleteSale(Long id);
}
