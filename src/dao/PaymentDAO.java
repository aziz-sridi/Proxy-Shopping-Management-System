package dao;

import model.Payment;

import java.sql.*;
import java.util.List;

public class PaymentDAO extends BaseDAO<Payment> {

    public void insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, comment) VALUES (?,?,?,?)";
        executeUpdate(sql, ps -> {
            ps.setInt(1, p.getOrderId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentMethod());
            ps.setString(4, p.getComment());
        });
    }

    public void update(Payment p) throws SQLException {
        String sql = "UPDATE payments SET amount = ?, payment_method = ?, comment = ? WHERE payment_id = ?";
        executeUpdate(sql, ps -> {
            ps.setDouble(1, p.getAmount());
            ps.setString(2, p.getPaymentMethod());
            ps.setString(3, p.getComment());
            ps.setInt(4, p.getPaymentId());
        });
    }

    public void delete(int paymentId) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        executeUpdate(sql, ps -> ps.setInt(1, paymentId));
    }

    public double getTotalPaidForOrder(int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE order_id = ?";
        return executeScalarQuery(sql, ps -> ps.setInt(1, orderId), 0.0);
    }

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        return executeQuery(sql, null, this::mapPayment);
    }

    public List<Payment> findByOrder(int orderId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_date";
        return executeQuery(sql, ps -> ps.setInt(1, orderId), this::mapPayment);
    }

    public List<Payment> findByClient(int clientId) throws SQLException {
        String sql = "SELECT p.* FROM payments p INNER JOIN orders o ON p.order_id = o.order_id WHERE o.client_id = ? ORDER BY p.payment_date DESC";
        return executeQuery(sql, ps -> ps.setInt(1, clientId), this::mapPayment);
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setAmount(rs.getDouble("amount"));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setComment(rs.getString("comment"));
        return p;
    }
}
