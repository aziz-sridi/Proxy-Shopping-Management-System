package ui.dialog;

import service.ShipmentService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Order;
import model.Platform;
import model.Shipment;
import ui.util.DialogUtils;

import java.sql.SQLException;

/**
 * Dialog helper class for shipment-related dialogs.
 * Extracts dialog logic from ShipmentsViewController.
 */
public class ShipmentDialogs {

    private final ShipmentService shipmentService;

    public ShipmentDialogs(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * Interface for callbacks when shipment operations complete
     */
    @FunctionalInterface
    public interface ShipmentCallback {
        void onComplete();
    }

    /**
     * Open dialog to add a new shipment.
     */
    public void openAddShipmentDialog(ShipmentCallback onSuccess) {
        openShipmentFormDialog(null, true, onSuccess);
    }

    /**
     * Open dialog to edit an existing shipment.
     */
    public void openEditShipmentDialog(Shipment shipment, ShipmentCallback onSuccess) {
        openShipmentFormDialog(shipment, false, onSuccess);
    }

    private void openShipmentFormDialog(Shipment shipment, boolean isAddMode, ShipmentCallback onSuccess) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isAddMode ? "Add New Shipment" : "Edit Shipment");
        dialog.setHeaderText(isAddMode ? "Create a new shipment" : "Edit shipment details");

        GridPane form = DialogUtils.createFormGrid(10, 10, 10);

        TextField txtBatch = new TextField(isAddMode ? "" : shipment.getBatchName());
        txtBatch.setPromptText("Batch name");

        TextField txtCost = new TextField(isAddMode ? "" : String.valueOf(shipment.getShipmentCost()));
        txtCost.setPromptText("Base cost");

        DatePicker dpDeparture = new DatePicker(isAddMode ? null : shipment.getDepartureDate());
        DatePicker dpArrival = new DatePicker(isAddMode ? null : shipment.getArrivalDate());

        TextField txtTransportationCost = new TextField(isAddMode ? "" : String.valueOf(shipment.getTransportationCost()));
        txtTransportationCost.setPromptText("Transportation cost");

        TextField txtOtherCosts = new TextField(isAddMode ? "" : String.valueOf(shipment.getOtherCosts()));
        txtOtherCosts.setPromptText("Other costs");

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("pending", "in_transit", "arrived", "distributed");
        cbStatus.setValue(isAddMode ? "pending" : shipment.getStatus());

        form.add(new Label("Batch Name:"), 0, 0);
        form.add(txtBatch, 1, 0);
        form.add(new Label("Base Cost:"), 0, 1);
        form.add(txtCost, 1, 1);
        form.add(new Label("🚚 Transportation Cost:"), 0, 2);
        form.add(txtTransportationCost, 1, 2);
        form.add(new Label("💰 Other Costs:"), 0, 3);
        form.add(txtOtherCosts, 1, 3);
        form.add(new Label("Departure Date:"), 0, 4);
        form.add(dpDeparture, 1, 4);
        form.add(new Label("Arrival Date:"), 0, 5);
        form.add(dpArrival, 1, 5);
        form.add(new Label("Status:"), 0, 6);
        form.add(cbStatus, 1, 6);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                processShipmentForm(shipment, isAddMode, txtBatch.getText(), txtCost.getText(),
                    dpDeparture.getValue(), dpArrival.getValue(), txtTransportationCost.getText(),
                    txtOtherCosts.getText(), cbStatus.getValue(), onSuccess);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void processShipmentForm(Shipment shipment, boolean isAddMode, String batchName, 
                                      String costText, java.time.LocalDate departureDate,
                                      java.time.LocalDate arrivalDate, String transportCostText,
                                      String otherCostsText, String status, ShipmentCallback onSuccess) {
        try {
            if (isAddMode) {
                Shipment newShipment = new Shipment();
                newShipment.setBatchName(batchName);
                newShipment.setShipmentCost(DialogUtils.parseDouble(costText, 0.0));
                newShipment.setDepartureDate(departureDate);
                newShipment.setArrivalDate(arrivalDate);
                newShipment.setTransportationCost(DialogUtils.parseDouble(transportCostText, 0.0));
                newShipment.setOtherCosts(DialogUtils.parseDouble(otherCostsText, 0.0));
                newShipment.setStatus(status);

                shipmentService.addShipment(newShipment);
            } else {
                shipment.setBatchName(batchName);
                shipment.setShipmentCost(DialogUtils.parseDouble(costText, 0.0));
                shipment.setDepartureDate(departureDate);
                shipment.setArrivalDate(arrivalDate);
                shipment.setTransportationCost(DialogUtils.parseDouble(transportCostText, 0.0));
                shipment.setOtherCosts(DialogUtils.parseDouble(otherCostsText, 0.0));
                shipment.setStatus(status);

                shipmentService.updateShipment(shipment);
            }
            
            if (onSuccess != null) onSuccess.onComplete();

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid number format in cost fields.");
        } catch (SQLException ex) {
            DialogUtils.showError("Error " + (isAddMode ? "creating" : "updating") + " shipment: " + ex.getMessage());
        }
    }

    /**
     * Show delete confirmation and delete shipment if confirmed.
     */
    public void deleteShipment(Shipment shipment, ShipmentCallback onSuccess) {
        if (DialogUtils.showConfirmation("Delete Shipment", 
                "Are you sure you want to delete shipment: " + shipment.getBatchName() + "?")) {
            try {
                shipmentService.deleteShipment(shipment.getShipmentId());
                if (onSuccess != null) onSuccess.onComplete();
            } catch (SQLException ex) {
                DialogUtils.showError("Error deleting shipment: " + ex.getMessage());
            }
        }
    }

    /**
     * Show a window with all orders in a shipment.
     */
    public void showShipmentOrders(Shipment shipment) {
        Stage orderStage = new Stage();
        orderStage.initModality(Modality.APPLICATION_MODAL);
        orderStage.setTitle("Orders in Shipment: " + shipment.getBatchName());

        TableView<Order> orderTable = createOrderTable();

        ObservableList<Order> ordersData = FXCollections.observableArrayList();
        try {
            ordersData.addAll(shipmentService.getOrdersForShipment(shipment.getShipmentId()));
        } catch (SQLException ex) {
            DialogUtils.showError("Error loading orders: " + ex.getMessage());
        }
        orderTable.setItems(ordersData);

        Label ordersLabel = new Label("Orders in this shipment (" + ordersData.size() + " total)");
        ordersLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox content = new VBox(10, ordersLabel, orderTable);
        content.setPadding(new Insets(15));

        Scene scene = new Scene(content, 800, 500);
        orderStage.setScene(scene);
        orderStage.showAndWait();
    }

    private TableView<Order> createOrderTable() {
        TableView<Order> orderTable = new TableView<>();

        TableColumn<Order, Integer> colOrderId = new TableColumn<>("Order ID");
        colOrderId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getOrderId()));

        TableColumn<Order, Integer> colClientId = new TableColumn<>("Client ID");
        colClientId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getClientId()));

        TableColumn<Order, String> colPlatform = new TableColumn<>("Platform");
        colPlatform.setCellValueFactory(c -> {
            Platform platform = c.getValue().getPlatform();
            return new SimpleStringProperty(platform != null ? platform.getDisplayName() : "");
        });

        TableColumn<Order, Double> colOriginalPrice = new TableColumn<>("Original Price");
        colOriginalPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getOriginalPrice()));

        TableColumn<Order, Double> colSellingPrice = new TableColumn<>("Selling Price");
        colSellingPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSellingPrice()));

        TableColumn<Order, String> colPaymentStatus = new TableColumn<>("Payment Status");
        colPaymentStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentStatus()));

        orderTable.getColumns().add(colOrderId);
        orderTable.getColumns().add(colClientId);
        orderTable.getColumns().add(colPlatform);
        orderTable.getColumns().add(colOriginalPrice);
        orderTable.getColumns().add(colSellingPrice);
        orderTable.getColumns().add(colPaymentStatus);

        return orderTable;
    }
}
