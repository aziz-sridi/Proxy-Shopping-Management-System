package dao;

import model.DeliveryOption;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryOptionDAO {

    public List<DeliveryOption> findAll() throws SQLException {
        List<DeliveryOption> list = new ArrayList<>();
        String sql = "SELECT * FROM delivery_options ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DeliveryOption d = new DeliveryOption();
                d.setDeliveryOptionId(rs.getInt("delivery_option_id"));
                d.setName(rs.getString("name"));
                d.setDescription(rs.getString("description"));
                d.setContactInfo(rs.getString("contact_info"));
                list.add(d);
            }
        }
        return list;
    }
}
