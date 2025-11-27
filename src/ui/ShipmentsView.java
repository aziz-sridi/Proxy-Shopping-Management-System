package ui;

import dao.OrderDAO;
import dao.ShipmentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Order;
import model.Platform;
import model.Shipment;

import java.sql.SQLException;

public class ShipmentsView {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    public BorderPane getView() {
        BorderPane view = new BorderPane();
        view.getStyleClass().add("page-container");
        
        TableView<Shipment> table = new TableView<>();
        table.getStyleClass().add("modern-table");

        TableColumn<Shipment, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getShipmentId()));

        TableColumn<Shipment, String> colShipmentName = new TableColumn<>("Shipment Name");
        colShipmentName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBatchName()));

        TableColumn<Shipment, Number> colTransportCost = new TableColumn<>("Transport Cost");
        colTransportCost.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTransportationCost()));

        TableColumn<Shipment, Number> colOtherCosts = new TableColumn<>("Other Costs");
        colOtherCosts.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getOtherCosts()));

        TableColumn<Shipment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        TableColumn<Shipment, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnViewOrders = new Button("View Orders");
            private final Button btnEditShipment = new Button("Edit Shipment");
            private final Button btnDelete = new Button("Delete");

            {
                btnViewOrders.getStyleClass().addAll("modern-button", "button-secondary");
                btnEditShipment.getStyleClass().addAll("modern-button", "button-primary");
                btnDelete.getStyleClass().addAll("modern-button", "button-error");
                
                btnViewOrders.setOnAction(e -> {
                    Shipment shipment = getTableView().getItems().get(getIndex());
                    ShipmentsView.this.showShipmentOrders(shipment);
                });

                btnEditShipment.setOnAction(e -> {
                    Shipment shipment = getTableView().getItems().get(getIndex());
                    ShipmentsView.this.openShipmentFormDialog(shipment, false); // false = edit mode
                });

                btnDelete.setOnAction(e -> {
                    Shipment shipment = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Shipment");
                    alert.setHeaderText("Are you sure you want to delete this shipment?");
                    alert.setContentText("Shipment: " + shipment.getBatchName());
                    
                    if (alert.showAndWait().get() == ButtonType.OK) {
                        try {
                            shipmentDAO.delete(shipment.getShipmentId());
                            loadShipments();
                        } catch (SQLException ex) {
                            showError("Error deleting shipment: " + ex.getMessage());
                        }
                    }
                });

                btnViewOrders.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px;");
                btnEditShipment.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(5, btnViewOrders, btnEditShipment, btnDelete));
                }
            }
        });

        table.getColumns().clear();
        table.getColumns().add(colId);
        table.getColumns().add(colShipmentName);
        table.getColumns().add(colTransportCost);
        table.getColumns().add(colOtherCosts);
        table.getColumns().add(colStatus);
        table.getColumns().add(colActions);
        table.setItems(shipmentData);

        // Add New Shipment button
        Button btnAddShipment = new Button("➕ Add New Shipment");
        btnAddShipment.getStyleClass().addAll("modern-button", "button-primary");
        btnAddShipment.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnAddShipment.setOnAction(e -> openShipmentFormDialog(null, true)); // true = add mode

        VBox topSection = new VBox(10);
        topSection.getStyleClass().add("action-buttons");
        topSection.setPadding(new Insets(15));
        topSection.getChildren().addAll(
            new Label("📦 Shipment Management"),
            btnAddShipment
        );

        view.setTop(topSection);
        view.setCenter(table);

        loadShipments();
        return view;
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

    private void openShipmentFormDialog(Shipment shipment, boolean isAddMode) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isAddMode ? "Add New Shipment" : "Edit Shipment");
        dialog.setHeaderText(isAddMode ? "Create a new shipment" : "Edit shipment details");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

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
                try {
                    if (isAddMode) {
                        // Create new shipment
                        Shipment newShipment = new Shipment();
                        newShipment.setBatchName(txtBatch.getText());
                        newShipment.setShipmentCost(Double.parseDouble(txtCost.getText()));
                        newShipment.setDepartureDate(dpDeparture.getValue());
                        newShipment.setArrivalDate(dpArrival.getValue());
                        
                        // Handle transportation cost
                        String transportCostText = txtTransportationCost.getText();
                        if (!transportCostText.isEmpty()) {
                            newShipment.setTransportationCost(Double.parseDouble(transportCostText));
                        } else {
                            newShipment.setTransportationCost(0.0);
                        }
                        
                        // Handle other costs
                        String otherCostsText = txtOtherCosts.getText();
                        if (!otherCostsText.isEmpty()) {
                            newShipment.setOtherCosts(Double.parseDouble(otherCostsText));
                        } else {
                            newShipment.setOtherCosts(0.0);
                        }
                        
                        newShipment.setStatus(cbStatus.getValue());
                        
                        shipmentDAO.insert(newShipment);
                    } else {
                        // Update existing shipment
                        shipment.setBatchName(txtBatch.getText());
                        shipment.setShipmentCost(Double.parseDouble(txtCost.getText()));
                        shipment.setDepartureDate(dpDeparture.getValue());
                        shipment.setArrivalDate(dpArrival.getValue());
                        shipment.setTransportationCost(Double.parseDouble(txtTransportationCost.getText()));
                        shipment.setOtherCosts(Double.parseDouble(txtOtherCosts.getText()));
                        shipment.setStatus(cbStatus.getValue());
                        
                        shipmentDAO.update(shipment);
                    }
                    
                    loadShipments();
                    
                } catch (NumberFormatException ex) {
                    showError("Invalid number format in cost fields.");
                } catch (SQLException ex) {
                    showError("Error " + (isAddMode ? "creating" : "updating") + " shipment: " + ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showShipmentOrders(Shipment shipment) {
        Stage orderStage = new Stage();
        orderStage.initModality(Modality.APPLICATION_MODAL);
        orderStage.setTitle("Orders in Shipment: " + shipment.getBatchName());
        
        // Create orders table
        TableView<Order> orderTable = new TableView<>();
        
        // Create columns for orders
        TableColumn<Order, Integer> colOrderId = new TableColumn<>("Order ID");
        colOrderId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOrderId()));
        
        TableColumn<Order, Integer> colClientId = new TableColumn<>("Client ID");
        colClientId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClientId()));
        
        TableColumn<Order, String> colPlatform = new TableColumn<>("Platform");
        colPlatform.setCellValueFactory(c -> {
            Platform platform = c.getValue().getPlatform();
            return new javafx.beans.property.SimpleStringProperty(platform != null ? platform.getDisplayName() : "");
        });
        
        TableColumn<Order, Double> colOriginalPrice = new TableColumn<>("Original Price");
        colOriginalPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOriginalPrice()));
        
        TableColumn<Order, Double> colSellingPrice = new TableColumn<>("Selling Price");
        colSellingPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getSellingPrice()));
        
        TableColumn<Order, String> colPaymentStatus = new TableColumn<>("Payment Status");
        colPaymentStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus()));
        
        orderTable.getColumns().add(colOrderId);
        orderTable.getColumns().add(colClientId);
        orderTable.getColumns().add(colPlatform);
        orderTable.getColumns().add(colOriginalPrice);
        orderTable.getColumns().add(colSellingPrice);
        orderTable.getColumns().add(colPaymentStatus);
        
        // Load orders for this shipment
        ObservableList<Order> ordersData = FXCollections.observableArrayList();
        try {
            ordersData.addAll(orderDAO.getOrdersByShipmentId(shipment.getShipmentId()));
        } catch (SQLException ex) {
            showError("Error loading orders: " + ex.getMessage());
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
}
