package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();  // Retrieve all categories
    Category getCategoryByName(String name);  // Find category by name
    Category getCategoryById(Long id);  // Find category by ID
    void createCategory(Category category);  // Create a new category
    void updateCategory(Category category);  // Update an existing category
    void deleteCategory(Long id);  // Delete a category by ID
}
