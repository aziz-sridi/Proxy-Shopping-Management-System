package service;

import dao.OrderDAO;
import model.Order;
import model.Shipment;
import model.Settings;
import util.SettingsManager;

import java.sql.SQLException;
import java.util.List;

public class ShipmentFinancialService {
    
    private final OrderDAO orderDAO;
    
    public ShipmentFinancialService() {
        this.orderDAO = new OrderDAO();
    }

    /**
     * Calculate total number of orders in a shipment
     */
    public int calculateTotalOrders(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        return orders.size();
    }

    /**
     * Calculate total cost of goods for all orders in a shipment
     * Formula: orderCost = unitPriceEUR * conversionRate * quantity
     */
    public double calculateTotalCostOfGoods(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        Settings settings = SettingsManager.getCurrentSettings();
        double conversionRate = settings.getConversionRate();
        
        double totalCost = 0.0;
        for (Order order : orders) {
            double orderCost = order.getOriginalPrice() * conversionRate * order.getQuantity();
            totalCost += orderCost;
        }
        return totalCost;
    }

    /**
     * Calculate total revenue for all orders in a shipment
     * Formula: orderRevenue = sellingPriceTND * quantity
     */
    public double calculateTotalRevenue(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        Settings settings = SettingsManager.getCurrentSettings();
        double sellingMultiplier = settings.getSellingMultiplier();
        
        double totalRevenue = 0.0;
        for (Order order : orders) {
            // Calculate selling price in TND using the multiplier
            double sellingPriceTND = order.getOriginalPrice() * sellingMultiplier;
            double orderRevenue = sellingPriceTND * order.getQuantity();
            totalRevenue += orderRevenue;
        }
        return totalRevenue;
    }

    /**
     * Calculate total expenses for a shipment
     * Formula: totalExpenses = totalCostOfGoods + transportationCost + otherCosts
     */
    public double calculateTotalExpenses(Shipment shipment) throws SQLException {
        double totalCostOfGoods = calculateTotalCostOfGoods(shipment);
        return totalCostOfGoods + shipment.getTransportationCost() + shipment.getOtherCosts();
    }

    /**
     * Calculate net profit for a shipment
     * Formula: netProfit = totalRevenue - totalExpenses
     */
    public double calculateNetProfit(Shipment shipment) throws SQLException {
        double totalRevenue = calculateTotalRevenue(shipment);
        double totalExpenses = calculateTotalExpenses(shipment);
        return totalRevenue - totalExpenses;
    }

    /**
     * Get complete financial summary for a shipment
     */
    public ShipmentFinancialSummary getFinancialSummary(Shipment shipment) throws SQLException {
        ShipmentFinancialSummary summary = new ShipmentFinancialSummary();
        summary.setShipment(shipment);
        summary.setTotalOrders(calculateTotalOrders(shipment));
        summary.setTotalCostOfGoods(calculateTotalCostOfGoods(shipment));
        summary.setTransportationCost(shipment.getTransportationCost());
        summary.setOtherCosts(shipment.getOtherCosts());
        summary.setTotalRevenue(calculateTotalRevenue(shipment));
        summary.setTotalExpenses(calculateTotalExpenses(shipment));
        summary.setNetProfit(calculateNetProfit(shipment));
        return summary;
    }

    /**
     * Data class for shipment financial summary
     */
    public static class ShipmentFinancialSummary {
        private Shipment shipment;
        private int totalOrders;
        private double totalCostOfGoods;
        private double transportationCost;
        private double otherCosts;
        private double totalRevenue;
        private double totalExpenses;
        private double netProfit;

        // Getters and setters
        public Shipment getShipment() { return shipment; }
        public void setShipment(Shipment shipment) { this.shipment = shipment; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public double getTotalCostOfGoods() { return totalCostOfGoods; }
        public void setTotalCostOfGoods(double totalCostOfGoods) { this.totalCostOfGoods = totalCostOfGoods; }

        public double getTransportationCost() { return transportationCost; }
        public void setTransportationCost(double transportationCost) { this.transportationCost = transportationCost; }

        public double getOtherCosts() { return otherCosts; }
        public void setOtherCosts(double otherCosts) { this.otherCosts = otherCosts; }

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

        public double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }

        public double getNetProfit() { return netProfit; }
        public void setNetProfit(double netProfit) { this.netProfit = netProfit; }
    }
}
