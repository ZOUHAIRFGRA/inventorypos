package com.fouiguira.pos.inventorypos.config;

import com.fouiguira.pos.inventorypos.controllers.SettingsController;
import com.fouiguira.pos.inventorypos.repositories.*;
import com.fouiguira.pos.inventorypos.services.impl.*;
import com.fouiguira.pos.inventorypos.services.interfaces.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
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

    public SettingsController settingsController(UserService userService, BusinessSettingsService settingsService) {
        return new SettingsController(userService, settingsService);
    }

    @Bean
    public Object configureServices(InvoiceService invoiceService, SalesService salesService, BusinessSettingsService businessSettingsService, SettingsController settingsController) {
        ((InvoiceServiceImpl) invoiceService).setSalesService(salesService);
        ((SalesServiceImpl) salesService).setInvoiceService(invoiceService);
        return new Object();
    }
}