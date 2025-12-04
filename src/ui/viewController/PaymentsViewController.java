package ui.viewController;

import service.OrderService;
import service.PaymentService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Payment;
import ui.dialog.PaymentDialogs;
import ui.util.DialogUtils;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * ViewController for PaymentsView - handles all payment-related UI interactions.
 * Refactored to use PaymentDialogs helper class for better maintainability.
 */
public class PaymentsViewController {

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TextField txtSearch;
    @FXML private Button btnAddPayment;
    @FXML private TableColumn<Payment, Number> colPayId;
    @FXML private TableColumn<Payment, Number> colOrderId;
    @FXML private TableColumn<Payment, Number> colAmount;
    @FXML private TableColumn<Payment, String> colDate;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, String> colComment;
    @FXML private TableColumn<Payment, Void> colActions;

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final PaymentDialogs paymentDialogs;
    private final ObservableList<Payment> paymentData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Callback for refreshing orders view when payments are edited/deleted
    private Runnable orderRefreshCallback;

    public PaymentsViewController() {
        this.paymentService = new PaymentService();
        this.orderService = new OrderService();
        this.paymentDialogs = new PaymentDialogs(paymentService, orderService);
    }

    /**
     * Set callback for refreshing orders view when payments are modified
     */
    public void setOrderRefreshCallback(Runnable callback) {
        this.orderRefreshCallback = callback;
    }

    private void notifyOrderRefresh() {
        if (orderRefreshCallback != null) {
            orderRefreshCallback.run();
        }
    }

    @FXML
    public void initialize() {
        setupColumns();
        paymentsTable.setItems(paymentData);
        setupSearchFilter();
        setupAddPaymentButton();
        loadPayments();
    }

    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((obs, old, cur) -> {
            String q = cur == null ? "" : cur.toLowerCase();
            paymentsTable.setItems(paymentData.filtered(p -> {
                if (q.isEmpty()) return true;
                String combined = ("" + p.getPaymentMethod() + " " + p.getComment() + " " + p.getOrderId()).toLowerCase();
                return combined.contains(q);
            }));
        });
    }

    private void setupAddPaymentButton() {
        if (btnAddPayment != null) {
            btnAddPayment.setOnAction(e -> openAddPaymentDialog());
        }
    }

    /**
     * Public method to refresh data - can be called from other views
     */
    public void refreshData() {
        loadPayments();
    }

    private void setupColumns() {
        colPayId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPaymentId()));
        colOrderId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOrderId()));
        colAmount.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAmount()));
        colDate.setCellValueFactory(c -> {
            if (c.getValue().getPaymentDate() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(c.getValue().getPaymentDate().format(dateFormatter));
        });
        colMethod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod()));
        colComment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComment()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("modern-button", "button-secondary");
                btnDelete.getStyleClass().addAll("modern-button", "button-error");

                btnEdit.setOnAction(e -> {
                    Payment p = getTableView().getItems().get(getIndex());
                    openEditPaymentDialog(p);
                });
                btnDelete.setOnAction(e -> {
                    Payment p = getTableView().getItems().get(getIndex());
                    deletePayment(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadPayments() {
        paymentData.clear();
        try {
            paymentData.addAll(paymentService.getAllPayments());
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    private void deletePayment(Payment selected) {
        paymentDialogs.deletePayment(
            selected,
            this::loadPayments,
            this::notifyOrderRefresh
        );
    }

    private void openEditPaymentDialog(Payment payment) {
        paymentDialogs.openEditPaymentDialog(
            payment,
            this::loadPayments,
            this::notifyOrderRefresh
        );
    }

    private void openAddPaymentDialog() {
        paymentDialogs.openAddPaymentDialog(
            this::loadPayments,
            this::notifyOrderRefresh
        );
    }
}
