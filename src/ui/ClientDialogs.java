package ui;

import dao.ClientDAO;
import dao.OrderDAO;
import dao.ShipmentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Client;
import model.Order;
import model.Shipment;
import model.Settings;
import util.SettingsManager;

import java.sql.SQLException;

public class ClientDialogs {

    private final ClientDAO clientDAO;
    private final OrderDAO orderDAO;
    private final ShipmentDAO shipmentDAO;
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    public ClientDialogs(ClientDAO clientDAO, OrderDAO orderDAO) {
        this.clientDAO = clientDAO;
        this.orderDAO = orderDAO;
        this.shipmentDAO = new ShipmentDAO();
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

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
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
                    clientDAO.insert(c);
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

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
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
                    clientDAO.update(client);
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Order for " + client.getUsername());

        TextField txtProduct = new TextField();
        txtProduct.setPromptText("Product link");

        TextField txtSize = new TextField();
        txtSize.setPromptText("Size");

        Spinner<Integer> spQty = new Spinner<>(1, 1000, 1);

        TextField txtOriginal = new TextField();
        txtOriginal.setPromptText("Unit Price (EUR)");

        TextField txtSelling = new TextField();
        txtSelling.setPromptText("Selling price (TND)");
        txtSelling.setEditable(false);
        txtSelling.setStyle("-fx-background-color: #f0f0f0; -fx-font-weight: bold;");

        TextField txtExpectedTotal = new TextField();
        txtExpectedTotal.setPromptText("Expected Total (TND)");
        txtExpectedTotal.setEditable(false);
        txtExpectedTotal.setStyle("-fx-background-color: #e8f5e8; -fx-font-weight: bold;");

        TextField txtDeposit = new TextField();
        txtDeposit.setPromptText("Deposit (TND)");
        txtDeposit.setEditable(false);
        txtDeposit.setStyle("-fx-background-color: #fff3e0; -fx-font-weight: bold;");

        ComboBox<String> cbPaymentType = new ComboBox<>();
        cbPaymentType.getItems().addAll("Deposit", "Full", "On Delivery");
        cbPaymentType.setValue("On Delivery");

        ComboBox<String> cbPlatform = new ComboBox<>();
        cbPlatform.getItems().addAll(model.Platform.getDisplayNames());
        cbPlatform.setValue("Other");
        cbPlatform.setPromptText("Select Platform");

        ComboBox<Shipment> cbShipment = new ComboBox<>(shipmentData);
        cbShipment.setPromptText("Select Shipment (REQUIRED)");
        cbShipment.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // Automatic calculation logic
        Runnable calculatePrices = () -> {
            String unitPriceText = txtOriginal.getText();
            
            if (unitPriceText == null || unitPriceText.trim().isEmpty()) {
                txtSelling.clear();
                txtExpectedTotal.clear();
                txtDeposit.clear();
                return;
            }
            
            try {
                // Handle potential locale issues with decimal separator
                String cleanPrice = unitPriceText.trim().replace(",", ".");
                double unitPriceEUR = Double.parseDouble(cleanPrice);
                int quantity = spQty.getValue();
                
                // Apply dynamic pricing rule: sellingPriceTND = unitPriceEUR * sellingMultiplier
                Settings settings = SettingsManager.getCurrentSettings();
                double sellingMultiplier = settings.getSellingMultiplier();
                double sellingPriceTND = unitPriceEUR * sellingMultiplier;
                
                // Calculate expected total: expectedTotalTND = sellingPriceTND * quantity
                double expectedTotalTND = sellingPriceTND * quantity;
                
                // Update the selling price and expected total fields
                txtSelling.setText(String.format("%.2f", sellingPriceTND));
                txtExpectedTotal.setText(String.format("%.2f", expectedTotalTND));
                
                // Calculate deposit if payment type is "Deposit"
                String paymentType = cbPaymentType.getValue();
                if ("Deposit".equals(paymentType)) {
                    double depositTND = expectedTotalTND * 0.5; // 50% deposit
                    txtDeposit.setText(String.format("%.2f", depositTND));
                } else {
                    txtDeposit.clear();
                }
                
            } catch (NumberFormatException ex) {
                txtSelling.clear();
                txtExpectedTotal.clear();
                txtDeposit.clear();
            }
        };

        // Add listeners to automatically recalculate when values change
        txtOriginal.textProperty().addListener((obs, old, val) -> {
            Platform.runLater(() -> calculatePrices.run());
        });
        spQty.valueProperty().addListener((obs, old, val) -> {
            Platform.runLater(() -> calculatePrices.run());
        });
        cbPaymentType.valueProperty().addListener((obs, old, val) -> {
            Platform.runLater(() -> calculatePrices.run());
        });

        // Also add focus listeners as backup
        txtOriginal.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) { // When field loses focus
                Platform.runLater(() -> calculatePrices.run());
            }
        });

        // Add key release listener to catch immediate typing
        txtOriginal.setOnKeyReleased(e -> Platform.runLater(() -> calculatePrices.run()));

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Notes");

        Button btnCalculate = new Button("Calculate Prices");
        btnCalculate.setOnAction(e -> calculatePrices.run());
        btnCalculate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Product:"), 0, 0);
        grid.add(txtProduct, 1, 0);
        grid.add(new Label("Size:"), 0, 1);
        grid.add(txtSize, 1, 1);
        grid.add(new Label("Qty:"), 0, 2);
        grid.add(spQty, 1, 2);
        grid.add(new Label("Unit Price (EUR):"), 0, 3);
        grid.add(txtOriginal, 1, 3);
        grid.add(btnCalculate, 2, 3); // Add calculate button next to price field
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
                System.out.println("=== ORDER VALIDATION DEBUG ===");
                
                // Validate shipment selection first
                Shipment shipment = cbShipment.getValue();
                if (shipment == null) {
                    if (onError != null) onError.accept("Please select a shipment. Orders must be assigned to a shipment.");
                    return null;
                }
                
                // Run calculation multiple times to be absolutely sure
                try {
                    calculatePrices.run();
                    Thread.sleep(50); // Small delay to ensure calculation completes
                    calculatePrices.run(); // Run again to be safe
                } catch (Exception e) {
                    System.out.println("Calculation error: " + e.getMessage());
                }
                
                Order o = new Order();
                o.setClientId(client.getClientId());
                o.setShipmentId(shipment.getShipmentId());
                o.setDeliveryOptionId(null);
                o.setProductLink(txtProduct.getText());
                o.setProductSize(txtSize.getText());
                o.setQuantity(spQty.getValue());
                
                // Debug: Print all field values
                System.out.println("Product: '" + txtProduct.getText() + "'");
                System.out.println("Original Price: '" + txtOriginal.getText() + "'");
                System.out.println("Selling Price: '" + txtSelling.getText() + "'");
                
                try {
                    // Parse original price with extensive validation
                    String originalText = txtOriginal.getText();
                    System.out.println("Validating original price: '" + originalText + "'");
                    
                    if (originalText == null) {
                        System.out.println("ERROR: Original price is null");
                        if (onError != null) onError.accept("Unit Price field is empty. Please enter a price in EUR.");
                        return null;
                    }
                    
                    String trimmed = originalText.trim();
                    if (trimmed.isEmpty()) {
                        System.out.println("ERROR: Original price is empty after trim");
                        if (onError != null) onError.accept("Unit Price field is empty. Please enter a price in EUR.");
                        return null;
                    }
                    
                    // Handle potential locale issues with decimal separator
                    String cleanPrice = trimmed.replace(",", ".");
                    System.out.println("Cleaned price: '" + cleanPrice + "'");
                    
                    double originalPrice = Double.parseDouble(cleanPrice);
                    System.out.println("Parsed original price: " + originalPrice);
                    o.setOriginalPrice(originalPrice);
                    
                    // Always calculate selling price using dynamic settings
                    Settings settings = SettingsManager.getCurrentSettings();
                    double sellingMultiplier = settings.getSellingMultiplier();
                    double sellingPrice = originalPrice * sellingMultiplier;
                    System.out.println("Calculated selling price: " + sellingPrice);
                    o.setSellingPrice(sellingPrice);
                    
                    System.out.println("=== VALIDATION SUCCESSFUL ===");
                    
                } catch (NumberFormatException ex) {
                    System.out.println("NumberFormatException: " + ex.getMessage());
                    if (onError != null) onError.accept("Invalid number format. Please enter a valid price like 18.50 or 18,50");
                    return null;
                } catch (Exception ex) {
                    System.out.println("Unexpected error: " + ex.getMessage());
                    if (onError != null) onError.accept("Validation error: " + ex.getMessage());
                    return null;
                }
                
                o.setPaymentType(cbPaymentType.getValue());
                o.setPaymentStatus("Unpaid");
                o.setPlatform(model.Platform.fromString(cbPlatform.getValue()));
                o.setNotes(txtNotes.getText());
                try {
                    orderDAO.insert(o);
                    System.out.println("=== ORDER SAVED SUCCESSFULLY ===");
                    if (onSuccess != null) onSuccess.run(); // Notify success
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                    if (onError != null) onError.accept("Database error: " + e.getMessage());
                }
            }
            return null;
        });

        // Trigger initial calculation when dialog opens
        Platform.runLater(() -> calculatePrices.run());

        dialog.showAndWait();
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentDAO.findAll());
        } catch (SQLException e) {
            System.err.println("Error loading shipments: " + e.getMessage());
        }
    }
}
