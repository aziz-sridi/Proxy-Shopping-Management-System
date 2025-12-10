package service.impl;

import service.api.ICurrencyRateService;
import dao.CurrencyRateDAO;
import model.CurrencyRate;
import util.SettingsManager;
import util.CurrencyRateFetcher;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CurrencyRateServiceImpl implements ICurrencyRateService {

    private static final Logger LOGGER = Logger.getLogger(CurrencyRateServiceImpl.class.getName());
    private final CurrencyRateDAO currencyRateDAO;
    private final CurrencyRateFetcher currencyRateFetcher;

    public CurrencyRateServiceImpl(CurrencyRateDAO currencyRateDAO) {
        this(currencyRateDAO, new CurrencyRateFetcher(Duration.ofSeconds(10)));
    }

    public CurrencyRateServiceImpl(CurrencyRateDAO currencyRateDAO, CurrencyRateFetcher currencyRateFetcher) {
        this.currencyRateDAO = currencyRateDAO;
        this.currencyRateFetcher = currencyRateFetcher;
    }

    public CurrencyRateServiceImpl() {
        this(new CurrencyRateDAO());
    }

    @Override
    public List<CurrencyRate> getRateHistory(String baseCurrency, String targetCurrency) throws SQLException {
        validateCurrencyPair(baseCurrency, targetCurrency);
        LOGGER.log(Level.INFO, "Fetching rate history for {0}/{1}", new Object[]{baseCurrency, targetCurrency});
        return currencyRateDAO.findHistory(baseCurrency, targetCurrency);
    }

    @Override
    public CurrencyRate getLatestRate(String baseCurrency, String targetCurrency) throws SQLException {
        validateCurrencyPair(baseCurrency, targetCurrency);
        LOGGER.log(Level.INFO, "Fetching latest rate for {0}/{1}", new Object[]{baseCurrency, targetCurrency});
        return currencyRateDAO.findLatest(baseCurrency, targetCurrency);
    }

    @Override
    public void addRate(CurrencyRate rate) throws SQLException {
        validateCurrencyRate(rate);
        LOGGER.log(Level.INFO, "Adding new currency rate: {0}/{1} = {2}", 
                  new Object[]{rate.getBaseCurrency(), rate.getTargetCurrency(), rate.getCustomRate()});
        currencyRateDAO.insert(rate);
        LOGGER.log(Level.INFO, "Currency rate added successfully");
    }

    @Override
    public double convert(double amount, String baseCurrency, String targetCurrency) throws SQLException {
        CurrencyRate rate = getLatestRate(baseCurrency, targetCurrency);
        if (rate == null) {
            try {
                LOGGER.log(Level.INFO, "No rate cached for {0}/{1}, fetching from API", new Object[]{baseCurrency, targetCurrency});
                rate = refreshLatestRateFromApi(baseCurrency, targetCurrency);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to fetch rate from API, returning original amount: " + e.getMessage());
            }
        }
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

    @Override
    public CurrencyRate refreshLatestRateFromApi(String baseCurrency, String targetCurrency) throws SQLException, IOException {
        validateCurrencyPair(baseCurrency, targetCurrency);
        double latestRate = currencyRateFetcher.fetchLatestRate(baseCurrency, targetCurrency);

        CurrencyRate rate = new CurrencyRate();
        rate.setBaseCurrency(baseCurrency);
        rate.setTargetCurrency(targetCurrency);
        rate.setOriginalRate(latestRate);
        rate.setCustomRate(latestRate); // allow overriding later via UI if needed

        LOGGER.log(Level.INFO, "Persisting fetched currency rate {0}/{1} = {2}",
                new Object[]{baseCurrency, targetCurrency, latestRate});
        currencyRateDAO.insert(rate);

        // Keep settings conversion rate in sync for parts of the app that rely on it
        SettingsManager.updateConversionRate(latestRate);
        return getLatestRate(baseCurrency, targetCurrency);
    }

    private void validateCurrencyPair(String baseCurrency, String targetCurrency) {
        if (baseCurrency == null || baseCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Base currency is required");
        }
        if (targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Target currency is required");
        }
    }

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
