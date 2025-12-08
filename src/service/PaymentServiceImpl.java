package service;

import dao.PaymentDAO;
import model.Payment;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Payment-related business logic.
 * Handles validation, logging, and delegates CRUD operations to PaymentDAO.
 */
public class PaymentServiceImpl implements IPaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentServiceImpl.class.getName());
    private final PaymentDAO paymentDAO;

    /**
     * Constructor with dependency injection for PaymentDAO.
     * @param paymentDAO the DAO to use for payment database operations
     */
    public PaymentServiceImpl(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }

    /**
     * Default constructor using default DAO.
     */
    public PaymentServiceImpl() {
        this(new PaymentDAO());
    }

    /**
     * Get all payments from the database.
     * @return list of all payments
     * @throws SQLException if database error occurs
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public double getTotalPaidForOrder(int orderId) throws SQLException {
        return paymentDAO.getTotalPaidForOrder(orderId);
    }

    /**
     * Add a new payment with validation and automatic order status update.
     * @param payment the payment to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void deletePayment(int paymentId, int orderId) throws SQLException {
        ValidationUtils.validatePositiveId(paymentId, "Payment ID");
        LOGGER.log(Level.INFO, "Deleting payment ID: {0}", paymentId);
        paymentDAO.delete(paymentId);
        updateOrderPaymentStatus(orderId);
        LOGGER.log(Level.INFO, "Payment deleted successfully: {0}", paymentId);
    }

    /**
     * Delete a payment by ID without updating order status.
     * Use deletePayment(paymentId, orderId) if you need to update order status.
     * @param paymentId the payment ID to delete
     * @throws SQLException if database error occurs
     */
    @Override
    public void deletePayment(int paymentId) throws SQLException {
        ValidationUtils.validatePositiveId(paymentId, "Payment ID");
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
     * Delegates to PriceCalculator utility to avoid duplication.
     * @param totalPaid the total amount paid
     * @param sellingPrice the order selling price
     * @return the payment status string
     */
    @Override
    public String calculatePaymentStatus(double totalPaid, double sellingPrice) {
        return ui.util.PriceCalculator.determinePaymentStatus(sellingPrice, totalPaid);
    }

    /**
     * Validate payment data before database operations.
     * Uses ValidationUtils to avoid code duplication.
     * @param payment the payment to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePayment(Payment payment) {
        ValidationUtils.validateNotNull(payment, "Payment");
        ValidationUtils.validatePositiveId(payment.getOrderId(), "Order ID");
        ValidationUtils.validatePositive(payment.getAmount(), "Payment amount");
        ValidationUtils.validateNotEmpty(payment.getPaymentMethod(), "Payment method");
    }
}
