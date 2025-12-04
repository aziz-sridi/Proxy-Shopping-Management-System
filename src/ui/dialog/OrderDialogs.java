package ui.dialog;

import service.OrderService;
import service.PaymentService;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.*;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Dialog helper class for order-related dialogs.
 * Extracts dialog logic from OrdersViewController to reduce file size and improve maintainability..
 */
public class OrderDialogs {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderDialogs(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * Interface for callbacks when order operations complete
     */
    @FunctionalInterface
    public interface OrderCallback {
        void onComplete();
    }

    /**
     * Open dialog to create a new order.
     */
    public void openNewOrderDialog(ObservableList<Client> clients, 
                                    ObservableList<Shipment> shipments,
                                    ObservableList<DeliveryOption> deliveryOptions,
                                    OrderCallback onSuccess,
                                    OrderCallback onPaymentCreated) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Order");

        GridPane form = DialogUtils.createFormGrid(5, 5, 10);

        // Form fields
        ComboBox<Client> cbClient = new ComboBox<>(clients);
        cbClient.getStyleClass().add("modern-field");
        cbClient.setPromptText("Select Client");

        ComboBox<Shipment> cbShipment = new ComboBox<>(shipments);
        cbShipment.getStyleClass().add("modern-field");
        cbShipment.setPromptText("Select Shipment (REQUIRED)");
        cbShipment.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        ComboBox<DeliveryOption> cbDelivery = new ComboBox<>(deliveryOptions);
        cbDelivery.getStyleClass().add("modern-field");
        cbDelivery.setPromptText("Delivery option (optional)");

        TextField txtProduct = DialogUtils.createStyledTextField("Product link");
        TextField txtSize = DialogUtils.createStyledTextField("Size");

        Spinner<Integer> spQty = new Spinner<>(1, 1000, 1);
        spQty.getStyleClass().add("modern-field");

        TextField txtOriginal = DialogUtils.createStyledTextField("Original price (EUR)");
        TextField txtSelling = DialogUtils.createReadOnlyTextField("Selling price (TND)", 
            "-fx-background-color: #f0f0f0; -fx-font-weight: bold;");

        ComboBox<String> cbPaymentType = new ComboBox<>();
        cbPaymentType.getStyleClass().add("modern-field");
        cbPaymentType.getItems().addAll("Deposit", "Full", "On Delivery");
        cbPaymentType.setValue("On Delivery");

        ComboBox<String> cbPlatform = new ComboBox<>();
        cbPlatform.getStyleClass().add("modern-field");
        cbPlatform.getItems().addAll(Platform.getDisplayNames());
        cbPlatform.setValue("Other");

        TextField txtDepositAmount = DialogUtils.createReadOnlyTextField("Deposit amount (TND)",
            "-fx-background-color: #fff3e0; -fx-font-weight: bold;");
        txtDepositAmount.setDisable(true);

        TextField txtNotes = DialogUtils.createStyledTextField("Notes");

        // Automatic price calculation using PriceCalculator
        Runnable calculatePrices = () -> {
            String originalText = txtOriginal.getText();
            if (originalText == null || originalText.trim().isEmpty()) {
                txtSelling.clear();
                txtDepositAmount.clear();
                return;
            }
            try {
                double originalPriceEUR = PriceCalculator.parsePrice(originalText);
                int quantity = spQty.getValue();

                double totalSellingPriceTND = PriceCalculator.calculateTotalSellingPrice(originalPriceEUR, quantity);
                txtSelling.setText(PriceCalculator.formatPrice(totalSellingPriceTND));

                if ("Deposit".equals(cbPaymentType.getValue())) {
                    double depositTND = PriceCalculator.calculateDeposit(totalSellingPriceTND);
                    txtDepositAmount.setText(PriceCalculator.formatPrice(depositTND));
                } else {
                    txtDepositAmount.clear();
                }
            } catch (NumberFormatException ex) {
                txtSelling.clear();
                txtDepositAmount.clear();
            }
        };

        // Add listeners
        txtOriginal.textProperty().addListener((obs, old, val) -> calculatePrices.run());
        spQty.valueProperty().addListener((obs, old, val) -> calculatePrices.run());
        cbPaymentType.valueProperty().addListener((obs, old, val) -> {
            boolean isDeposit = "Deposit".equals(val);
            txtDepositAmount.setDisable(!isDeposit);
            if (!isDeposit) txtDepositAmount.clear();
            calculatePrices.run();
        });

        Button btnCalculate = new Button("Calculate");
        btnCalculate.getStyleClass().addAll("modern-button", "button-secondary");
        btnCalculate.setOnAction(e -> calculatePrices.run());

        // Layout form
        form.add(new Label("Client:"), 0, 0);
        form.add(cbClient, 1, 0);
        form.add(new Label("Shipment:"), 0, 1);
        form.add(cbShipment, 1, 1);
        form.add(new Label("Delivery:"), 0, 2);
        form.add(cbDelivery, 1, 2);
        form.add(new Label("Product:"), 0, 3);
        form.add(txtProduct, 1, 3);
        form.add(new Label("Size:"), 0, 4);
        form.add(txtSize, 1, 4);
        form.add(new Label("Qty:"), 0, 5);
        form.add(spQty, 1, 5);
        form.add(new Label("Original (EUR):"), 0, 6);
        form.add(txtOriginal, 1, 6);
        form.add(btnCalculate, 2, 6);
        form.add(new Label("Selling (TND):"), 0, 7);
        form.add(txtSelling, 1, 7);
        form.add(new Label("Payment Type:"), 0, 8);
        form.add(cbPaymentType, 1, 8);
        form.add(new Label("Platform:"), 0, 9);
        form.add(cbPlatform, 1, 9);
        form.add(new Label("Deposit:"), 0, 10);
        form.add(txtDepositAmount, 1, 10);
        form.add(new Label("Notes:"), 0, 11);
        form.add(txtNotes, 1, 11);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                saveNewOrder(cbClient.getValue(), cbShipment.getValue(), cbDelivery.getValue(),
                    txtProduct.getText(), txtSize.getText(), spQty.getValue(),
                    txtOriginal.getText(), txtSelling.getText(), cbPaymentType.getValue(),
                    cbPlatform.getValue(), txtDepositAmount.getText(), txtNotes.getText(),
                    onSuccess, onPaymentCreated);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void saveNewOrder(Client client, Shipment shipment, DeliveryOption delivery,
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

            // Handle deposit/full payment
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

    /**
     * Show a popup with client information.
     */
    public void showClientInfoPopup(Client client) {
        if (client == null) {
            DialogUtils.showError("Client not found.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Client Info");

        GridPane grid = DialogUtils.createFormGrid(5, 5, 10);
        grid.addRow(0, new Label("Username:"), new Label(client.getUsername()));
        grid.addRow(1, new Label("Phone:"), new Label(client.getPhone()));
        grid.addRow(2, new Label("Source:"), new Label(client.getSource()));
        grid.addRow(3, new Label("Address:"), new Label(client.getAddress()));

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
