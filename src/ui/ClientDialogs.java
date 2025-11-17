package ui;

import dao.ClientDAO;
import dao.OrderDAO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Client;
import model.Order;

import java.sql.SQLException;

public class ClientDialogs {

    private final ClientDAO clientDAO;
    private final OrderDAO orderDAO;

    public ClientDialogs(ClientDAO clientDAO, OrderDAO orderDAO) {
        this.clientDAO = clientDAO;
        this.orderDAO = orderDAO;
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("New Order for " + client.getUsername());

        TextField txtProduct = new TextField();
        txtProduct.setPromptText("Product link");

        TextField txtSize = new TextField();
        txtSize.setPromptText("Size");

        Spinner<Integer> spQty = new Spinner<>(1, 1000, 1);

        TextField txtOriginal = new TextField();
        txtOriginal.setPromptText("Original price (EUR)");

        TextField txtSelling = new TextField();
        txtSelling.setPromptText("Selling price (TND)");

        ComboBox<String> cbPaymentType = new ComboBox<>();
        cbPaymentType.getItems().addAll("Deposit", "Full", "On Delivery");
        cbPaymentType.setValue("On Delivery");

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Notes");

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
        grid.add(new Label("Original (EUR):"), 0, 3);
        grid.add(txtOriginal, 1, 3);
        grid.add(new Label("Selling (TND):"), 0, 4);
        grid.add(txtSelling, 1, 4);
        grid.add(new Label("Payment Type:"), 0, 5);
        grid.add(cbPaymentType, 1, 5);
        grid.add(new Label("Notes:"), 0, 6);
        grid.add(txtNotes, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Order o = new Order();
                o.setClientId(client.getClientId());
                o.setShipmentId(null);
                o.setDeliveryOptionId(null);
                o.setProductLink(txtProduct.getText());
                o.setProductSize(txtSize.getText());
                o.setQuantity(spQty.getValue());
                try {
                    o.setOriginalPrice(Double.parseDouble(txtOriginal.getText()));
                    o.setSellingPrice(Double.parseDouble(txtSelling.getText()));
                } catch (NumberFormatException ex) {
                    if (onError != null) onError.accept("Invalid price values.");
                    return null;
                }
                o.setPaymentType(cbPaymentType.getValue());
                o.setPaymentStatus("Unpaid");
                o.setNotes(txtNotes.getText());
                try {
                    orderDAO.insert(o);
                } catch (SQLException e) {
                    if (onError != null) onError.accept(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
