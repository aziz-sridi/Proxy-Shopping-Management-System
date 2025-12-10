package ui.util;

import model.Settings;
import util.SettingsManager;

public class PriceCalculator {

    private PriceCalculator() {}

    // Calculate selling price in TND from EUR using selling multiplier
    public static double calculateSellingPrice(double originalPriceEUR) {
        Settings settings = SettingsManager.getCurrentSettings();
        double sellingMultiplier = settings.getSellingMultiplier();
        return originalPriceEUR * sellingMultiplier;
    }

    public static double calculateTotalSellingPrice(double originalPriceEUR, int quantity) {
        return calculateSellingPrice(originalPriceEUR) * quantity;
    }

    public static double calculateDeposit(double totalPrice) {
        return totalPrice * 0.5;
    }

    public static double calculateRemaining(double sellingPrice, double totalPaid) {
        return Math.max(0, sellingPrice - totalPaid);
    }

    public static String determinePaymentStatus(double sellingPrice, double totalPaid) {
        if (totalPaid <= 0) {
            return "Unpaid";
        } else if (totalPaid >= sellingPrice) {
            return "Paid";
        } else {
            return "Partial";
        }
    }

    public static double parsePrice(String priceText) throws NumberFormatException {
        if (priceText == null || priceText.trim().isEmpty()) {
            throw new NumberFormatException("Price text is empty");
        }
        String cleanPrice = priceText.trim().replace(",", ".");
        return Double.parseDouble(cleanPrice);
    }

    public static double parsePriceOrDefault(String priceText, double defaultValue) {
        try {
            return parsePrice(priceText);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String formatPrice(double price) {
        return String.format("%.2f", price);
    }

    public static String formatPriceWithCurrency(double price, String currency) {
        return String.format("%.2f %s", price, currency);
    }
}
