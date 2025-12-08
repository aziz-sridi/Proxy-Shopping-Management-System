package service;

import model.DeliveryOption;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for DeliveryOption service operations.
 */
public interface IDeliveryOptionService {
    
    /**
     * Get all delivery options from the database.
     * @return list of all delivery options
     * @throws SQLException if database error occurs
     */
    List<DeliveryOption> getAllDeliveryOptions() throws SQLException;
    
    /**
     * Get delivery option by ID.
     * @param deliveryOptionId the delivery option ID
     * @return the delivery option, or null if not found
     * @throws SQLException if database error occurs
     */
    DeliveryOption getDeliveryOptionById(int deliveryOptionId) throws SQLException;
}
