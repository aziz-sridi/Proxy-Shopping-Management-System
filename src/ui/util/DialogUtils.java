package ui.util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Utility class providing common dialog operations across the application.
 * Reduces code duplication in view controllers.
 */
public class DialogUtils {

    private DialogUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Show an error dialog with the given message.
     */
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    /**
     * Show an error dialog with title and message.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * Show a confirmation dialog and return true if user confirms.
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirmation", message);
    }

    /**
     * Show a confirmation dialog with custom title and return true if user confirms.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Show an information dialog.
     */
    public static void showInfo(String message) {
        showInfo("Information", message);
    }

    /**
     * Show an information dialog with custom title.
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * Create a standard GridPane for form layouts.
     */
    public static GridPane createFormGrid() {
        return createFormGrid(10, 10, 10);
    }

    /**
     * Create a GridPane for form layouts with custom spacing.
     */
    public static GridPane createFormGrid(double hgap, double vgap, double padding) {
        GridPane grid = new GridPane();
        grid.setHgap(hgap);
        grid.setVgap(vgap);
        grid.setPadding(new Insets(padding));
        return grid;
    }

    /**
     * Parse a double from text, returning a default value if parsing fails.
     * Delegates to PriceCalculator to avoid duplication.
     */
    public static double parseDouble(String text, double defaultValue) {
        return PriceCalculator.parsePriceOrDefault(text, defaultValue);
    }

    /**
     * Parse an integer from text, returning a default value if parsing fails.
     */
    public static int parseInt(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Create a styled TextField with the app-field class.
     */
    public static TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.getStyleClass().add("app-field");
        return field;
    }

    /**
     * Create a read-only styled TextField.
     */
    public static TextField createReadOnlyTextField(String promptText, String style) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setEditable(false);
        field.setStyle(style);
        return field;
    }

    /**
     * Create a styled ComboBox.
     */
    public static <T> ComboBox<T> createStyledComboBox(String promptText) {
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setPromptText(promptText);
        comboBox.getStyleClass().add("app-field");
        return comboBox;
    }

    /**
     * Create a label for displaying remaining amount or similar info.
     */
    public static Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        return label;
    }
}
