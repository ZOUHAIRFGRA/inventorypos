package com.fouiguira.pos.inventorypos.repositories;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category); // Now expects a Category object
    List<Product> findByStockQuantityLessThan(int threshold);
}
