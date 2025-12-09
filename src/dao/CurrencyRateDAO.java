package dao;

import model.CurrencyRate;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRateDAO {

    public List<CurrencyRate> findHistory(String base, String target) throws SQLException {
        List<CurrencyRate> rates = new ArrayList<>();
        String sql = "SELECT rate_id, base_currency, target_currency, original_rate, custom_rate, updated_at " +
                "FROM currency_rates WHERE base_currency = ? AND target_currency = ? " +
                "ORDER BY updated_at DESC, rate_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, base);
            ps.setString(2, target);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rates.add(mapCurrencyRate(rs));
                }
            }
        }
        return rates;
    }

    public void insert(CurrencyRate rate) throws SQLException {
        String sql = "INSERT INTO currency_rates (base_currency, target_currency, original_rate, custom_rate) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rate.getBaseCurrency());
            ps.setString(2, rate.getTargetCurrency());
            ps.setDouble(3, rate.getOriginalRate());
            ps.setDouble(4, rate.getCustomRate());
            ps.executeUpdate();
        }
    }

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
                    return mapCurrencyRate(rs);
                }
            }
        }
        return null;
    }

    private CurrencyRate mapCurrencyRate(ResultSet rs) throws SQLException {
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
