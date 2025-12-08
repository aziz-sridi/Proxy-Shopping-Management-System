package service;

import model.Order;
import model.Platform;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for Order service operations.
 */
public interface IOrderService {
    
    /**
     * Get all orders from the database.
     * @return list of all orders
     * @throws SQLException if database error occurs
     */
    List<Order> getAllOrders() throws SQLException;
    
    /**
     * Get orders by shipment ID.
     * @param shipmentId the shipment ID
     * @return list of orders for the shipment
     * @throws SQLException if database error occurs
     */
    List<Order> getOrdersByShipment(int shipmentId) throws SQLException;
    
    /**
     * Get orders by client ID.
     * @param clientId the client ID
     * @return list of orders for the client
     * @throws SQLException if database error occurs
     */
    List<Order> getOrdersByClient(int clientId) throws SQLException;
    
    /**
     * Get order by ID.
     * @param orderId the order ID
     * @return the order or null if not found
     * @throws SQLException if database error occurs
     */
    Order getOrderById(int orderId) throws SQLException;
    
    /**
     * Get orders by platform.
     * @param platform the platform
     * @return list of orders for the platform
     * @throws SQLException if database error occurs
     */
    List<Order> getOrdersByPlatform(Platform platform) throws SQLException;
    
    /**
     * Add a new order with validation and price calculation.
     * @param order the order to add
     * @return the generated order ID
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    int addOrder(Order order) throws SQLException;
    
    /**
     * Insert an order without returning the ID.
     * @param order the order to insert
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void insertOrder(Order order) throws SQLException;
    
    /**
     * Update order payment status.
     * @param orderId the order ID
     * @param status the new payment status
     * @throws SQLException if database error occurs
     */
    void updatePaymentStatus(int orderId, String status) throws SQLException;
    
    /**
     * Calculate and update the payment status based on total paid.
     * @param order the order to update
     * @throws SQLException if database error occurs
     */
    void recalculatePaymentStatus(Order order) throws SQLException;
    
    /**
     * Calculate remaining amount for an order.
     * @param order the order
     * @return the remaining amount to pay
     * @throws SQLException if database error occurs
     */
    double getRemainingAmount(Order order) throws SQLException;
    
    /**
     * Calculate selling price based on original price and settings.
     * @param originalPriceEUR the original price in EUR
     * @param quantity the quantity
     * @return the calculated selling price in TND
     */
    double calculateSellingPrice(double originalPriceEUR, int quantity);
    
    /**
     * Calculate deposit amount (50% of total).
     * @param totalSellingPrice the total selling price
     * @return the deposit amount
     */
    double calculateDeposit(double totalSellingPrice);
    
    /**
     * Delete an order by ID.
     * @param orderId the order ID
     * @throws SQLException if database error occurs
     */
    void deleteOrder(int orderId) throws SQLException;
    
    /**
     * Update an existing order.
     * @param order the order to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void updateOrder(Order order) throws SQLException;
}
