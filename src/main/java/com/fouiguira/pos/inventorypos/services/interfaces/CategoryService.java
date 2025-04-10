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
package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();  // Retrieve all categories
    Category getCategoryByName(String name);  // Find category by name
    Category getCategoryById(Long id);  // Find category by ID
    Category createCategory(Category category);  // Create a new category
    Category updateCategory(Long id,Category category);  // Update an existing category
    void deleteCategory(Long id);  // Delete a category by ID
}
