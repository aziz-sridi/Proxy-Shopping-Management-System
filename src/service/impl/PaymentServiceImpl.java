package service.impl;

import service.api.IPaymentService;
import dao.PaymentDAO;
import model.Payment;
import service.ValidationUtils;
import ui.util.PriceCalculator;

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

    public PaymentServiceImpl(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }

    public PaymentServiceImpl() {
        this(new PaymentDAO());
    }

    @Override
    public List<Payment> getAllPayments() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all payments");
        return paymentDAO.findAll();
    }

    @Override
    public List<Payment> getPaymentsByOrder(int orderId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching payments for order ID: {0}", orderId);
        return paymentDAO.findByOrder(orderId);
    }

    @Override
    public List<Payment> getPaymentsByClient(int clientId) throws SQLException {
        LOGGER.log(Level.INFO, "Fetching payments for client ID: {0}", clientId);
        return paymentDAO.findByClient(clientId);
    }

    @Override
    public double getTotalPaidForOrder(int orderId) throws SQLException {
        return paymentDAO.getTotalPaidForOrder(orderId);
    }

    @Override
    public void addPayment(Payment payment) throws SQLException {
        validatePayment(payment);
        LOGGER.log(Level.INFO, "Adding new payment for order ID: {0}, amount: {1}", 
                  new Object[]{payment.getOrderId(), payment.getAmount()});
        paymentDAO.insert(payment);
        updateOrderPaymentStatus(payment.getOrderId());
        LOGGER.log(Level.INFO, "Payment added successfully for order ID: {0}", payment.getOrderId());
    }

    @Override
    public void addDepositPayment(int orderId, double amount, String comment) throws SQLException {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod("Deposit");
        payment.setComment(comment != null ? comment : "Initial deposit payment");
        addPayment(payment);
    }

    @Override
    public void addFullPayment(int orderId, double amount, String comment) throws SQLException {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod("Full Payment");
        payment.setComment(comment != null ? comment : "Full payment received");
        addPayment(payment);
    }

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

    @Override
    public void deletePayment(int paymentId, int orderId) throws SQLException {
        ValidationUtils.validatePositiveId(paymentId, "Payment ID");
        LOGGER.log(Level.INFO, "Deleting payment ID: {0}", paymentId);
        paymentDAO.delete(paymentId);
        updateOrderPaymentStatus(orderId);
        LOGGER.log(Level.INFO, "Payment deleted successfully: {0}", paymentId);
    }

    @Override
    public void deletePayment(int paymentId) throws SQLException {
        ValidationUtils.validatePositiveId(paymentId, "Payment ID");
        LOGGER.log(Level.INFO, "Deleting payment ID: {0}", paymentId);
        paymentDAO.delete(paymentId);
        LOGGER.log(Level.INFO, "Payment deleted successfully: {0}", paymentId);
    }

    private void updateOrderPaymentStatus(int orderId) throws SQLException {
        double totalPaid = paymentDAO.getTotalPaidForOrder(orderId);
        LOGGER.log(Level.INFO, "Total paid for order {0}: {1}", new Object[]{orderId, totalPaid});
    }

    @Override
    public String calculatePaymentStatus(double totalPaid, double sellingPrice) {
        return PriceCalculator.determinePaymentStatus(sellingPrice, totalPaid);
    }

    private void validatePayment(Payment payment) {
        ValidationUtils.validateNotNull(payment, "Payment");
        ValidationUtils.validatePositiveId(payment.getOrderId(), "Order ID");
        ValidationUtils.validatePositive(payment.getAmount(), "Payment amount");
        ValidationUtils.validateNotEmpty(payment.getPaymentMethod(), "Payment method");
    }
}
