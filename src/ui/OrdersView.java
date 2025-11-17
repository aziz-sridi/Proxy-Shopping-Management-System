package ui;

import dao.ClientDAO;
import dao.DeliveryOptionDAO;
import dao.OrderDAO;
import dao.ShipmentDAO;
import dao.CurrencyRateDAO;
import dao.PaymentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import model.Client;
import model.DeliveryOption;
import model.Order;
import model.Shipment;
import model.Payment;

import java.sql.SQLException;

public class OrdersView {

    private final OrderDAO orderDAO = new OrderDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final DeliveryOptionDAO deliveryOptionDAO = new DeliveryOptionDAO();
    private final CurrencyRateDAO currencyRateDAO = new CurrencyRateDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final ObservableList<Order> orderData = FXCollections.observableArrayList();
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();
    private final ObservableList<DeliveryOption> deliveryData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Order> table = new TableView<>();

        TableColumn<Order, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrderId()));

        TableColumn<Order, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(c -> {
            int clientId = c.getValue().getClientId();
            Client client = clientData.stream().filter(cl -> cl.getClientId() == clientId).findFirst().orElse(null);
            String name = client != null ? client.getUsername() : ("#" + clientId);
            return new javafx.beans.property.SimpleStringProperty(name);
        });

        TableColumn<Order, String> colProduct = new TableColumn<>("Product Link");
        colProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductLink()));

        TableColumn<Order, Number> colOrig = new TableColumn<>("Original");
        colOrig.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getOriginalPrice()));

        TableColumn<Order, Number> colSell = new TableColumn<>("Selling");
        colSell.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Order, Number> colRemaining = new TableColumn<>("Remaining");
        colRemaining.setCellValueFactory(c -> {
            Order o = c.getValue();
            double remaining = computeRemainingForOrder(o);
            return new javafx.beans.property.SimpleDoubleProperty(remaining);
        });

        TableColumn<Order, String> colStatus = new TableColumn<>("Payment");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus()));

        TableColumn<Order, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnPay = new Button("+ Payment");

            {
                btnPay.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    openAddPaymentDialog(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnPay);
                }
            }
        });

        table.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Order order = row.getItem();
                    showClientInfoPopup(order.getClientId());
                }
            });
            return row;
        });

        table.getColumns().clear();
        table.getColumns().add(colId);
        table.getColumns().add(colClient);
        table.getColumns().add(colProduct);
        table.getColumns().add(colOrig);
        table.getColumns().add(colSell);
        table.getColumns().add(colRemaining);
        table.getColumns().add(colStatus);
        table.getColumns().add(colActions);
        table.setItems(orderData);

        ComboBox<Client> cbClient = new ComboBox<>(clientData);
        cbClient.setPromptText("Client");

        TextField txtClientSearch = new TextField();
        txtClientSearch.setPromptText("Search by client username or product...");

        ComboBox<String> cbStatusFilter = new ComboBox<>();
        cbStatusFilter.getItems().addAll("All", "Unpaid", "Partial", "Paid");
        cbStatusFilter.setValue("All");

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
                txtProduct.clear();
                txtSize.clear();
                spQty.getValueFactory().setValue(1);
                txtOriginal.clear();
                txtSelling.clear();
                cbPaymentType.setValue("On Delivery");
                txtDepositAmount.clear();
                txtNotes.clear();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });
        Button btnOpenForm = new Button("New Order");
        btnOpenForm.setOnAction(e -> openOrderFormDialog(cbClient, cbShipment, cbDelivery, txtProduct, txtSize,
            spQty, txtOriginal, txtSelling, cbPaymentType, txtDepositAmount, txtNotes, btnAdd));

        txtClientSearch.textProperty().addListener((obs, oldVal, newVal) -> applyOrderFilters(table, txtClientSearch.getText(), cbStatusFilter.getValue()));
        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyOrderFilters(table, txtClientSearch.getText(), newVal));

        HBox topBar = new HBox(5, new Label("Search:"), txtClientSearch, new Label("Status:"), cbStatusFilter, btnOpenForm);
        topBar.setPadding(new Insets(10));
        HBox.setHgrow(txtClientSearch, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(table);

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

    private double computeRemainingForOrder(Order order) {
        try {
            double totalPaid = paymentDAO.getTotalPaidForOrder(order.getOrderId());
            return Math.max(0, order.getSellingPrice() - totalPaid);
        } catch (SQLException e) {
            // On error, just show full selling price as remaining
            return order.getSellingPrice();
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

    private void showClientInfoPopup(int clientId) {
        Client client = clientData.stream().filter(c -> c.getClientId() == clientId).findFirst().orElse(null);
        if (client == null) {
            showError("Client not found.");
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Client Info");

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Username:"), new Label(client.getUsername()));
        grid.addRow(1, new Label("Phone:"), new Label(client.getPhone()));
        grid.addRow(2, new Label("Source:"), new Label(client.getSource()));
        grid.addRow(3, new Label("Address:"), new Label(client.getAddress()));

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void openAddPaymentDialog(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Payment for Order " + order.getOrderId());

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Amount");
        ComboBox<String> cbMethod = new ComboBox<>();
        cbMethod.getItems().addAll("cash", "card", "post");
        cbMethod.setValue("cash");
        TextField txtComment = new TextField();
        txtComment.setPromptText("Notes");

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(5);
        form.setVgap(5);
        form.add(new Label("Amount:"), 0, 0);
        form.add(txtAmount, 1, 0);
        form.add(new Label("Method:"), 0, 1);
        form.add(cbMethod, 1, 1);
        form.add(new Label("Notes:"), 0, 2);
        form.add(txtComment, 1, 2);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Payment p = new Payment();
                p.setOrderId(order.getOrderId());
                try {
                    p.setAmount(Double.parseDouble(txtAmount.getText()));
                } catch (NumberFormatException ex) {
                    showError("Invalid amount.");
                    return null;
                }
                p.setPaymentMethod(cbMethod.getValue());
                p.setComment(txtComment.getText());
                try {
                    paymentDAO.insert(p);

                    // update payment status based on total paid vs selling price
                    double totalPaid = paymentDAO.getTotalPaidForOrder(order.getOrderId());
                    double selling = order.getSellingPrice();
                    String newStatus;
                    if (totalPaid <= 0) {
                        newStatus = "Unpaid";
                    } else if (totalPaid < selling) {
                        newStatus = "Partial";
                    } else {
                        newStatus = "Paid";
                    }
                    orderDAO.updatePaymentStatus(order.getOrderId(), newStatus);
                    order.setPaymentStatus(newStatus);

                    loadOrders();
                } catch (SQLException e) {
                    showError(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void openOrderFormDialog(ComboBox<Client> cbClient,
                                     ComboBox<Shipment> cbShipment,
                                     ComboBox<DeliveryOption> cbDelivery,
                                     TextField txtProduct,
                                     TextField txtSize,
                                     Spinner<Integer> spQty,
                                     TextField txtOriginal,
                                     TextField txtSelling,
                                     ComboBox<String> cbPaymentType,
                                     TextField txtDepositAmount,
                                     TextField txtNotes,
                                     Button btnAdd) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Order");

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(5);
        form.setVgap(5);
        form.add(new Label("Client:"), 0, 0);
        form.add(cbClient, 1, 0);
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

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                btnAdd.fire();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void applyOrderFilters(TableView<Order> table, String searchText, String statusFilter) {
        String keyword = searchText == null ? "" : searchText.trim().toLowerCase();
        String status = statusFilter == null ? "All" : statusFilter;

        table.setItems(orderData.filtered(o -> {
            boolean matchesSearch;
            Client client = clientData.stream().filter(c -> c.getClientId() == o.getClientId()).findFirst().orElse(null);
            String clientName = client != null ? client.getUsername() : "";
            String combined = (clientName + " " + o.getProductLink()).toLowerCase();
            matchesSearch = keyword.isEmpty() || combined.contains(keyword);

            boolean matchesStatus;
            if ("All".equals(status)) {
                matchesStatus = true;
            } else {
                matchesStatus = status.equalsIgnoreCase(o.getPaymentStatus());
            }

            return matchesSearch && matchesStatus;
        }));
    }
}
