package service.api;

import model.Profit;

import java.sql.SQLException;

/**
 * Interface for Profit service operations.
 */
public interface IProfitService {
    
    /**
     * Save or update profit for an order.
     * @param profit the profit to save
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void saveProfit(Profit profit) throws SQLException;
    
    /**
     * Calculate profit for an order.
     * @param orderId the order ID
     * @param originalRate the original exchange rate
     * @param customRate the custom exchange rate
     * @param originalPriceEUR the original price in EUR
     * @param sellingPriceTND the selling price in TND
     * @param shipmentCostShare the order's share of shipment cost
     * @return the calculated profit
     */
    Profit calculateProfit(int orderId, double originalRate, double customRate, 
                          double originalPriceEUR, double sellingPriceTND, double shipmentCostShare);
}
