package Controller;

import service.IClientService;
import service.ClientServiceImpl;
import service.IDeliveryOptionService;
import service.DeliveryOptionServiceImpl;
import service.IOrderService;
import service.OrderServiceImpl;
import service.IShipmentService;
import service.ShipmentServiceImpl;
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
import javafx.scene.layout.HBox;
import model.Client;
import model.DeliveryOption;
import model.Order;
import model.Platform;
import model.Shipment;
import ui.dialog.OrderDialogs;
import ui.dialog.PaymentDialogs;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * ViewController for OrdersView - handles all order-related UI interactions.
 * Refactored to use helper dialog classes for better maintainability.
 */
public class OrdersController implements Initializable {

    // Services
    private final IOrderService orderService = new OrderServiceImpl();
    private final IClientService clientService = new ClientServiceImpl();
    private final IShipmentService shipmentService = new ShipmentServiceImpl();
    private final IDeliveryOptionService deliveryOptionService = new DeliveryOptionServiceImpl();
    private final IPaymentService paymentService = new PaymentServiceImpl();

    // Dialog helpers
    private final OrderDialogs orderDialogs;
    private final PaymentDialogs paymentDialogs;

    // Observable data lists
    private final ObservableList<Order> orderData = FXCollections.observableArrayList();
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();
    private final ObservableList<DeliveryOption> deliveryData = FXCollections.observableArrayList();

    // Callbacks for refreshing other views
    private Runnable paymentRefreshCallback;

    // FXML injected components
    @FXML private TextField txtClientSearch;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private ComboBox<String> cbPlatformFilter;
    @FXML private Button btnNewOrder;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Number> colId;
    @FXML private TableColumn<Order, String> colClient;
    @FXML private TableColumn<Order, String> colProduct;
    @FXML private TableColumn<Order, String> colPlatform;
    @FXML private TableColumn<Order, String> colShipment;
    @FXML private TableColumn<Order, Number> colOriginal;
    @FXML private TableColumn<Order, Number> colSelling;
    @FXML private TableColumn<Order, Number> colRemaining;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Void> colActions;

    public OrdersController() {
        this.orderDialogs = new OrderDialogs(orderService, paymentService);
        this.paymentDialogs = new PaymentDialogs(paymentService, orderService);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        setupTableColumns();
        setupTableRowFactory();
        loadAllData();
    }

    private void setupFilters() {
        cbStatusFilter.getItems().addAll("All", "Unpaid", "Partial", "Paid");
        cbStatusFilter.setValue("All");
        cbStatusFilter.getStyleClass().add("app-field");

        cbPlatformFilter.getItems().addAll("All Platforms", "Shein", "Temu", "AliExpress", "Alibaba", "Other");
        cbPlatformFilter.setValue("All Platforms");
        cbPlatformFilter.getStyleClass().add("app-field");

        txtClientSearch.getStyleClass().add("app-field");

        // Add filter listeners
        txtClientSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbPlatformFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOrderId()));

        colClient.setCellValueFactory(c -> {
            int clientId = c.getValue().getClientId();
            Client client = clientData.stream()
                .filter(cl -> cl.getClientId() == clientId)
                .findFirst().orElse(null);
            String name = client != null ? client.getUsername() : ("#" + clientId);
            return new SimpleStringProperty(name);
        });

        colProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductLink()));

        colPlatform.setCellValueFactory(c -> {
            Platform platform = c.getValue().getPlatform();
            return new SimpleStringProperty(platform != null ? platform.getDisplayName() : "Other");
        });

        colShipment.setCellValueFactory(c -> {
            Integer shipmentId = c.getValue().getShipmentId();
            if (shipmentId != null) {
                Shipment shipment = shipmentData.stream()
                    .filter(s -> s.getShipmentId() == shipmentId)
                    .findFirst().orElse(null);
                return new SimpleStringProperty(shipment != null ? shipment.getBatchName() : "ID: " + shipmentId);
            }
            return new SimpleStringProperty("Not Assigned");
        });

        colOriginal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getOriginalPrice()));
        colSelling.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSellingPrice()));

        colRemaining.setCellValueFactory(c -> {
            Order o = c.getValue();
            double remaining = computeRemainingForOrder(o);
            return new SimpleDoubleProperty(remaining);
        });

        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentStatus()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnPay = new Button("+ Payment");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(5, btnPay, btnEdit, btnDelete);

            {
                btnPay.getStyleClass().addAll("app-button", "button-primary");
                btnEdit.getStyleClass().addAll("app-button", "button-secondary");
                btnDelete.getStyleClass().addAll("app-button", "button-error");
                
                btnPay.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    openAddPaymentDialog(order);
                });
                
                btnEdit.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    editOrder(order);
                });
                
                btnDelete.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    // Hide payment button if order is already paid
                    btnPay.setVisible(!("Paid".equalsIgnoreCase(order.getPaymentStatus())));
                    setGraphic(box);
                }
            }
        });

        ordersTable.setItems(orderData);
    }

    private void setupTableRowFactory() {
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showClientInfoPopup(row.getItem().getClientId());
                }
            });
            return row;
        });
    }

    private void loadAllData() {
        loadClients();
        loadShipments();
        loadDeliveryOptions();
        loadOrders();
    }

    private void loadClients() {
        clientData.clear();
        try {
            clientData.addAll(clientService.getAllClients());
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentService.getAllShipments());
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    private void loadDeliveryOptions() {
        deliveryData.clear();
        try {
            deliveryData.addAll(deliveryOptionService.getAllDeliveryOptions());
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    private void loadOrders() {
        orderData.clear();
        try {
            orderData.addAll(orderService.getAllOrders());
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    /**
     * Public method to refresh all data - can be called from other views
     */
    public void refreshData() {
        loadOrders();
        loadClients();
        loadShipments();
    }

    /**
     * Set callback for refreshing payments view when payments are added
     */
    public void setPaymentRefreshCallback(Runnable callback) {
        this.paymentRefreshCallback = callback;
    }

    /**
     * Public method to refresh shipments - updates the shipment combo box in order dialogs
     */
    public void refreshShipments() {
        loadShipments();
    }

    private void notifyPaymentRefresh() {
        if (paymentRefreshCallback != null) {
            paymentRefreshCallback.run();
        }
    }

    private double computeRemainingForOrder(Order order) {
        try {
            double totalPaid = paymentService.getTotalPaidForOrder(order.getOrderId());
            return PriceCalculator.calculateRemaining(order.getSellingPrice(), totalPaid);
        } catch (SQLException e) {
            return order.getSellingPrice();
        }
    }

    private void applyFilters() {
        String keyword = txtClientSearch.getText() == null ? "" : txtClientSearch.getText().trim().toLowerCase();
        String status = cbStatusFilter.getValue() == null ? "All" : cbStatusFilter.getValue();
        String platform = cbPlatformFilter.getValue() == null ? "All Platforms" : cbPlatformFilter.getValue();

        ordersTable.setItems(orderData.filtered(o -> {
            // Search filter
            Client client = clientData.stream()
                .filter(c -> c.getClientId() == o.getClientId())
                .findFirst().orElse(null);
            String clientName = client != null ? client.getUsername() : "";
            String combined = (clientName + " " + o.getProductLink()).toLowerCase();
            boolean matchesSearch = keyword.isEmpty() || combined.contains(keyword);

            // Status filter
            boolean matchesStatus = "All".equals(status) || status.equalsIgnoreCase(o.getPaymentStatus());

            // Platform filter
            boolean matchesPlatform;
            if ("All Platforms".equals(platform)) {
                matchesPlatform = true;
            } else {
                String orderPlatform = o.getPlatform() != null ? o.getPlatform().getDisplayName() : "Other";
                matchesPlatform = platform.equals(orderPlatform);
            }

            return matchesSearch && matchesStatus && matchesPlatform;
        }));
    }

    @FXML
    private void handleNewOrder() {
        orderDialogs.openNewOrderDialog(
            clientData, 
            shipmentData, 
            deliveryData,
            this::loadOrders,
            this::notifyPaymentRefresh
        );
    }

    private void openAddPaymentDialog(Order order) {
        paymentDialogs.openAddPaymentForOrderDialog(
            order,
            () -> {
                loadOrders();
                notifyPaymentRefresh();
            },
            null
        );
    }

    private void editOrder(Order order) {
        orderDialogs.openEditOrderDialog(
            order,
            clientData,
            shipmentData,
            deliveryData,
            this::loadOrders
        );
    }

    private void deleteOrder(Order order) {
        if (DialogUtils.showConfirmation("Delete Order", 
                "Are you sure you want to delete order #" + order.getOrderId() + "?")) {
            try {
                orderService.deleteOrder(order.getOrderId());
                loadOrders();
                notifyPaymentRefresh();
            } catch (SQLException e) {
                DialogUtils.showError("Error deleting order: " + e.getMessage());
            }
        }
    }

    private void showClientInfoPopup(int clientId) {
        Client client = clientData.stream()
            .filter(c -> c.getClientId() == clientId)
            .findFirst().orElse(null);
        orderDialogs.showClientInfoPopup(client);
    }
}
