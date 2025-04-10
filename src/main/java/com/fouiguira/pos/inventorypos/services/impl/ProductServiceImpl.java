package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.repositories.SaleProductRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SaleProductRepository saleProductRepository;

    public ProductServiceImpl(ProductRepository productRepository, SaleProductRepository saleProductRepository) {
        this.productRepository = productRepository;
        this.saleProductRepository = saleProductRepository;
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
    }    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = getProductById(id);
        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setPrice(product.getPrice());
        existing.setPurchasePrice(product.getPurchasePrice());
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
    @Transactional
    public void updateStockAfterSale(List<SaleProduct> saleProducts) {
        for (SaleProduct sp : saleProducts) {
            Product product = productRepository.findById(sp.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + sp.getProduct().getId()));
            int newStock = product.getStockQuantity() - sp.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newStock);
            product.setUpdatedAt(new Date());
            productRepository.save(product);
        }
    }

    @Override
    public Map<Product, Integer> getTopSellingProducts(int limit) {
        List<SaleProduct> allSaleProducts = saleProductRepository.findAll();
        
        Map<Product, Integer> productSales = allSaleProducts.stream()
            .collect(Collectors.groupingBy(
                SaleProduct::getProduct,
                Collectors.summingInt(SaleProduct::getQuantity)
            ));

        return productSales.entrySet().stream()
            .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}