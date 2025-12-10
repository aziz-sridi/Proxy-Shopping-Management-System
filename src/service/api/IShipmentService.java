package service.api;

import model.Order;
import model.Shipment;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interface for Shipment service operations.
 */
public interface IShipmentService {
    
    /**
     * Get all shipments from the database.
     * @return list of all shipments
     * @throws SQLException if database error occurs
     */
    List<Shipment> getAllShipments() throws SQLException;
    
    /**
     * Add a new shipment with validation.
     * @param shipment the shipment to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void addShipment(Shipment shipment) throws SQLException;
    
    /**
     * Update an existing shipment with validation.
     * @param shipment the shipment to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void updateShipment(Shipment shipment) throws SQLException;
    
    /**
     * Delete a shipment by ID.
     * @param shipmentId the shipment ID to delete
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if shipment ID is invalid
     */
    void deleteShipment(int shipmentId) throws SQLException;
    
    /**
     * Get orders for a specific shipment.
     * @param shipmentId the shipment ID
     * @return list of orders in the shipment
     * @throws SQLException if database error occurs
     */
    List<Order> getOrdersForShipment(int shipmentId) throws SQLException;
    
    /**
     * Calculate total number of orders in a shipment.
     * @param shipment the shipment
     * @return total number of orders
     * @throws SQLException if database error occurs
     */
    int calculateTotalOrders(Shipment shipment) throws SQLException;
    
    /**
     * Calculate total cost of goods for all orders in a shipment.
     * Formula: orderCost = unitPriceEUR * conversionRate * quantity
     * @param shipment the shipment
     * @return total cost of goods
     * @throws SQLException if database error occurs
     */
    double calculateTotalCostOfGoods(Shipment shipment) throws SQLException;
    
    /**
     * Calculate total revenue for all orders in a shipment.
     * Formula: orderRevenue = sellingPriceTND * quantity
     * @param shipment the shipment
     * @return total revenue
     * @throws SQLException if database error occurs
     */
    double calculateTotalRevenue(Shipment shipment) throws SQLException;
    
    /**
     * Calculate total expenses for a shipment.
     * Formula: totalExpenses = totalCostOfGoods + transportationCost + otherCosts
     * @param shipment the shipment
     * @return total expenses
     * @throws SQLException if database error occurs
     */
    double calculateTotalExpenses(Shipment shipment) throws SQLException;
    
    /**
     * Calculate net profit for a shipment.
     * Formula: netProfit = totalRevenue - totalExpenses
     * @param shipment the shipment
     * @return net profit
     * @throws SQLException if database error occurs
     */
    double calculateNetProfit(Shipment shipment) throws SQLException;
    
    /**
     * Get complete financial summary for a shipment.
     * @param shipment the shipment
     * @return financial summary as a map with keys: totalOrders, totalCostOfGoods, totalRevenue, 
     *         totalExpenses, netProfit, transportationCost, otherCosts
     * @throws SQLException if database error occurs
     */
    Map<String, Double> getFinancialSummary(Shipment shipment) throws SQLException;
}
