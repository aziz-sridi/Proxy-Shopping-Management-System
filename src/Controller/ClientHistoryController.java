package Controller;

import service.IOrderService;
import service.OrderServiceImpl;
import service.IPaymentService;
import service.PaymentServiceImpl;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Client;
import model.Order;
import model.Payment;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * ViewController for ClientHistoryView - displays order and payment history for a client
 */
public class ClientHistoryController implements Initializable {

    // Services
    private final IOrderService orderService = new OrderServiceImpl();
    private final IPaymentService paymentService = new PaymentServiceImpl();

    // Observable data
    private final ObservableList<Order> orderData = FXCollections.observableArrayList();
    private final ObservableList<Payment> paymentData = FXCollections.observableArrayList();

    // Current client being viewed
    private Client currentClient;

    // FXML injected components
    @FXML private Label lblHeader;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Number> colOrderId;
    @FXML private TableColumn<Order, String> colProduct;
    @FXML private TableColumn<Order, Number> colTotal;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Number> colPayId;
    @FXML private TableColumn<Payment, Number> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupOrdersTable();
        setupPaymentsTable();
    }

    private void setupOrdersTable() {
        colOrderId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOrderId()));
        colProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductLink()));
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSellingPrice()));
        ordersTable.setItems(orderData);
    }

    private void setupPaymentsTable() {
        colPayId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPaymentId()));
        colAmount.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAmount()));
        colMethod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod()));
        paymentsTable.setItems(paymentData);
    }

    /**
     * Set the client to display history for and load their data
     * @param client The client to show history for
     */
    public void setClient(Client client) {
        this.currentClient = client;
        if (client != null) {
            lblHeader.setText("History for " + client.getUsername());
            loadData();
        }
    }

    private void loadData() {
        if (currentClient == null) return;

        orderData.clear();
        paymentData.clear();
        try {
            orderData.addAll(orderService.getOrdersByClient(currentClient.getClientId()));
            paymentData.addAll(paymentService.getPaymentsByClient(currentClient.getClientId()));
        } catch (SQLException e) {
            // Log error but don't show popup in embedded view
            System.err.println("Error loading client history: " + e.getMessage());
        }
    }

    /**
     * Refresh the data for the current client
     */
    public void refreshData() {
        loadData();
    }
}
