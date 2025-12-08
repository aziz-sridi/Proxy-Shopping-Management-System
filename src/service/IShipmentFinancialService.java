package service;

import model.Shipment;

import java.sql.SQLException;

/**
 * Interface for Shipment financial calculations.
 * @deprecated Use IShipmentService instead for financial calculations.
 * This interface is kept for backward compatibility.
 */
@Deprecated
public interface IShipmentFinancialService {
    
    /**
     * Calculate total number of orders in a shipment
     */
    int calculateTotalOrders(Shipment shipment) throws SQLException;
    
    /**
     * Calculate total cost of goods for all orders in a shipment
     * Formula: orderCost = unitPriceEUR * conversionRate * quantity
     */
    double calculateTotalCostOfGoods(Shipment shipment) throws SQLException;
    
    /**
     * Calculate total revenue for all orders in a shipment
     * Formula: orderRevenue = sellingPriceTND * quantity
     */
    double calculateTotalRevenue(Shipment shipment) throws SQLException;
    
    /**
     * Calculate total expenses for a shipment
     * Formula: totalExpenses = totalCostOfGoods + transportationCost + otherCosts
     */
    double calculateTotalExpenses(Shipment shipment) throws SQLException;
    
    /**
     * Calculate net profit for a shipment
     * Formula: netProfit = totalRevenue - totalExpenses
     */
    double calculateNetProfit(Shipment shipment) throws SQLException;
    
    /**
     * Get complete financial summary for a shipment
     */
    ShipmentFinancialServiceImpl.ShipmentFinancialSummary getFinancialSummary(Shipment shipment) throws SQLException;
}
