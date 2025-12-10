package service.impl;

import service.api.IClientService;
import dao.ClientDAO;
import model.Client;
import service.ValidationUtils;

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

    public ClientServiceImpl(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    public ClientServiceImpl() {
        this(new ClientDAO());
    }

    @Override
    public List<Client> getAllClients() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all clients");
        return clientDAO.findAll();
    }

    @Override
    public List<Client> searchClients(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            LOGGER.log(Level.INFO, "Empty search keyword, returning all clients");
            return getAllClients();
        }
        LOGGER.log(Level.INFO, "Searching clients with keyword: {0}", keyword);
        return clientDAO.findByUsernameOrPhone(keyword.trim());
    }

    @Override
    public void addClient(Client client) throws SQLException {
        validateClient(client);
        LOGGER.log(Level.INFO, "Adding new client: {0}", client.getUsername());
        clientDAO.insert(client);
        LOGGER.log(Level.INFO, "Client added successfully: {0}", client.getUsername());
    }

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

    @Override
    public void deleteClient(int clientId) throws SQLException {
        ValidationUtils.validatePositiveId(clientId, "Client ID");
        LOGGER.log(Level.INFO, "Deleting client ID: {0}", clientId);
        clientDAO.delete(clientId);
        LOGGER.log(Level.INFO, "Client deleted successfully: {0}", clientId);
    }

    private void validateClient(Client client) {
        ValidationUtils.validateNotNull(client, "Client");
        ValidationUtils.validateNotEmpty(client.getUsername(), "Username");
        ValidationUtils.validateMaxLength(client.getUsername(), 100, "Username");
        ValidationUtils.validatePhoneNumber(client.getPhone(), "Phone number");
    }
}
