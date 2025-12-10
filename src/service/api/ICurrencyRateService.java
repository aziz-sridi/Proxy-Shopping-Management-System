package service.api;

import model.CurrencyRate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface ICurrencyRateService {
    
    List<CurrencyRate> getRateHistory(String baseCurrency, String targetCurrency) throws SQLException;
    
    CurrencyRate getLatestRate(String baseCurrency, String targetCurrency) throws SQLException;
    
    void addRate(CurrencyRate rate) throws SQLException;
    
    double convert(double amount, String baseCurrency, String targetCurrency) throws SQLException;

    CurrencyRate refreshLatestRateFromApi(String baseCurrency, String targetCurrency) throws SQLException, IOException;
}
