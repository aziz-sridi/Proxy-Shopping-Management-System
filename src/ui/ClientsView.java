package ui;

import dao.ClientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.Client;

import java.sql.SQLException;

public class ClientsView {

    private final ClientDAO clientDAO = new ClientDAO();
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Client> table = new TableView<>();

        TableColumn<Client, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getClientId()));

        TableColumn<Client, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));

        TableColumn<Client, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<Client, String> colSource = new TableColumn<>("Source");
        colSource.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSource()));

        table.getColumns().addAll(colId, colUsername, colPhone, colSource);
        table.setItems(clientData);

        TextField txtUsername = new TextField();
        TextField txtPhone = new TextField();
        ComboBox<String> cbSource = new ComboBox<>();
        cbSource.getItems().addAll("facebook", "instagram", "whatsapp");
        TextField txtAddress = new TextField();

        Button btnAdd = new Button("Add Client");
        btnAdd.setOnAction(e -> {
            Client c = new Client();
            c.setUsername(txtUsername.getText());
            c.setPhone(txtPhone.getText());
            c.setSource(cbSource.getValue());
            c.setAddress(txtAddress.getText());
            try {
                clientDAO.insert(c);
                loadClients();
                txtUsername.clear();
                txtPhone.clear();
                cbSource.getSelectionModel().clearSelection();
                txtAddress.clear();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        Button btnUpdate = new Button("Update Selected");
        btnUpdate.setOnAction(e -> {
            Client selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a client to update.");
                return;
            }
            selected.setUsername(txtUsername.getText());
            selected.setPhone(txtPhone.getText());
            selected.setSource(cbSource.getValue());
            selected.setAddress(txtAddress.getText());
            try {
                clientDAO.update(selected);
                loadClients();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        Button btnDelete = new Button("Delete Selected");
        btnDelete.setOnAction(e -> {
            Client selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a client to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected client?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                try {
                    clientDAO.delete(selected.getClientId());
                    loadClients();
                } catch (SQLException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Search by username or phone");
        Button btnSearch = new Button("Find");
        btnSearch.setOnAction(e -> {
            String keyword = txtSearch.getText();
            if (keyword == null || keyword.trim().isEmpty()) {
                loadClients();
                return;
            }
            clientData.clear();
            try {
                clientData.addAll(clientDAO.findByUsernameOrPhone(keyword.trim()));
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtUsername.setText(newSel.getUsername());
                txtPhone.setText(newSel.getPhone());
                cbSource.setValue(newSel.getSource());
                txtAddress.setText(newSel.getAddress());
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(5);
        form.setVgap(5);
        form.add(new Label("Username:"), 0, 0);
        form.add(txtUsername, 1, 0);
        form.add(new Label("Phone:"), 0, 1);
        form.add(txtPhone, 1, 1);
        form.add(new Label("Source:"), 0, 2);
        form.add(cbSource, 1, 2);
        form.add(new Label("Address:"), 0, 3);
        form.add(txtAddress, 1, 3);

        HBox actions = new HBox(5, btnAdd, btnUpdate, btnDelete);
        form.add(actions, 1, 4);

        HBox searchBox = new HBox(5, txtSearch, btnSearch);
        form.add(searchBox, 1, 5);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(form);

        loadClients();
        return root;
    }

    private void loadClients() {
        clientData.clear();
        try {
            clientData.addAll(clientDAO.findAll());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
