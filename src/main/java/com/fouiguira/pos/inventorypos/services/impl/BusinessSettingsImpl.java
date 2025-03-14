package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.BusinessSettings;
import com.fouiguira.pos.inventorypos.repositories.BusinessSettingsRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.BusinessSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessSettingsImpl implements BusinessSettingsService {

    private final BusinessSettingsRepository settingsRepository;

    @Autowired
    public BusinessSettingsImpl(BusinessSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    @Transactional(readOnly = true, timeout = 5) // Timeout to fail fast if locked
    public BusinessSettings getSettings() {
        System.out.println("Fetching settings from DB...");
        return settingsRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> {
                System.out.println("No settings found, creating default...");
                BusinessSettings defaultSettings = new BusinessSettings();
                // Save in a separate transaction to avoid locking
                try {
                    return saveDefaultSettings(defaultSettings);
                } catch (Exception e) {
                    System.out.println("Failed to save default settings: " + e.getMessage());
                    return defaultSettings; // Return unsaved default if save fails
                }
            });
    }

    @Transactional
    private BusinessSettings saveDefaultSettings(BusinessSettings settings) {
        return settingsRepository.save(settings);
    }

    @Override
    @Transactional
    public BusinessSettings saveSettings(BusinessSettings settings) {
        System.out.println("Saving settings: " + settings.getBusinessName());
        BusinessSettings existing = settingsRepository.findAll().stream()
            .findFirst()
            .orElse(null);
        if (existing != null) {
            existing.setBusinessName(settings.getBusinessName());
            existing.setAddress(settings.getAddress());
            existing.setPhone(settings.getPhone());
            existing.setEmail(settings.getEmail());
            existing.setLogoPath(settings.getLogoPath());
            return settingsRepository.save(existing);
        } else {
            return settingsRepository.save(settings);
        }
    }
}