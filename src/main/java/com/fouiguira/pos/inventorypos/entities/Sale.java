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
    private Long id;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL) // Adjusted to mappedBy
    private List<SaleProduct> products;

    @Column(nullable = false)
    private Double totalPrice;

    private String clientName;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(nullable = false)
    private String paymentMethod; // Added for POS (e.g., "Cash", "Card")

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();
}