package dao;

import model.Client;

import java.sql.*;
import java.util.List;

public class ClientDAO extends BaseDAO<Client> {

    public void insert(Client client) throws SQLException {
        String sql = "INSERT INTO clients (username, phone, source, address) VALUES (?,?,?,?)";
        executeUpdate(sql, ps -> {
            ps.setString(1, client.getUsername());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getSource());
            ps.setString(4, client.getAddress());
        });
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE clients SET username = ?, phone = ?, source = ?, address = ? WHERE client_id = ?";
        executeUpdate(sql, ps -> {
            ps.setString(1, client.getUsername());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getSource());
            ps.setString(4, client.getAddress());
            ps.setInt(5, client.getClientId());
        });
    }

    public void delete(int clientId) throws SQLException {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        executeUpdate(sql, ps -> ps.setInt(1, clientId));
    }

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT client_id, username, phone, source, address, created_at FROM clients ORDER BY client_id DESC";
        return executeQuery(sql, null, this::mapClient);
    }

    public List<Client> findByUsernameOrPhone(String keyword) throws SQLException {
        String sql = "SELECT client_id, username, phone, source, address, created_at FROM clients " +
                     "WHERE username LIKE ? OR phone LIKE ? ORDER BY client_id DESC";
        return executeQuery(sql, ps -> {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
        }, this::mapClient);
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setClientId(rs.getInt("client_id"));
        c.setUsername(rs.getString("username"));
        c.setPhone(rs.getString("phone"));
        c.setSource(rs.getString("source"));
        c.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
