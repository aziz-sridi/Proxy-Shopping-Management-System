package Controller;

import service.IClientService;
import service.ClientServiceImpl;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Client;
import ui.ClientDialogs;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ClientsController implements Initializable {

    private final IClientService clientService = new ClientServiceImpl();
    private final ClientDialogs dialogs = new ClientDialogs(clientService);
    private Consumer<Client> historyOpener;
    private Runnable orderRefreshCallback;
    private final ObservableList<Client> clientData = FXCollections.observableArrayList();

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnNewClient;

    @FXML
    private TableView<Client> table;

    @FXML
    private TableColumn<Client, Number> colId;

    @FXML
    private TableColumn<Client, String> colUsername;

    @FXML
    private TableColumn<Client, String> colPhone;

    @FXML
    private TableColumn<Client, String> colSource;

    @FXML
    private TableColumn<Client, String> colAddress;

    @FXML
    private TableColumn<Client, Void> colOrder;

    @FXML
    private TableColumn<Client, Void> colActions;

    public void setHistoryOpener(Consumer<Client> historyOpener) {
        this.historyOpener = historyOpener;
    }

    public void setOrderRefreshCallback(Runnable orderRefreshCallback) {
        this.orderRefreshCallback = orderRefreshCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupEventHandlers();
        table.setItems(clientData);
        loadClients();
    }

    private void setupColumns() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getClientId()));
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colSource.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSource()));
        colAddress.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAddress()));

        colOrder.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("+");
            {
                btn.getStyleClass().add("btn-primary");
                btn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    dialogs.showAddOrderDialog(client, orderRefreshCallback, ClientsController.this::showError);
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

        colActions.setCellFactory(col -> new TableCell<Client, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnHistory = new Button("History");
            private final HBox box = new HBox(5, btnEdit, btnDelete, btnHistory);

            {
                btnEdit.getStyleClass().addAll("app-button", "button-secondary");
                btnDelete.getStyleClass().addAll("app-button", "button-error");
                btnHistory.getStyleClass().addAll("app-button", "button-secondary");

                btnEdit.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    dialogs.showEditClientDialog(client, ClientsController.this::loadClients, ClientsController.this::showError);
                });
                btnDelete.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    deleteClient(client);
                });
                btnHistory.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    if (historyOpener != null) {
                        historyOpener.accept(client);
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
    }

    private void setupEventHandlers() {
        btnNewClient.setOnAction(e -> dialogs.showAddClientDialog(this::loadClients, this::showError));

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal == null ? "" : newVal.trim();
            clientData.clear();
            if (keyword.isEmpty()) {
                loadClients();
            } else {
                try {
                    clientData.addAll(clientService.searchClients(keyword));
                } catch (SQLException ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    private void loadClients() {
        clientData.clear();
        try {
            clientData.addAll(clientService.getAllClients());
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
                clientService.deleteClient(client.getClientId());
                loadClients();
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }
}
