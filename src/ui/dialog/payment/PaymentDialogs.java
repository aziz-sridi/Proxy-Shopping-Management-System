package ui.dialog.payment;

import service.api.IOrderService;
import service.api.IPaymentService;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Order;
import model.Payment;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Main entry point for payment-related dialogs.
 * Delegates form building and processing to specialized classes.
 */
public class PaymentDialogs {

    private final IPaymentService paymentService;
    private final IOrderService orderService;
    private final PaymentProcessor processor;

    public PaymentDialogs(IPaymentService paymentService, IOrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.processor = new PaymentProcessor(paymentService, orderService);
    }

    public void openAddPaymentForOrderDialog(Order order, PaymentProcessor.PaymentCallback onSuccess, PaymentProcessor.PaymentCallback onOrderUpdate) {
        double remaining;
        try {
            double totalPaid = paymentService.getTotalPaidForOrder(order.getOrderId());
            remaining = PriceCalculator.calculateRemaining(order.getSellingPrice(), totalPaid);
            if (remaining <= 0) {
                DialogUtils.showError("This order is already fully paid");
                return;
            }
        } catch (SQLException e) {
            DialogUtils.showError("Error calculating remaining amount: " + e.getMessage());
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Payment for Order " + order.getOrderId());

        // Use PaymentFormBuilder components
        PaymentFormBuilder.PaymentFormComponents components = PaymentFormBuilder.buildAddPaymentForOrderComponents(order, remaining);
        GridPane form = PaymentFormBuilder.layoutAddPaymentForOrderForm(components);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double maxAmount = remaining;
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                processor.processAddPayment(order.getOrderId(), order.getSellingPrice(), components.txtAmount.getText(),
                    components.cbMethod.getValue(), components.txtComment.getText(), maxAmount, onSuccess, onOrderUpdate);
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void openAddPaymentDialog(PaymentProcessor.PaymentCallback onSuccess, PaymentProcessor.PaymentCallback onOrderUpdate) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add New Payment");

        GridPane form;
        try {
            form = PaymentFormBuilder.buildGenericPaymentForm(orderService, paymentService);
        } catch (SQLException e) {
            DialogUtils.showError("Error loading form: " + e.getMessage());
            return;
        }

        TextField txtOrderId = (TextField) form.getChildren().get(1);
        TextField txtAmount = (TextField) form.getChildren().get(5);
    
        ComboBox<String> cbMethod = (ComboBox<String>) form.getChildren().get(9);
        TextField txtComment = (TextField) form.getChildren().get(13);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                processor.processAddPaymentWithOrderId(txtOrderId.getText(), txtAmount.getText(),
                    cbMethod.getValue(), txtComment.getText(), onSuccess, onOrderUpdate);
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void openEditPaymentDialog(Payment payment, PaymentProcessor.PaymentCallback onSuccess, PaymentProcessor.PaymentCallback onOrderUpdate) {
        double maxAmount;
        try {
            Order order = orderService.getOrderById(payment.getOrderId());
            if (order != null) {
                double totalPaid = paymentService.getTotalPaidForOrder(payment.getOrderId());
                maxAmount = PriceCalculator.calculateRemaining(order.getSellingPrice(), totalPaid) + payment.getAmount();
            } else {
                DialogUtils.showError("Order not found");
                return;
            }
        } catch (SQLException e) {
            DialogUtils.showError("Error loading order: " + e.getMessage());
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Payment");

        GridPane form = PaymentFormBuilder.buildEditPaymentForm(payment, maxAmount);
        TextField txtAmount = (TextField) form.getChildren().get(5);
   
        ComboBox<String> cbMethod = (ComboBox<String>) form.getChildren().get(9);
        TextField txtComment = (TextField) form.getChildren().get(13);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double finalMaxAmount = maxAmount;
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                processor.processEditPayment(payment, txtAmount.getText(), cbMethod.getValue(),
                    txtComment.getText(), finalMaxAmount, onSuccess, onOrderUpdate);
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void deletePayment(Payment payment, PaymentProcessor.PaymentCallback onSuccess, PaymentProcessor.PaymentCallback onOrderUpdate) {
        processor.processDeletePayment(payment, onSuccess, onOrderUpdate);
    }
}
