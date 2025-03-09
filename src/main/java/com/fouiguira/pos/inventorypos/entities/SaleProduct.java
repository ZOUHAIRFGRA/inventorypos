package com.fouiguira.pos.inventorypos.entities;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "sales_products")
@Data
public class SaleProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "products_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}