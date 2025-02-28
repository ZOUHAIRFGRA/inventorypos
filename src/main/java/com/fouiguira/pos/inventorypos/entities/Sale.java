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
    private Long id; // Unique identifier for each sale

    @OneToMany(cascade = CascadeType.ALL)
    private List<SaleProduct> products; // List of products sold

    @Column(nullable = false)
    private Double totalPrice; // Total price of the sale

    private String clientName; // Optional client name

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier; // Cashier processing the sale

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date(); // Timestamp of the sale
}


