package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Client;
import service.AuthService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main ViewController - coordinates all view tabs and their controllers
 * All views are now loaded from FXML for consistency
 */
public class MainController implements Initializable {

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

    @FXML
    private Tab adminTab;

    @FXML
    private Tab logsTab;

    @FXML
    private Label userInfoLabel;

    @FXML
    private Button logoutButton;

    // Hold references to view controllers for refresh callbacks
    private OrdersController ordersViewController;
    private PaymentsController paymentsViewController;
    private ShipmentsController shipmentsViewController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AuthService authService = AuthService.getInstance();
        
        // Set user info in header
        if (authService.isLoggedIn()) {
            userInfoLabel.setText("User: " + authService.getCurrentUser().getUsername() + 
                " (" + authService.getCurrentUser().getRole() + ")");
        }
        
        if (authService.isAdmin()) {
            // Admin users only see User Management and Logs
            removeBusinessTabs();
            loadAdminView();
            loadLogsView();
        } else {
            // Regular users see all business tabs
            loadOrdersView();
            loadClientsView();
            loadShipmentsView();
            loadPaymentsView();
            loadSettingsView();
            // Dashboard is loaded via FXML include in MainView.fxml
            
            // Hide admin and logs tabs for non-admin users
            tabPane.getTabs().removeAll(adminTab, logsTab);
        }
    }

    @FXML
    public void handleLogout() {
        try {
            AuthService.getInstance().logout();
            
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(getClass().getResource("/ui/app.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/ui/light-theme.css").toExternalForm());

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("Proxy Shopping Management - Login");
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void removeBusinessTabs() {
        // Remove all business-related tabs for admin users
        tabPane.getTabs().removeAll(clientsTab, ordersTab, shipmentsTab, paymentsTab, dashboardTab, settingsTab);
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
            ClientsController clientsController = loader.getController();

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
            
            // Set up shipment refresh callback so when shipments are added/edited/deleted in shipments view, orders view updates
            shipmentsViewController.setOrderRefreshCallback(() -> {
                if (ordersViewController != null) {
                    ordersViewController.refreshShipments();
                }
            });
            
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

    private void loadAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/AdminUsersView.fxml"));
            Parent adminView = loader.load();
            adminTab.setContent(adminView);
            adminTab.setText("User Management");
        } catch (IOException e) {
            System.err.println("Error loading AdminUsersView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLogsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/LogsView.fxml"));
            Parent logsView = loader.load();
            logsTab.setContent(logsView);
        } catch (IOException e) {
            System.err.println("Error loading LogsView: " + e.getMessage());
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
            ClientHistoryController historyController = loader.getController();
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
