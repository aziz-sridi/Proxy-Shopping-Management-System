package dao;

import model.DeliveryOption;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryOptionDAO {

    public List<DeliveryOption> findAll() throws SQLException {
        List<DeliveryOption> options = new ArrayList<>();
        String sql = "SELECT * FROM delivery_options ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(mapDeliveryOption(rs));
            }
        }
        return options;
    }

    private DeliveryOption mapDeliveryOption(ResultSet rs) throws SQLException {
        DeliveryOption d = new DeliveryOption();
        d.setDeliveryOptionId(rs.getInt("delivery_option_id"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        d.setContactInfo(rs.getString("contact_info"));
        return d;
    }
}
