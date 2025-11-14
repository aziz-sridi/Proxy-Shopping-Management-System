package model;

import java.time.LocalDateTime;

public class Profit {
    private int profitId;
    private int orderId;
    private double originalRate;
    private double customRate;
    private double shipmentCost;
    private double calculatedProfit;
    private LocalDateTime createdAt;

    public int getProfitId() { return profitId; }
    public void setProfitId(int profitId) { this.profitId = profitId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getOriginalRate() { return originalRate; }
    public void setOriginalRate(double originalRate) { this.originalRate = originalRate; }

    public double getCustomRate() { return customRate; }
    public void setCustomRate(double customRate) { this.customRate = customRate; }

    public double getShipmentCost() { return shipmentCost; }
    public void setShipmentCost(double shipmentCost) { this.shipmentCost = shipmentCost; }

    public double getCalculatedProfit() { return calculatedProfit; }
    public void setCalculatedProfit(double calculatedProfit) { this.calculatedProfit = calculatedProfit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
