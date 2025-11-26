import model.Settings;
import util.SettingsManager;

public class TestSettings {
    public static void main(String[] args) {
        System.out.println("=== Testing Dynamic Settings System ===");
        
        // Test 1: Default settings
        Settings defaultSettings = SettingsManager.getCurrentSettings();
        System.out.println("Default Settings:");
        System.out.println("  Conversion Rate: " + defaultSettings.getConversionRate());
        System.out.println("  Selling Multiplier: " + defaultSettings.getSellingMultiplier());
        
        // Test 2: Update settings
        Settings newSettings = new Settings(4.0, 6.5);
        SettingsManager.saveSettings(newSettings);
        System.out.println("\nSaved new settings:");
        System.out.println("  Conversion Rate: " + newSettings.getConversionRate());
        System.out.println("  Selling Multiplier: " + newSettings.getSellingMultiplier());
        
        // Test 3: Load settings from file
        Settings loadedSettings = SettingsManager.getCurrentSettings();
        System.out.println("\nLoaded Settings:");
        System.out.println("  Conversion Rate: " + loadedSettings.getConversionRate());
        System.out.println("  Selling Multiplier: " + loadedSettings.getSellingMultiplier());
        
        // Test 4: Price calculation simulation
        double testEurPrice = 50.0;
        double calculatedTndPrice = testEurPrice * loadedSettings.getSellingMultiplier();
        System.out.println("\nPrice Calculation Test:");
        System.out.println("  EUR Price: €" + testEurPrice);
        System.out.println("  Multiplier: " + loadedSettings.getSellingMultiplier());
        System.out.println("  TND Price: " + calculatedTndPrice + " TND");
        
        System.out.println("\n=== Settings System Test Complete ===");
    }
}
