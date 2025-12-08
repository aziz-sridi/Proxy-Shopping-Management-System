package service;

import model.Client;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for Client service operations.
 */
public interface IClientService {
    
    /**
     * Get all clients from the database.
     * @return list of all clients
     * @throws SQLException if database error occurs
     */
    List<Client> getAllClients() throws SQLException;
    
    /**
     * Search clients by username or phone.
     * @param keyword the search keyword
     * @return list of matching clients
     * @throws SQLException if database error occurs
     */
    List<Client> searchClients(String keyword) throws SQLException;
    
    /**
     * Add a new client with validation.
     * @param client the client to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void addClient(Client client) throws SQLException;
    
    /**
     * Update an existing client with validation.
     * @param client the client to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    void updateClient(Client client) throws SQLException;
    
    /**
     * Delete a client by ID.
     * @param clientId the client ID to delete
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if client ID is invalid
     */
    void deleteClient(int clientId) throws SQLException;
}
