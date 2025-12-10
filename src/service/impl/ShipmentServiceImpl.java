package service.impl;

import service.api.IShipmentService;
import dao.ShipmentDAO;
import dao.OrderDAO;
import model.Shipment;
import model.Order;
import model.Settings;
import service.ValidationUtils;
import ui.util.PriceCalculator;
import util.SettingsManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShipmentServiceImpl implements IShipmentService {

    private static final Logger LOGGER = Logger.getLogger(ShipmentServiceImpl.class.getName());
    private final ShipmentDAO shipmentDAO;
    private final OrderDAO orderDAO;

    public ShipmentServiceImpl(ShipmentDAO shipmentDAO, OrderDAO orderDAO) {
        this.shipmentDAO = shipmentDAO;
        this.orderDAO = orderDAO;
    }

    public ShipmentServiceImpl() {
        this(new ShipmentDAO(), new OrderDAO());
    }

    @Override
    public List<Shipment> getAllShipments() throws SQLException {
        LOGGER.log(Level.INFO, "Fetching all shipments");
        return shipmentDAO.findAll();
    }

    @Override
    public void addShipment(Shipment shipment) throws SQLException {
        validateShipment(shipment);
        LOGGER.log(Level.INFO, "Adding new shipment: {0}", shipment.getBatchName());
        shipmentDAO.insert(shipment);
        LOGGER.log(Level.INFO, "Shipment added successfully: {0}", shipment.getBatchName());
    }

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

    @Override
    public void deleteShipment(int shipmentId) throws SQLException {
        ValidationUtils.validatePositiveId(shipmentId, "Shipment ID");
        LOGGER.log(Level.INFO, "Deleting shipment ID: {0}", shipmentId);
        shipmentDAO.delete(shipmentId);
        LOGGER.log(Level.INFO, "Shipment deleted successfully: {0}", shipmentId);
    }

    @Override
    public List<Order> getOrdersForShipment(int shipmentId) throws SQLException {
        return orderDAO.getOrdersByShipmentId(shipmentId);
    }

    @Override
    public int calculateTotalOrders(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        return orders.size();
    }

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

    @Override
    public double calculateTotalRevenue(Shipment shipment) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByShipmentId(shipment.getShipmentId());
        
        double totalRevenue = 0.0;
        for (Order order : orders) {
            double orderRevenue = PriceCalculator.calculateTotalSellingPrice(
                order.getOriginalPrice(), order.getQuantity());
            totalRevenue += orderRevenue;
        }
        return totalRevenue;
    }

    @Override
    public double calculateTotalExpenses(Shipment shipment) throws SQLException {
        double totalCostOfGoods = calculateTotalCostOfGoods(shipment);
        return totalCostOfGoods + shipment.getTransportationCost() + shipment.getOtherCosts();
    }

    @Override
    public double calculateNetProfit(Shipment shipment) throws SQLException {
        double totalRevenue = calculateTotalRevenue(shipment);
        double totalExpenses = calculateTotalExpenses(shipment);
        return totalRevenue - totalExpenses;
    }

    @Override
    public Map<String, Double> getFinancialSummary(Shipment shipment) throws SQLException {
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalOrders", (double) calculateTotalOrders(shipment));
        summary.put("totalCostOfGoods", calculateTotalCostOfGoods(shipment));
        summary.put("transportationCost", (double) shipment.getTransportationCost());
        summary.put("otherCosts", (double) shipment.getOtherCosts());
        summary.put("totalRevenue", calculateTotalRevenue(shipment));
        summary.put("totalExpenses", calculateTotalExpenses(shipment));
        summary.put("netProfit", calculateNetProfit(shipment));
        return summary;
    }

    private void validateShipment(Shipment shipment) {
        ValidationUtils.validateNotNull(shipment, "Shipment");
        ValidationUtils.validateNotEmpty(shipment.getBatchName(), "Batch name");
        ValidationUtils.validateNonNegative(shipment.getShipmentCost(), "Shipment cost");
        ValidationUtils.validateNonNegative(shipment.getTransportationCost(), "Transportation cost");
        ValidationUtils.validateNonNegative(shipment.getOtherCosts(), "Other costs");
    }
}
