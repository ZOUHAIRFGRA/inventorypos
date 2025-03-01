package com.fouiguira.pos.inventorypos.repositories;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCashier(User cashier);

    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) FROM Sale s WHERE DATE(s.timestamp) = DATE('now')")
    Double findTotalSalesToday();

    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) FROM Sale s")
    Double findTotalRevenue();

    @Query("SELECT COALESCE(SUM(s.totalPrice) - COALESCE(SUM(sp.quantity * sp.product.price), 0), 0) " +
            "FROM Sale s LEFT JOIN SaleProduct sp ON sp.sale.id = s.id")
    Double findPendingPayments();


    List<Sale> findTop10ByOrderByTimestampDesc();
}
