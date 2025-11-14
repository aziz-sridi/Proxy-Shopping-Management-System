package ui;

import dao.ShipmentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import model.Shipment;

import java.sql.SQLException;
import java.time.LocalDate;

public class ShipmentsView {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Shipment> table = new TableView<>();

        TableColumn<Shipment, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getShipmentId()));

        TableColumn<Shipment, String> colBatch = new TableColumn<>("Batch");
        colBatch.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBatchName()));

        TableColumn<Shipment, Number> colCost = new TableColumn<>("Cost");
        colCost.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getShipmentCost()));

        TableColumn<Shipment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        table.getColumns().addAll(colId, colBatch, colCost, colStatus);
        table.setItems(shipmentData);

        TextField txtBatch = new TextField();
        txtBatch.setPromptText("Batch name");

        TextField txtCost = new TextField();
        txtCost.setPromptText("Shipment cost");

        DatePicker dpDeparture = new DatePicker();
        DatePicker dpArrival = new DatePicker();

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("pending", "in_transit", "arrived", "distributed");
        cbStatus.setValue("pending");

        Button btnAdd = new Button("Add Shipment");
        btnAdd.setOnAction(e -> {
            Shipment s = new Shipment();
            s.setBatchName(txtBatch.getText());
            try {
                s.setShipmentCost(Double.parseDouble(txtCost.getText()));
            } catch (NumberFormatException ex) {
                showError("Invalid cost.");
                return;
            }
            LocalDate dep = dpDeparture.getValue();
            LocalDate arr = dpArrival.getValue();
            s.setDepartureDate(dep);
            s.setArrivalDate(arr);
            s.setStatus(cbStatus.getValue());
            try {
                shipmentDAO.insert(s);
                loadShipments();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(5);
        form.setVgap(5);
        form.add(new Label("Batch:"), 0, 0);
        form.add(txtBatch, 1, 0);
        form.add(new Label("Cost:"), 0, 1);
        form.add(txtCost, 1, 1);
        form.add(new Label("Departure:"), 0, 2);
        form.add(dpDeparture, 1, 2);
        form.add(new Label("Arrival:"), 0, 3);
        form.add(dpArrival, 1, 3);
        form.add(new Label("Status:"), 0, 4);
        form.add(cbStatus, 1, 4);
        form.add(btnAdd, 1, 5);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(form);

        loadShipments();
        return root;
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
