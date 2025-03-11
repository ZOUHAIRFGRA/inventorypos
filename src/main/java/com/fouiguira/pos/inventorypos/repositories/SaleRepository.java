package com.fouiguira.pos.inventorypos.repositories;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCashier(User cashier);
    List<Sale> findByClientNameContainingIgnoreCase(String clientName);
    List<Sale> findByPaymentMethod(String paymentMethod);
    List<Sale> findByTimestampBetween(Date start, Date end);
    List<Sale> findByOrderByTimestampDesc(PageRequest pageable);
}