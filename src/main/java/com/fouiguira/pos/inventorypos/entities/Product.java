package com.fouiguira.pos.inventorypos.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double purchasePrice = 0.0;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    private String imagePath;

    private String description;

    @Column(nullable = false)
    private Integer initialStock = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();

    @PrePersist
    public void onPrePersist() {
        if (initialStock == null) {
            initialStock = stockQuantity;
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
    }
}