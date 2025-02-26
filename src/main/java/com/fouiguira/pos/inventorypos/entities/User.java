package com.fouiguira.pos.inventorypos.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "users")
@Data // Lombok annotation to generate getters, setters, and other methods
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // Unique identifier for each user

    @Column(unique = true, nullable = false)
    private String username; // Unique username

    @Column(nullable = false)
    private String password; // Hashed password

    @Enumerated(EnumType.STRING)
    private Role role; // Role of the user (Owner, Cashier, Staff)

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date(); // Timestamp of user creation

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date(); // Timestamp of last update

    public enum Role {
        OWNER, CASHIER, STAFF
    }
}
