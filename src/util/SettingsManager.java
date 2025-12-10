package util;

import model.Settings;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SettingsManager {
    
    private static final String SETTINGS_FILE = "settings.properties";
    private static Settings currentSettings;
    public static Settings loadSettings() {
        if (currentSettings != null) {
            return currentSettings;
        }
        
        Properties props = new Properties();
        File settingsFile = new File(SETTINGS_FILE);
        
        if (settingsFile.exists()) {
            try (InputStream input = Files.newInputStream(Paths.get(SETTINGS_FILE))) {
                props.load(input);
                
                double conversionRate = Double.parseDouble(
                    props.getProperty("conversionRate", "3.5")
                );
                double sellingMultiplier = Double.parseDouble(
                    props.getProperty("sellingMultiplier", "5.0")
                );
                
                currentSettings = new Settings(conversionRate, sellingMultiplier);
                System.out.println("Settings loaded: " + currentSettings);
                
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading settings: " + e.getMessage());
                currentSettings = new Settings(); // Use defaults
            }
        } else {
            System.out.println("Settings file not found, using defaults");
            currentSettings = new Settings(); // Use defaults
            saveSettings(currentSettings); // Create the file with defaults
        }
        
        return currentSettings;
    }
    
    public static void saveSettings(Settings settings) {
        Properties props = new Properties();
        props.setProperty("conversionRate", String.valueOf(settings.getConversionRate()));
        props.setProperty("sellingMultiplier", String.valueOf(settings.getSellingMultiplier()));
        
        try (OutputStream output = Files.newOutputStream(Paths.get(SETTINGS_FILE))) {
            props.store(output, "Shop Management System Settings");
            currentSettings = settings;
            System.out.println("Settings saved: " + settings);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    public static Settings getCurrentSettings() {
        if (currentSettings == null) {
            return loadSettings();
        }
        return currentSettings;
    }
    
    public static void updateSettings(double conversionRate, double sellingMultiplier) {
        Settings newSettings = new Settings(conversionRate, sellingMultiplier);
        saveSettings(newSettings);
    }

    public static void updateConversionRate(double conversionRate) {
        Settings current = getCurrentSettings();
        double sellingMultiplier = current.getSellingMultiplier();
        updateSettings(conversionRate, sellingMultiplier);
    }
}
