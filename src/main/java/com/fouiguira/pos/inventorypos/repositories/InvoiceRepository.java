package com.fouiguira.pos.inventorypos.repositories;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTimestampBetween(LocalDate start, LocalDate end);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    List<Invoice> findByClientNameContainingIgnoreCase(String clientName);
    List<Invoice> findByCashier(User cashier);
    List<Invoice> findBySaleId(Long saleId);
}