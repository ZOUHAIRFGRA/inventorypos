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

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SaleProduct> products;

    @Column(nullable = false)
    private Double totalPrice;

    private String clientName;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(nullable = false)
    private String paymentMethod;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();
}