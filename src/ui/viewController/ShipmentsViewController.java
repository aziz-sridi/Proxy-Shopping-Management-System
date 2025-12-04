package ui.viewController;

import service.ShipmentService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Shipment;
import ui.dialog.ShipmentDialogs;
import ui.util.DialogUtils;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * ViewController for ShipmentsView - handles all shipment-related UI interactions.
 * Refactored to use ShipmentDialogs helper class for better maintainability.
 */
public class ShipmentsViewController implements Initializable {

    // Services
    private final ShipmentService shipmentService = new ShipmentService();
    private final ShipmentDialogs shipmentDialogs;

    // Observable data
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    // FXML injected components
    @FXML private Button btnAddShipment;
    @FXML private TableView<Shipment> shipmentsTable;
    @FXML private TableColumn<Shipment, Number> colId;
    @FXML private TableColumn<Shipment, String> colShipmentName;
    @FXML private TableColumn<Shipment, Number> colTransportCost;
    @FXML private TableColumn<Shipment, Number> colOtherCosts;
    @FXML private TableColumn<Shipment, String> colStatus;
    @FXML private TableColumn<Shipment, Void> colActions;

    public ShipmentsViewController() {
        this.shipmentDialogs = new ShipmentDialogs(shipmentService);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadShipments();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getShipmentId()));
        colShipmentName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatchName()));
        colTransportCost.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTransportationCost()));
        colOtherCosts.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getOtherCosts()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnViewOrders = new Button("View Orders");
            private final Button btnEditShipment = new Button("Edit Shipment");
            private final Button btnDelete = new Button("Delete");

            {
                btnViewOrders.getStyleClass().addAll("modern-button", "button-secondary");
                btnEditShipment.getStyleClass().addAll("modern-button", "button-primary");
                btnDelete.getStyleClass().addAll("modern-button", "button-error");

                btnViewOrders.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px;");
                btnEditShipment.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px;");

                btnViewOrders.setOnAction(e -> {
                    Shipment shipment = getTableView().getItems().get(getIndex());
                    shipmentDialogs.showShipmentOrders(shipment);
                });

                btnEditShipment.setOnAction(e -> {
                    Shipment shipment = getTableView().getItems().get(getIndex());
                    shipmentDialogs.openEditShipmentDialog(shipment, this::refresh);
                });

                btnDelete.setOnAction(e -> {
                    Shipment shipment = getTableView().getItems().get(getIndex());
                    shipmentDialogs.deleteShipment(shipment, this::refresh);
                });
            }

            private void refresh() {
                loadShipments();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, btnViewOrders, btnEditShipment, btnDelete));
                }
            }
        });

        shipmentsTable.setItems(shipmentData);
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentService.getAllShipments());
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    /**
     * Public method to refresh shipments - can be called from other views
     */
    public void refreshData() {
        loadShipments();
    }

    @FXML
    private void handleAddShipment() {
        shipmentDialogs.openAddShipmentDialog(this::loadShipments);
    }
}
