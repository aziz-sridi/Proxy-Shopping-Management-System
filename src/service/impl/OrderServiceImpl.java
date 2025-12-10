package service.impl;

import service.api.IOrderService;
import dao.OrderDAO;
import dao.PaymentDAO;
import model.Order;
import model.Platform;
import service.ValidationUtils;
import ui.util.PriceCalculator;

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

    public OrderServiceImpl(OrderDAO orderDAO, PaymentDAO paymentDAO) {
        this.orderDAO = orderDAO;
        this.paymentDAO = paymentDAO;
    }

    public OrderServiceImpl() {
        this(new OrderDAO(), new PaymentDAO());
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all orders");
        return orderDAO.findAll();
    }

    @Override
    public List<Order> getOrdersByShipment(int shipmentId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching orders for shipment ID: {0}", shipmentId);
        return orderDAO.findByShipment(shipmentId);
    }

    @Override
    public List<Order> getOrdersByClient(int clientId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching orders for client ID: {0}", clientId);
        return orderDAO.findByClient(clientId);
    }

    @Override
    public Order getOrderById(int orderId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching order ID: {0}", orderId);
        return orderDAO.findById(orderId);
    }

    @Override
    public List<Order> getOrdersByPlatform(Platform platform) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching orders for platform: {0}", platform);
        return orderDAO.findByPlatform(platform);
    }

    @Override
    public int addOrder(Order order) throws SQLException {
        validateOrder(order);
        calculateSellingPrice(order);
        LOGGER.log(Level.INFO, "Adding new order for client ID: {0}", order.getClientId());
        int orderId = orderDAO.insertAndReturnId(order);
        LOGGER.log(Level.INFO, "Order added successfully with ID: {0}", orderId);
        return orderId;
    }

    @Override
    public void insertOrder(Order order) throws SQLException {
        validateOrder(order);
        calculateSellingPrice(order);
        LOGGER.log(Level.INFO, "Inserting order for client ID: {0}", order.getClientId());
        orderDAO.insert(order);
        LOGGER.log(Level.INFO, "Order inserted successfully");
    }

    @Override
    public void updatePaymentStatus(int orderId, String status) throws SQLException {
        ValidationUtils.validatePositiveId(orderId, "Order ID");
        ValidationUtils.validateNotEmpty(status, "Payment status");
        LOGGER.log(Level.INFO, "Updating payment status for order {0} to {1}", new Object[]{orderId, status});
        orderDAO.updatePaymentStatus(orderId, status);
    }

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

    @Override
    public double getRemainingAmount(Order order) throws SQLException {
        double totalPaid = paymentDAO.getTotalPaidForOrder(order.getOrderId());
        return Math.max(0, order.getSellingPrice() - totalPaid);
    }

    @Override
    public double calculateSellingPrice(double originalPriceEUR, int quantity) {
        return PriceCalculator.calculateTotalSellingPrice(originalPriceEUR, quantity);
    }

    @Override
    public double calculateDeposit(double totalSellingPrice) {
        return PriceCalculator.calculateDeposit(totalSellingPrice);
    }

    @Override
    public void deleteOrder(int orderId) throws SQLException {
        ValidationUtils.validatePositiveId(orderId, "Order ID");
        LOGGER.log(Level.INFO, "Deleting order ID: {0}", orderId);
        orderDAO.delete(orderId);
        LOGGER.log(Level.INFO, "Order deleted successfully");
    }

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

    private void calculateSellingPrice(Order order) {
        if (order.getSellingPrice() <= 0 && order.getOriginalPrice() > 0) {
            double sellingPrice = PriceCalculator.calculateTotalSellingPrice(
                order.getOriginalPrice(), order.getQuantity());
            order.setSellingPrice(sellingPrice);
            LOGGER.log(Level.INFO, "Calculated selling price: {0}", sellingPrice);
        }
    }

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
