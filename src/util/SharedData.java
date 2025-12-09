    package util;

import dao.ShipmentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Shipment;

import java.sql.SQLException;

public class SharedData {
    private static final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private static final ObservableList<Shipment> shipments = FXCollections.observableArrayList();

    // Return the shared observable list (same instance for all views)
    public static ObservableList<Shipment> getShipments() {
        return shipments;
    }

    // Refresh shipments from DB into the shared list
    public static void refreshShipments() {
        try {
            shipments.clear();
            shipments.addAll(shipmentDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
