package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Return;
import java.util.List;

public interface ReturnService {
    Return createReturn(Return returnRecord);
    Return getReturnById(Long id);
    List<Return> getAllReturns();
    List<Return> getReturnsBySaleId(Long saleId);
    void deleteReturn(Long id);
}
