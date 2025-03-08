package com.fouiguira.pos.inventorypos.services.interfaces;
import com.fouiguira.pos.inventorypos.entities.Product;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String categoryName); // Change parameter to categoryName
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    List<Product> getLowStockProducts(int threshold);
}
