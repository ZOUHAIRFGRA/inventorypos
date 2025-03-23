package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.BusinessSettings;
import com.fouiguira.pos.inventorypos.entities.Product;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.services.interfaces.BusinessSettingsService;
import com.fouiguira.pos.inventorypos.services.interfaces.CategoryService;
import com.fouiguira.pos.inventorypos.services.interfaces.ProductService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import jakarta.persistence.OptimisticLockException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.opencsv.CSVWriter;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Component
public class SettingsController {

    @FXML
    private MFXTextField businessNameField;

    @FXML
    private MFXTextField addressField;

    @FXML
    private MFXTextField phoneField;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXTextField logoPathField;

    @FXML
    private MFXButton uploadLogoButton;

    @FXML
    private MFXButton saveBusinessInfoButton;

    @FXML
    private MFXButton changePasswordButton;

    @FXML
    private MFXButton backupDataButton;

    @FXML
    private MFXButton restoreDataButton;

    @FXML
    private MFXButton exportSalesButton;

    @FXML
    private MFXButton exportProductsButton;

    @FXML
    private MFXButton exportInventoryButton;

    @SuppressWarnings("unused")
    private final UserService userService;
    private final BusinessSettingsService settingsService;
    private final ProductService productService;
    private final SalesService salesService;
    @SuppressWarnings("unused")
    private final CategoryService categoryService;
    private BusinessSettings cachedSettings;

    private static final String CONFIG_DIR = "config";
    private static final String DEFAULT_LOGO = CONFIG_DIR + File.separator + "default_logo.png";

    @Autowired
    private DataSource dataSource;

    public SettingsController(UserService userService,
            BusinessSettingsService settingsService,
            ProductService productService,
            SalesService salesService,
            CategoryService categoryService,
            DataSource dataSource) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.productService = productService;
        this.salesService = salesService;
        this.categoryService = categoryService;
        this.dataSource = dataSource;
        initializeConfig();
    }

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void initializeConfig() {
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File defaultLogoFile = new File(DEFAULT_LOGO);
        if (!defaultLogoFile.exists()) {
            try (InputStream is = getClass().getResourceAsStream("/images/icon.png")) {
                if (is != null) {
                    Files.copy(is, defaultLogoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Default logo copied to: " + defaultLogoFile.getAbsolutePath());
                } else {
                    Files.createFile(defaultLogoFile.toPath());
                    System.out.println("Created empty default logo file: " + defaultLogoFile.getAbsolutePath());
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize default logo: " + e.getMessage());
            }
        }
    }

    private void loadSettings() {
        try {
            cachedSettings = settingsService.getSettings();
            if (cachedSettings.getId() == null) {
                throw new RuntimeException("Settings loaded but not persisted to DB");
            }
            System.out.println("Settings loaded: " + cachedSettings.getBusinessName() + ", Version: " + cachedSettings.getVersion());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load settings: " + e.getMessage());
            cachedSettings = settingsService.getSettings(); // Retry to ensure persistence
            System.out.println("Retried settings load: " + cachedSettings.getBusinessName());
        }
        updateFields();
    }

    private void updateFields() {
        businessNameField.setText(cachedSettings.getBusinessName());
        addressField.setText(cachedSettings.getAddress());
        phoneField.setText(cachedSettings.getPhone());
        emailField.setText(cachedSettings.getEmail());
        logoPathField.setText(cachedSettings.getLogoPath());
    }

    @FXML
    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Logo Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(uploadLogoButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String newLogoPath = CONFIG_DIR + File.separator + "logo_" + System.currentTimeMillis() + "." + getFileExtension(selectedFile);
                Files.copy(selectedFile.toPath(), new File(newLogoPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                logoPathField.setText(newLogoPath);
                cachedSettings.setLogoPath(newLogoPath);
                System.out.println("Logo uploaded: " + newLogoPath);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload logo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveBusinessInfo() {
        try {
            BusinessSettings freshSettings = settingsService.getSettings();
            System.out.println("Fetched fresh settings before save: " + freshSettings.getBusinessName() + ", Version: " + freshSettings.getVersion());
            
            freshSettings.setBusinessName(businessNameField.getText());
            freshSettings.setAddress(addressField.getText());
            freshSettings.setPhone(phoneField.getText());
            freshSettings.setEmail(emailField.getText());
            freshSettings.setLogoPath(logoPathField.getText());
            
            cachedSettings = settingsService.saveSettings(freshSettings);
            System.out.println("Settings saved: " + cachedSettings.getBusinessName() + ", Version: " + cachedSettings.getVersion());
            updateFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Business information saved successfully!");
        } catch (OptimisticLockException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save: Settings were modified by another user. Please try again.");
            System.err.println("OptimisticLockException: " + e.getMessage());
            loadSettings();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save business info: " + e.getMessage());
            System.err.println("Failed to save business info: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackupData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup File");
        fileChooser.setInitialFileName("backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Backup Files", "*.zip"));
        File file = fileChooser.showSaveDialog(backupDataButton.getScene().getWindow());
        
        if (file != null) {
            try {
                // Create a temporary folder for backup
                File tempBackupDir = Files.createTempDirectory("pos_backup_").toFile();
                tempBackupDir.deleteOnExit();

                // Get a connection from the datasource and backup to temp dir
                try (Connection conn = dataSource.getConnection()) {
                    String tempBackupFile = new File(tempBackupDir, "inventory.mv.db").getAbsolutePath();
                    String backupScript = "BACKUP TO '" + tempBackupFile + "'";
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(backupScript);
                        
                        // Create zip file from the backup
                        try (FileOutputStream fos = new FileOutputStream(file);
                             ZipOutputStream zos = new ZipOutputStream(fos)) {
                            
                            // Add the database file to zip
                            File dbBackupFile = new File(tempBackupFile);
                            FileInputStream fis = new FileInputStream(dbBackupFile);
                            ZipEntry zipEntry = new ZipEntry("inventory.mv.db");
                            zos.putNextEntry(zipEntry);
                            
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                            
                            zos.closeEntry();
                            fis.close();
                        }
                        
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Data backed up to: " + file.getAbsolutePath());
                    }
                }
            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to backup data: " + e.getMessage());
                System.err.println("Failed to backup data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRestoreData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Backup Files", "*.zip"));
        File file = fileChooser.showOpenDialog(restoreDataButton.getScene().getWindow());
        
        if (file != null) {
            try {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Restore");
                confirm.setHeaderText(null);
                confirm.setContentText("The application needs to close to perform the restore. Save any unsaved work before continuing. Do you want to proceed?");
                
                if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
                    // Create a temporary directory for restoration
                    File tempRestoreDir = Files.createTempDirectory("pos_restore_").toFile();
                    tempRestoreDir.deleteOnExit();
                    
                    // Extract zip file
                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                        ZipEntry zipEntry = zis.getNextEntry();
                        if (zipEntry != null && zipEntry.getName().equals("inventory.mv.db")) {
                            File extractedDb = new File(tempRestoreDir, "inventory.mv.db");
                            try (FileOutputStream fos = new FileOutputStream(extractedDb)) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, length);
                                }
                            }
                            
                            // Get a connection from the datasource
                            try (Connection conn = dataSource.getConnection()) {
                                // Create a backup of current database before restore
                                String currentBackup = "inventory_before_restore_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".zip";
                                String backupScript = "BACKUP TO '" + currentBackup + "'";
                                try (Statement stmt = conn.createStatement()) {
                                    stmt.execute(backupScript);
                                }
                                
                                // Execute RESTORE command using the extracted file
                                String restoreScript = "RESTORE FROM '" + extractedDb.getAbsolutePath() + "'";
                                try (Statement stmt = conn.createStatement()) {
                                    stmt.execute(restoreScript);
                                    showAlert(Alert.AlertType.INFORMATION, "Success", "Data restored successfully. Application will now close. Please restart.");
                                    System.exit(0);
                                }
                            }
                        } else {
                            throw new IOException("Invalid backup file format");
                        }
                    }
                }
            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to restore data: " + e.getMessage());
                System.err.println("Failed to restore data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportSales() {
        exportToCSV("sales_" + getTimestamp() + ".csv", 
            Arrays.asList("ID", "Date", "Client", "Cashier", "Total", "Payment Method"),
            () -> {
                List<String[]> data = new ArrayList<>();
                for (Sale sale : salesService.getAllSales()) {
                    data.add(new String[]{
                        String.valueOf(sale.getId()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sale.getTimestamp()),
                        sale.getClientName(),
                        sale.getCashier().getUsername(),
                        String.format("%.2f", sale.getTotalPrice()),
                        sale.getPaymentMethod()
                    });
                }
                return data;
            }
        );
    }

    @FXML
    private void handleExportProducts() {
        exportToCSV("products_" + getTimestamp() + ".csv", 
            Arrays.asList("ID", "Name", "Category", "Price", "Stock", "Description"),
            () -> {
                List<String[]> data = new ArrayList<>();
                for (Product product : productService.getAllProducts()) {
                    data.add(new String[]{
                        String.valueOf(product.getId()),
                        product.getName(),
                        product.getCategory() != null ? product.getCategory().getName() : "",
                        String.format("%.2f", product.getPrice()),
                        String.valueOf(product.getStockQuantity()),
                        product.getDescription()
                    });
                }
                return data;
            }
        );
    }

    @FXML
    private void handleExportInventory() {
        exportToCSV("inventory_" + getTimestamp() + ".csv", 
            Arrays.asList("ID", "Product", "Current Stock", "Category", "Last Updated"),
            () -> {
                List<String[]> data = new ArrayList<>();
                for (Product product : productService.getAllProducts()) {
                    data.add(new String[]{
                        String.valueOf(product.getId()),
                        product.getName(),
                        String.valueOf(product.getStockQuantity()),
                        product.getCategory() != null ? product.getCategory().getName() : "",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(product.getUpdatedAt())
                    });
                }
                return data;
            }
        );
    }

    private void exportToCSV(String filename, List<String> headers, DataProvider dataProvider) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.setInitialFileName(filename);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(exportSalesButton.getScene().getWindow());
        
        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                writer.writeNext(headers.toArray(new String[0]));
                writer.writeAll(dataProvider.getData());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data exported to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export data: " + e.getMessage());
            }
        }
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    @FunctionalInterface
    private interface DataProvider {
        List<String[]> getData();
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return lastIndex == -1 ? "png" : name.substring(lastIndex + 1);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BusinessSettings getCachedSettings() {
        if (cachedSettings == null) {
            loadSettings();
        }
        return cachedSettings;
    }
}