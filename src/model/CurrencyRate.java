package model;

import java.time.LocalDateTime;

public class CurrencyRate {
    private int rateId;
    private String baseCurrency;
    private String targetCurrency;
    private double originalRate;
    private double customRate;
    private LocalDateTime updatedAt;

    public int getRateId() { return rateId; }
    public void setRateId(int rateId) { this.rateId = rateId; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public double getOriginalRate() { return originalRate; }
    public void setOriginalRate(double originalRate) { this.originalRate = originalRate; }

    public double getCustomRate() { return customRate; }
    public void setCustomRate(double customRate) { this.customRate = customRate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
