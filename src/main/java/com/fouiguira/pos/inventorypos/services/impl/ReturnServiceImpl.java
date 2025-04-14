/*
 * Inventory POS System
 * Copyright (c) 2025 ZOUHAIR FOUIGUIRA. All rights reserved.
 */
package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Return;
import com.fouiguira.pos.inventorypos.repositories.ReturnRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.ReturnService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ReturnServiceImpl implements ReturnService {
    
    private final ReturnRepository returnRepository;
    
    public ReturnServiceImpl(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }
    
    @Override
    public Return createReturn(Return returnRecord) {
        return returnRepository.save(returnRecord);
    }
    
    @Override
    public Return getReturnById(Long id) {
        return returnRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }
    
    @Override
    public List<Return> getReturnsBySaleId(Long saleId) {
        return returnRepository.findBySaleId(saleId);
    }
    
    @Override
    public void deleteReturn(Long id) {
        returnRepository.deleteById(id);
    }
}
