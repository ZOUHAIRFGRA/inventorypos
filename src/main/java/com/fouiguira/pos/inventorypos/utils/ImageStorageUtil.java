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
package com.fouiguira.pos.inventorypos.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageStorageUtil {
    private static final String IMAGE_DIR = "storage/images/";

    public static String saveImage(File selectedFile) throws IOException {
        File storageFolder = new File(IMAGE_DIR);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
        File destinationFile = new File(storageFolder, uniqueFileName);

        Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return destinationFile.getAbsolutePath();
    }
}
