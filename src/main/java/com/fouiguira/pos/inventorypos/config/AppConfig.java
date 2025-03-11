package com.fouiguira.pos.inventorypos.config;

import com.fouiguira.pos.inventorypos.repositories.UserRepository;
import com.fouiguira.pos.inventorypos.repositories.ProductRepository;
import com.fouiguira.pos.inventorypos.repositories.SaleRepository;
import com.fouiguira.pos.inventorypos.repositories.CategoryRepository;
import com.fouiguira.pos.inventorypos.repositories.InvoiceRepository;
import com.fouiguira.pos.inventorypos.services.impl.*;
import com.fouiguira.pos.inventorypos.services.interfaces.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.fouiguira.pos.inventorypos.repositories")
public class AppConfig {

    // @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    // @Bean
    public ProductService productService(ProductRepository productRepository) {
        return new ProductServiceImpl(productRepository);
    }

    // @Bean
    public CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryServiceImpl(categoryRepository, null); // Fix null if CategoryServiceImpl needs a second dependency
    }

    // @Bean
    public InvoiceService invoiceService(InvoiceRepository invoiceRepository, SalesService salesService) {
        InvoiceServiceImpl invoiceServiceImpl = new InvoiceServiceImpl(invoiceRepository);
        invoiceServiceImpl.setSalesService(salesService); // Setter injection since constructor doesn’t take SalesService
        return invoiceServiceImpl;
    }

    // @Bean
    public SalesService salesService(SaleRepository saleRepository, InvoiceService invoiceService) {
        SalesServiceImpl salesServiceImpl = new SalesServiceImpl(saleRepository);
        salesServiceImpl.setInvoiceService(invoiceService); // Setter injection
        return salesServiceImpl;
    }

    @Bean
    public Object configureServices(InvoiceService invoiceService, SalesService salesService) {
        ((InvoiceServiceImpl) invoiceService).setSalesService(salesService);
        ((SalesServiceImpl) salesService).setInvoiceService(invoiceService);
        return new Object();
    }
}