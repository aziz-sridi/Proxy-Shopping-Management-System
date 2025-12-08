package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Settings;
import util.SettingsManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ViewController for SettingsView - handles application settings management
 */
public class SettingsController implements Initializable {

    @FXML private TextField txtConversionRate;
    @FXML private TextField txtSellingMultiplier;
    @FXML private Label lblCurrentConversionRate;
    @FXML private Label lblCurrentSellingMultiplier;
    @FXML private Label lblCalculationExample;
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    @FXML private Button btnReload;

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
        lblCurrentConversionRate.setText("Conversion Rate: " + settings.getConversionRate());
        lblCurrentSellingMultiplier.setText("Selling Multiplier: " + settings.getSellingMultiplier());
    }

    @FXML
    private void handleSave() {
        try {
            double conversionRate = Double.parseDouble(txtConversionRate.getText().trim());
            double sellingMultiplier = Double.parseDouble(txtSellingMultiplier.getText().trim());

            if (conversionRate <= 0 || sellingMultiplier <= 0) {
                showError("Values must be greater than 0");
                return;
            }

            SettingsManager.updateSettings(conversionRate, sellingMultiplier);
            refreshCurrentSettings();
            updateCalculationExample();
            showSuccess("Settings saved successfully!");

        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values");
        }
    }

    @FXML
    private void handleReset() {
        Settings defaults = new Settings(); // Uses default values
        txtConversionRate.setText(String.valueOf(defaults.getConversionRate()));
        txtSellingMultiplier.setText(String.valueOf(defaults.getSellingMultiplier()));
        updateCalculationExample();
    }

    @FXML
    private void handleReload() {
        Settings settings = SettingsManager.getCurrentSettings();
        txtConversionRate.setText(String.valueOf(settings.getConversionRate()));
        txtSellingMultiplier.setText(String.valueOf(settings.getSellingMultiplier()));
        updateCalculationExample();
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
}
