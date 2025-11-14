package dao;

import model.Profit;
import util.DBConnection;

import java.sql.*;

public class ProfitDAO {

    public void upsertProfit(Profit p) throws SQLException {
        // simple implementation: delete existing profit for order then insert new one
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM profits WHERE order_id = ?")) {
                del.setInt(1, p.getOrderId());
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO profits (order_id, original_rate, custom_rate, shipment_cost, calculated_profit) VALUES (?,?,?,?,?)")) {
                ins.setInt(1, p.getOrderId());
                ins.setDouble(2, p.getOriginalRate());
                ins.setDouble(3, p.getCustomRate());
                ins.setDouble(4, p.getShipmentCost());
                ins.setDouble(5, p.getCalculatedProfit());
                ins.executeUpdate();
            }
        }
    }
}
