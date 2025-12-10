package service.impl;

import service.api.IDeliveryOptionService;
import dao.DeliveryOptionDAO;
import model.DeliveryOption;
import service.ValidationUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for DeliveryOption-related business logic.
 * Handles validation, logging, and delegates CRUD operations to DeliveryOptionDAO.
 */
public class DeliveryOptionServiceImpl implements IDeliveryOptionService {

    private static final Logger LOGGER = Logger.getLogger(DeliveryOptionServiceImpl.class.getName());
    private final DeliveryOptionDAO deliveryOptionDAO;

    public DeliveryOptionServiceImpl(DeliveryOptionDAO deliveryOptionDAO) {
        this.deliveryOptionDAO = deliveryOptionDAO;
    }

    public DeliveryOptionServiceImpl() {
        this(new DeliveryOptionDAO());
    }

    @Override
    public List<DeliveryOption> getAllDeliveryOptions() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all delivery options");
        return deliveryOptionDAO.findAll();
    }

    @Override
    public DeliveryOption getDeliveryOptionById(int deliveryOptionId) throws SQLException {
        ValidationUtils.validatePositiveId(deliveryOptionId, "Delivery option ID");
        LOGGER.log(Level.INFO, "Fetching delivery option by ID: {0}", deliveryOptionId);
        List<DeliveryOption> all = deliveryOptionDAO.findAll();
        return all.stream()
                  .filter(d -> d.getDeliveryOptionId() == deliveryOptionId)
                  .findFirst()
                  .orElse(null);
    }
}
