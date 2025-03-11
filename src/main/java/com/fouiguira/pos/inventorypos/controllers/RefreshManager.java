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