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
package com.fouiguira.pos.inventorypos.seeder;

import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.repositories.UserRepository;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.repositories.CategoryRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String PLACEHOLDER_IMAGE = "/images/placeholder.png";

    public DatabaseSeeder(UserRepository userRepository, ProductRepository productRepository, 
                         CategoryRepository categoryRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        // seedCategoriesAndProducts();
    }

    private void seedUsers() {
        long userCount = userRepository.count();
        logger.info("⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡ Database contains {} user(s).", userCount);
        if (userCount == 0) {
            logger.info("⚡ Database is empty. Seeding default admin user...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.OWNER);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());
            admin.setTemporaryPassword(true);  // Set initial password as temporary
            userRepository.save(admin);
            logger.info("✅ Default admin user created with temporary password.");

            logger.info("⚡ Seeding default SupportAdmin user...");
            User support = new User();
            support.setUsername("fouiguira_support");
            support.setPassword(passwordEncoder.encode("fouiguira_support"));
            support.setRole(User.Role.SUPPORT_ADMIN);
            support.setCreatedAt(new Date());
            support.setUpdatedAt(new Date());
            support.setTemporaryPassword(false);
            userRepository.save(support);
            logger.info("✅ Default support user created.");

        } else {
            logger.info("⚡ Database already contains {} user(s). No seeding needed.", userCount);
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            logger.info("Categories or products already exist. Skipping seeding.");
            return;
        }

        logger.info("🌱 Seeding categories and products...");

        // Create categories
        Map<String, String> categories = Map.of(
            "Tools", "Hand and power tools for construction",
            "Building Materials", "Basic construction materials",
            "Hardware", "General hardware and fasteners",
            "Plumbing", "Plumbing supplies and fixtures",
            "Electrical", "Electrical supplies and equipment",
            "Paint & Supplies", "Paints, primers, and painting supplies",
            "Safety Equipment", "Personal protective equipment and safety gear"
        );

        categories.forEach((name, description) -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            categoryRepository.save(category);
        });

        // Create products for each category
        Category tools = categoryRepository.findByName("Tools").orElseThrow();
        Category buildingMaterials = categoryRepository.findByName("Building Materials").orElseThrow();
        Category hardware = categoryRepository.findByName("Hardware").orElseThrow();
        Category plumbing = categoryRepository.findByName("Plumbing").orElseThrow();
        Category electrical = categoryRepository.findByName("Electrical").orElseThrow();
        Category paint = categoryRepository.findByName("Paint & Supplies").orElseThrow();
        Category safety = categoryRepository.findByName("Safety Equipment").orElseThrow();

        List<Product> products = Arrays.asList(
            // Tools
            createProduct("Power Drill Set", tools, 159.99, 20, "Professional 18V cordless drill set with batteries"),
            createProduct("Hammer", tools, 24.99, 50, "16 oz claw hammer with fiberglass handle"),
            createProduct("Circular Saw", tools, 129.99, 15, "7-1/4 inch circular saw with laser guide"),
            createProduct("Measuring Tape", tools, 12.99, 100, "25ft retractable measuring tape"),
            createProduct("Wrench Set", tools, 49.99, 30, "10-piece adjustable wrench set"),

            // Building Materials
            createProduct("Cement (50lb)", buildingMaterials, 12.99, 200, "Portland cement 50lb bag"),
            createProduct("Plywood Sheet", buildingMaterials, 32.99, 75, "4'x8' construction grade plywood"),
            createProduct("2x4 Lumber", buildingMaterials, 5.99, 300, "8ft pressure treated 2x4"),
            createProduct("Bricks", buildingMaterials, 0.89, 1000, "Standard red clay brick"),
            createProduct("Drywall Sheet", buildingMaterials, 15.99, 100, "4'x8' standard drywall sheet"),

            // Hardware
            createProduct("Screws (100pc)", hardware, 8.99, 150, "100-piece multipurpose screw set"),
            createProduct("Door Hinges", hardware, 4.99, 200, "3.5\" brass door hinges"),
            createProduct("Door Lock Set", hardware, 29.99, 50, "Keyed entry door lock set"),
            createProduct("Cabinet Handles", hardware, 3.99, 300, "Modern cabinet pull handles"),
            createProduct("Wall Anchors", hardware, 6.99, 200, "50-piece plastic wall anchor set"),

            // Plumbing
            createProduct("PVC Pipe 10ft", plumbing, 8.99, 100, "2-inch PVC pipe"),
            createProduct("Pipe Wrench", plumbing, 24.99, 40, "14-inch pipe wrench"),
            createProduct("Kitchen Faucet", plumbing, 89.99, 25, "Stainless steel kitchen faucet"),
            createProduct("Toilet", plumbing, 149.99, 15, "Two-piece toilet with seat"),
            createProduct("Water Heater", plumbing, 399.99, 10, "40-gallon electric water heater"),

            // Electrical
            createProduct("Wire (100ft)", electrical, 49.99, 50, "12-gauge copper wire"),
            createProduct("Light Switch", electrical, 4.99, 200, "Single-pole light switch"),
            createProduct("Outlet", electrical, 3.99, 300, "Standard electrical outlet"),
            createProduct("Circuit Breaker", electrical, 9.99, 100, "20-amp circuit breaker"),
            createProduct("LED Bulbs (4pk)", electrical, 12.99, 150, "60W equivalent LED bulbs"),

            // Paint & Supplies
            createProduct("Interior Paint", paint, 34.99, 80, "1-gallon interior latex paint"),
            createProduct("Paint Roller Set", paint, 14.99, 100, "9-inch roller with frame and cover"),
            createProduct("Paint Brushes", paint, 9.99, 150, "3-piece paintbrush set"),
            createProduct("Paint Tray", paint, 4.99, 200, "Metal paint tray"),
            createProduct("Primer", paint, 25.99, 60, "1-gallon interior primer"),

            // Safety Equipment
            createProduct("Safety Glasses", safety, 7.99, 200, "Clear safety glasses"),
            createProduct("Work Gloves", safety, 12.99, 150, "Heavy-duty work gloves"),
            createProduct("Hard Hat", safety, 24.99, 100, "ANSI-certified hard hat"),
            createProduct("Dust Masks", safety, 19.99, 300, "20-pack disposable dust masks"),
            createProduct("First Aid Kit", safety, 29.99, 50, "Comprehensive first aid kit")
        );

        productRepository.saveAll(products);
        logger.info("✅ Successfully seeded {} categories and {} products", categories.size(), products.size());
    }

    private Product createProduct(String name, Category category, double price, int stock, String description) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setDescription(description);
        product.setImagePath(PLACEHOLDER_IMAGE);
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        return product;
    }
}
