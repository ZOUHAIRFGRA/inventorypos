package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;

import java.util.List;

public interface SaleProductService {
    List<SaleProduct> getSaleProductsBySale(Sale sale);
}