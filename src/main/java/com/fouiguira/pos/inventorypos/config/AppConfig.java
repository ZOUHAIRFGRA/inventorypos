package com.fouiguira.pos.inventorypos.config;

import com.fouiguira.pos.inventorypos.repositories.UserRepository;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.repositories.SaleRepository;
import com.fouiguira.pos.inventorypos.repositories.CategoryRepository;
import com.fouiguira.pos.inventorypos.repositories.InvoiceRepository;
import com.fouiguira.pos.inventorypos.services.impl.*;
import com.fouiguira.pos.inventorypos.services.interfaces.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.fouiguira.pos.inventorypos.repositories")
public class AppConfig {

    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    public ProductService productService(ProductRepository productRepository) {
        return new ProductServiceImpl(productRepository);
    }

    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryServiceImpl(categoryRepository, null);
    }

    
    public SalesService salesService(SaleRepository saleRepository) {
        return new SalesServiceImpl(saleRepository);
    }

    
    public InvoiceService invoiceService(InvoiceRepository invoiceRepository) {
        return new InvoiceServiceImpl(invoiceRepository);
    }
}