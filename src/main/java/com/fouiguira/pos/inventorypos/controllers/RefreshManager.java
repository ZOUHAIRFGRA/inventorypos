/*
 * Inventory POS System
 * Copyright (c) 2025 ZOUHAIR FOUIGUIRA. All rights reserved.
 *
 * Licensed under Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International
 * You may not use this file except in compliance with the License.
 *
 * @author ZOUHAIR FOUIGUIRA
 * @version 1.0
 * @since 2025-02-24
 */
package com.fouiguira.pos.inventorypos.controllers;

import java.util.ArrayList;
import java.util.List;

public class RefreshManager {
    private static final List<Runnable> categoryRefreshListeners = new ArrayList<>();

    public static void addCategoryRefreshListener(Runnable listener) {
        categoryRefreshListeners.add(listener);
    }

    public static void removeCategoryRefreshListener(Runnable listener) {
        categoryRefreshListeners.remove(listener);
    }

    public static void notifyCategoryRefresh() {
        categoryRefreshListeners.forEach(Runnable::run);
    }
}