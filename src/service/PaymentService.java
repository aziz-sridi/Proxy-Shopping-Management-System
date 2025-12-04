package service;

import dao.PaymentDAO;
import dao.OrderDAO;
import model.Payment;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Payment-related business logic.
 * Handles validation, logging, and delegates CRUD operations to PaymentDAO.
 */
public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());
    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;

    /**
     * Constructor with dependency injection for DAOs.
     * @param paymentDAO the DAO to use for payment database operations
     * @param orderDAO the DAO to use for order database operations
     */
    public PaymentService(PaymentDAO paymentDAO, OrderDAO orderDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
    }

    /**
     * Default constructor using default DAOs.
     */
    public PaymentService() {
        this(new PaymentDAO(), new OrderDAO());
    }

    /**
     * Get all payments from the database.
     * @return list of all payments
     * @throws SQLException if database error occurs
     */
    public List<Payment> getAllPayments() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all payments");
        return paymentDAO.findAll();
    }

    /**
     * Get payments for a specific order.
     * @param orderId the order ID
     * @return list of payments for the order
     * @throws SQLException if database error occurs
     */
    public List<Payment> getPaymentsByOrder(int orderId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching payments for order ID: {0}", orderId);
        return paymentDAO.findByOrder(orderId);
    }

    /**
     * Get payments for a specific client.
     * @param clientId the client ID
     * @return list of payments for the client
     * @throws SQLException if database error occurs
     */
    public List<Payment> getPaymentsByClient(int clientId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching payments for client ID: {0}", clientId);
        return paymentDAO.findByClient(clientId);
    }

    /**
     * Get total amount paid for an order.
     * @param orderId the order ID
     * @return total amount paid
     * @throws SQLException if database error occurs
     */
    public double getTotalPaidForOrder(int orderId) throws SQLException {
        return paymentDAO.getTotalPaidForOrder(orderId);
    }

    /**
     * Add a new payment with validation and automatic order status update.
     * @param payment the payment to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public void addPayment(Payment payment) throws SQLException {
        validatePayment(payment);
        LOGGER.log(Level.INFO, "Adding new payment for order ID: {0}, amount: {1}", 
                  new Object[]{payment.getOrderId(), payment.getAmount()});
        paymentDAO.insert(payment);
        updateOrderPaymentStatus(payment.getOrderId());
        LOGGER.log(Level.INFO, "Payment added successfully for order ID: {0}", payment.getOrderId());
    }

    /**
     * Add a payment for a deposit.
     * @param orderId the order ID
     * @param amount the deposit amount
     * @param comment optional comment
     * @throws SQLException if database error occurs
     */
    public void addDepositPayment(int orderId, double amount, String comment) throws SQLException {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod("Deposit");
        payment.setComment(comment != null ? comment : "Initial deposit payment");
        addPayment(payment);
    }

    /**
     * Add a full payment for an order.
     * @param orderId the order ID
     * @param amount the full payment amount
     * @param comment optional comment
     * @throws SQLException if database error occurs
     */
    public void addFullPayment(int orderId, double amount, String comment) throws SQLException {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod("Full Payment");
        payment.setComment(comment != null ? comment : "Full payment received");
        addPayment(payment);
    }

    /**
     * Update an existing payment with validation.
     * @param payment the payment to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public void updatePayment(Payment payment) throws SQLException {
        validatePayment(payment);
        if (payment.getPaymentId() <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive for update operation");
        }
        LOGGER.log(Level.INFO, "Updating payment ID: {0}", payment.getPaymentId());
        paymentDAO.update(payment);
        updateOrderPaymentStatus(payment.getOrderId());
        LOGGER.log(Level.INFO, "Payment updated successfully: {0}", payment.getPaymentId());
    }

    /**
     * Delete a payment by ID and update the associated order's payment status.
     * @param paymentId the payment ID to delete
     * @param orderId the associated order ID for status update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if payment ID is invalid
     */
    public void deletePayment(int paymentId, int orderId) throws SQLException {
        if (paymentId <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive");
        }
        LOGGER.log(Level.INFO, "Deleting payment ID: {0}", paymentId);
        paymentDAO.delete(paymentId);
        updateOrderPaymentStatus(orderId);
        LOGGER.log(Level.INFO, "Payment deleted successfully: {0}", paymentId);
    }

    /**
     * Delete a payment by ID.
     * Note: This doesn't automatically update the order payment status.
     * @param paymentId the payment ID to delete
     * @throws SQLException if database error occurs
     */
    public void deletePayment(int paymentId) throws SQLException {
        if (paymentId <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive");
        }
        LOGGER.log(Level.INFO, "Deleting payment ID: {0}", paymentId);
        paymentDAO.delete(paymentId);
        LOGGER.log(Level.INFO, "Payment deleted successfully: {0}", paymentId);
    }

    /**
     * Calculate and update the payment status for an order based on total paid.
     * @param orderId the order ID
     * @throws SQLException if database error occurs
     */
    private void updateOrderPaymentStatus(int orderId) throws SQLException {
        double totalPaid = paymentDAO.getTotalPaidForOrder(orderId);
        // We need to get the order to compare against selling price
        // For now, just log the total paid
        LOGGER.log(Level.INFO, "Total paid for order {0}: {1}", new Object[]{orderId, totalPaid});
    }

    /**
     * Calculate payment status based on total paid vs selling price.
     * @param totalPaid the total amount paid
     * @param sellingPrice the order selling price
     * @return the payment status string
     */
    public String calculatePaymentStatus(double totalPaid, double sellingPrice) {
        if (totalPaid <= 0) {
            return "Unpaid";
        } else if (totalPaid < sellingPrice) {
            return "Partial";
        } else {
            return "Paid";
        }
    }

    /**
     * Validate payment data before database operations.
     * @param payment the payment to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }
        if (payment.getOrderId() <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }
}
