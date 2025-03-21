package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Category;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.repositories.CategoryRepository;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        System.out.println("getAllCategories fetched: " + categories);
        return categories;
    }

    @Override
    public Category getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name).orElse(null);
        System.out.println("getCategoryByName '" + name + "' result: " + category);
        return category;
    }

    @Override
    public Category getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        System.out.println("getCategoryById " + id + " result: " + category);
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public Category createCategory(Category category) {
        System.out.println("createCategory started for: " + category);
        Category saved = categoryRepository.save(category);
        System.out.println("Category saved: " + saved);
        System.out.println("After save, all categories: " + categoryRepository.findAll());
        return saved;
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        System.out.println("updateCategory started for id " + id + " with: " + category);
        Category existing = getCategoryById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        Category updated = categoryRepository.save(existing);
        System.out.println("Category updated: " + updated);
        return updated;
    }

    @Override
    public void deleteCategory(Long id) {
        System.out.println("deleteCategory started for id: " + id);
        Category category = getCategoryById(id);
        List<Product> products = productRepository.findByCategory(category);
        System.out.println("Products with category " + category + ": " + products.size());
        for (Product product : products) {
            product.setCategory(null);
            productRepository.save(product);
            System.out.println("Cleared category for product: " + product);
        }
        categoryRepository.delete(category);
        System.out.println("Category deleted: " + id);
    }
}