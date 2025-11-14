package ui;

import dao.OrderDAO;
import dao.PaymentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import model.Order;
import model.Payment;

import java.sql.SQLException;

public class PaymentsView {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<Order> orderData = FXCollections.observableArrayList();
    private final ObservableList<Payment> paymentData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Order> ordersTable = new TableView<>();

        TableColumn<Order, Number> colId = new TableColumn<>("Order ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrderId()));

        TableColumn<Order, Number> colSell = new TableColumn<>("Selling");
        colSell.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus()));

        ordersTable.getColumns().addAll(colId, colSell, colStatus);
        ordersTable.setItems(orderData);

        TableView<Payment> paymentsTable = new TableView<>();

        TableColumn<Payment, Number> colPayId = new TableColumn<>("ID");
        colPayId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPaymentId()));

        TableColumn<Payment, Number> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getAmount()));

        TableColumn<Payment, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentMethod()));

        paymentsTable.getColumns().addAll(colPayId, colAmount, colMethod);
        paymentsTable.setItems(paymentData);

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Amount");

        TextField txtMethod = new TextField();
        txtMethod.setPromptText("Method");

        TextField txtComment = new TextField();
        txtComment.setPromptText("Comment");

        Button btnAdd = new Button("Add Payment");
        btnAdd.setOnAction(e -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                showError("Select an order first.");
                return;
            }
            Payment p = new Payment();
            p.setOrderId(selectedOrder.getOrderId());
            try {
                p.setAmount(Double.parseDouble(txtAmount.getText()));
            } catch (NumberFormatException ex) {
                showError("Invalid amount.");
                return;
            }
            p.setPaymentMethod(txtMethod.getText());
            p.setComment(txtComment.getText());
            try {
                paymentDAO.insert(p);
                refreshPaymentsForOrder(selectedOrder);
                refreshOrders();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, cur) -> {
            if (cur != null) {
                refreshPaymentsForOrder(cur);
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(5);
        form.setVgap(5);
        form.add(new Label("Amount:"), 0, 0);
        form.add(txtAmount, 1, 0);
        form.add(new Label("Method:"), 0, 1);
        form.add(txtMethod, 1, 1);
        form.add(new Label("Comment:"), 0, 2);
        form.add(txtComment, 1, 2);
        form.add(btnAdd, 1, 3);

        SplitPane split = new SplitPane(ordersTable, paymentsTable);
        split.setDividerPositions(0.5);

        BorderPane root = new BorderPane();
        root.setCenter(split);
        root.setBottom(form);

        refreshOrders();
        return root;
    }

    private void refreshOrders() {
        orderData.clear();
        try {
            orderData.addAll(orderDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void refreshPaymentsForOrder(Order order) {
        paymentData.clear();
        try {
            paymentData.addAll(paymentDAO.findByOrder(order.getOrderId()));
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
