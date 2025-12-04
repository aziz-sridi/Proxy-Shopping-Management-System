package ui.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.Client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main ViewController - coordinates all view tabs and their controllers
 * All views are now loaded from FXML for consistency
 */
public class MainViewController implements Initializable {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab clientsTab;

    @FXML
    private Tab ordersTab;

    @FXML
    private Tab shipmentsTab;

    @FXML
    private Tab paymentsTab;

    @FXML
    private Tab dashboardTab;

    @FXML
    private Tab settingsTab;

    // Hold references to view controllers for refresh callbacks
    private OrdersViewController ordersViewController;
    private PaymentsViewController paymentsViewController;
    private ShipmentsViewController shipmentsViewController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadOrdersView();
        loadClientsView();
        loadShipmentsView();
        loadPaymentsView();
        loadSettingsView();
        // Dashboard is loaded via FXML include in MainView.fxml
    }

    private void loadOrdersView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/OrdersView.fxml"));
            Parent ordersView = loader.load();
            ordersViewController = loader.getController();
            
            // Set up payment refresh callback so when payments are added in orders view, payments view updates
            ordersViewController.setPaymentRefreshCallback(() -> {
                if (paymentsViewController != null) {
                    paymentsViewController.refreshData();
                }
            });
            
            // Set up shipment refresh callback
            ordersViewController.setShipmentRefreshCallback(() -> {
                if (shipmentsViewController != null) {
                    shipmentsViewController.refreshData();
                }
            });
            
            ordersTab.setContent(ordersView);
        } catch (IOException e) {
            System.err.println("Error loading OrdersView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadClientsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/ClientsView.fxml"));
            Parent clientsView = loader.load();
            ClientsViewController clientsController = loader.getController();

            // Set up history opener callback
            clientsController.setHistoryOpener(client -> openClientHistoryTab(client));

            // Set up order refresh callback
            clientsController.setOrderRefreshCallback(() -> {
                if (ordersViewController != null) {
                    ordersViewController.refreshData();
                }
                if (paymentsViewController != null) {
                    paymentsViewController.refreshData();
                }
            });

            clientsTab.setContent(clientsView);
        } catch (IOException e) {
            System.err.println("Error loading ClientsView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadShipmentsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/ShipmentsView.fxml"));
            Parent shipmentsView = loader.load();
            shipmentsViewController = loader.getController();
            shipmentsTab.setContent(shipmentsView);
        } catch (IOException e) {
            System.err.println("Error loading ShipmentsView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPaymentsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/PaymentsView.fxml"));
            Parent paymentsView = loader.load();
            paymentsViewController = loader.getController();
            
            // Set up order refresh callback so when payments are edited/deleted in payments view, orders view updates
            paymentsViewController.setOrderRefreshCallback(() -> {
                if (ordersViewController != null) {
                    ordersViewController.refreshData();
                }
            });
            
            paymentsTab.setContent(paymentsView);
        } catch (IOException e) {
            System.err.println("Error loading PaymentsView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSettingsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/SettingsView.fxml"));
            Parent settingsView = loader.load();
            settingsTab.setContent(settingsView);
        } catch (IOException e) {
            System.err.println("Error loading SettingsView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens a new tab showing the client's order and payment history
     */
    private void openClientHistoryTab(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/ClientHistoryView.fxml"));
            Parent historyView = loader.load();
            ClientHistoryViewController historyController = loader.getController();
            historyController.setClient(client);

            Tab historyTab = new Tab("History: " + client.getUsername());
            historyTab.setClosable(true);
            historyTab.setContent(historyView);

            tabPane.getTabs().add(historyTab);
            tabPane.getSelectionModel().selectLast();
        } catch (IOException e) {
            System.err.println("Error loading ClientHistoryView: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
