package ui;

import dao.ClientDAO;
import dao.DeliveryOptionDAO;
import dao.OrderDAO;
import dao.ShipmentDAO;
import dao.CurrencyRateDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import model.Client;
import model.DeliveryOption;
import model.Order;
import model.Shipment;

import java.sql.SQLException;

public class OrdersView {

    private final OrderDAO orderDAO = new OrderDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final DeliveryOptionDAO deliveryOptionDAO = new DeliveryOptionDAO();
    private final CurrencyRateDAO currencyRateDAO = new CurrencyRateDAO();

    private final ObservableList<Order> orderData = FXCollections.observableArrayList();
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();
    private final ObservableList<DeliveryOption> deliveryData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Order> table = new TableView<>();

        TableColumn<Order, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrderId()));

        TableColumn<Order, Number> colClientId = new TableColumn<>("Client");
        colClientId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getClientId()));

        TableColumn<Order, String> colProduct = new TableColumn<>("Product Link");
        colProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductLink()));

        TableColumn<Order, Number> colOrig = new TableColumn<>("Original");
        colOrig.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getOriginalPrice()));

        TableColumn<Order, Number> colSell = new TableColumn<>("Selling");
        colSell.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Order, String> colStatus = new TableColumn<>("Payment");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus()));

        table.getColumns().addAll(colId, colClientId, colProduct, colOrig, colSell, colStatus);
        table.setItems(orderData);

        ComboBox<Client> cbClient = new ComboBox<>(clientData);
        cbClient.setPromptText("Client");

        TextField txtClientSearch = new TextField();
        txtClientSearch.setPromptText("Search client by username");

        txtClientSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.trim().toLowerCase();
            cbClient.getItems().setAll(clientData.filtered(c -> {
                String name = c.getUsername();
                return filter.isEmpty() || (name != null && name.toLowerCase().contains(filter));
            }));
        });

        ComboBox<Shipment> cbShipment = new ComboBox<>(shipmentData);
        cbShipment.setPromptText("Shipment (optional)");

        ComboBox<DeliveryOption> cbDelivery = new ComboBox<>(deliveryData);
        cbDelivery.setPromptText("Delivery option (optional)");

        TextField txtProduct = new TextField();
        txtProduct.setPromptText("Product link");

        TextField txtSize = new TextField();
        txtSize.setPromptText("Size");

        Spinner<Integer> spQty = new Spinner<>(1, 1000, 1);

        TextField txtOriginal = new TextField();
        txtOriginal.setPromptText("Original price (EUR)");

        TextField txtSelling = new TextField();
        txtSelling.setPromptText("Selling price (TND)");
        txtSelling.setEditable(false);

        txtOriginal.textProperty().addListener((obs, old, val) -> {
            String text = val == null ? "" : val.trim();
            if (text.isEmpty()) {
                txtSelling.clear();
                return;
            }
            try {
                double original = Double.parseDouble(text);
                double rate = fetchCustomRate();
                if (rate > 0) {
                    double selling = original * rate;
                    txtSelling.setText(String.format("%.2f", selling));
                } else {
                    txtSelling.clear();
                }
            } catch (NumberFormatException ex) {
                txtSelling.clear();
            }
        });

        ComboBox<String> cbPaymentType = new ComboBox<>();
        cbPaymentType.getItems().addAll("Deposit", "Full", "On Delivery");
        cbPaymentType.setValue("On Delivery");

        TextField txtDepositAmount = new TextField();
        txtDepositAmount.setPromptText("Deposit amount (TND)");
        txtDepositAmount.setDisable(true);

        cbPaymentType.valueProperty().addListener((obs, old, val) -> {
            boolean isDeposit = "Deposit".equals(val);
            txtDepositAmount.setDisable(!isDeposit);
            if (!isDeposit) {
                txtDepositAmount.clear();
            }
        });

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Notes");

        Button btnAdd = new Button("Add Order");
        btnAdd.setOnAction(e -> {
            Client client = cbClient.getValue();
            if (client == null) {
                showError("Please select a client.");
                return;
            }
            Order o = new Order();
            o.setClientId(client.getClientId());
            Shipment sh = cbShipment.getValue();
            o.setShipmentId(sh != null ? sh.getShipmentId() : null);
            DeliveryOption d = cbDelivery.getValue();
            o.setDeliveryOptionId(d != null ? d.getDeliveryOptionId() : null);
            o.setProductLink(txtProduct.getText());
            o.setProductSize(txtSize.getText());
            o.setQuantity(spQty.getValue());
            try {
                o.setOriginalPrice(Double.parseDouble(txtOriginal.getText()));
                o.setSellingPrice(Double.parseDouble(txtSelling.getText()));
            } catch (NumberFormatException ex) {
                showError("Invalid price values.");
                return;
            }
            o.setPaymentType(cbPaymentType.getValue());
            o.setPaymentStatus("Unpaid");
            o.setNotes(txtNotes.getText());
            try {
                orderDAO.insert(o);
                loadOrders();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(5);
        form.setVgap(5);
        form.add(new Label("Client:"), 0, 0);
        form.add(cbClient, 1, 0);
        form.add(txtClientSearch, 2, 0);
        form.add(new Label("Shipment:"), 0, 1);
        form.add(cbShipment, 1, 1);
        form.add(new Label("Delivery:"), 0, 2);
        form.add(cbDelivery, 1, 2);
        form.add(new Label("Product:"), 0, 3);
        form.add(txtProduct, 1, 3);
        form.add(new Label("Size:"), 0, 4);
        form.add(txtSize, 1, 4);
        form.add(new Label("Qty:"), 0, 5);
        form.add(spQty, 1, 5);
        form.add(new Label("Original:"), 0, 6);
        form.add(txtOriginal, 1, 6);
        form.add(new Label("Selling:"), 0, 7);
        form.add(txtSelling, 1, 7);
        form.add(new Label("Payment Type:"), 0, 8);
        form.add(cbPaymentType, 1, 8);
        form.add(new Label("Deposit:"), 0, 9);
        form.add(txtDepositAmount, 1, 9);
        form.add(new Label("Notes:"), 0, 10);
        form.add(txtNotes, 1, 10);
        form.add(btnAdd, 1, 11);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(form);

        loadClients();
        loadShipments();
        loadDeliveryOptions();
        loadOrders();

        return root;
    }

    private void loadClients() {
        clientData.clear();
        try {
            clientData.addAll(clientDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void loadDeliveryOptions() {
        deliveryData.clear();
        try {
            deliveryData.addAll(deliveryOptionDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void loadOrders() {
        orderData.clear();
        try {
            orderData.addAll(orderDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private double fetchCustomRate() {
        try {
            var rate = currencyRateDAO.findLatest("EUR", "TND");
            return rate != null ? rate.getCustomRate() : 0.0;
        } catch (SQLException e) {
            showError("Failed to load currency rate: " + e.getMessage());
            return 0.0;
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
