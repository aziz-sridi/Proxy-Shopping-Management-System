package service;

import dao.ProfitDAO;
import model.Profit;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Profit-related business logic.
 * Handles validation, logging, and delegates CRUD operations to ProfitDAO.
 */
public class ProfitServiceImpl implements IProfitService {

    private static final Logger LOGGER = Logger.getLogger(ProfitServiceImpl.class.getName());
    private final ProfitDAO profitDAO;

    /**
     * Constructor with dependency injection for ProfitDAO.
     * @param profitDAO the DAO to use for database operations
     */
    public ProfitServiceImpl(ProfitDAO profitDAO) {
        this.profitDAO = profitDAO;
    }

    /**
     * Default constructor using default ProfitDAO.
     */
    public ProfitServiceImpl() {
        this(new ProfitDAO());
    }

    /**
     * Save or update profit for an order.
     * @param profit the profit to save
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void saveProfit(Profit profit) throws SQLException {
        validateProfit(profit);
        LOGGER.log(Level.INFO, "Saving profit for order ID: {0}, amount: {1}", 
                  new Object[]{profit.getOrderId(), profit.getCalculatedProfit()});
        profitDAO.upsertProfit(profit);
        LOGGER.log(Level.INFO, "Profit saved successfully for order ID: {0}", profit.getOrderId());
    }

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
    @Override
    public Profit calculateProfit(int orderId, double originalRate, double customRate, 
                                   double originalPriceEUR, double sellingPriceTND, double shipmentCostShare) {
        Profit profit = new Profit();
        profit.setOrderId(orderId);
        profit.setOriginalRate(originalRate);
        profit.setCustomRate(customRate);
        profit.setShipmentCost(shipmentCostShare);
        
        // Calculate profit: selling price - (cost in TND + shipment share)
        double costInTND = originalPriceEUR * customRate;
        double calculatedProfit = sellingPriceTND - costInTND - shipmentCostShare;
        profit.setCalculatedProfit(calculatedProfit);
        
        LOGGER.log(Level.INFO, "Calculated profit for order {0}: {1}", new Object[]{orderId, calculatedProfit});
        return profit;
    }

    /**
     * Validate profit data before database operations.
     * @param profit the profit to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateProfit(Profit profit) {
        if (profit == null) {
            throw new IllegalArgumentException("Profit cannot be null");
        }
        if (profit.getOrderId() <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (profit.getOriginalRate() <= 0) {
            throw new IllegalArgumentException("Original rate must be positive");
        }
        if (profit.getCustomRate() <= 0) {
            throw new IllegalArgumentException("Custom rate must be positive");
        }
    }
}
