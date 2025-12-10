package ui.dialog.payment;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Order;
import model.Payment;
import service.api.IOrderService;
import service.api.IPaymentService;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Builds UI components for payment form dialogs.
 * Separates form construction from business logic.
 */
public class PaymentFormBuilder {
    /**
     * Container for payment form components
     */
    public static class PaymentFormComponents {
        public TextField txtAmount;
        public ComboBox<String> cbMethod;
        public TextField txtComment;
        public Label lblRemaining;
        public PaymentFormComponents() {}
    }

    public static PaymentFormComponents buildAddPaymentForOrderComponents(Order order, double remaining) {
        PaymentFormComponents components = new PaymentFormComponents();
        components.txtAmount = new TextField();
        components.txtAmount.setPromptText("Amount (max: " + PriceCalculator.formatPrice(remaining) + ")");
        components.lblRemaining = DialogUtils.createInfoLabel(
            String.format("Remaining: %s TND", PriceCalculator.formatPrice(remaining)));
        components.cbMethod = createPaymentMethodComboBox();
        components.txtComment = new TextField();
        components.txtComment.setPromptText("Notes");
        return components;
    }

    public static GridPane layoutAddPaymentForOrderForm(PaymentFormComponents components) {
        GridPane form = DialogUtils.createFormGrid(5, 5, 10);
        form.add(new Label("Amount:"), 0, 0);
        form.add(components.txtAmount, 1, 0);
        form.add(components.lblRemaining, 2, 0);
        form.add(new Label("Method:"), 0, 1);
        form.add(components.cbMethod, 1, 1);
        form.add(new Label("Notes:"), 0, 2);
        form.add(components.txtComment, 1, 2);
        return form;
    }

    public static ComboBox<String> createPaymentMethodComboBox() {
        ComboBox<String> cbMethod = new ComboBox<>();
        cbMethod.getItems().addAll("cash", "card", "post");
        cbMethod.setValue("cash");
        return cbMethod;
    }

    public static GridPane buildAddPaymentForOrderForm(Order order, double remaining) {
        GridPane form = DialogUtils.createFormGrid(5, 5, 10);

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Amount (max: " + PriceCalculator.formatPrice(remaining) + ")");

        Label lblRemaining = DialogUtils.createInfoLabel(
            String.format("Remaining: %s TND", PriceCalculator.formatPrice(remaining)));

        ComboBox<String> cbMethod = createPaymentMethodComboBox();

        TextField txtComment = new TextField();
        txtComment.setPromptText("Notes");

        form.add(new Label("Amount:"), 0, 0);
        form.add(txtAmount, 1, 0);
        form.add(lblRemaining, 2, 0);
        form.add(new Label("Method:"), 0, 1);
        form.add(cbMethod, 1, 1);
        form.add(new Label("Notes:"), 0, 2);
        form.add(txtComment, 1, 2);

        return form;
    }

    public static GridPane buildGenericPaymentForm(IOrderService orderService, IPaymentService paymentService) throws SQLException {
        GridPane form = DialogUtils.createFormGrid(8, 8, 10);

        TextField txtOrderId = new TextField();
        txtOrderId.setPromptText("Order ID");

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Amount");

        Label lblRemaining = DialogUtils.createInfoLabel("");

        txtOrderId.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRemainingLabel(newVal, lblRemaining, orderService, paymentService);
        });

        ComboBox<String> cbMethod = createPaymentMethodComboBox();
        cbMethod.getItems().addAll("Deposit", "Full Payment");

        TextField txtComment = new TextField();
        txtComment.setPromptText("Comment (optional)");

        form.add(new Label("Order ID:"), 0, 0);
        form.add(txtOrderId, 1, 0);
        form.add(lblRemaining, 2, 0);
        form.add(new Label("Amount:"), 0, 1);
        form.add(txtAmount, 1, 1);
        form.add(new Label("Method:"), 0, 2);
        form.add(cbMethod, 1, 2);
        form.add(new Label("Comment:"), 0, 3);
        form.add(txtComment, 1, 3);

        return form;
    }

    public static GridPane buildEditPaymentForm(Payment payment, double maxAmount) {
        GridPane form = DialogUtils.createFormGrid(8, 8, 10);

        TextField txtOrderId = new TextField(String.valueOf(payment.getOrderId()));
        txtOrderId.setEditable(false);
        txtOrderId.setStyle("-fx-background-color: #f0f0f0;");

        TextField txtAmount = new TextField(String.valueOf(payment.getAmount()));

        Label lblRemaining = DialogUtils.createInfoLabel(
            String.format("Max: %s TND", PriceCalculator.formatPrice(maxAmount)));

        ComboBox<String> cbMethod = createPaymentMethodComboBox();
        cbMethod.getItems().addAll("Deposit", "Full Payment");
        cbMethod.setValue(payment.getPaymentMethod());
        cbMethod.setEditable(true);

        TextField txtComment = new TextField(payment.getComment());

        form.add(new Label("Order ID:"), 0, 0);
        form.add(txtOrderId, 1, 0);
        form.add(new Label("Amount:"), 0, 1);
        form.add(txtAmount, 1, 1);
        form.add(lblRemaining, 2, 1);
        form.add(new Label("Method:"), 0, 2);
        form.add(cbMethod, 1, 2);
        form.add(new Label("Comment:"), 0, 3);
        form.add(txtComment, 1, 3);

        return form;
    }

    private static void updateRemainingLabel(String orderIdText, Label lblRemaining,
                                             IOrderService orderService, IPaymentService paymentService) {
        if (orderIdText == null || orderIdText.trim().isEmpty()) {
            lblRemaining.setText("");
            return;
        }
        try {
            int orderId = Integer.parseInt(orderIdText.trim());
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                double totalPaid = paymentService.getTotalPaidForOrder(orderId);
                double remaining = PriceCalculator.calculateRemaining(order.getSellingPrice(), totalPaid);
                if (remaining <= 0) {
                    lblRemaining.setText("Order fully paid!");
                    lblRemaining.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 11px; -fx-font-weight: bold;");
                } else {
                    lblRemaining.setText(String.format("Remaining: %s TND", PriceCalculator.formatPrice(remaining)));
                    lblRemaining.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
                }
            } else {
                lblRemaining.setText("Order not found");
                lblRemaining.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 11px;");
            }
        } catch (NumberFormatException e) {
            lblRemaining.setText("");
        } catch (SQLException e) {
            lblRemaining.setText("Error loading order");
        }
    }
}
