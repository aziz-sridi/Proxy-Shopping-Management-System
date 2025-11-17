package ui;

import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Client;
import model.Order;
import model.Payment;

import java.sql.SQLException;

public class ClientHistoryView {

    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<Order> orderData = FXCollections.observableArrayList();
    private final ObservableList<Payment> paymentData = FXCollections.observableArrayList();

    public BorderPane getView(Client client, Runnable onBack) {
        Label header = new Label("History for " + client.getUsername());
        header.getStyleClass().add("page-title");

        // Orders table
        TableView<Order> ordersTable = new TableView<>(orderData);
        TableColumn<Order, Number> colOrderId = new TableColumn<>("Order #");
        colOrderId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrderId()));
        TableColumn<Order, String> colProduct = new TableColumn<>("Product");
        colProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductLink()));
        TableColumn<Order, Number> colTotal = new TableColumn<>("Selling (TND)");
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSellingPrice()));
        ordersTable.getColumns().clear();
        ordersTable.getColumns().add(colOrderId);
        ordersTable.getColumns().add(colProduct);
        ordersTable.getColumns().add(colTotal);

        // Payments table
        TableView<Payment> paymentsTable = new TableView<>(paymentData);
        TableColumn<Payment, Number> colPayId = new TableColumn<>("Payment #");
        colPayId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPaymentId()));
        TableColumn<Payment, Number> colAmount = new TableColumn<>("Amount (TND)");
        colAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getAmount()));
        TableColumn<Payment, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentMethod()));
        paymentsTable.getColumns().clear();
        paymentsTable.getColumns().add(colPayId);
        paymentsTable.getColumns().add(colAmount);
        paymentsTable.getColumns().add(colMethod);

        VBox content = new VBox(10, header, new Label("Orders"), ordersTable, new Label("Payments"), paymentsTable);
        content.setPadding(new Insets(16));

        BorderPane root = new BorderPane();
        root.setCenter(content);

        loadData(client);

        return root;
    }

    private void loadData(Client client) {
        orderData.clear();
        paymentData.clear();
        try {
            orderData.addAll(orderDAO.findAll());
        } catch (SQLException e) {
            // ignore for now
        }
    }
}
