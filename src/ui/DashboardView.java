package ui;

import dao.OrderDAO;
import dao.PaymentDAO;
import dao.ProfitDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.Order;

import java.sql.SQLException;

public class DashboardView {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ProfitDAO profitDAO = new ProfitDAO();

    private final ObservableList<Order> orderData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Order> table = new TableView<>();

        TableColumn<Order, Number> colId = new TableColumn<>("Order ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrderId()));

        TableColumn<Order, Number> colSelling = new TableColumn<>("Selling");
        colSelling.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getSellingPrice()));

        TableColumn<Order, String> colStatus = new TableColumn<>("Payment Status");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentStatus()));

        table.getColumns().addAll(colId, colSelling, colStatus);
        table.setItems(orderData);

        Label lblTotalProfit = new Label("Total profit (dummy for now)");

        VBox box = new VBox(10, lblTotalProfit, table);
        box.setPadding(new Insets(10));

        BorderPane root = new BorderPane(box);

        loadOrders();
        return root;
    }

    private void loadOrders() {
        orderData.clear();
        try {
            orderData.addAll(orderDAO.findAll());
        } catch (SQLException e) {
            // simple dashboard, ignore for now
        }
    }
}
