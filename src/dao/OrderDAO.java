package dao;

import model.Order;
import model.Platform;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public void insert(Order o) throws SQLException {
        String sql = "INSERT INTO orders (client_id, shipment_id, delivery_option_id, product_link, product_size, quantity, original_price, selling_price, platform, payment_type, payment_status, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getClientId());
            if (o.getShipmentId() != null) ps.setInt(2, o.getShipmentId()); else ps.setNull(2, Types.INTEGER);
            if (o.getDeliveryOptionId() != null) ps.setInt(3, o.getDeliveryOptionId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, o.getProductLink());
            ps.setString(5, o.getProductSize());
            ps.setInt(6, o.getQuantity());
            ps.setDouble(7, o.getOriginalPrice());
            ps.setDouble(8, o.getSellingPrice());
            ps.setString(9, o.getPlatform() != null ? o.getPlatform().getDisplayName() : "Other");
            ps.setString(10, o.getPaymentType());
            ps.setString(11, o.getPaymentStatus());
            ps.setString(12, o.getNotes());
            ps.executeUpdate();
        }
    }

    public int insertAndReturnId(Order o) throws SQLException {
        String sql = "INSERT INTO orders (client_id, shipment_id, delivery_option_id, product_link, product_size, quantity, original_price, selling_price, platform, payment_type, payment_status, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getClientId());
            if (o.getShipmentId() != null) ps.setInt(2, o.getShipmentId()); else ps.setNull(2, Types.INTEGER);
            if (o.getDeliveryOptionId() != null) ps.setInt(3, o.getDeliveryOptionId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, o.getProductLink());
            ps.setString(5, o.getProductSize());
            ps.setInt(6, o.getQuantity());
            ps.setDouble(7, o.getOriginalPrice());
            ps.setDouble(8, o.getSellingPrice());
            ps.setString(9, o.getPlatform() != null ? o.getPlatform().getDisplayName() : "Other");
            ps.setString(10, o.getPaymentType());
            ps.setString(11, o.getPaymentStatus());
            ps.setString(12, o.getNotes());
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained.");
                }
            }
        }
    }

    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }

    public List<Order> findByShipment(int shipmentId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE shipment_id = ? ORDER BY order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        }
        return orders;
    }

    public List<Order> findByClient(int clientId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE client_id = ? ORDER BY order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        }
        return orders;
    }

    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void updatePaymentStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET payment_status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public List<Order> findByPlatform(Platform platform) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE platform = ? ORDER BY order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, platform.getDisplayName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        }
        return orders;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setClientId(rs.getInt("client_id"));
        int sh = rs.getInt("shipment_id");
        o.setShipmentId(rs.wasNull() ? null : sh);
        int del = rs.getInt("delivery_option_id");
        o.setDeliveryOptionId(rs.wasNull() ? null : del);
        o.setProductLink(rs.getString("product_link"));
        o.setProductSize(rs.getString("product_size"));
        o.setQuantity(rs.getInt("quantity"));
        o.setOriginalPrice(rs.getDouble("original_price"));
        o.setSellingPrice(rs.getDouble("selling_price"));
        o.setPlatform(Platform.fromString(rs.getString("platform")));
        o.setPaymentType(rs.getString("payment_type"));
        o.setPaymentStatus(rs.getString("payment_status"));
        Timestamp ts = rs.getTimestamp("order_date");
        if (ts != null) o.setOrderDate(ts.toLocalDateTime());
        o.setNotes(rs.getString("notes"));
        return o;
    }

    public List<Order> getOrdersByShipmentId(int shipmentId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE shipment_id = ? ORDER BY order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        }
        return orders;
    }

    public void delete(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    public void update(Order o) throws SQLException {
        String sql = "UPDATE orders SET client_id = ?, shipment_id = ?, delivery_option_id = ?, product_link = ?, product_size = ?, quantity = ?, original_price = ?, selling_price = ?, platform = ?, payment_type = ?, payment_status = ?, notes = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getClientId());
            if (o.getShipmentId() != null) ps.setInt(2, o.getShipmentId()); else ps.setNull(2, Types.INTEGER);
            if (o.getDeliveryOptionId() != null) ps.setInt(3, o.getDeliveryOptionId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, o.getProductLink());
            ps.setString(5, o.getProductSize());
            ps.setInt(6, o.getQuantity());
            ps.setDouble(7, o.getOriginalPrice());
            ps.setDouble(8, o.getSellingPrice());
            ps.setString(9, o.getPlatform() != null ? o.getPlatform().getDisplayName() : "Other");
            ps.setString(10, o.getPaymentType());
            ps.setString(11, o.getPaymentStatus());
            ps.setString(12, o.getNotes());
            ps.setInt(13, o.getOrderId());
            ps.executeUpdate();
        }
    }
}
