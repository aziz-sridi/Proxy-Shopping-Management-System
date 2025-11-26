package ui;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainView {

    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("modern-tabs");

        // Create OrdersView instance first so we can reference it
        OrdersView ordersView = new OrdersView();

        // Create ClientsView with both history opener and order refresh callback
        ClientsView clientsView = new ClientsView(
            client -> {
                ClientHistoryView historyView = new ClientHistoryView();
                tabPane.getTabs().add(createHistoryTab(historyView, client, tabPane));
                tabPane.getSelectionModel().selectLast();
            },
            () -> ordersView.refreshData() // Refresh orders when new order is added
        );

        Tab clientsTab = new Tab("Clients");
        clientsTab.setClosable(false);
        clientsTab.setContent(clientsView.getView());

        Tab ordersTab = new Tab("Orders");
        ordersTab.setClosable(false);
        ordersTab.setContent(ordersView.getView());

        Tab shipmentsTab = new Tab("Shipments");
        shipmentsTab.setClosable(false);
        shipmentsTab.setContent(new ShipmentsView().getView());

        Tab paymentsTab = new Tab("Payments");
        paymentsTab.setClosable(false);
        paymentsTab.setContent(new PaymentsView().getView());

        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(new DashboardView().getView());

        Tab settingsTab = new Tab("Settings");
        settingsTab.setClosable(false);
        settingsTab.setContent(new SettingsView().getView());

        tabPane.getTabs().addAll(clientsTab, ordersTab, shipmentsTab, paymentsTab, dashboardTab, settingsTab);

        Scene scene = new Scene(tabPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm()); // Default to light theme

        stage.setTitle("Proxy Shopping Management System");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private Tab createHistoryTab(ClientHistoryView historyView, model.Client client, TabPane tabPane) {
        Tab historyTab = new Tab("History: " + client.getUsername());
        historyTab.setClosable(true);
        historyTab.setContent(historyView.getView(client, () -> {
            tabPane.getTabs().remove(historyTab);
            tabPane.getSelectionModel().selectFirst();
        }));
        return historyTab;
    }
}
