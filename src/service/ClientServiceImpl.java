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
public class ClientServiceImpl implements IClientService {

    private static final Logger LOGGER = Logger.getLogger(ClientServiceImpl.class.getName());
    private final ClientDAO clientDAO;

    /**
     * Constructor with dependency injection for ClientDAO.
     * @param clientDAO the DAO to use for database operations
     */
    public ClientServiceImpl(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    /**
     * Default constructor using default ClientDAO.
     */
    public ClientServiceImpl() {
        this(new ClientDAO());
    }

    /**
     * Get all clients from the database.
     * @return list of all clients
     * @throws SQLException if database error occurs
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void deleteClient(int clientId) throws SQLException {
        ValidationUtils.validatePositiveId(clientId, "Client ID");
        LOGGER.log(Level.INFO, "Deleting client ID: {0}", clientId);
        clientDAO.delete(clientId);
        LOGGER.log(Level.INFO, "Client deleted successfully: {0}", clientId);
    }

    /**
     * Validate client data before database operations.
     * Uses ValidationUtils to avoid code duplication.
     * @param client the client to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateClient(Client client) {
        ValidationUtils.validateNotNull(client, "Client");
        ValidationUtils.validateNotEmpty(client.getUsername(), "Username");
        ValidationUtils.validateMaxLength(client.getUsername(), 100, "Username");
        ValidationUtils.validatePhoneNumber(client.getPhone(), "Phone number");
    }
}
