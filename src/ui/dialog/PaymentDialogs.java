package ui.dialog;

import service.IOrderService;
import service.IPaymentService;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Order;
import model.Payment;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Dialog helper class for payment-related dialogs.
 * Extracts dialog logic from PaymentsViewController and OrdersViewController.
 */
public class PaymentDialogs {

    private final IPaymentService paymentService;
    private final IOrderService orderService;

    public PaymentDialogs(IPaymentService paymentService, IOrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    /**
     * Interface for callbacks when payment operations complete
     */
    @FunctionalInterface
    public interface PaymentCallback {
        void onComplete();
    }

    /**
     * Open dialog to add payment for a specific order.
     */
    public void openAddPaymentForOrderDialog(Order order, PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        // Calculate remaining amount
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

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Amount (max: " + PriceCalculator.formatPrice(remaining) + ")");

        Label lblRemaining = DialogUtils.createInfoLabel(
            String.format("Remaining: %s TND", PriceCalculator.formatPrice(remaining)));

        ComboBox<String> cbMethod = createPaymentMethodComboBox();

        TextField txtComment = new TextField();
        txtComment.setPromptText("Notes");

        GridPane form = DialogUtils.createFormGrid(5, 5, 10);
        form.add(new Label("Amount:"), 0, 0);
        form.add(txtAmount, 1, 0);
        form.add(lblRemaining, 2, 0);
        form.add(new Label("Method:"), 0, 1);
        form.add(cbMethod, 1, 1);
        form.add(new Label("Notes:"), 0, 2);
        form.add(txtComment, 1, 2);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double maxAmount = remaining;
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                processAddPayment(order.getOrderId(), order.getSellingPrice(), txtAmount.getText(), 
                    cbMethod.getValue(), txtComment.getText(), maxAmount, onSuccess, onOrderUpdate);
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Open dialog to add a new payment (standalone, without pre-selected order).
     */
    public void openAddPaymentDialog(PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add New Payment");

        TextField txtOrderId = new TextField();
        txtOrderId.setPromptText("Order ID");
        
        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Amount");
        
        Label lblRemaining = DialogUtils.createInfoLabel("");
        
        // Update remaining amount when order ID changes
        txtOrderId.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRemainingLabel(newVal, lblRemaining);
        });
        
        ComboBox<String> cbMethod = createPaymentMethodComboBox();
        cbMethod.getItems().addAll("Deposit", "Full Payment");
        
        TextField txtComment = new TextField();
        txtComment.setPromptText("Comment (optional)");

        GridPane form = DialogUtils.createFormGrid(8, 8, 10);
        form.add(new Label("Order ID:"), 0, 0);
        form.add(txtOrderId, 1, 0);
        form.add(lblRemaining, 2, 0);
        form.add(new Label("Amount:"), 0, 1);
        form.add(txtAmount, 1, 1);
        form.add(new Label("Method:"), 0, 2);
        form.add(cbMethod, 1, 2);
        form.add(new Label("Comment:"), 0, 3);
        form.add(txtComment, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                processAddPaymentWithOrderId(txtOrderId.getText(), txtAmount.getText(), 
                    cbMethod.getValue(), txtComment.getText(), onSuccess, onOrderUpdate);
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Open dialog to edit an existing payment.
     */
    public void openEditPaymentDialog(Payment payment, PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        // Get order to calculate remaining amount
        double maxAmount;
        try {
            Order order = orderService.getOrderById(payment.getOrderId());
            if (order != null) {
                double totalPaid = paymentService.getTotalPaidForOrder(payment.getOrderId());
                // Add back the current payment amount since we're editing it
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

        GridPane form = DialogUtils.createFormGrid(8, 8, 10);
        form.add(new Label("Order ID:"), 0, 0);
        form.add(txtOrderId, 1, 0);
        form.add(new Label("Amount:"), 0, 1);
        form.add(txtAmount, 1, 1);
        form.add(lblRemaining, 2, 1);
        form.add(new Label("Method:"), 0, 2);
        form.add(cbMethod, 1, 2);
        form.add(new Label("Comment:"), 0, 3);
        form.add(txtComment, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double finalMaxAmount = maxAmount;
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                processEditPayment(payment, txtAmount.getText(), cbMethod.getValue(), 
                    txtComment.getText(), finalMaxAmount, onSuccess, onOrderUpdate);
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show delete confirmation and delete payment if confirmed.
     */
    public void deletePayment(Payment payment, PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        if (DialogUtils.showConfirmation("Delete selected payment?")) {
            try {
                paymentService.deletePayment(payment.getPaymentId(), payment.getOrderId());
                
                // Recalculate and update payment status for the order
                Order order = orderService.getOrderById(payment.getOrderId());
                if (order != null) {
                    double totalPaid = paymentService.getTotalPaidForOrder(payment.getOrderId());
                    String newStatus = PriceCalculator.determinePaymentStatus(order.getSellingPrice(), totalPaid);
                    orderService.updatePaymentStatus(payment.getOrderId(), newStatus);
                }
                
                if (onSuccess != null) onSuccess.onComplete();
                if (onOrderUpdate != null) onOrderUpdate.onComplete();
            } catch (SQLException ex) {
                DialogUtils.showError(ex.getMessage());
            }
        }
    }

    // ============ Private Helper Methods ============

    private ComboBox<String> createPaymentMethodComboBox() {
        ComboBox<String> cbMethod = new ComboBox<>();
        cbMethod.getItems().addAll("cash", "card", "post");
        cbMethod.setValue("cash");
        return cbMethod;
    }

    private double validatePaymentAmount(String amountText, double maxAmount) {
        try {
            double amount = PriceCalculator.parsePrice(amountText);
            if (amount <= 0) {
                DialogUtils.showError("Amount must be positive");
                return -1;
            }
            if (amount > maxAmount) {
                DialogUtils.showError(String.format("Amount cannot exceed remaining amount (%s TND)", 
                    PriceCalculator.formatPrice(maxAmount)));
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            DialogUtils.showError("Invalid amount format");
            return -1;
        }
    }

    private Payment createPayment(int orderId, double amount, String method, String comment) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setComment(comment);
        return payment;
    }

    private void updateOrderPaymentStatus(int orderId, double sellingPrice) throws SQLException {
        double totalPaid = paymentService.getTotalPaidForOrder(orderId);
        String newStatus = PriceCalculator.determinePaymentStatus(sellingPrice, totalPaid);
        orderService.updatePaymentStatus(orderId, newStatus);
    }

    private void executeCallbacks(PaymentCallback... callbacks) {
        for (PaymentCallback callback : callbacks) {
            if (callback != null) callback.onComplete();
        }
    }

    private void updateRemainingLabel(String orderIdText, Label lblRemaining) {
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

    private void processAddPayment(int orderId, double sellingPrice, String amountText, 
                                    String method, String comment, double maxAmount,
                                    PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        try {
            double amount = validatePaymentAmount(amountText, maxAmount);
            if (amount < 0) return; // Validation failed
            
            Payment payment = createPayment(orderId, amount, method, comment);
            paymentService.addPayment(payment);

            // Update payment status
            updateOrderPaymentStatus(orderId, sellingPrice);

            executeCallbacks(onSuccess, onOrderUpdate);
            
        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid amount.");
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    private void processAddPaymentWithOrderId(String orderIdText, String amountText, 
                                               String method, String comment,
                                               PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        try {
            int orderId = Integer.parseInt(orderIdText.trim());
            if (orderId <= 0) {
                DialogUtils.showError("Order ID must be positive");
                return;
            }
            
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                DialogUtils.showError("Order not found");
                return;
            }
            
            double totalPaid = paymentService.getTotalPaidForOrder(orderId);
            double remaining = PriceCalculator.calculateRemaining(order.getSellingPrice(), totalPaid);
            
            if (remaining <= 0) {
                DialogUtils.showError("This order is already fully paid");
                return;
            }
            
            double amount = validatePaymentAmount(amountText, remaining);
            if (amount < 0) return; // Validation failed
            
            Payment payment = createPayment(orderId, amount, method, comment);
            paymentService.addPayment(payment);
            executeCallbacks(onSuccess, onOrderUpdate);
            
        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid number format. Please enter valid Order ID and Amount.");
        } catch (Exception ex) {
            DialogUtils.showError("Error adding payment: " + ex.getMessage());
        }
    }

    private void processEditPayment(Payment payment, String amountText, String method, 
                                     String comment, double maxAmount,
                                     PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        try {
            double amount = validatePaymentAmount(amountText, maxAmount);
            if (amount < 0) return; // Validation failed
            
            payment.setAmount(amount);
            payment.setPaymentMethod(method);
            payment.setComment(comment);
            paymentService.updatePayment(payment);
            
            // Recalculate and update payment status for the order
            Order order = orderService.getOrderById(payment.getOrderId());
            if (order != null) {
                updateOrderPaymentStatus(payment.getOrderId(), order.getSellingPrice());
            }
            
            executeCallbacks(onSuccess, onOrderUpdate);
        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid amount format");
        } catch (Exception ex) {
            DialogUtils.showError("Error updating payment: " + ex.getMessage());
        }
    }
}
