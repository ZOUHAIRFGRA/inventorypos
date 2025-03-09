package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = getProductById(id);
        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setImagePath(product.getImagePath());
        existing.setDescription(product.getDescription());
        existing.setUpdatedAt(new Date());
        return productRepository.save(existing);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThan(threshold);
    }

    @Override
    public void updateStockAfterSale(List<SaleProduct> saleProducts) {
        saleProducts.forEach(sp -> {
            Product product = sp.getProduct();
            int newStock = product.getStockQuantity() - sp.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newStock);
            product.setUpdatedAt(new Date());
            productRepository.save(product);
        });
    }
}