package ui.dialog.order;

import service.api.IOrderService;
import service.api.IPaymentService;
import model.*;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Handles order saving logic and validation.
 * Separates business logic from UI.
 */
public class OrderSaveHandler {

    private final IOrderService orderService;
    private final IPaymentService paymentService;

    public interface OrderCallback {
        void onComplete();
    }

    public OrderSaveHandler(IOrderService orderService, IPaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public void saveEditOrder(Order order, Client client, Shipment shipment, DeliveryOption delivery,
                              String product, String size, int quantity,
                              String originalText, String sellingText, String paymentType,
                              String platform, String notes, OrderCallback onSuccess) {
        if (client == null) {
            DialogUtils.showError("Please select a client.");
            return;
        }

        if (shipment == null) {
            DialogUtils.showError("Please select a shipment. Orders must be assigned to a shipment.");
            return;
        }

        try {
            order.setClientId(client.getClientId());
            order.setShipmentId(shipment.getShipmentId());
            order.setDeliveryOptionId(delivery != null ? delivery.getDeliveryOptionId() : null);
            order.setProductLink(product);
            order.setProductSize(size);
            order.setQuantity(quantity);

            double originalPrice = PriceCalculator.parsePrice(originalText);
            if (originalPrice <= 0) {
                DialogUtils.showError("Original price must be greater than 0.");
                return;
            }
            order.setOriginalPrice(originalPrice);

            double sellingPrice = sellingText != null && !sellingText.trim().isEmpty()
                ? PriceCalculator.parsePrice(sellingText)
                : PriceCalculator.calculateTotalSellingPrice(originalPrice, quantity);
            order.setSellingPrice(sellingPrice);

            order.setPaymentType(paymentType);
            order.setPlatform(Platform.fromString(platform));
            order.setNotes(notes);

            orderService.updateOrder(order);

            if (onSuccess != null) onSuccess.onComplete();

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Please enter valid numeric values for prices.");
        } catch (SQLException ex) {
            DialogUtils.showError("Database error: " + ex.getMessage());
        }
    }

    public void saveNewOrder(Client client, Shipment shipment, DeliveryOption delivery,
                             String product, String size, int quantity,
                             String originalText, String sellingText, String paymentType,
                             String platform, String depositText, String notes,
                             OrderCallback onSuccess, OrderCallback onPaymentCreated) {
        if (client == null) {
            DialogUtils.showError("Please select a client.");
            return;
        }

        if (shipment == null) {
            DialogUtils.showError("Please select a shipment. Orders must be assigned to a shipment.");
            return;
        }

        try {
            Order order = new Order();
            order.setClientId(client.getClientId());
            order.setShipmentId(shipment.getShipmentId());
            order.setDeliveryOptionId(delivery != null ? delivery.getDeliveryOptionId() : null);
            order.setProductLink(product);
            order.setProductSize(size);
            order.setQuantity(quantity);

            double originalPrice = PriceCalculator.parsePrice(originalText);
            if (originalPrice <= 0) {
                DialogUtils.showError("Original price must be greater than 0.");
                return;
            }
            order.setOriginalPrice(originalPrice);

            double sellingPrice = sellingText != null && !sellingText.trim().isEmpty()
                ? PriceCalculator.parsePrice(sellingText)
                : PriceCalculator.calculateTotalSellingPrice(originalPrice, quantity);
            order.setSellingPrice(sellingPrice);

            order.setPaymentType(paymentType);
            order.setPaymentStatus("Unpaid");
            order.setPlatform(Platform.fromString(platform));
            order.setNotes(notes);

            int newOrderId = orderService.addOrder(order);

            if ("Deposit".equals(paymentType) && depositText != null && !depositText.trim().isEmpty()) {
                double depositAmount = PriceCalculator.parsePriceOrDefault(depositText, 0);
                if (depositAmount > 0) {
                    createPayment(newOrderId, depositAmount, "Deposit", "Initial deposit payment");
                    orderService.updatePaymentStatus(newOrderId, "Partial");
                    if (onPaymentCreated != null) onPaymentCreated.onComplete();
                }
            } else if ("Full".equals(paymentType) && sellingPrice > 0) {
                createPayment(newOrderId, sellingPrice, "Full Payment", "Full payment received");
                orderService.updatePaymentStatus(newOrderId, "Paid");
                if (onPaymentCreated != null) onPaymentCreated.onComplete();
            }

            if (onSuccess != null) onSuccess.onComplete();

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Please enter valid numeric values for prices.");
        } catch (SQLException ex) {
            DialogUtils.showError("Database error: " + ex.getMessage());
        }
    }

    private void createPayment(int orderId, double amount, String method, String comment) throws SQLException {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setComment(comment);
        paymentService.addPayment(payment);
    }
}
