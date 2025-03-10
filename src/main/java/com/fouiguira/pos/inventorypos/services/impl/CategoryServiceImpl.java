package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.repositories.CategoryRepository;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // Added to manage products

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category existing = getCategoryById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        // Find all products with this category and set their category to null
        List<Product> products = productRepository.findByCategory(category);
        for (Product product : products) {
            product.setCategory(null);
            productRepository.save(product);
        }
        // Now delete the category
        categoryRepository.delete(category);
    }
}