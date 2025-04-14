package com.fouiguira.pos.inventorypos.repositories;

import com.fouiguira.pos.inventorypos.entities.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findBySaleId(Long saleId);
}
