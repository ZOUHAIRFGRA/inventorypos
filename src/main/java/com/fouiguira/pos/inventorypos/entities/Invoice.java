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
    private Long id; // Unique identifier for each invoice

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale; // Reference to the Sale ID

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date(); // Timestamp of invoice generation

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status; // Payment status

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date(); // Timestamp of invoice creation

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date(); // Timestamp of last update

    public enum InvoiceStatus {
        PAID, PENDING
    }
}
