package service;

import model.CurrencyRate;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for CurrencyRate service operations.
 */
public interface ICurrencyRateService {
    
    /**
     * Get rate history for a currency pair.
     * @param baseCurrency the base currency code (e.g., "EUR")
     * @param targetCurrency the target currency code (e.g., "TND")
     * @return list of historical currency rates
     * @throws SQLException if database error occurs
     */
    List<CurrencyRate> getRateHistory(String baseCurrency, String targetCurrency) throws SQLException;
    
    /**
     * Get the latest rate for a currency pair.
     * @param baseCurrency the base currency code
     * @param targetCurrency the target currency code
     * @return the latest currency rate, or null if not found
     * @throws SQLException if database error occurs
     */
    CurrencyRate getLatestRate(String baseCurrency, String targetCurrency) throws SQLException;
    
    /**
     * Add a new currency rate with validation.
     * @param rate the currency rate to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void addRate(CurrencyRate rate) throws SQLException;
    
    /**
     * Convert amount using the latest rate.
     * @param amount the amount to convert
     * @param baseCurrency the source currency
     * @param targetCurrency the target currency
     * @return the converted amount, or the original amount if no rate found
     * @throws SQLException if database error occurs
     */
    double convert(double amount, String baseCurrency, String targetCurrency) throws SQLException;
}
