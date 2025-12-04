package ui.util;

import model.Settings;
import util.SettingsManager;

/**
 * Utility class for price calculations used across the application.
 * Centralizes pricing logic to avoid duplication in view controllers and dialogs.
 */
public class PriceCalculator {

    private PriceCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculate the selling price in TND from original price in EUR.
     * Uses the selling multiplier from application settings.
     * 
     * @param originalPriceEUR The original price in EUR
     * @return The calculated selling price in TND
     */
    public static double calculateSellingPrice(double originalPriceEUR) {
        Settings settings = SettingsManager.getCurrentSettings();
        double sellingMultiplier = settings.getSellingMultiplier();
        return originalPriceEUR * sellingMultiplier;
    }

    /**
     * Calculate the total selling price for multiple items.
     * 
     * @param originalPriceEUR The original unit price in EUR
     * @param quantity The quantity of items
     * @return The total selling price in TND
     */
    public static double calculateTotalSellingPrice(double originalPriceEUR, int quantity) {
        return calculateSellingPrice(originalPriceEUR) * quantity;
    }

    /**
     * Calculate the deposit amount (50% of total).
     * 
     * @param totalPrice The total price
     * @return The deposit amount (50% of total)
     */
    public static double calculateDeposit(double totalPrice) {
        return totalPrice * 0.5;
    }

    /**
     * Calculate the remaining amount after payments.
     * 
     * @param sellingPrice The total selling price
     * @param totalPaid The total amount already paid
     * @return The remaining amount to be paid
     */
    public static double calculateRemaining(double sellingPrice, double totalPaid) {
        return Math.max(0, sellingPrice - totalPaid);
    }

    /**
     * Determine payment status based on amounts.
     * 
     * @param sellingPrice The total selling price
     * @param totalPaid The total amount paid
     * @return "Paid", "Partial", or "Unpaid"
     */
    public static String determinePaymentStatus(double sellingPrice, double totalPaid) {
        if (totalPaid <= 0) {
            return "Unpaid";
        } else if (totalPaid >= sellingPrice) {
            return "Paid";
        } else {
            return "Partial";
        }
    }

    /**
     * Parse a price string, handling locale issues with decimal separators.
     * 
     * @param priceText The price string to parse
     * @return The parsed double value
     * @throws NumberFormatException if the text cannot be parsed
     */
    public static double parsePrice(String priceText) throws NumberFormatException {
        if (priceText == null || priceText.trim().isEmpty()) {
            throw new NumberFormatException("Price text is empty");
        }
        String cleanPrice = priceText.trim().replace(",", ".");
        return Double.parseDouble(cleanPrice);
    }

    /**
     * Parse a price string with a default value if parsing fails.
     * 
     * @param priceText The price string to parse
     * @param defaultValue The default value if parsing fails
     * @return The parsed value or default
     */
    public static double parsePriceOrDefault(String priceText, double defaultValue) {
        try {
            return parsePrice(priceText);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Format a price for display.
     * 
     * @param price The price to format
     * @return Formatted string with 2 decimal places
     */
    public static String formatPrice(double price) {
        return String.format("%.2f", price);
    }

    /**
     * Format a price with currency suffix.
     * 
     * @param price The price to format
     * @param currency The currency code (e.g., "TND", "EUR")
     * @return Formatted string with currency
     */
    public static String formatPriceWithCurrency(double price, String currency) {
        return String.format("%.2f %s", price, currency);
    }
}
