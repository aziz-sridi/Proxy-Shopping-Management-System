package ui;

import dao.ShipmentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Shipment;
import service.ShipmentFinancialService;

import java.sql.SQLException;

public class DashboardView {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final ShipmentFinancialService financialService = new ShipmentFinancialService();
    private final ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();
    
    // Chart references
    private PieChart revenueBreakdownChart;
    private LineChart<String, Number> profitTrendChart;
    private BarChart<String, Number> costDistributionChart;
    private AreaChart<String, Number> performanceChart;
    
    // Financial summary labels - detailed breakdown
    private Label lblTotalOrders = new Label("0");
    private Label lblTotalCostOfGoods = new Label("0.00 TND");
    private Label lblTransportationCost = new Label("0.00 TND");
    private Label lblOtherCosts = new Label("0.00 TND");
    private Label lblTotalRevenue = new Label("0.00 TND");
    private Label lblTotalExpenses = new Label("0.00 TND");
    private Label lblNetProfit = new Label("0.00 TND");
    
    // Editable cost fields
    private TextField txtTransportationCost = new TextField();
    private TextField txtOtherCosts = new TextField();
    private Button btnUpdateCosts = new Button("💾 Update Costs");
    
    private ComboBox<Shipment> cbShipment = new ComboBox<>();

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("page-container");
        
        // Top section: Title and shipment selection
        VBox topSection = createHeaderSection();
        
        // Center section: Split between charts and detailed financial breakdown
        HBox centerSection = createMainContentSection();
        
        // Bottom section: Cost editing
        VBox bottomSection = createCostEditingSection();
        
        root.setTop(topSection);
        root.setCenter(centerSection);
        root.setBottom(bottomSection);
        
        loadShipments();
        setupEventHandlers();
        
        return root;
    }
    
    private HBox createMainContentSection() {
        // Left side: Charts (60% width)
        HBox chartsSection = createChartsSection();
        chartsSection.setPrefWidth(800);
        
        // Right side: Detailed Financial Breakdown (40% width)
        VBox detailedBreakdownSection = createDetailedFinancialBreakdownSection();
        detailedBreakdownSection.setPrefWidth(500);
        
        HBox mainContent = new HBox(20, chartsSection, detailedBreakdownSection);
        mainContent.setPadding(new Insets(20));
        
        return mainContent;
    }
    
    private VBox createDetailedFinancialBreakdownSection() {
        Label breakdownTitle = new Label("📋 Detailed Financial Breakdown");
        breakdownTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        // Create detailed breakdown grid
        GridPane detailsGrid = createFinancialDetailsGrid();
        
        VBox breakdownSection = new VBox(15, breakdownTitle, detailsGrid);
        breakdownSection.setStyle("-fx-background-color: white; -fx-border-color: #E8E8E8; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 20px;");
        
        return breakdownSection;
    }
    
    private GridPane createFinancialDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        
        // Row headers
        int row = 0;
        
        // Orders section
        grid.add(createSectionHeader("📦 Order Analytics"), 0, row++, 2, 1);
        grid.add(createDetailRow("Total Orders:", lblTotalOrders), 0, row++, 2, 1);
        
        grid.add(new Separator(), 0, row++, 2, 1);
        
        // Cost breakdown section
        grid.add(createSectionHeader("💰 Cost Breakdown"), 0, row++, 2, 1);
        grid.add(createDetailRow("Cost of Goods:", lblTotalCostOfGoods), 0, row++, 2, 1);
        grid.add(createDetailRow("Transportation Cost:", lblTransportationCost), 0, row++, 2, 1);
        grid.add(createDetailRow("Other Costs:", lblOtherCosts), 0, row++, 2, 1);
        grid.add(createDetailRow("Total Expenses:", lblTotalExpenses), 0, row++, 2, 1);
        
        grid.add(new Separator(), 0, row++, 2, 1);
        
        // Revenue section
        grid.add(createSectionHeader("📈 Revenue Analysis"), 0, row++, 2, 1);
        grid.add(createDetailRow("Total Revenue:", lblTotalRevenue), 0, row++, 2, 1);
        
        grid.add(new Separator(), 0, row++, 2, 1);
        
        // Profit section
        grid.add(createSectionHeader("🎯 Profitability"), 0, row++, 2, 1);
        VBox profitBox = new VBox(5);
        Label profitLabel = new Label("Net Profit:");
        profitLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        lblNetProfit.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        profitBox.getChildren().addAll(profitLabel, lblNetProfit);
        grid.add(profitBox, 0, row++, 2, 1);
        
        return grid;
    }
    
    private Label createSectionHeader(String text) {
        Label header = new Label(text);
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495E; -fx-background-color: #ECF0F1; -fx-padding: 8px; -fx-background-radius: 4px;");
        return header;
    }
    
    private HBox createDetailRow(String label, Label valueLabel) {
        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D; -fx-font-weight: bold;");
        titleLabel.setPrefWidth(150);
        
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        HBox row = new HBox(10, titleLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        
        return row;
    }
    
    private VBox createCostEditingSection() {
        Label editTitle = new Label("⚙️ Cost Management");
        editTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        // Professional form layout
        GridPane editForm = new GridPane();
        editForm.setHgap(15);
        editForm.setVgap(15);
        editForm.setPadding(new Insets(20));
        editForm.setStyle("-fx-background-color: white; -fx-border-color: #E8E8E8; -fx-border-width: 1px; -fx-border-radius: 8px;");
        
        Label transportLabel = new Label("Transportation Cost (TND):");
        transportLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        txtTransportationCost.setPromptText("Enter transportation cost");
        txtTransportationCost.setPrefWidth(200);
        txtTransportationCost.setStyle("-fx-border-color: #BDC3C7; -fx-border-radius: 4px;");
        
        Label otherLabel = new Label("Other Costs (TND):");
        otherLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        txtOtherCosts.setPromptText("Enter other costs");
        txtOtherCosts.setPrefWidth(200);
        txtOtherCosts.setStyle("-fx-border-color: #BDC3C7; -fx-border-radius: 4px;");
        
        btnUpdateCosts.setText("💾 Update Costs");
        btnUpdateCosts.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");
        btnUpdateCosts.setPrefWidth(150);
        
        editForm.add(transportLabel, 0, 0);
        editForm.add(txtTransportationCost, 1, 0);
        editForm.add(otherLabel, 2, 0);
        editForm.add(txtOtherCosts, 3, 0);
        editForm.add(btnUpdateCosts, 4, 0);
        
        VBox editSection = new VBox(15, editTitle, editForm);
        editSection.setPadding(new Insets(20));
        
        return editSection;
    }
    
    private VBox createHeaderSection() {
        Label titleLabel = new Label("📊 Financial Analytics Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        Label subtitleLabel = new Label("Comprehensive shipment profitability analysis");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D;");
        
        Label selectLabel = new Label("Select Shipment:");
        selectLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        cbShipment.setPromptText("Choose a shipment for detailed analysis");
        cbShipment.setPrefWidth(400);
        cbShipment.setStyle("-fx-font-size: 12px;");
        
        HBox selectionBox = new HBox(15, selectLabel, cbShipment);
        selectionBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox headerBox = new VBox(10, titleLabel, subtitleLabel, new Separator(), selectionBox);
        headerBox.setPadding(new Insets(20));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-width: 0 0 2 0;");
        
        return headerBox;
    }
    
    private HBox createChartsSection() {
        // Revenue vs Cost Breakdown Chart
        revenueBreakdownChart = createRevenueBreakdownChart();
        
        // Profit Trend Line Chart
        profitTrendChart = createProfitTrendChart();
        
        // Cost Distribution Bar Chart
        costDistributionChart = createCostDistributionChart();
        
        // Shipment Performance Comparison
        performanceChart = createPerformanceChart();
        
        // Organize charts in a professional 2x2 grid
        VBox leftColumn = new VBox(15, 
            createChartContainer("💰 Revenue Breakdown", revenueBreakdownChart),
            createChartContainer("📊 Cost Distribution", costDistributionChart)
        );
        
        VBox rightColumn = new VBox(15,
            createChartContainer("📈 Profit Trend", profitTrendChart),
            createChartContainer("🚀 Performance Comparison", performanceChart)
        );
        
        HBox chartsSection = new HBox(20, leftColumn, rightColumn);
        chartsSection.setPadding(new Insets(20));
        
        return chartsSection;
    }
    
    private VBox createChartContainer(String title, javafx.scene.Node chart) {
        Label chartTitle = new Label(title);
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        VBox container = new VBox(10, chartTitle, chart);
        container.setStyle("-fx-background-color: white; -fx-border-color: #E8E8E8; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 15px;");
        container.setPrefSize(380, 300);
        
        return container;
    }
    
    private PieChart createRevenueBreakdownChart() {
        PieChart chart = new PieChart();
        chart.setTitle("");
        chart.setLegendVisible(true);
        chart.setPrefSize(350, 250);
        return chart;
    }
    
    private LineChart<String, Number> createProfitTrendChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Shipments");
        yAxis.setLabel("Profit (TND)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setCreateSymbols(true);
        chart.setPrefSize(350, 250);
        return chart;
    }
    
    private BarChart<String, Number> createCostDistributionChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Cost Categories");
        yAxis.setLabel("Amount (TND)");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setLegendVisible(false);
        chart.setPrefSize(350, 250);
        return chart;
    }
    
    private AreaChart<String, Number> createPerformanceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Shipments");
        yAxis.setLabel("Performance Metrics");
        
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setCreateSymbols(false);
        chart.setPrefSize(350, 250);
        return chart;
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
            double totalCostOfGoods = financialService.calculateTotalCostOfGoods(shipment);
            double totalRevenue = financialService.calculateTotalRevenue(shipment);
            double transportationCost = shipment.getTransportationCost();
            double otherCosts = shipment.getOtherCosts();
            double netProfit = financialService.calculateNetProfit(shipment);
            
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
                double profit = financialService.calculateNetProfit(shipment);
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
                double revenue = financialService.calculateTotalRevenue(shipment);
                double expenses = financialService.calculateTotalExpenses(shipment);
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
            shipmentData.addAll(shipmentDAO.findAll());
            cbShipment.setItems(shipmentData);
        } catch (SQLException e) {
            showError("Failed to load shipments: " + e.getMessage());
        }
    }
    
    private void updateFinancialSummary(Shipment shipment) {
        try {
            // Calculate all financial metrics using the exact formulas requested
            int totalOrders = financialService.calculateTotalOrders(shipment);
            double totalCostOfGoods = financialService.calculateTotalCostOfGoods(shipment);
            double totalRevenue = financialService.calculateTotalRevenue(shipment);
            double transportationCost = shipment.getTransportationCost();
            double otherCosts = shipment.getOtherCosts();
            double totalExpenses = financialService.calculateTotalExpenses(shipment);
            double netProfit = financialService.calculateNetProfit(shipment);
            
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
            
            shipmentDAO.update(selected);
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
