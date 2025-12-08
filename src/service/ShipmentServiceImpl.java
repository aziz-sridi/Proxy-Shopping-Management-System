package service;

import dao.ShipmentDAO;
import dao.OrderDAO;
import model.Shipment;
import model.Order;
import model.Settings;
import util.SettingsManager;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for Shipment-related business logic.
 * Handles validation, financial calculations, logging, and delegates CRUD operations to ShipmentDAO.
 */
public class ShipmentServiceImpl implements IShipmentService {

    private static final Logger LOGGER = Logger.getLogger(ShipmentServiceImpl.class.getName());
    private final ShipmentDAO shipmentDAO;
    private final OrderDAO orderDAO;

    /**
     * Constructor with dependency injection for DAOs.
     * @param shipmentDAO the DAO to use for shipment database operations
     * @param orderDAO the DAO to use for order database operations
     */
    public ShipmentServiceImpl(ShipmentDAO shipmentDAO, OrderDAO orderDAO) {
        this.shipmentDAO = shipmentDAO;
        this.orderDAO = orderDAO;
    }

    /**
     * Default constructor using default DAOs.
     */
    public ShipmentServiceImpl() {
        this(new ShipmentDAO(), new OrderDAO());
    }

    /**
     * Get all shipments from the database.
     * @return list of all shipments
     * @throws SQLException if database error occurs
     */
    @Override
    public List<Shipment> getAllShipments() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all shipments");
        return shipmentDAO.findAll();
    }

    /**
     * Add a new shipment with validation.
     * @param shipment the shipment to add
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void addShipment(Shipment shipment) throws SQLException {
        validateShipment(shipment);
        LOGGER.log(Level.INFO, "Adding new shipment: {0}", shipment.getBatchName());
        shipmentDAO.insert(shipment);
        LOGGER.log(Level.INFO, "Shipment added successfully: {0}", shipment.getBatchName());
    }

    /**
     * Update an existing shipment with validation.
     * @param shipment the shipment to update
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void updateShipment(Shipment shipment) throws SQLException {
        validateShipment(shipment);
        if (shipment.getShipmentId() <= 0) {
            throw new IllegalArgumentException("Shipment ID must be positive for update operation");
        }
        LOGGER.log(Level.INFO, "Updating shipment ID: {0}", shipment.getShipmentId());
        shipmentDAO.update(shipment);
        LOGGER.log(Level.INFO, "Shipment updated successfully: {0}", shipment.getBatchName());
    }

    /**
     * Delete a shipment by ID.
     * @param shipmentId the shipment ID to delete
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if shipment ID is invalid
     */
    @Override
    public void deleteShipment(int shipmentId) throws SQLException {
        ValidationUtils.validatePositiveId(shipmentId, "Shipment ID");
        LOGGER.log(Level.INFO, "Deleting shipment ID: {0}", shipmentId);
        shipmentDAO.delete(shipmentId);
        LOGGER.log(Level.INFO, "Shipment deleted successfully: {0}", shipmentId);
    }

    /**
     * Get orders for a specific shipment.
     * @param shipmentId the shipment ID
     * @return list of orders in the shipment
     * @throws SQLException if database error occurs
     */
    @Override
    public List<Order> getOrdersForShipment(int shipmentId) throws SQLException {
        return orderDAO.getOrdersByShipmentId(shipmentId);
    }

    /**
     * Calculate total number of orders in a shipment.
     * @param shipment the shipment
     * @return total number of orders
     * @throws SQLException if database error occurs
     */
    @Override
    public int calculateTotalOrders(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        return orders.size();
    }

    /**
     * Calculate total cost of goods for all orders in a shipment.
     * Formula: orderCost = unitPriceEUR * conversionRate * quantity
     * @param shipment the shipment
     * @return total cost of goods
     * @throws SQLException if database error occurs
     */
    @Override
    public double calculateTotalCostOfGoods(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        Settings settings = SettingsManager.getCurrentSettings();
        double conversionRate = settings.getConversionRate();
        
        double totalCost = 0.0;
        for (Order order : orders) {
            double orderCost = order.getOriginalPrice() * conversionRate * order.getQuantity();
            totalCost += orderCost;
        }
        return totalCost;
    }

    /**
     * Calculate total revenue for all orders in a shipment.
     * Uses PriceCalculator to avoid duplication.
     * @param shipment the shipment
     * @return total revenue
     * @throws SQLException if database error occurs
     */
    @Override
    public double calculateTotalRevenue(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        
        double totalRevenue = 0.0;
        for (Order order : orders) {
            double orderRevenue = ui.util.PriceCalculator.calculateTotalSellingPrice(
                order.getOriginalPrice(), order.getQuantity());
            totalRevenue += orderRevenue;
        }
        return totalRevenue;
    }

    /**
     * Calculate total expenses for a shipment.
     * Formula: totalExpenses = totalCostOfGoods + transportationCost + otherCosts
     * @param shipment the shipment
     * @return total expenses
     * @throws SQLException if database error occurs
     */
    @Override
    public double calculateTotalExpenses(Shipment shipment) throws SQLException {
        double totalCostOfGoods = calculateTotalCostOfGoods(shipment);
        return totalCostOfGoods + shipment.getTransportationCost() + shipment.getOtherCosts();
    }

    /**
     * Calculate net profit for a shipment.
     * Formula: netProfit = totalRevenue - totalExpenses
     * @param shipment the shipment
     * @return net profit
     * @throws SQLException if database error occurs
     */
    @Override
    public double calculateNetProfit(Shipment shipment) throws SQLException {
        double totalRevenue = calculateTotalRevenue(shipment);
        double totalExpenses = calculateTotalExpenses(shipment);
        return totalRevenue - totalExpenses;
    }

    /**
     * Get complete financial summary for a shipment.
     * @param shipment the shipment
     * @return financial summary
     * @throws SQLException if database error occurs
     */
    @Override
    public ShipmentFinancialSummary getFinancialSummary(Shipment shipment) throws SQLException {
        ShipmentFinancialSummary summary = new ShipmentFinancialSummary();
        summary.setShipment(shipment);
        summary.setTotalOrders(calculateTotalOrders(shipment));
        summary.setTotalCostOfGoods(calculateTotalCostOfGoods(shipment));
        summary.setTransportationCost(shipment.getTransportationCost());
        summary.setOtherCosts(shipment.getOtherCosts());
        summary.setTotalRevenue(calculateTotalRevenue(shipment));
        summary.setTotalExpenses(calculateTotalExpenses(shipment));
        summary.setNetProfit(calculateNetProfit(shipment));
        return summary;
    }

    /**
     * Validate shipment data before database operations.
     * Uses ValidationUtils to avoid code duplication.
     * @param shipment the shipment to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateShipment(Shipment shipment) {
        ValidationUtils.validateNotNull(shipment, "Shipment");
        ValidationUtils.validateNotEmpty(shipment.getBatchName(), "Batch name");
        ValidationUtils.validateNonNegative(shipment.getShipmentCost(), "Shipment cost");
        ValidationUtils.validateNonNegative(shipment.getTransportationCost(), "Transportation cost");
        ValidationUtils.validateNonNegative(shipment.getOtherCosts(), "Other costs");
    }

    /**
     * Data class for shipment financial summary.
     */
    public static class ShipmentFinancialSummary {
        private Shipment shipment;
        private int totalOrders;
        private double totalCostOfGoods;
        private double transportationCost;
        private double otherCosts;
        private double totalRevenue;
        private double totalExpenses;
        private double netProfit;

        // Getters and setters
        public Shipment getShipment() { return shipment; }
        public void setShipment(Shipment shipment) { this.shipment = shipment; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public double getTotalCostOfGoods() { return totalCostOfGoods; }
        public void setTotalCostOfGoods(double totalCostOfGoods) { this.totalCostOfGoods = totalCostOfGoods; }

        public double getTransportationCost() { return transportationCost; }
        public void setTransportationCost(double transportationCost) { this.transportationCost = transportationCost; }

        public double getOtherCosts() { return otherCosts; }
        public void setOtherCosts(double otherCosts) { this.otherCosts = otherCosts; }

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

        public double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }

        public double getNetProfit() { return netProfit; }
        public void setNetProfit(double netProfit) { this.netProfit = netProfit; }
    }
}
