/**
 * Debug test for ClientDialogs calculation functionality
 */
public class CalculationDebugTest {
    
    public static void main(String[] args) {
        System.out.println("=== ClientDialogs Calculation Debug Test ===");
        
        // Simulate the exact calculation logic from ClientDialogs
        System.out.println("Testing calculation logic:");
        
        // Test 1: Basic calculation
        testCalculation("18.00", 2, "Deposit");
        
        // Test 2: Different price
        testCalculation("25.5", 1, "Full");
        
        // Test 3: On Delivery (no deposit)
        testCalculation("10", 3, "On Delivery");
        
        // Test 4: Invalid input
        testCalculation("", 1, "Deposit");
        testCalculation("abc", 1, "Deposit");
        
        System.out.println("=== Debug Test Complete ===");
    }
    
    private static void testCalculation(String unitPriceText, int quantity, String paymentType) {
        System.out.printf("%nInput: '%s' EUR, Qty: %d, Payment: %s%n", unitPriceText, quantity, paymentType);
        
        if (unitPriceText == null || unitPriceText.trim().isEmpty()) {
            System.out.println("Result: Empty input - fields would be cleared");
            return;
        }
        
        try {
            double unitPriceEUR = Double.parseDouble(unitPriceText.trim());
            
            // Apply pricing rule: sellingPriceTND = unitPriceEUR * 5
            double sellingPriceTND = unitPriceEUR * 5.0;
            
            // Calculate expected total: expectedTotalTND = sellingPriceTND * quantity
            double expectedTotalTND = sellingPriceTND * quantity;
            
            System.out.printf("Selling Price TND: %.2f%n", sellingPriceTND);
            System.out.printf("Expected Total TND: %.2f%n", expectedTotalTND);
            
            // Calculate deposit if payment type is "Deposit"
            if ("Deposit".equals(paymentType)) {
                double depositTND = expectedTotalTND * 0.5; // 50% deposit
                System.out.printf("Deposit TND: %.2f%n", depositTND);
            } else {
                System.out.println("Deposit TND: (cleared - not a deposit)");
            }
            
        } catch (NumberFormatException ex) {
            System.out.println("Result: Invalid number format - fields would be cleared");
        }
    }
}
