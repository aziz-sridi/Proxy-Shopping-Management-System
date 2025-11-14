package dao;

import model.CurrencyRate;
import util.DBConnection;

import java.sql.*;

public class CurrencyRateDAO {

    public CurrencyRate findLatest(String base, String target) throws SQLException {
        String sql = "SELECT rate_id, base_currency, target_currency, original_rate, custom_rate, updated_at " +
                "FROM currency_rates WHERE base_currency = ? AND target_currency = ? " +
                "ORDER BY updated_at DESC, rate_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, base);
            ps.setString(2, target);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CurrencyRate r = new CurrencyRate();
                    r.setRateId(rs.getInt("rate_id"));
                    r.setBaseCurrency(rs.getString("base_currency"));
                    r.setTargetCurrency(rs.getString("target_currency"));
                    r.setOriginalRate(rs.getDouble("original_rate"));
                    r.setCustomRate(rs.getDouble("custom_rate"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    if (ts != null) r.setUpdatedAt(ts.toLocalDateTime());
                    return r;
                }
            }
        }
        return null;
    }
}
