package service;

import dao.CurrencyRateDAO;
import model.CurrencyRate;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for CurrencyRate-related business logic.
 * Handles validation, logging, and delegates CRUD operations to CurrencyRateDAO.
 */
public class CurrencyRateServiceImpl implements ICurrencyRateService {

    private static final Logger LOGGER = Logger.getLogger(CurrencyRateServiceImpl.class.getName());
    private final CurrencyRateDAO currencyRateDAO;

    /**
     * Constructor with dependency injection for CurrencyRateDAO.
     * @param currencyRateDAO the DAO to use for database operations
     */
    public CurrencyRateServiceImpl(CurrencyRateDAO currencyRateDAO) {
        this.currencyRateDAO = currencyRateDAO;
    }

    /**
     * Default constructor using default CurrencyRateDAO.
     */
    public CurrencyRateServiceImpl() {
        this(new CurrencyRateDAO());
    }

    /**
     * Get rate history for a currency pair.
     * @param baseCurrency the base currency code (e.g., "EUR")
     * @param targetCurrency the target currency code (e.g., "TND")
     * @return list of historical currency rates
     * @throws SQLException if database error occurs
     */
    @Override
    public List<CurrencyRate> getRateHistory(String baseCurrency, String targetCurrency) throws SQLException {
        validateCurrencyPair(baseCurrency, targetCurrency);
        LOGGER.log(Level.INFO, "Fetching rate history for {0}/{1}", new Object[]{baseCurrency, targetCurrency});
        return currencyRateDAO.findHistory(baseCurrency, targetCurrency);
    }

    /**
     * Get the latest rate for a currency pair.
     * @param baseCurrency the base currency code
     * @param targetCurrency the target currency code
     * @return the latest currency rate, or null if not found
     * @throws SQLException if database error occurs
     */
    @Override
    public CurrencyRate getLatestRate(String baseCurrency, String targetCurrency) throws SQLException {
        validateCurrencyPair(baseCurrency, targetCurrency);
        LOGGER.log(Level.INFO, "Fetching latest rate for {0}/{1}", new Object[]{baseCurrency, targetCurrency});
        return currencyRateDAO.findLatest(baseCurrency, targetCurrency);
    }

    /**
     * Add a new currency rate with validation.
     * @param rate the currency rate to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void addRate(CurrencyRate rate) throws SQLException {
        validateCurrencyRate(rate);
        LOGGER.log(Level.INFO, "Adding new currency rate: {0}/{1} = {2}", 
                  new Object[]{rate.getBaseCurrency(), rate.getTargetCurrency(), rate.getCustomRate()});
        currencyRateDAO.insert(rate);
        LOGGER.log(Level.INFO, "Currency rate added successfully");
    }

    /**
     * Convert amount using the latest rate.
     * @param amount the amount to convert
     * @param baseCurrency the source currency
     * @param targetCurrency the target currency
     * @return the converted amount, or the original amount if no rate found
     * @throws SQLException if database error occurs
     */
    @Override
    public double convert(double amount, String baseCurrency, String targetCurrency) throws SQLException {
        CurrencyRate rate = getLatestRate(baseCurrency, targetCurrency);
        if (rate == null) {
            LOGGER.log(Level.WARNING, "No rate found for {0}/{1}, returning original amount", 
                      new Object[]{baseCurrency, targetCurrency});
            return amount;
        }
        double convertedAmount = amount * rate.getCustomRate();
        LOGGER.log(Level.INFO, "Converted {0} {1} to {2} {3}", 
                  new Object[]{amount, baseCurrency, convertedAmount, targetCurrency});
        return convertedAmount;
    }

    /**
     * Validate currency pair parameters.
     * @param baseCurrency the base currency code
     * @param targetCurrency the target currency code
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCurrencyPair(String baseCurrency, String targetCurrency) {
        if (baseCurrency == null || baseCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Base currency is required");
        }
        if (targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Target currency is required");
        }
    }

    /**
     * Validate currency rate data before database operations.
     * @param rate the currency rate to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCurrencyRate(CurrencyRate rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Currency rate cannot be null");
        }
        validateCurrencyPair(rate.getBaseCurrency(), rate.getTargetCurrency());
        if (rate.getOriginalRate() <= 0) {
            throw new IllegalArgumentException("Original rate must be positive");
        }
        if (rate.getCustomRate() <= 0) {
            throw new IllegalArgumentException("Custom rate must be positive");
        }
    }
}
