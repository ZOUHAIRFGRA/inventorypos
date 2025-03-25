package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;

import java.util.List;
import java.util.Map;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> getProductsByCategory(String categoryName);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    List<Product> getLowStockProducts(int threshold);
    void updateStockAfterSale(List<SaleProduct> saleProducts);
    
    // New method for dashboard
    Map<Product, Integer> getTopSellingProducts(int limit);
}