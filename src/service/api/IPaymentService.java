package service.api;

import model.Payment;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for Payment service operations.
 */
public interface IPaymentService {
    
    /**
     * Get all payments from the database.
     * @return list of all payments
     * @throws SQLException if database error occurs
     */
    List<Payment> getAllPayments() throws SQLException;
    
    /**
     * Get payments for a specific order.
     * @param orderId the order ID
     * @return list of payments for the order
     * @throws SQLException if database error occurs
     */
    List<Payment> getPaymentsByOrder(int orderId) throws SQLException;
    
    /**
     * Get payments for a specific client.
     * @param clientId the client ID
     * @return list of payments for the client
     * @throws SQLException if database error occurs
     */
    List<Payment> getPaymentsByClient(int clientId) throws SQLException;
    
    /**
     * Get total amount paid for an order.
     * @param orderId the order ID
     * @return total amount paid
     * @throws SQLException if database error occurs
     */
    double getTotalPaidForOrder(int orderId) throws SQLException;
    
    /**
     * Add a new payment with validation and automatic order status update.
     * @param payment the payment to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void addPayment(Payment payment) throws SQLException;
    
    /**
     * Add a payment for a deposit.
     * @param orderId the order ID
     * @param amount the deposit amount
     * @param comment optional comment
     * @throws SQLException if database error occurs
     */
    void addDepositPayment(int orderId, double amount, String comment) throws SQLException;
    
    /**
     * Add a full payment for an order.
     * @param orderId the order ID
     * @param amount the full payment amount
     * @param comment optional comment
     * @throws SQLException if database error occurs
     */
    void addFullPayment(int orderId, double amount, String comment) throws SQLException;
    
    /**
     * Update an existing payment with validation.
     * @param payment the payment to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void updatePayment(Payment payment) throws SQLException;
    
    /**
     * Delete a payment by ID and update the associated order's payment status.
     * @param paymentId the payment ID to delete
     * @param orderId the associated order ID for status update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if payment ID is invalid
     */
    void deletePayment(int paymentId, int orderId) throws SQLException;
    
    /**
     * Delete a payment by ID.
     * Note: This doesn't automatically update the order payment status.
     * @param paymentId the payment ID to delete
     * @throws SQLException if database error occurs
     */
    void deletePayment(int paymentId) throws SQLException;
    
    /**
     * Calculate payment status based on total paid vs selling price.
     * @param totalPaid the total amount paid
     * @param sellingPrice the order selling price
     * @return the payment status string
     */
    String calculatePaymentStatus(double totalPaid, double sellingPrice);
}
