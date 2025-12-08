package service;

import dao.DeliveryOptionDAO;
import model.DeliveryOption;

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

    /**
     * Constructor with dependency injection for DeliveryOptionDAO.
     * @param deliveryOptionDAO the DAO to use for database operations
     */
    public DeliveryOptionServiceImpl(DeliveryOptionDAO deliveryOptionDAO) {
        this.deliveryOptionDAO = deliveryOptionDAO;
    }

    /**
     * Default constructor using default DeliveryOptionDAO.
     */
    public DeliveryOptionServiceImpl() {
        this(new DeliveryOptionDAO());
    }

    /**
     * Get all delivery options from the database.
     * @return list of all delivery options
     * @throws SQLException if database error occurs
     */
    @Override
    public List<DeliveryOption> getAllDeliveryOptions() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all delivery options");
        return deliveryOptionDAO.findAll();
    }

    /**
     * Get delivery option by ID.
     * @param deliveryOptionId the delivery option ID
     * @return the delivery option, or null if not found
     * @throws SQLException if database error occurs
     */
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
