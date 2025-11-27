package dao;

import model.Shipment;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO {

    public void insert(Shipment s) throws SQLException {
        String sql = "INSERT INTO shipments (batch_name, departure_country, arrival_country, shipment_cost, departure_date, arrival_date, status, transportation_cost, other_costs) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getBatchName());
            ps.setString(2, s.getDepartureCountry());
            ps.setString(3, s.getArrivalCountry());
            ps.setDouble(4, s.getShipmentCost());
            if (s.getDepartureDate() != null) ps.setDate(5, Date.valueOf(s.getDepartureDate())); else ps.setNull(5, Types.DATE);
            if (s.getArrivalDate() != null) ps.setDate(6, Date.valueOf(s.getArrivalDate())); else ps.setNull(6, Types.DATE);
            ps.setString(7, s.getStatus());
            ps.setDouble(8, s.getTransportationCost());
            ps.setDouble(9, s.getOtherCosts());
            ps.executeUpdate();
        }
    }

    public List<Shipment> findAll() throws SQLException {
        List<Shipment> list = new ArrayList<>();
        String sql = "SELECT * FROM shipments ORDER BY shipment_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Shipment s = new Shipment();
                s.setShipmentId(rs.getInt("shipment_id"));
                s.setBatchName(rs.getString("batch_name"));
                s.setDepartureCountry(rs.getString("departure_country"));
                s.setArrivalCountry(rs.getString("arrival_country"));
                s.setShipmentCost(rs.getDouble("shipment_cost"));
                Date dep = rs.getDate("departure_date");
                if (dep != null) s.setDepartureDate(dep.toLocalDate());
                Date arr = rs.getDate("arrival_date");
                if (arr != null) s.setArrivalDate(arr.toLocalDate());
                s.setStatus(rs.getString("status"));
                s.setTransportationCost(rs.getDouble("transportation_cost"));
                s.setOtherCosts(rs.getDouble("other_costs"));
                list.add(s);
            }
        }
        return list;
    }

    public void update(Shipment s) throws SQLException {
        String sql = "UPDATE shipments SET batch_name=?, departure_country=?, arrival_country=?, shipment_cost=?, departure_date=?, arrival_date=?, status=?, transportation_cost=?, other_costs=? WHERE shipment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getBatchName());
            ps.setString(2, s.getDepartureCountry());
            ps.setString(3, s.getArrivalCountry());
            ps.setDouble(4, s.getShipmentCost());
            if (s.getDepartureDate() != null) ps.setDate(5, Date.valueOf(s.getDepartureDate())); else ps.setNull(5, Types.DATE);
            if (s.getArrivalDate() != null) ps.setDate(6, Date.valueOf(s.getArrivalDate())); else ps.setNull(6, Types.DATE);
            ps.setString(7, s.getStatus());
            ps.setDouble(8, s.getTransportationCost());
            ps.setDouble(9, s.getOtherCosts());
            ps.setInt(10, s.getShipmentId());
            ps.executeUpdate();
        }
    }

    public void delete(int shipmentId) throws SQLException {
        String sql = "DELETE FROM shipments WHERE shipment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shipmentId);
            ps.executeUpdate();
        }
    }
}
