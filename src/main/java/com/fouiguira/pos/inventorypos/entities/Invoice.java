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
    private Double totalAmount; // Added to store invoice total

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();

    public enum InvoiceStatus {
        PAID, PENDING
    }
}