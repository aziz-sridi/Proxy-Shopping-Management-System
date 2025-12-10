package Controller;

import service.api.IShipmentService;
import service.impl.ShipmentServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import model.Shipment;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private final IShipmentService shipmentService = new ShipmentServiceImpl();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Shipment> cbShipment;

    @FXML
    private PieChart revenueBreakdownChart;

    @FXML
    private BarChart<String, Number> costDistributionChart;

    @FXML
    private LineChart<String, Number> profitTrendChart;

    @FXML
    private AreaChart<String, Number> performanceChart;

    @FXML
    private Label lblTotalOrders;

    @FXML
    private Label lblTotalCostOfGoods;

    @FXML
    private Label lblTransportationCost;

    @FXML
    private Label lblOtherCosts;

    @FXML
    private Label lblTotalExpenses;

    @FXML
    private Label lblTotalRevenue;

    @FXML
    private Label lblNetProfit;

    @FXML
    private TextField txtTransportationCost;

    @FXML
    private TextField txtOtherCosts;

    @FXML
    private Button btnUpdateCosts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadShipments();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        cbShipment.setOnAction(e -> {
            Shipment selected = cbShipment.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateFinancialSummary(selected);
                updateAllCharts(selected);
                populateCostFields(selected);
            }
        });

        btnUpdateCosts.setOnAction(e -> updateShipmentCosts());
    }

    private void updateAllCharts(Shipment shipment) {
        try {
            // Get financial data
            double totalCostOfGoods = shipmentService.calculateTotalCostOfGoods(shipment);
            double totalRevenue = shipmentService.calculateTotalRevenue(shipment);
            double transportationCost = shipment.getTransportationCost();
            double otherCosts = shipment.getOtherCosts();
            double netProfit = shipmentService.calculateNetProfit(shipment);

            // Update charts using instance fields
            updateRevenueBreakdownChart(totalRevenue, totalCostOfGoods, netProfit);
            updateCostDistributionChart(totalCostOfGoods, transportationCost, otherCosts);
            updateProfitTrendChart();
            updatePerformanceChart();

        } catch (SQLException e) {
            showError("Failed to update charts: " + e.getMessage());
        }
    }

    private void updateRevenueBreakdownChart(double revenue, double costs, double profit) {
        revenueBreakdownChart.getData().clear();
        revenueBreakdownChart.getData().addAll(
                new PieChart.Data("Net Profit", Math.max(0, profit)),
                new PieChart.Data("Cost of Goods", costs),
                new PieChart.Data("Operating Costs", Math.max(0, revenue - costs - Math.max(0, profit)))
        );
    }

    private void updateCostDistributionChart(double costOfGoods, double transportation, double others) {
        costDistributionChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Costs");

        series.getData().add(new XYChart.Data<>("Cost of Goods", costOfGoods));
        series.getData().add(new XYChart.Data<>("Transportation", transportation));
        series.getData().add(new XYChart.Data<>("Other Costs", others));

        costDistributionChart.getData().add(series);
    }

    private void updateProfitTrendChart() {
        try {
            profitTrendChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Profit Trend");

            // Show profit comparison across all shipments
            for (Shipment shipment : shipmentData) {
                double profit = shipmentService.calculateNetProfit(shipment);
                String label = shipment.getBatchName().length() > 8 ?
                        shipment.getBatchName().substring(0, 8) + "..." : shipment.getBatchName();
                series.getData().add(new XYChart.Data<>(label, profit));
            }

            profitTrendChart.getData().add(series);

        } catch (SQLException e) {
            // Handle silently for chart updates
        }
    }

    private void updatePerformanceChart() {
        try {
            performanceChart.getData().clear();

            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenue");

            XYChart.Series<String, Number> costSeries = new XYChart.Series<>();
            costSeries.setName("Costs");

            // Show performance comparison across shipments
            for (Shipment shipment : shipmentData) {
                double revenue = shipmentService.calculateTotalRevenue(shipment);
                double expenses = shipmentService.calculateTotalExpenses(shipment);
                String label = shipment.getBatchName().length() > 6 ?
                        shipment.getBatchName().substring(0, 6) + "..." : shipment.getBatchName();

                revenueSeries.getData().add(new XYChart.Data<>(label, revenue));
                costSeries.getData().add(new XYChart.Data<>(label, expenses));
            }

            performanceChart.getData().add(revenueSeries);
            performanceChart.getData().add(costSeries);

        } catch (SQLException e) {
            // Handle silently for chart updates
        }
    }

    private void loadShipments() {
        shipmentData.clear();
        try {
            shipmentData.addAll(shipmentService.getAllShipments());
            cbShipment.setItems(shipmentData);
        } catch (SQLException e) {
            showError("Failed to load shipments: " + e.getMessage());
        }
    }

    /**
     * Public method to refresh dashboard data - can be called from other views
     */
    public void refreshData() {
        loadShipments();
        Shipment selected = cbShipment.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateFinancialSummary(selected);
            updateAllCharts(selected);
        }
    }

    private void updateFinancialSummary(Shipment shipment) {
        try {
            // Calculate all financial metrics using the exact formulas requested
            int totalOrders = shipmentService.calculateTotalOrders(shipment);
            double totalCostOfGoods = shipmentService.calculateTotalCostOfGoods(shipment);
            double totalRevenue = shipmentService.calculateTotalRevenue(shipment);
            double transportationCost = shipment.getTransportationCost();
            double otherCosts = shipment.getOtherCosts();
            double totalExpenses = shipmentService.calculateTotalExpenses(shipment);
            double netProfit = shipmentService.calculateNetProfit(shipment);

            // Update all detailed breakdown labels
            lblTotalOrders.setText(String.valueOf(totalOrders));
            lblTotalCostOfGoods.setText(String.format("%.2f TND", totalCostOfGoods));
            lblTransportationCost.setText(String.format("%.2f TND", transportationCost));
            lblOtherCosts.setText(String.format("%.2f TND", otherCosts));
            lblTotalRevenue.setText(String.format("%.2f TND", totalRevenue));
            lblTotalExpenses.setText(String.format("%.2f TND", totalExpenses));

            // Update profit with dynamic color coding
            lblNetProfit.setText(String.format("%.2f TND", netProfit));
            if (netProfit > 0) {
                lblNetProfit.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");
            } else if (netProfit < 0) {
                lblNetProfit.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #E74C3C;");
            } else {
                lblNetProfit.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F39C12;");
            }

        } catch (SQLException e) {
            showError("Failed to calculate financial summary: " + e.getMessage());
            resetFinancialSummary();
        }
    }

    private void resetFinancialSummary() {
        lblTotalOrders.setText("0");
        lblTotalCostOfGoods.setText("0.00 TND");
        lblTransportationCost.setText("0.00 TND");
        lblOtherCosts.setText("0.00 TND");
        lblTotalRevenue.setText("0.00 TND");
        lblTotalExpenses.setText("0.00 TND");
        lblNetProfit.setText("0.00 TND");
        lblNetProfit.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
    }

    private void populateCostFields(Shipment shipment) {
        txtTransportationCost.setText(String.format("%.2f", shipment.getTransportationCost()));
        txtOtherCosts.setText(String.format("%.2f", shipment.getOtherCosts()));
    }

    private void updateShipmentCosts() {
        Shipment selected = cbShipment.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a shipment first.");
            return;
        }

        try {
            double transportationCost = Double.parseDouble(txtTransportationCost.getText());
            double otherCosts = Double.parseDouble(txtOtherCosts.getText());

            selected.setTransportationCost(transportationCost);
            selected.setOtherCosts(otherCosts);

            shipmentService.updateShipment(selected);
            updateFinancialSummary(selected);

            showSuccess("Shipment costs updated successfully!");

        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values for costs.");
        } catch (SQLException e) {
            showError("Failed to update shipment costs: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
