package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import model.CurrencyRate;
import model.Settings;
import service.impl.CurrencyRateServiceImpl;
import util.SettingsManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TextField txtSellingMultiplier;
    @FXML private Label lblCurrentConversionRate;
    @FXML private Label lblCurrentSellingMultiplier;
    @FXML private Label lblCalculationExample;
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    @FXML private Button btnReload;
    @FXML private Button btnRefreshRate;
    @FXML private Button btnViewHistory;
    
    private final CurrencyRateServiceImpl currencyRateService = new CurrencyRateServiceImpl();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add listener for real-time example update
        txtSellingMultiplier.textProperty().addListener((obs, old, val) -> updateCalculationExample());

        // Load current settings
        refreshCurrentSettings();
        updateCalculationExample();
    }

    private void refreshCurrentSettings() {
        Settings settings = SettingsManager.getCurrentSettings();
        lblCurrentConversionRate.setText(String.format("Conversion Rate (EUR→TND): %.4f (wllh api )", settings.getConversionRate()));
        lblCurrentSellingMultiplier.setText("Selling Multiplier: " + settings.getSellingMultiplier());
    }

    @FXML
    private void handleSave() {
        try {
            double sellingMultiplier = Double.parseDouble(txtSellingMultiplier.getText().trim());

            if (sellingMultiplier <= 0) {
                showError("Selling multiplier must be greater than 0");
                return;
            }

            Settings current = SettingsManager.getCurrentSettings();
            SettingsManager.updateSettings(current.getConversionRate(), sellingMultiplier);
            refreshCurrentSettings();
            updateCalculationExample();
            showSuccess("Selling multiplier saved successfully!");

        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric value");
        }
    }

    @FXML
    private void handleReset() {
        Settings defaults = new Settings();
        txtSellingMultiplier.setText(String.valueOf(defaults.getSellingMultiplier()));
        updateCalculationExample();
    }

    @FXML
    private void handleReload() {
        Settings settings = SettingsManager.getCurrentSettings();
        txtSellingMultiplier.setText(String.valueOf(settings.getSellingMultiplier()));
        updateCalculationExample();
    }
    
    @FXML
    private void handleRefreshRate() {
        try {
            showInfo("Fetching latest EUR→TND rate from API...");
            CurrencyRate rate = currencyRateService.refreshLatestRateFromApi("EUR", "TND");
            refreshCurrentSettings();
            showSuccess(String.format("Rate updated successfully!\nNew rate: %.4f TND per EUR", rate.getCustomRate()));
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (IOException e) {
            showError("Failed to fetch rate from API: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewHistory() {
        try {
            List<CurrencyRate> history = currencyRateService.getRateHistory("EUR", "TND");
            if (history.isEmpty()) {
                showInfo("No rate history available yet.\nClick 'Refresh Rate' to fetch the first rate.");
                return;
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("EUR → TND Rate History");
            dialog.setHeaderText("Historical Exchange Rates");
            
            VBox content = new VBox(10);
            content.setPadding(new Insets(15));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (CurrencyRate rate : history) {
                String dateStr = rate.getUpdatedAt() != null ? rate.getUpdatedAt().format(formatter) : "Unknown";
                Label lbl = new Label(String.format("%s: %.4f TND per EUR", dateStr, rate.getCustomRate()));
                lbl.setStyle("-fx-font-family: monospace;");
                content.getChildren().add(lbl);
            }
            
            ScrollPane scroll = new ScrollPane(content);
            scroll.setPrefSize(500, 300);
            scroll.setFitToWidth(true);
            
            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (SQLException e) {
            showError("Failed to load rate history: " + e.getMessage());
        }
    }

    private void updateCalculationExample() {
        try {
            String multiplierText = txtSellingMultiplier.getText();
            if (multiplierText == null || multiplierText.trim().isEmpty()) {
                lblCalculationExample.setText("Enter a selling multiplier to see calculation example");
                return;
            }

            double multiplier = Double.parseDouble(multiplierText.trim());

            String example = String.format(
                "Example with EUR 18.50, Quantity 2, Payment: Deposit%n%n" +
                "Selling Price TND = 18.50 EUR × %.2f = %.2f TND%n" +
                "Expected Total TND = %.2f × 2 = %.2f TND%n" +
                "Deposit TND = %.2f × 0.5 = %.2f TND",
                multiplier,
                18.50 * multiplier,
                18.50 * multiplier,
                18.50 * multiplier * 2,
                18.50 * multiplier * 2,
                18.50 * multiplier * 2 * 0.5
            );

            lblCalculationExample.setText(example);

        } catch (NumberFormatException e) {
            lblCalculationExample.setText("Invalid multiplier value");
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
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
