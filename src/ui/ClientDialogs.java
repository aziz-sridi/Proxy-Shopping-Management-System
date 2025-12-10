package ui;

import service.api.IClientService;
import service.api.IOrderService;
import service.impl.OrderServiceImpl;
import service.api.IShipmentService;
import service.impl.ShipmentServiceImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Client;
import model.Order;
import model.Shipment;
import ui.util.DialogUtils;
import ui.util.PriceCalculator;

import java.sql.SQLException;

/**
 * Dialog helper class for client-related dialogs.
 * Refactored to use DialogUtils and PriceCalculator for reduced code duplication.
 */
public class ClientDialogs {

    private final IClientService clientService;
    private final IOrderService orderService;
    private final IShipmentService shipmentService;
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    public ClientDialogs(IClientService clientService) {
        this.clientService = clientService;
        this.orderService = new OrderServiceImpl();
        this.shipmentService = new ShipmentServiceImpl();
        loadShipments();
    }

    public void showAddClientDialog(Runnable onSuccess, java.util.function.Consumer<String> onError) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Client");

        TextField txtUsername = new TextField();
        TextField txtPhone = new TextField();
        ComboBox<String> cbSource = new ComboBox<>();
        cbSource.getItems().addAll("facebook", "instagram", "whatsapp");
        TextField txtAddress = new TextField();

        GridPane grid = DialogUtils.createFormGrid(8, 8, 10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(txtPhone, 1, 1);
        grid.add(new Label("Source:"), 0, 2);
        grid.add(cbSource, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(txtAddress, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Client c = new Client();
                c.setUsername(txtUsername.getText());
                c.setPhone(txtPhone.getText());
                c.setSource(cbSource.getValue());
                c.setAddress(txtAddress.getText());
                try {
                    clientService.addClient(c);
                    if (onSuccess != null) onSuccess.run();
                } catch (SQLException e) {
                    if (onError != null) onError.accept(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void showEditClientDialog(Client client, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Client");

        TextField txtUsername = new TextField(client.getUsername());
        TextField txtPhone = new TextField(client.getPhone());
        TextField txtSource = new TextField(client.getSource());
        TextField txtAddress = new TextField(client.getAddress());

        GridPane grid = DialogUtils.createFormGrid(8, 8, 10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(txtPhone, 1, 1);
        grid.add(new Label("Source:"), 0, 2);
        grid.add(txtSource, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(txtAddress, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                client.setUsername(txtUsername.getText());
                client.setPhone(txtPhone.getText());
                client.setSource(txtSource.getText());
                client.setAddress(txtAddress.getText());
                try {
                    clientService.updateClient(client);
                    if (onSuccess != null) onSuccess.run();
                } catch (SQLException e) {
                    if (onError != null) onError.accept(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void showAddOrderDialog(Client client, java.util.function.Consumer<String> onError) {
        showAddOrderDialog(client, null, onError);
    }

    public void showAddOrderDialog(Client client, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        // Reload shipments every time the dialog is opened to ensure fresh data
        loadShipments();
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Order for " + client.getUsername());

        // Form fields
        TextField txtProduct = new TextField();
        txtProduct.setPromptText("Product link");

        TextField txtSize = new TextField();
        txtSize.setPromptText("Size");

        Spinner<Integer> spQty = new Spinner<>(1, 1000, 1);

        TextField txtOriginal = new TextField();
        txtOriginal.setPromptText("Unit Price (EUR)");

        TextField txtSelling = DialogUtils.createReadOnlyTextField("Selling price (TND)", 
            "-fx-background-color: #f0f0f0; -fx-font-weight: bold;");

        TextField txtExpectedTotal = DialogUtils.createReadOnlyTextField("Expected Total (TND)",
            "-fx-background-color: #e8f5e8; -fx-font-weight: bold;");

        TextField txtDeposit = DialogUtils.createReadOnlyTextField("Deposit (TND)",
            "-fx-background-color: #fff3e0; -fx-font-weight: bold;");

        ComboBox<String> cbPaymentType = new ComboBox<>();
        cbPaymentType.getItems().addAll("Deposit", "Full", "On Delivery");
        cbPaymentType.setValue("On Delivery");

        ComboBox<String> cbPlatform = new ComboBox<>();
        cbPlatform.getItems().addAll(model.Platform.getDisplayNames());
        cbPlatform.setValue("Other");

        ComboBox<Shipment> cbShipment = new ComboBox<>(shipmentData);
        cbShipment.setPromptText("Select Shipment (REQUIRED)");
        cbShipment.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Notes");

        // Price calculation using PriceCalculator
        Runnable calculatePrices = () -> {
            String unitPriceText = txtOriginal.getText();
            
            if (unitPriceText == null || unitPriceText.trim().isEmpty()) {
                txtSelling.clear();
                txtExpectedTotal.clear();
                txtDeposit.clear();
                return;
            }
            
            try {
                double unitPriceEUR = PriceCalculator.parsePrice(unitPriceText);
                int quantity = spQty.getValue();
                
                double sellingPriceTND = PriceCalculator.calculateSellingPrice(unitPriceEUR);
                double expectedTotalTND = PriceCalculator.calculateTotalSellingPrice(unitPriceEUR, quantity);
                
                txtSelling.setText(PriceCalculator.formatPrice(sellingPriceTND));
                txtExpectedTotal.setText(PriceCalculator.formatPrice(expectedTotalTND));
                
                if ("Deposit".equals(cbPaymentType.getValue())) {
                    double depositTND = PriceCalculator.calculateDeposit(expectedTotalTND);
                    txtDeposit.setText(PriceCalculator.formatPrice(depositTND));
                } else {
                    txtDeposit.clear();
                }
            } catch (NumberFormatException ex) {
                txtSelling.clear();
                txtExpectedTotal.clear();
                txtDeposit.clear();
            }
        };

        // Add listeners
        txtOriginal.textProperty().addListener((obs, old, val) -> Platform.runLater(calculatePrices));
        spQty.valueProperty().addListener((obs, old, val) -> Platform.runLater(calculatePrices));
        cbPaymentType.valueProperty().addListener((obs, old, val) -> Platform.runLater(calculatePrices));
        txtOriginal.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) Platform.runLater(calculatePrices);
        });
        txtOriginal.setOnKeyReleased(e -> Platform.runLater(calculatePrices));

        Button btnCalculate = new Button("Calculate Prices");
        btnCalculate.setOnAction(e -> calculatePrices.run());
        btnCalculate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        GridPane grid = DialogUtils.createFormGrid(8, 8, 10);
        grid.add(new Label("Product:"), 0, 0);
        grid.add(txtProduct, 1, 0);
        grid.add(new Label("Size:"), 0, 1);
        grid.add(txtSize, 1, 1);
        grid.add(new Label("Qty:"), 0, 2);
        grid.add(spQty, 1, 2);
        grid.add(new Label("Unit Price (EUR):"), 0, 3);
        grid.add(txtOriginal, 1, 3);
        grid.add(btnCalculate, 2, 3);
        grid.add(new Label("Selling Price (TND):"), 0, 4);
        grid.add(txtSelling, 1, 4);
        grid.add(new Label("Expected Total (TND):"), 0, 5);
        grid.add(txtExpectedTotal, 1, 5);
        grid.add(new Label("Payment Type:"), 0, 6);
        grid.add(cbPaymentType, 1, 6);
        grid.add(new Label("Platform:"), 0, 7);
        grid.add(cbPlatform, 1, 7);
        grid.add(new Label("Shipment:"), 0, 8);
        grid.add(cbShipment, 1, 8);
        grid.add(new Label("Deposit (TND):"), 0, 9);
        grid.add(txtDeposit, 1, 9);
        grid.add(new Label("Notes:"), 0, 10);
        grid.add(txtNotes, 1, 10);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                processAddOrder(client, cbShipment.getValue(), txtProduct.getText(), txtSize.getText(),
                    spQty.getValue(), txtOriginal.getText(), cbPaymentType.getValue(), 
                    cbPlatform.getValue(), txtNotes.getText(), onSuccess, onError);
            }
            return null;
        });

        Platform.runLater(calculatePrices);
        dialog.showAndWait();
    }

    private void processAddOrder(Client client, Shipment shipment, String product, String size,
                                  int quantity, String originalPriceText, String paymentType,
                                  String platform, String notes, Runnable onSuccess,
                                  java.util.function.Consumer<String> onError) {
        // Validate shipment selection
        if (shipment == null) {
            if (onError != null) onError.accept("Please select a shipment. Orders must be assigned to a shipment.");
            return;
        }

        try {
            double originalPrice = PriceCalculator.parsePrice(originalPriceText);
            double sellingPrice = PriceCalculator.calculateSellingPrice(originalPrice);

            Order o = new Order();
            o.setClientId(client.getClientId());
            o.setShipmentId(shipment.getShipmentId());
            o.setDeliveryOptionId(null);
            o.setProductLink(product);
            o.setProductSize(size);
            o.setQuantity(quantity);
            o.setOriginalPrice(originalPrice);
            o.setSellingPrice(sellingPrice);
            o.setPaymentType(paymentType);
            o.setPaymentStatus("Unpaid");
            o.setPlatform(model.Platform.fromString(platform));
            o.setNotes(notes);

            orderService.insertOrder(o);
            if (onSuccess != null) onSuccess.run();
            
        } catch (NumberFormatException ex) {
            if (onError != null) onError.accept("Invalid number format. Please enter a valid price like 18.50 or 18,50");
        } catch (SQLException e) {
            if (onError != null) onError.accept("Database error: " + e.getMessage());
        }
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentService.getAllShipments());
        } catch (SQLException e) {
            System.err.println("Error loading shipments: " + e.getMessage());
        }
    }
}
