package service;

import dao.OrderDAO;
import dao.PaymentDAO;
import model.Order;
import model.Platform;
import model.Settings;
import util.SettingsManager;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Order-related business logic.
 * Handles validation, price calculations, logging, and delegates CRUD operations to OrderDAO.
 */
public class OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());
    private final OrderDAO orderDAO;
    private final PaymentDAO paymentDAO;

    /**
     * Constructor with dependency injection for DAOs.
     * @param orderDAO the DAO to use for order database operations
     * @param paymentDAO the DAO to use for payment database operations
     */
    public OrderService(OrderDAO orderDAO, PaymentDAO paymentDAO) {
        this.orderDAO = orderDAO;
        this.paymentDAO = paymentDAO;
    }

    /**
     * Default constructor using default DAOs.
     */
    public OrderService() {
        this(new OrderDAO(), new PaymentDAO());
    }

    /**
     * Get all orders from the database.
     * @return list of all orders
     * @throws SQLException if database error occurs
     */
    public List<Order> getAllOrders() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all orders");
        return orderDAO.findAll();
    }

    /**
     * Get orders by shipment ID.
     * @param shipmentId the shipment ID
     * @return list of orders for the shipment
     * @throws SQLException if database error occurs
     */
    public List<Order> getOrdersByShipment(int shipmentId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching orders for shipment ID: {0}", shipmentId);
        return orderDAO.findByShipment(shipmentId);
    }

    /**
     * Get orders by client ID.
     * @param clientId the client ID
     * @return list of orders for the client
     * @throws SQLException if database error occurs
     */
    public List<Order> getOrdersByClient(int clientId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching orders for client ID: {0}", clientId);
        return orderDAO.findByClient(clientId);
    }

    /**
     * Get order by ID.
     * @param orderId the order ID
     * @return the order or null if not found
     * @throws SQLException if database error occurs
     */
    public Order getOrderById(int orderId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching order ID: {0}", orderId);
        return orderDAO.findById(orderId);
    }

    /**
     * Get orders by platform.
     * @param platform the platform
     * @return list of orders for the platform
     * @throws SQLException if database error occurs
     */
    public List<Order> getOrdersByPlatform(Platform platform) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching orders for platform: {0}", platform);
        return orderDAO.findByPlatform(platform);
    }

    /**
     * Add a new order with validation and price calculation.
     * @param order the order to add
     * @return the generated order ID
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public int addOrder(Order order) throws SQLException {
        validateOrder(order);
        calculateSellingPrice(order);
        LOGGER.log(Level.INFO, "Adding new order for client ID: {0}", order.getClientId());
        int orderId = orderDAO.insertAndReturnId(order);
        LOGGER.log(Level.INFO, "Order added successfully with ID: {0}", orderId);
        return orderId;
    }

    /**
     * Insert an order without returning the ID.
     * @param order the order to insert
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public void insertOrder(Order order) throws SQLException {
        validateOrder(order);
        calculateSellingPrice(order);
        LOGGER.log(Level.INFO, "Inserting order for client ID: {0}", order.getClientId());
        orderDAO.insert(order);
        LOGGER.log(Level.INFO, "Order inserted successfully");
    }

    /**
     * Update order payment status.
     * @param orderId the order ID
     * @param status the new payment status
     * @throws SQLException if database error occurs
     */
    public void updatePaymentStatus(int orderId, String status) throws SQLException {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment status cannot be empty");
        }
        LOGGER.log(Level.INFO, "Updating payment status for order {0} to {1}", new Object[]{orderId, status});
        orderDAO.updatePaymentStatus(orderId, status);
    }

    /**
     * Calculate and update the payment status based on total paid.
     * @param order the order to update
     * @throws SQLException if database error occurs
     */
    public void recalculatePaymentStatus(Order order) throws SQLException {
        double totalPaid = paymentDAO.getTotalPaidForOrder(order.getOrderId());
        double sellingPrice = order.getSellingPrice();
        
        String newStatus;
        if (totalPaid <= 0) {
            newStatus = "Unpaid";
        } else if (totalPaid < sellingPrice) {
            newStatus = "Partial";
        } else {
            newStatus = "Paid";
        }
        
        updatePaymentStatus(order.getOrderId(), newStatus);
        order.setPaymentStatus(newStatus);
    }

    /**
     * Calculate remaining amount for an order.
     * @param order the order
     * @return the remaining amount to pay
     * @throws SQLException if database error occurs
     */
    public double getRemainingAmount(Order order) throws SQLException {
        double totalPaid = paymentDAO.getTotalPaidForOrder(order.getOrderId());
        return Math.max(0, order.getSellingPrice() - totalPaid);
    }

    /**
     * Calculate selling price based on original price and settings.
     * @param originalPriceEUR the original price in EUR
     * @param quantity the quantity
     * @return the calculated selling price in TND
     */
    public double calculateSellingPrice(double originalPriceEUR, int quantity) {
        Settings settings = SettingsManager.getCurrentSettings();
        double sellingMultiplier = settings.getSellingMultiplier();
        return originalPriceEUR * sellingMultiplier * quantity;
    }

    /**
     * Calculate deposit amount (50% of total).
     * @param totalSellingPrice the total selling price
     * @return the deposit amount
     */
    public double calculateDeposit(double totalSellingPrice) {
        return totalSellingPrice * 0.5;
    }

    /**
     * Calculate selling price for an order and set it.
     * @param order the order to calculate price for
     */
    private void calculateSellingPrice(Order order) {
        if (order.getSellingPrice() <= 0 && order.getOriginalPrice() > 0) {
            Settings settings = SettingsManager.getCurrentSettings();
            double sellingMultiplier = settings.getSellingMultiplier();
            double sellingPrice = order.getOriginalPrice() * sellingMultiplier * order.getQuantity();
            order.setSellingPrice(sellingPrice);
            LOGGER.log(Level.INFO, "Calculated selling price: {0}", sellingPrice);
        }
    }

    /**
     * Validate order data before database operations.
     * @param order the order to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.getClientId() <= 0) {
            throw new IllegalArgumentException("Client ID must be positive");
        }
        if (order.getShipmentId() == null || order.getShipmentId() <= 0) {
            throw new IllegalArgumentException("Shipment must be selected");
        }
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (order.getOriginalPrice() <= 0) {
            throw new IllegalArgumentException("Original price must be positive");
        }
    }
}
