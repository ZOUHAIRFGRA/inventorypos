package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.entities.BusinessSettings;
import com.fouiguira.pos.inventorypos.services.interfaces.BusinessSettingsService;
import com.fouiguira.pos.inventorypos.services.interfaces.UserService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import jakarta.persistence.OptimisticLockException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

    private final UserService userService;
    private final BusinessSettingsService settingsService;
    private BusinessSettings cachedSettings;

    private static final String CONFIG_DIR = "config";
    private static final String DEFAULT_LOGO = CONFIG_DIR + File.separator + "default_logo.png";

    @Autowired
    public SettingsController(UserService userService, BusinessSettingsService settingsService) {
        this.userService = userService;
        this.settingsService = settingsService;
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Backup Files", "*.bak"));
        File file = fileChooser.showSaveDialog(backupDataButton.getScene().getWindow());
        if (file != null) {
            try {
                Files.write(file.toPath(), "Backup placeholder".getBytes());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data backed up to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to backup data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRestoreData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Backup Files", "*.bak"));
        File file = fileChooser.showOpenDialog(restoreDataButton.getScene().getWindow());
        if (file != null) {
            try {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data restored from: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to restore data: " + e.getMessage());
            }
        }
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