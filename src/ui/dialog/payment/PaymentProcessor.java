package ui.dialog.payment;

import service.api.IOrderService;
import service.api.IPaymentService;
import model.Order;
import model.Payment;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Handles payment processing logic and validation.
 * Separates business logic from UI.
 */
public class PaymentProcessor {

    private final IPaymentService paymentService;
    private final IOrderService orderService;

    public interface PaymentCallback {
        void onComplete();
    }

    public PaymentProcessor(IPaymentService paymentService, IOrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    public double validatePaymentAmount(String amountText, double maxAmount) {
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

    public Payment createPayment(int orderId, double amount, String method, String comment) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setComment(comment);
        return payment;
    }

    public void updateOrderPaymentStatus(int orderId, double sellingPrice) throws SQLException {
        double totalPaid = paymentService.getTotalPaidForOrder(orderId);
        String newStatus = PriceCalculator.determinePaymentStatus(sellingPrice, totalPaid);
        orderService.updatePaymentStatus(orderId, newStatus);
    }

    public void executeCallbacks(PaymentCallback... callbacks) {
        for (PaymentCallback callback : callbacks) {
            if (callback != null) callback.onComplete();
        }
    }

    public void processAddPayment(int orderId, double sellingPrice, String amountText,
                                  String method, String comment, double maxAmount,
                                  PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        try {
            double amount = validatePaymentAmount(amountText, maxAmount);
            if (amount < 0) return;

            Payment payment = createPayment(orderId, amount, method, comment);
            paymentService.addPayment(payment);
            updateOrderPaymentStatus(orderId, sellingPrice);

            executeCallbacks(onSuccess, onOrderUpdate);

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid amount.");
        } catch (SQLException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    public void processAddPaymentWithOrderId(String orderIdText, String amountText,
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

            processAddPayment(orderId, order.getSellingPrice(), amountText, method, comment,
                remaining, onSuccess, onOrderUpdate);

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid order ID or amount.");
        } catch (SQLException ex) {
            DialogUtils.showError(ex.getMessage());
        }
    }

    public void processEditPayment(Payment payment, String amountText, String method,
                                   String comment, double maxAmount,
                                   PaymentCallback onSuccess, PaymentCallback onOrderUpdate) {
        try {
            double amount = validatePaymentAmount(amountText, maxAmount);
            if (amount < 0) return;

            payment.setAmount(amount);
            payment.setPaymentMethod(method);
            payment.setComment(comment);

            paymentService.updatePayment(payment);

            Order order = orderService.getOrderById(payment.getOrderId());
            if (order != null) {
                updateOrderPaymentStatus(payment.getOrderId(), order.getSellingPrice());
            }

            executeCallbacks(onSuccess, onOrderUpdate);

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Invalid amount.");
        } catch (SQLException ex) {
            DialogUtils.showError(ex.getMessage());
        }
    }

    public void processDeletePayment(Payment payment, PaymentCallback onSuccess,
                                     PaymentCallback onOrderUpdate) {
        if (DialogUtils.showConfirmation("Delete selected payment?")) {
            try {
                paymentService.deletePayment(payment.getPaymentId(), payment.getOrderId());

                Order order = orderService.getOrderById(payment.getOrderId());
                if (order != null) {
                    double totalPaid = paymentService.getTotalPaidForOrder(payment.getOrderId());
                    String newStatus = PriceCalculator.determinePaymentStatus(order.getSellingPrice(), totalPaid);
                    orderService.updatePaymentStatus(payment.getOrderId(), newStatus);
                }

                executeCallbacks(onSuccess, onOrderUpdate);

            } catch (SQLException ex) {
                DialogUtils.showError(ex.getMessage());
            }
        }
    }
}
