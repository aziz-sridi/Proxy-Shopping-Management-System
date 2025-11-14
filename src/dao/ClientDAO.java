package dao;

import model.Client;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void insert(Client client) throws SQLException {
        String sql = "INSERT INTO clients (username, phone, source, address) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getUsername());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getSource());
            ps.setString(4, client.getAddress());
            ps.executeUpdate();
        }
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE clients SET username = ?, phone = ?, source = ?, address = ? WHERE client_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getUsername());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getSource());
            ps.setString(4, client.getAddress());
            ps.setInt(5, client.getClientId());
            ps.executeUpdate();
        }
    }

    public void delete(int clientId) throws SQLException {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ps.executeUpdate();
        }
    }

    public List<Client> findAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT client_id, username, phone, source, address, created_at FROM clients ORDER BY client_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Client c = new Client();
                c.setClientId(rs.getInt("client_id"));
                c.setUsername(rs.getString("username"));
                c.setPhone(rs.getString("phone"));
                c.setSource(rs.getString("source"));
                c.setAddress(rs.getString("address"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                list.add(c);
            }
        }
        return list;
    }

    public List<Client> findByUsernameOrPhone(String keyword) throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT client_id, username, phone, source, address, created_at FROM clients " +
                     "WHERE username LIKE ? OR phone LIKE ? ORDER BY client_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Client c = new Client();
                    c.setClientId(rs.getInt("client_id"));
                    c.setUsername(rs.getString("username"));
                    c.setPhone(rs.getString("phone"));
                    c.setSource(rs.getString("source"));
                    c.setAddress(rs.getString("address"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                    list.add(c);
                }
            }
        }
        return list;
    }
}
