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
package com.fouiguira.pos.inventorypos.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String clientName; // Added for clarity on invoice

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier; // Added to track who issued the invoice

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.PENDING; // Default to PENDING

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();

    public enum InvoiceStatus {
        PAID, PENDING
    }

    // Ensure totalAmount is set based on sale
    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        if (sale != null && totalAmount == null) {
            totalAmount = sale.getTotalPrice();
        }
        if (sale != null && clientName == null) {
            clientName = sale.getClientName() != null ? sale.getClientName() : "Unknown";
        }
        if (sale != null && cashier == null) {
            cashier = sale.getCashier();
        }
    }
}