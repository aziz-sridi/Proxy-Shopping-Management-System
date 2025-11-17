package ui;

import dao.CurrencyRateDAO;
import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Order;

import java.sql.SQLException;

public class DashboardView {

    private final OrderDAO orderDAO = new OrderDAO();
    private final CurrencyRateDAO currencyRateDAO = new CurrencyRateDAO();

    private final ObservableList<Order> orderData = FXCollections.observableArrayList();

    public BorderPane getView() {
        // currency rate form
        TextField txtBase = new TextField("EUR");
        TextField txtTarget = new TextField("TND");
        TextField txtOriginalRate = new TextField();
        TextField txtCustomRate = new TextField();
        Button btnSaveRate = new Button("Save Rate");
        btnSaveRate.setOnAction(e -> saveCurrencyRate(txtBase.getText(), txtTarget.getText(), txtOriginalRate.getText(), txtCustomRate.getText()));

        Label lblCurrentOriginal = new Label();
        Label lblCurrentCustom = new Label();

        Button btnLoadCurrent = new Button("Load Current");
        btnLoadCurrent.setOnAction(e -> loadCurrentRate(txtBase.getText(), txtTarget.getText(), lblCurrentOriginal, lblCurrentCustom));

        Button btnRateHistory = new Button("Open Rate History");
        btnRateHistory.setOnAction(e -> openRateHistoryPopup(txtBase.getText(), txtTarget.getText()));

        GridPane rateForm = new GridPane();
        rateForm.setHgap(5);
        rateForm.setVgap(5);
        rateForm.setPadding(new Insets(10));
        rateForm.addRow(0, new Label("Base:"), txtBase, new Label("Target:"), txtTarget);
        rateForm.addRow(1, new Label("Original Rate:"), txtOriginalRate, new Label("Custom Rate:"), txtCustomRate);
        HBox rateButtons = new HBox(10, btnLoadCurrent, btnSaveRate, btnRateHistory);
        rateForm.add(rateButtons, 1, 2, 4, 1);

        Label lblRevenue = new Label();
        Label lblSpendings = new Label();
        Label lblProfit = new Label();

        VBox statsBox = new VBox(5, lblCurrentOriginal, lblCurrentCustom, lblRevenue, lblSpendings, lblProfit);
        statsBox.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> profitChart = new BarChart<>(xAxis, yAxis);
        profitChart.setTitle("Revenue vs Spendings");
        xAxis.setLabel("Metric");
        yAxis.setLabel("Amount");

        VBox topSection = new VBox(10, new Label("Currency & Rates"), rateForm, statsBox);
        topSection.setPadding(new Insets(10));

        VBox middleSection = new VBox(10, new Label("Revenue / Spendings"), profitChart);
        middleSection.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(middleSection);

        loadOrders();
        updateStats(lblRevenue, lblSpendings, lblProfit, profitChart);
        return root;
    }

    private void loadOrders() {
        orderData.clear();
        try {
            orderData.addAll(orderDAO.findAll());
        } catch (SQLException e) {
            // simple dashboard, ignore for now
        }
    }

    private void saveCurrencyRate(String base, String target, String originalText, String customText) {
        try {
            double original = Double.parseDouble(originalText);
            double custom = Double.parseDouble(customText);
            var rate = new model.CurrencyRate();
            rate.setBaseCurrency(base);
            rate.setTargetCurrency(target);
            rate.setOriginalRate(original);
            rate.setCustomRate(custom);
            currencyRateDAO.insert(rate);
        } catch (Exception e) {
            // keep simple: no popup here to avoid UI dependency
        }
    }

    private void loadCurrentRate(String base, String target, Label lblOriginal, Label lblCustom) {
        try {
            var rate = currencyRateDAO.findLatest(base, target);
            if (rate != null) {
                lblOriginal.setText("Current Original Rate: " + rate.getOriginalRate());
                lblCustom.setText("Current Selling Rate: " + rate.getCustomRate());
            } else {
                lblOriginal.setText("Current Original Rate: -");
                lblCustom.setText("Current Selling Rate: -");
            }
        } catch (SQLException e) {
            lblOriginal.setText("Current Original Rate: error");
            lblCustom.setText("Current Selling Rate: error");
        }
    }

    private void updateStats(Label lblRevenue, Label lblSpendings, Label lblProfit, BarChart<String, Number> chart) {
        double totalRevenue = orderData.stream().mapToDouble(Order::getSellingPrice).sum();
        double totalSpendings = orderData.stream().mapToDouble(Order::getOriginalPrice).sum();
        double profit = totalRevenue - totalSpendings;

        lblRevenue.setText("Total Revenue: " + String.format("%.2f", totalRevenue));
        lblSpendings.setText("Total Spendings: " + String.format("%.2f", totalSpendings));
        lblProfit.setText("Profit: " + String.format("%.2f", profit));

        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Revenue", totalRevenue));
        series.getData().add(new XYChart.Data<>("Spendings", totalSpendings));
        series.getData().add(new XYChart.Data<>("Profit", profit));
        chart.getData().add(series);
    }

    private void openRateHistoryPopup(String base, String target) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Rate History for " + base + "/" + target);

        TableView<model.CurrencyRate> table = new TableView<>();
        TableColumn<model.CurrencyRate, String> colPair = new TableColumn<>("Pair");
        colPair.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getBaseCurrency() + "/" + c.getValue().getTargetCurrency()));
        TableColumn<model.CurrencyRate, Number> colOrig = new TableColumn<>("Original");
        colOrig.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getOriginalRate()));
        TableColumn<model.CurrencyRate, Number> colCustom = new TableColumn<>("Custom");
        colCustom.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getCustomRate()));

        table.getColumns().add(colPair);
        table.getColumns().add(colOrig);
        table.getColumns().add(colCustom);

        try {
            var list = currencyRateDAO.findHistory(base, target);
            table.getItems().setAll(list);
        } catch (SQLException ex) {
            table.getItems().clear();
        }

        VBox box = new VBox(5, table);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
