package ui;

import dao.PaymentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import model.Payment;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class PaymentsView {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final ObservableList<Payment> paymentData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BorderPane getView() {
        BorderPane view = new BorderPane();
        view.getStyleClass().add("page-container");
        
        TableView<Payment> paymentsTable = new TableView<>();
        paymentsTable.getStyleClass().add("modern-table");
        paymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Payment, Number> colPayId = new TableColumn<>("ID");
        colPayId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPaymentId()));

        TableColumn<Payment, Number> colOrderId = new TableColumn<>("Order ID");
        colOrderId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrderId()));

        TableColumn<Payment, Number> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getAmount()));

        TableColumn<Payment, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c -> {
            if (c.getValue().getPaymentDate() == null) return new javafx.beans.property.SimpleStringProperty("");
            return new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentDate().format(dateFormatter));
        });

        TableColumn<Payment, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentMethod()));

        TableColumn<Payment, String> colComment = new TableColumn<>("Comment");
        colComment.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getComment()));

        TableColumn<Payment, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("modern-button", "button-secondary");
                btnDelete.getStyleClass().addAll("modern-button", "button-error");
                
                btnEdit.setOnAction(e -> {
                    Payment p = getTableView().getItems().get(getIndex());
                    openEditPaymentDialog(p, () -> loadPayments(), PaymentsView.this::showError);
                });
                btnDelete.setOnAction(e -> {
                    Payment p = getTableView().getItems().get(getIndex());
                    deletePayment(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        paymentsTable.getColumns().clear();
        paymentsTable.getColumns().add(colPayId);
        paymentsTable.getColumns().add(colOrderId);
        paymentsTable.getColumns().add(colAmount);
        paymentsTable.getColumns().add(colDate);
        paymentsTable.getColumns().add(colMethod);
        paymentsTable.getColumns().add(colComment);
        paymentsTable.getColumns().add(colActions);
        paymentsTable.setItems(paymentData);

        TextField txtSearch = new TextField();
        txtSearch.getStyleClass().add("modern-field");
        txtSearch.setPromptText("Search by method, comment or order ID...");

        txtSearch.textProperty().addListener((obs, old, cur) -> {
            String q = cur == null ? "" : cur.toLowerCase();
            paymentsTable.setItems(paymentData.filtered(p -> {
                if (q.isEmpty()) return true;
                String combined = ("" + p.getPaymentMethod() + " " + p.getComment() + " " + p.getOrderId()).toLowerCase();
                return combined.contains(q);
            }));
        });

        HBox searchBar = new HBox(10, new Label("Search:"), txtSearch);
        searchBar.getStyleClass().add("action-buttons");
        searchBar.setPadding(new Insets(10));
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        view.setTop(searchBar);
        view.setCenter(paymentsTable);

        loadPayments();
        return view;
    }

    private void loadPayments() {
        paymentData.clear();
        try {
            paymentData.addAll(paymentDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void deletePayment(Payment selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected payment?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    paymentDAO.delete(selected.getPaymentId());
                    loadPayments();
                } catch (SQLException ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    private void openEditPaymentDialog(Payment payment, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Payment");

        TextField txtOrderId = new TextField(String.valueOf(payment.getOrderId()));
        TextField txtAmount = new TextField(String.valueOf(payment.getAmount()));
        TextField txtMethod = new TextField(payment.getPaymentMethod());
        TextField txtComment = new TextField(payment.getComment());

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(8);
        form.setVgap(8);
        form.setHgap(8);
        form.setVgap(8);
        form.add(new Label("Order ID:"), 0, 0);
        form.add(txtOrderId, 1, 0);
        form.add(new Label("Amount:"), 0, 1);
        form.add(txtAmount, 1, 1);
        form.add(new Label("Method:"), 0, 2);
        form.add(txtMethod, 1, 2);
        form.add(new Label("Comment:"), 0, 3);
        form.add(txtComment, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    payment.setOrderId(Integer.parseInt(txtOrderId.getText()));
                    payment.setAmount(Double.parseDouble(txtAmount.getText()));
                } catch (NumberFormatException ex) {
                    if (onError != null) onError.accept("Invalid numeric values.");
                    return null;
                }
                payment.setPaymentMethod(txtMethod.getText());
                payment.setComment(txtComment.getText());
                try {
                    paymentDAO.update(payment);
                    if (onSuccess != null) onSuccess.run();
                } catch (SQLException e) {
                    if (onError != null) onError.accept(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
