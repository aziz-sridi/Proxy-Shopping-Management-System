package ui;

import dao.ClientDAO;
import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import model.Client;

import java.sql.SQLException;

public class ClientsView {

    private final ClientDAO clientDAO = new ClientDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ClientDialogs dialogs = new ClientDialogs(clientDAO, orderDAO);
    private HistoryOpener historyOpener;
    private Runnable orderRefreshCallback;

    public ClientsView() {
    }

    public ClientsView(HistoryOpener historyOpener) {
        this.historyOpener = historyOpener;
    }

    public ClientsView(HistoryOpener historyOpener, Runnable orderRefreshCallback) {
        this.historyOpener = historyOpener;
        this.orderRefreshCallback = orderRefreshCallback;
    }
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();

    public BorderPane getView() {
        TableView<Client> table = new TableView<>();
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Client, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getClientId()));

        TableColumn<Client, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));

        TableColumn<Client, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<Client, String> colSource = new TableColumn<>("Source");
        colSource.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSource()));

        TableColumn<Client, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAddress()));

        TableColumn<Client, Void> colOrder = new TableColumn<>("New Order");
        colOrder.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("+");
            {
                btn.getStyleClass().add("btn-primary");
                btn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    dialogs.showAddOrderDialog(client, orderRefreshCallback, ClientsView.this::showError);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        TableColumn<Client, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<Client, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnHistory = new Button("History");
            private final HBox box = new HBox(5, btnEdit, btnDelete, btnHistory);

            {
                btnEdit.getStyleClass().addAll("modern-button", "button-secondary");
                btnDelete.getStyleClass().addAll("modern-button", "button-error");
                btnHistory.getStyleClass().addAll("modern-button", "button-secondary");
                
                btnEdit.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    dialogs.showEditClientDialog(client, ClientsView.this::loadClients, ClientsView.this::showError);
                });
                btnDelete.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    deleteClient(client);
                });
                btnHistory.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    if (historyOpener != null) {
                        historyOpener.open(client);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        table.getColumns().clear();
        //table.getColumns().add(colId);
        table.getColumns().add(colUsername);
        table.getColumns().add(colPhone);
        table.getColumns().add(colSource);
        table.getColumns().add(colAddress);
        table.getColumns().add(colOrder);
        table.getColumns().add(colActions);
        table.setItems(clientData);

        TextField txtSearch = new TextField();
        txtSearch.getStyleClass().add("modern-field");
        txtSearch.setPromptText("Search clients by name or phone...");
        Button btnNewClient = new Button("+ New Client");
        btnNewClient.getStyleClass().addAll("modern-button");
        btnNewClient.setOnAction(e -> dialogs.showAddClientDialog(this::loadClients, this::showError));
        HBox searchBar = new HBox(10, new Label("Search:"), txtSearch, btnNewClient);
        searchBar.getStyleClass().add("modern-container");
        searchBar.setPadding(new Insets(10));
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal == null ? "" : newVal.trim();
            clientData.clear();
            if (keyword.isEmpty()) {
                loadClients();
            } else {
                try {
                    clientData.addAll(clientDAO.findByUsernameOrPhone(keyword));
                } catch (SQLException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        BorderPane root = new BorderPane();
        root.getStyleClass().add("page-container");
        root.setTop(searchBar);
        root.setCenter(table);

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


    private void deleteClient(Client client) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected client?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                clientDAO.delete(client.getClientId());
                loadClients();
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    public interface HistoryOpener {
        void open(Client client);
    }
}
