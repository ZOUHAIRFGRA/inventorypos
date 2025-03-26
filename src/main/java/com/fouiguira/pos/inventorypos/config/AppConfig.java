package com.fouiguira.pos.inventorypos.config;

import com.fouiguira.pos.inventorypos.controllers.SettingsController;
import com.fouiguira.pos.inventorypos.repositories.*;
import com.fouiguira.pos.inventorypos.services.impl.*;
import com.fouiguira.pos.inventorypos.services.interfaces.*;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableJpaRepositories(basePackages = "com.fouiguira.pos.inventorypos.repositories")
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    public UserService userService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        return new UserServiceImpl(userRepository, passwordEncoder);
    }

    public ProductService productService(ProductRepository productRepository, SaleProductRepository saleProductRepository) {
        return new ProductServiceImpl(productRepository, saleProductRepository);
    }

    public CategoryService categoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        return new CategoryServiceImpl(categoryRepository, productRepository);
    }

    public BusinessSettingsService businessSettingsService(BusinessSettingsRepository settingsRepository) {
        return new BusinessSettingsImpl(settingsRepository);
    }

    public InvoiceService invoiceService(InvoiceRepository invoiceRepository, SalesService salesService, BusinessSettingsService settingsService, SettingsController settingsController) {
        InvoiceServiceImpl invoiceServiceImpl = new InvoiceServiceImpl(invoiceRepository, settingsService, settingsController);
        invoiceServiceImpl.setSalesService(salesService);
        return invoiceServiceImpl;
    }

    public SalesService salesService(SaleRepository saleRepository, InvoiceService invoiceService) {
        SalesServiceImpl salesServiceImpl = new SalesServiceImpl(saleRepository);
        salesServiceImpl.setInvoiceService(invoiceService);
        return salesServiceImpl;
    }

    @Bean
    public SettingsController settingsController(
        UserService userService, 
        BusinessSettingsService settingsService,
        ProductService productService,
        CategoryService categoryService,
        SalesService salesService,
        DataSource dataSource
    ) {
        return new SettingsController(userService, settingsService, productService, salesService, categoryService, dataSource);
    }

    @Bean
    public Object configureServices(InvoiceService invoiceService, SalesService salesService, BusinessSettingsService businessSettingsService, SettingsController settingsController) {
        ((InvoiceServiceImpl) invoiceService).setSalesService(salesService);
        ((SalesServiceImpl) salesService).setInvoiceService(invoiceService);
        return new Object();
    }
}