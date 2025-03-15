package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.BusinessSettings;
import com.fouiguira.pos.inventorypos.repositories.BusinessSettingsRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.BusinessSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessSettingsImpl implements BusinessSettingsService {

    private final BusinessSettingsRepository settingsRepository;

    public BusinessSettingsImpl(BusinessSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    @Transactional // Remove readOnly=true to allow default creation
    public BusinessSettings getSettings() {
        System.out.println("Fetching settings from DB...");
        BusinessSettings settings = settingsRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> {
                System.out.println("No settings found, creating default...");
                BusinessSettings defaultSettings = new BusinessSettings();
                return saveDefaultSettings(defaultSettings);
            });
        System.out.println("Settings fetched with ID: " + settings.getId() + ", Version: " + settings.getVersion());
        return settings;
    }

    @Transactional
    private BusinessSettings saveDefaultSettings(BusinessSettings settings) {
        BusinessSettings saved = settingsRepository.save(settings);
        System.out.println("Default settings saved with ID: " + saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public BusinessSettings saveSettings(BusinessSettings settings) {
        System.out.println("Saving settings: " + settings.getBusinessName() + ", ID: " + settings.getId() + ", Version: " + settings.getVersion());
        if (settings.getId() != null) {
            BusinessSettings existing = settingsRepository.findById(settings.getId())
                .orElseThrow(() -> new RuntimeException("Settings with ID " + settings.getId() + " not found"));
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