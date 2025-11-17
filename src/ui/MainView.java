package ui;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainView {

    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab clientsTab = new Tab("Clients");
        clientsTab.setClosable(false);
        clientsTab.setContent(new ClientsView(client -> {
            ClientHistoryView historyView = new ClientHistoryView();
            tabPane.getTabs().add(createHistoryTab(historyView, client, tabPane));
            tabPane.getSelectionModel().selectLast();
        }).getView());

        Tab ordersTab = new Tab("Orders");
        ordersTab.setClosable(false);
        ordersTab.setContent(new OrdersView().getView());

        Tab shipmentsTab = new Tab("Shipments");
        shipmentsTab.setClosable(false);
        shipmentsTab.setContent(new ShipmentsView().getView());

        Tab paymentsTab = new Tab("Payments");
        paymentsTab.setClosable(false);
        paymentsTab.setContent(new PaymentsView().getView());

        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(new DashboardView().getView());

        tabPane.getTabs().addAll(clientsTab, ordersTab, shipmentsTab, paymentsTab, dashboardTab);

        Scene scene = new Scene(tabPane, 1000, 700);

        stage.setTitle("Proxy Shopping Management System");
        stage.setScene(scene);
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
