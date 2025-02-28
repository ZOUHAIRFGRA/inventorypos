package com.fouiguira.pos.inventorypos.entities;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "sales_products",
        uniqueConstraints = @UniqueConstraint(columnNames = "products_id"))
@Data
public class SaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // Unique identifier for this table

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "products_id", nullable = false)
    private Product product; // Ensure this is ManyToOne, NOT OneToOne

    @Column(nullable = false)
    private Integer quantity;
}

