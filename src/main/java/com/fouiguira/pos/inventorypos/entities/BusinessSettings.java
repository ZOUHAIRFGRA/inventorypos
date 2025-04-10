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
@Table(name = "business_settings")
@Data
public class BusinessSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String businessName;

    @Column
    private String address;

    @Column
    private String phone;

    @Column
    private String email;

    @Column
    private String logoPath;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();

    @Version // Add version field for optimistic locking
    private Long version;

    @PrePersist
    public void onCreate() {
        if (businessName == null) businessName = "My Business Name";
        if (logoPath == null) logoPath = "config/default_logo.png";
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }
}