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
    private Long id; // Unique identifier for each product

    @Column(nullable = false)
    private String name; // Name of the product


    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Double price; // Price of the product

    @Column(nullable = false)
    private Integer stockQuantity; // Available stock quantity

    private String imagePath; // Path to product image

    private String description; // Description of the product

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date(); // Timestamp of product creation

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date(); // Timestamp of last update
}
