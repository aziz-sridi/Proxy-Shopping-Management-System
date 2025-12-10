package service.impl;

import service.api.IProfitService;
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

    public ProfitServiceImpl(ProfitDAO profitDAO) {
        this.profitDAO = profitDAO;
    }

    public ProfitServiceImpl() {
        this(new ProfitDAO());
    }

    @Override
    public void saveProfit(Profit profit) throws SQLException {
        validateProfit(profit);
        LOGGER.log(Level.INFO, "Saving profit for order ID: {0}, amount: {1}", 
                  new Object[]{profit.getOrderId(), profit.getCalculatedProfit()});
        profitDAO.upsertProfit(profit);
        LOGGER.log(Level.INFO, "Profit saved successfully for order ID: {0}", profit.getOrderId());
    }

    @Override
    public Profit calculateProfit(int orderId, double originalRate, double customRate, 
                                   double originalPriceEUR, double sellingPriceTND, double shipmentCostShare) {
        Profit profit = new Profit();
        profit.setOrderId(orderId);
        profit.setOriginalRate(originalRate);
        profit.setCustomRate(customRate);
        profit.setShipmentCost(shipmentCostShare);
        
        double costInTND = originalPriceEUR * customRate;
        double calculatedProfit = sellingPriceTND - costInTND - shipmentCostShare;
        profit.setCalculatedProfit(calculatedProfit);
        
        LOGGER.log(Level.INFO, "Calculated profit for order {0}: {1}", new Object[]{orderId, calculatedProfit});
        return profit;
    }

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
