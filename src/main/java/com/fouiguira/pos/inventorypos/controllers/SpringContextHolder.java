package com.fouiguira.pos.inventorypos.controllers;

import com.fouiguira.pos.inventorypos.main.MainApp;

public class SpringContextHolder {
    public static <T> T getBean(Class<T> clazz) {
        return MainApp.springContext.getBean(clazz);
    }
}