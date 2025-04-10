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

import com.fouiguira.pos.inventorypos.entities.Product;
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

    @Override
    public List<SaleProduct> getSaleProductsByProduct(Product product) {
        return saleProductRepository.findByProduct(product);
    }
}