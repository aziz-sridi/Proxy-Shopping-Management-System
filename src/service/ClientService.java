package service;

import dao.ClientDAO;
import model.Client;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Client-related business logic.
 * Handles validation, logging, and delegates CRUD operations to ClientDAO.
 */
public class ClientService {

    private static final Logger LOGGER = Logger.getLogger(ClientService.class.getName());
    private final ClientDAO clientDAO;

    /**
     * Constructor with dependency injection for ClientDAO.
     * @param clientDAO the DAO to use for database operations
     */
    public ClientService(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    /**
     * Default constructor using default ClientDAO.
     */
    public ClientService() {
        this(new ClientDAO());
    }

    /**
     * Get all clients from the database.
     * @return list of all clients
     * @throws SQLException if database error occurs
     */
    public List<Client> getAllClients() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all clients");
        return clientDAO.findAll();
    }

    /**
     * Search clients by username or phone.
     * @param keyword the search keyword
     * @return list of matching clients
     * @throws SQLException if database error occurs
     */
    public List<Client> searchClients(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            LOGGER.log(Level.INFO, "Empty search keyword, returning all clients");
            return getAllClients();
        }
        LOGGER.log(Level.INFO, "Searching clients with keyword: {0}", keyword);
        return clientDAO.findByUsernameOrPhone(keyword.trim());
    }

    /**
     * Add a new client with validation.
     * @param client the client to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public void addClient(Client client) throws SQLException {
        validateClient(client);
        LOGGER.log(Level.INFO, "Adding new client: {0}", client.getUsername());
        clientDAO.insert(client);
        LOGGER.log(Level.INFO, "Client added successfully: {0}", client.getUsername());
    }

    /**
     * Update an existing client with validation.
     * @param client the client to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    public void updateClient(Client client) throws SQLException {
        validateClient(client);
        if (client.getClientId() <= 0) {
            throw new IllegalArgumentException("Client ID must be positive for update operation");
        }
        LOGGER.log(Level.INFO, "Updating client ID: {0}", client.getClientId());
        clientDAO.update(client);
        LOGGER.log(Level.INFO, "Client updated successfully: {0}", client.getUsername());
    }

    /**
     * Delete a client by ID.
     * @param clientId the client ID to delete
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if client ID is invalid
     */
    public void deleteClient(int clientId) throws SQLException {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Client ID must be positive");
        }
        LOGGER.log(Level.INFO, "Deleting client ID: {0}", clientId);
        clientDAO.delete(clientId);
        LOGGER.log(Level.INFO, "Client deleted successfully: {0}", clientId);
    }

    /**
     * Validate client data before database operations.
     * @param client the client to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (client.getUsername() == null || client.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (client.getUsername().length() > 100) {
            throw new IllegalArgumentException("Username cannot exceed 100 characters");
        }
        // Phone validation - optional but if provided should be valid
        if (client.getPhone() != null && client.getPhone().length() != 8) {
            throw new IllegalArgumentException("Phone number must be exactly 8 characters");
        }
    }
}
