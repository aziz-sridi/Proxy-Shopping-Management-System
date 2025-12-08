package service;

import dao.OrderDAO;
import dao.PaymentDAO;
import model.Order;
import model.Platform;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Order-related business logic.
 * Handles validation, price calculations, logging, and delegates CRUD operations to OrderDAO.
 */
public class OrderServiceImpl implements IOrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderServiceImpl.class.getName());
    private final OrderDAO orderDAO;
    private final PaymentDAO paymentDAO;

    /**
     * Constructor with dependency injection for DAOs.
     * @param orderDAO the DAO to use for order database operations
     * @param paymentDAO the DAO to use for payment database operations
     */
    public OrderServiceImpl(OrderDAO orderDAO, PaymentDAO paymentDAO) {
        this.orderDAO = orderDAO;
        this.paymentDAO = paymentDAO;
    }

    /**
     * Default constructor using default DAOs.
     */
    public OrderServiceImpl() {
        this(new OrderDAO(), new PaymentDAO());
    }

    /**
     * Get all orders from the database.
     * @return list of all orders
     * @throws SQLException if database error occurs
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void updatePaymentStatus(int orderId, String status) throws SQLException {
        ValidationUtils.validatePositiveId(orderId, "Order ID");
        ValidationUtils.validateNotEmpty(status, "Payment status");
        LOGGER.log(Level.INFO, "Updating payment status for order {0} to {1}", new Object[]{orderId, status});
        orderDAO.updatePaymentStatus(orderId, status);
    }

    /**
     * Calculate and update the payment status based on total paid.
     * @param order the order to update
     * @throws SQLException if database error occurs
     */
    @Override
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
    @Override
    public double getRemainingAmount(Order order) throws SQLException {
        double totalPaid = paymentDAO.getTotalPaidForOrder(order.getOrderId());
        return Math.max(0, order.getSellingPrice() - totalPaid);
    }

    /**
     * Calculate selling price based on original price and settings.
     * Delegates to PriceCalculator utility.
     * @param originalPriceEUR the original price in EUR
     * @param quantity the quantity
     * @return the calculated selling price in TND
     */
    @Override
    public double calculateSellingPrice(double originalPriceEUR, int quantity) {
        return ui.util.PriceCalculator.calculateTotalSellingPrice(originalPriceEUR, quantity);
    }

    /**
     * Calculate deposit amount (50% of total).
     * Delegates to PriceCalculator utility.
     * @param totalSellingPrice the total selling price
     * @return the deposit amount
     */
    @Override
    public double calculateDeposit(double totalSellingPrice) {
        return ui.util.PriceCalculator.calculateDeposit(totalSellingPrice);
    }

    /**
     * Delete an order by ID.
     * @param orderId the order ID
     * @throws SQLException if database error occurs
     */
    @Override
    public void deleteOrder(int orderId) throws SQLException {
        ValidationUtils.validatePositiveId(orderId, "Order ID");
        LOGGER.log(Level.INFO, "Deleting order ID: {0}", orderId);
        orderDAO.delete(orderId);
        LOGGER.log(Level.INFO, "Order deleted successfully");
    }

    /**
     * Update an existing order.
     * @param order the order to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void updateOrder(Order order) throws SQLException {
        if (order == null || order.getOrderId() <= 0) {
            throw new IllegalArgumentException("Order must be valid with a positive ID");
        }
        validateOrder(order);
        LOGGER.log(Level.INFO, "Updating order ID: {0}", order.getOrderId());
        orderDAO.update(order);
        LOGGER.log(Level.INFO, "Order updated successfully");
    }

    /**
     * Calculate selling price for an order and set it.
     * Uses PriceCalculator utility to avoid duplication.
     * @param order the order to calculate price for
     */
    private void calculateSellingPrice(Order order) {
        if (order.getSellingPrice() <= 0 && order.getOriginalPrice() > 0) {
            double sellingPrice = ui.util.PriceCalculator.calculateTotalSellingPrice(
                order.getOriginalPrice(), order.getQuantity());
            order.setSellingPrice(sellingPrice);
            LOGGER.log(Level.INFO, "Calculated selling price: {0}", sellingPrice);
        }
    }

    /**
     * Validate order data before database operations.
     * Uses ValidationUtils to avoid code duplication.
     * @param order the order to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateOrder(Order order) {
        ValidationUtils.validateNotNull(order, "Order");
        ValidationUtils.validatePositiveId(order.getClientId(), "Client ID");
        if (order.getShipmentId() == null || order.getShipmentId() <= 0) {
            throw new IllegalArgumentException("Shipment must be selected");
        }
        ValidationUtils.validatePositiveId(order.getQuantity(), "Quantity");
        ValidationUtils.validatePositive(order.getOriginalPrice(), "Original price");
    }
}
