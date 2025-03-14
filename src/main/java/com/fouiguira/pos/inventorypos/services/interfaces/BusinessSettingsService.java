package com.fouiguira.pos.inventorypos.services.interfaces;

import com.fouiguira.pos.inventorypos.entities.BusinessSettings;

public interface BusinessSettingsService {
    BusinessSettings getSettings();
    BusinessSettings saveSettings(BusinessSettings settings);
}