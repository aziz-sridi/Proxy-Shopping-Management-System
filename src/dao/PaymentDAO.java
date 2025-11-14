package dao;

import model.Payment;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public void insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, comment) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentMethod());
            ps.setString(4, p.getComment());
            ps.executeUpdate();
        }
    }

    public double getTotalPaidForOrder(int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    public List<Payment> findByOrder(int orderId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setOrderId(rs.getInt("order_id"));
                    p.setAmount(rs.getDouble("amount"));
                    Timestamp ts = rs.getTimestamp("payment_date");
                    if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setComment(rs.getString("comment"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}
