package model;

public class Settings {
    private double conversionRate;
    private double sellingMultiplier;

    public Settings() {
        this.conversionRate = 3.5;
        this.sellingMultiplier = 5.0;
    }

    public Settings(double conversionRate, double sellingMultiplier) {
        this.conversionRate = conversionRate;
        this.sellingMultiplier = sellingMultiplier;
    }

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
