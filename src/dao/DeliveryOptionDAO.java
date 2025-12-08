package dao;

import model.DeliveryOption;

import java.sql.*;
import java.util.List;

public class DeliveryOptionDAO extends BaseDAO<DeliveryOption> {

    public List<DeliveryOption> findAll() throws SQLException {
        String sql = "SELECT * FROM delivery_options ORDER BY name";
        return executeQuery(sql, null, this::mapDeliveryOption);
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
