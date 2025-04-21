package com.fouiguira.pos.inventorypos.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class VersionUtil {
    private static final String VERSION_MAPPING = "0.0.1-SNAPSHOT=1.0";
    
    public static String getPomVersion() {
        try {
            Properties properties = new Properties();
            try (InputStream is = VersionUtil.class.getResourceAsStream("/META-INF/maven/com.fouiguira.pos/inventorypos/pom.properties")) {
                if (is != null) {
                    properties.load(is);
                    return properties.getProperty("version");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0.0.1-SNAPSHOT"; // fallback version
    }
    
    public static String getReleaseVersion() {
        String pomVersion = getPomVersion();
        String[] mappings = VERSION_MAPPING.split(",");
        for (String mapping : mappings) {
            String[] parts = mapping.split("=");
            if (parts[0].equals(pomVersion)) {
                return parts[1];
            }
        }
        return "1.0"; // fallback version
    }
    
    public static UpdateInfo checkForUpdates(String currentVersion, String githubApiUrl) throws IOException {
        URL url = new URL(githubApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to check for updates: " + conn.getResponseCode());
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            JSONObject json = new JSONObject(response.toString());
            String latestVersion = json.getString("tag_name").replace("v", "");
            String downloadUrl = json.getString("html_url");
            String releaseNotes = json.getString("body");
            
            boolean hasUpdate = compareVersions(currentVersion, latestVersion) < 0;
            
            return new UpdateInfo(hasUpdate, latestVersion, downloadUrl, releaseNotes);
        }
    }
    
    private static int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");
        
        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2 = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            
            if (v1 < v2) return -1;
            if (v1 > v2) return 1;
        }
        return 0;
    }
    
    public static class UpdateInfo {
        private final boolean updateAvailable;
        private final String latestVersion;
        private final String downloadUrl;
        private final String releaseNotes;
        
        public UpdateInfo(boolean updateAvailable, String latestVersion, String downloadUrl, String releaseNotes) {
            this.updateAvailable = updateAvailable;
            this.latestVersion = latestVersion;
            this.downloadUrl = downloadUrl;
            this.releaseNotes = releaseNotes;
        }
        
        public boolean isUpdateAvailable() {
            return updateAvailable;
        }
        
        public String getLatestVersion() {
            return latestVersion;
        }
        
        public String getDownloadUrl() {
            return downloadUrl;
        }
        
        public String getReleaseNotes() {
            return releaseNotes;
        }
    }
}
