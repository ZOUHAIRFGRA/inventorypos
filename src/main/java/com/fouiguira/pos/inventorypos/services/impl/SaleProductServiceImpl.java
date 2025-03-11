package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.repositories.SaleProductRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.SaleProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleProductServiceImpl implements SaleProductService {

    private final SaleProductRepository saleProductRepository;

    public SaleProductServiceImpl(SaleProductRepository saleProductRepository) {
        this.saleProductRepository = saleProductRepository;
    }

    @Override
    public List<SaleProduct> getSaleProductsBySale(Sale sale) {
        return saleProductRepository.findBySale(sale);
    }
}