package ui.dialog.order;

import service.api.IOrderService;
import service.api.IPaymentService;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.*;

/**
 * Main entry point for order-related dialogs.
 * Delegates form building and saving to specialized classes.
 */
public class OrderDialogs {

    private final OrderSaveHandler saveHandler;

    public OrderDialogs(IOrderService orderService, IPaymentService paymentService) {
        this.saveHandler = new OrderSaveHandler(orderService, paymentService);
    }

    public void openNewOrderDialog(ObservableList<Client> clients,
                                    ObservableList<Shipment> shipments,
                                    ObservableList<DeliveryOption> deliveryOptions,
                                    OrderSaveHandler.OrderCallback onSuccess,
                                    OrderSaveHandler.OrderCallback onPaymentCreated) {
        openOrderFormDialog(null, clients, shipments, deliveryOptions, onSuccess, onPaymentCreated);
    }

    public void openEditOrderDialog(Order order,
                                     ObservableList<Client> clients,
                                     ObservableList<Shipment> shipments,
                                     ObservableList<DeliveryOption> deliveryOptions,
                                     OrderSaveHandler.OrderCallback onSuccess) {
        openOrderFormDialog(order, clients, shipments, deliveryOptions, onSuccess, null);
    }

    public void showClientInfoPopup(Client client) {
        if (client == null) {
            ui.util.DialogUtils.showError("Client not found.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Client Info");

        GridPane grid = ui.util.DialogUtils.createFormGrid(5, 5, 10);
        grid.addRow(0, new Label("Username:"), new Label(client.getUsername()));
        grid.addRow(1, new Label("Phone:"), new Label(client.getPhone()));
        grid.addRow(2, new Label("Source:"), new Label(client.getSource()));
        grid.addRow(3, new Label("Address:"), new Label(client.getAddress()));

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void openOrderFormDialog(Order editingOrder,
                                      ObservableList<Client> clients,
                                      ObservableList<Shipment> shipments,
                                      ObservableList<DeliveryOption> deliveryOptions,
                                      OrderSaveHandler.OrderCallback onSuccess,
                                      OrderSaveHandler.OrderCallback onPaymentCreated) {
        boolean isEditMode = editingOrder != null;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Edit Order #" + editingOrder.getOrderId() : "New Order");

        OrderFormBuilder.OrderFormComponents components = OrderFormBuilder.buildOrderForm(
            editingOrder, clients, shipments, deliveryOptions);

        GridPane form = OrderFormBuilder.layoutOrderForm(components);

        Runnable calculatePrices = OrderFormBuilder.createPriceCalculator(components);
        OrderFormBuilder.setupPriceListeners(components, calculatePrices);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (isEditMode) {
                    saveHandler.saveEditOrder(editingOrder, components.cbClient.getValue(), components.cbShipment.getValue(),
                        components.cbDelivery.getValue(), components.txtProduct.getText(), components.txtSize.getText(),
                        components.spQty.getValue(), components.txtOriginal.getText(), components.txtSelling.getText(),
                        components.cbPaymentType.getValue(), components.cbPlatform.getValue(), components.txtNotes.getText(),
                        onSuccess);
                } else {
                    saveHandler.saveNewOrder(components.cbClient.getValue(), components.cbShipment.getValue(),
                        components.cbDelivery.getValue(), components.txtProduct.getText(), components.txtSize.getText(),
                        components.spQty.getValue(), components.txtOriginal.getText(), components.txtSelling.getText(),
                        components.cbPaymentType.getValue(), components.cbPlatform.getValue(), components.txtDepositAmount.getText(),
                        components.txtNotes.getText(), onSuccess, onPaymentCreated);
                }
            }
            return null;
        });

        calculatePrices.run();
        dialog.showAndWait();
    }
}
