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
        clientsTab.setContent(new ClientsView().getView());

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

        stage.setTitle("Proxy Shopping Management System");
        stage.setScene(new Scene(tabPane, 1000, 700));
        stage.show();
    }
}
