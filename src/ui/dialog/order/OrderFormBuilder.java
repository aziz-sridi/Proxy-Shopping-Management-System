package ui.dialog.order;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.*;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

public class OrderFormBuilder {

    public static class OrderFormComponents {
        public ComboBox<Client> cbClient;
        public ComboBox<Shipment> cbShipment;
        public ComboBox<DeliveryOption> cbDelivery;
        public TextField txtProduct;
        public TextField txtSize;
        public Spinner<Integer> spQty;
        public TextField txtOriginal;
        public TextField txtSelling;
        public ComboBox<String> cbPaymentType;
        public ComboBox<String> cbPlatform;
        public TextField txtDepositAmount;
        public TextField txtNotes;
        public Button btnCalculate;
    }

    public static OrderFormComponents buildOrderForm(Order editingOrder,
                                                      ObservableList<Client> clients,
                                                      ObservableList<Shipment> shipments,
                                                      ObservableList<DeliveryOption> deliveryOptions) {
        OrderFormComponents components = new OrderFormComponents();
        boolean isEditMode = editingOrder != null;

        components.cbClient = new ComboBox<>(clients);
        components.cbClient.getStyleClass().add("app-field");
        components.cbClient.setPromptText("Select Client");
        if (isEditMode) {
            components.cbClient.setValue(clients.stream()
                .filter(c -> c.getClientId() == editingOrder.getClientId())
                .findFirst().orElse(null));
        }

        components.cbShipment = new ComboBox<>(shipments);
        components.cbShipment.getStyleClass().add("app-field");
        components.cbShipment.setPromptText("Select Shipment (REQUIRED)");
        components.cbShipment.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        if (isEditMode && editingOrder.getShipmentId() != null) {
            components.cbShipment.setValue(shipments.stream()
                .filter(s -> s.getShipmentId() == editingOrder.getShipmentId())
                .findFirst().orElse(null));
        }

        components.cbDelivery = new ComboBox<>(deliveryOptions);
        components.cbDelivery.getStyleClass().add("app-field");
        components.cbDelivery.setPromptText("Delivery option (optional)");
        if (isEditMode && editingOrder.getDeliveryOptionId() != null) {
            components.cbDelivery.setValue(deliveryOptions.stream()
                .filter(d -> d.getDeliveryOptionId() == editingOrder.getDeliveryOptionId())
                .findFirst().orElse(null));
        }

        components.txtProduct = DialogUtils.createStyledTextField("Product link");
        if (isEditMode) components.txtProduct.setText(editingOrder.getProductLink());

        components.txtSize = DialogUtils.createStyledTextField("Size");
        if (isEditMode) components.txtSize.setText(editingOrder.getProductSize());

        components.spQty = new Spinner<>(1, 1000, isEditMode ? editingOrder.getQuantity() : 1);
        components.spQty.getStyleClass().add("app-field");

        components.txtOriginal = DialogUtils.createStyledTextField("Original price (EUR)");
        if (isEditMode) components.txtOriginal.setText(String.valueOf(editingOrder.getOriginalPrice()));

        components.txtSelling = DialogUtils.createReadOnlyTextField("Selling price (TND)",
            "-fx-background-color: #f0f0f0; -fx-font-weight: bold;");
        if (isEditMode) components.txtSelling.setText(String.valueOf(editingOrder.getSellingPrice()));

        components.cbPaymentType = new ComboBox<>();
        components.cbPaymentType.getStyleClass().add("app-field");
        components.cbPaymentType.getItems().addAll("Deposit", "Full", "On Delivery");
        if (isEditMode) {
            components.cbPaymentType.setValue(editingOrder.getPaymentType());
        } else {
            components.cbPaymentType.setValue("On Delivery");
        }

        components.cbPlatform = new ComboBox<>();
        components.cbPlatform.getStyleClass().add("app-field");
        components.cbPlatform.getItems().addAll(Platform.getDisplayNames());
        if (isEditMode) {
            components.cbPlatform.setValue(editingOrder.getPlatform() != null ? editingOrder.getPlatform().getDisplayName() : "Other");
        } else {
            components.cbPlatform.setValue("Other");
        }

        components.txtDepositAmount = DialogUtils.createReadOnlyTextField("Deposit amount (TND)",
            "-fx-background-color: #fff3e0; -fx-font-weight: bold;");
        components.txtDepositAmount.setDisable(true);

        components.txtNotes = DialogUtils.createStyledTextField("Notes");
        if (isEditMode) components.txtNotes.setText(editingOrder.getNotes());

        components.btnCalculate = new Button("Calculate");
        components.btnCalculate.getStyleClass().addAll("app-button", "button-secondary");

        return components;
    }

    public static GridPane layoutOrderForm(OrderFormComponents components) {
        GridPane form = DialogUtils.createFormGrid(5, 5, 10);

        form.add(new Label("Client:"), 0, 0);
        form.add(components.cbClient, 1, 0);
        form.add(new Label("Shipment:"), 0, 1);
        form.add(components.cbShipment, 1, 1);
        form.add(new Label("Delivery:"), 0, 2);
        form.add(components.cbDelivery, 1, 2);
        form.add(new Label("Product:"), 0, 3);
        form.add(components.txtProduct, 1, 3);
        form.add(new Label("Size:"), 0, 4);
        form.add(components.txtSize, 1, 4);
        form.add(new Label("Qty:"), 0, 5);
        form.add(components.spQty, 1, 5);
        form.add(new Label("Original (EUR):"), 0, 6);
        form.add(components.txtOriginal, 1, 6);
        form.add(components.btnCalculate, 2, 6);
        form.add(new Label("Selling (TND):"), 0, 7);
        form.add(components.txtSelling, 1, 7);
        form.add(new Label("Payment Type:"), 0, 8);
        form.add(components.cbPaymentType, 1, 8);
        form.add(new Label("Platform:"), 0, 9);
        form.add(components.cbPlatform, 1, 9);
        form.add(new Label("Deposit:"), 0, 10);
        form.add(components.txtDepositAmount, 1, 10);
        form.add(new Label("Notes:"), 0, 11);
        form.add(components.txtNotes, 1, 11);

        return form;
    }

    public static Runnable createPriceCalculator(OrderFormComponents components) {
        return () -> {
            String originalText = components.txtOriginal.getText();
            if (originalText == null || originalText.trim().isEmpty()) {
                components.txtSelling.clear();
                components.txtDepositAmount.clear();
                return;
            }
            try {
                double originalPriceEUR = PriceCalculator.parsePrice(originalText);
                int quantity = components.spQty.getValue();

                double totalSellingPriceTND = PriceCalculator.calculateTotalSellingPrice(originalPriceEUR, quantity);
                components.txtSelling.setText(PriceCalculator.formatPrice(totalSellingPriceTND));

                if ("Deposit".equals(components.cbPaymentType.getValue())) {
                    double depositTND = PriceCalculator.calculateDeposit(totalSellingPriceTND);
                    components.txtDepositAmount.setText(PriceCalculator.formatPrice(depositTND));
                } else {
                    components.txtDepositAmount.clear();
                }
            } catch (NumberFormatException ex) {
                components.txtSelling.clear();
                components.txtDepositAmount.clear();
            }
        };
    }

    public static void setupPriceListeners(OrderFormComponents components, Runnable calculatePrices) {
        components.txtOriginal.textProperty().addListener((obs, old, val) -> calculatePrices.run());
        components.spQty.valueProperty().addListener((obs, old, val) -> calculatePrices.run());
        components.cbPaymentType.valueProperty().addListener((obs, old, val) -> {
            boolean isDeposit = "Deposit".equals(val);
            components.txtDepositAmount.setDisable(!isDeposit);
            if (!isDeposit) components.txtDepositAmount.clear();
            calculatePrices.run();
        });
        components.btnCalculate.setOnAction(e -> calculatePrices.run());
    }
}
