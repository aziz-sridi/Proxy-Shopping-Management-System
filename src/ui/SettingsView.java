package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Settings;
import util.SettingsManager;

/**
 * Settings view for managing application configuration
 */
public class SettingsView {
    
    private TextField txtConversionRate;
    private TextField txtSellingMultiplier;
    private Label lblCurrentConversionRate;
    private Label lblCurrentSellingMultiplier;
    private Label lblCalculationExample;
    
    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Application Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Current settings display
        VBox currentSettingsBox = createCurrentSettingsBox();
        
        // Settings form
        VBox settingsForm = createSettingsForm();
        
        // Calculation example
        VBox exampleBox = createExampleBox();
        
        // Main content
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(titleLabel, currentSettingsBox, settingsForm, exampleBox);
        
        root.setCenter(mainContent);
        
        // Load current settings
        refreshCurrentSettings();
        updateCalculationExample();
        
        return root;
    }
    
    private VBox createCurrentSettingsBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label titleLabel = new Label("Current Settings");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        lblCurrentConversionRate = new Label();
        lblCurrentSellingMultiplier = new Label();
        
        box.getChildren().addAll(titleLabel, lblCurrentConversionRate, lblCurrentSellingMultiplier);
        return box;
    }
    
    private VBox createSettingsForm() {
        VBox formBox = new VBox(15);
        formBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-background-radius: 5; " +
                        "-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-border-width: 1;");
        
        Label formTitle = new Label("Update Settings");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER_LEFT);
        
        // Conversion Rate
        Label lblConversionRate = new Label("Conversion Rate:");
        lblConversionRate.setStyle("-fx-font-weight: bold;");
        txtConversionRate = new TextField();
        txtConversionRate.setPromptText("e.g., 3.5");
        Label lblConversionDesc = new Label("(Currently not used in calculations, reserved for future use)");
        lblConversionDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        // Selling Multiplier
        Label lblSellingMultiplier = new Label("Selling Multiplier:");
        lblSellingMultiplier.setStyle("-fx-font-weight: bold;");
        txtSellingMultiplier = new TextField();
        txtSellingMultiplier.setPromptText("e.g., 5.0");
        Label lblMultiplierDesc = new Label("(Used to calculate: Selling Price TND = EUR Price × This Value)");
        lblMultiplierDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        // Add listeners for real-time example update
        txtSellingMultiplier.textProperty().addListener((obs, old, val) -> updateCalculationExample());
        
        grid.add(lblConversionRate, 0, 0);
        grid.add(txtConversionRate, 1, 0);
        grid.add(lblConversionDesc, 2, 0);
        
        grid.add(lblSellingMultiplier, 0, 1);
        grid.add(txtSellingMultiplier, 1, 1);
        grid.add(lblMultiplierDesc, 2, 1);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button btnSave = new Button("Save Settings");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-padding: 10 20; -fx-background-radius: 5;");
        btnSave.setOnAction(e -> saveSettings());
        
        Button btnReset = new Button("Reset to Defaults");
        btnReset.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10 20; -fx-background-radius: 5;");
        btnReset.setOnAction(e -> resetToDefaults());
        
        Button btnReload = new Button("Reload Current");
        btnReload.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-padding: 10 20; -fx-background-radius: 5;");
        btnReload.setOnAction(e -> loadCurrentSettings());
        
        buttonBox.getChildren().addAll(btnSave, btnReset, btnReload);
        
        formBox.getChildren().addAll(formTitle, grid, buttonBox);
        return formBox;
    }
    
    private VBox createExampleBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 15; -fx-background-radius: 5; " +
                    "-fx-border-color: #3498db; -fx-border-radius: 5; -fx-border-width: 1;");
        
        Label titleLabel = new Label("Calculation Example");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        
        lblCalculationExample = new Label();
        lblCalculationExample.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        
        box.getChildren().addAll(titleLabel, lblCalculationExample);
        return box;
    }
    
    private void refreshCurrentSettings() {
        Settings settings = SettingsManager.getCurrentSettings();
        lblCurrentConversionRate.setText("Conversion Rate: " + settings.getConversionRate());
        lblCurrentSellingMultiplier.setText("Selling Multiplier: " + settings.getSellingMultiplier());
    }
    
    private void loadCurrentSettings() {
        Settings settings = SettingsManager.getCurrentSettings();
        txtConversionRate.setText(String.valueOf(settings.getConversionRate()));
        txtSellingMultiplier.setText(String.valueOf(settings.getSellingMultiplier()));
        updateCalculationExample();
    }
    
    private void saveSettings() {
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
    
    private void resetToDefaults() {
        Settings defaults = new Settings(); // Uses default values
        txtConversionRate.setText(String.valueOf(defaults.getConversionRate()));
        txtSellingMultiplier.setText(String.valueOf(defaults.getSellingMultiplier()));
        updateCalculationExample();
    }
    
    private void updateCalculationExample() {
        try {
            String multiplierText = txtSellingMultiplier.getText().trim();
            if (multiplierText.isEmpty()) {
                lblCalculationExample.setText("Enter a selling multiplier to see calculation example");
                return;
            }
            
            double multiplier = Double.parseDouble(multiplierText);
            
            String example = "Example with EUR 18.50, Quantity 2, Payment: Deposit\\n" +
                           "\\n" +
                           "Selling Price TND = 18.50 EUR × " + multiplier + " = " + 
                           String.format("%.2f", 18.50 * multiplier) + " TND\\n" +
                           "Expected Total TND = " + String.format("%.2f", 18.50 * multiplier) + 
                           " × 2 = " + String.format("%.2f", 18.50 * multiplier * 2) + " TND\\n" +
                           "Deposit TND = " + String.format("%.2f", 18.50 * multiplier * 2) + 
                           " × 0.5 = " + String.format("%.2f", 18.50 * multiplier * 2 * 0.5) + " TND";
            
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
