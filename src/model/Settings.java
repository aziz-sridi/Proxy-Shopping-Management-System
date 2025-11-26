package model;

/**
 * Settings model for managing application configuration
 */
public class Settings {
    private double conversionRate;
    private double sellingMultiplier;
    
    // Default constructor with default values
    public Settings() {
        this.conversionRate = 3.5;
        this.sellingMultiplier = 5.0;
    }
    
    // Constructor with parameters
    public Settings(double conversionRate, double sellingMultiplier) {
        this.conversionRate = conversionRate;
        this.sellingMultiplier = sellingMultiplier;
    }
    
    // Getters and Setters
    public double getConversionRate() {
        return conversionRate;
    }
    
    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }
    
    public double getSellingMultiplier() {
        return sellingMultiplier;
    }
    
    public void setSellingMultiplier(double sellingMultiplier) {
        this.sellingMultiplier = sellingMultiplier;
    }
    
    @Override
    public String toString() {
        return "Settings{" +
                "conversionRate=" + conversionRate +
                ", sellingMultiplier=" + sellingMultiplier +
                '}';
    }
}
