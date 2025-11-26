/**
 * Test to debug the EUR price parsing issue
 */
public class OrdersPriceTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing EUR Price Parsing ===");
        
        // Test various formats that might be entered
        testParsing("198");          // Integer
        testParsing("198.0");        // Decimal with period
        testParsing("198,0");        // Decimal with comma
        testParsing("18.50");        // Standard format
        testParsing("18,50");        // European format
        testParsing(" 18.50 ");      // With spaces
        testParsing("");             // Empty
        testParsing("abc");          // Invalid
        testParsing("18.50.30");     // Multiple decimals
        
        System.out.println("=== Test Complete ===");
    }
    
    private static void testParsing(String input) {
        System.out.printf("Testing: '%s' → ", input);
        
        try {
            if (input == null) {
                System.out.println("NULL INPUT");
                return;
            }
            
            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                System.out.println("EMPTY AFTER TRIM");
                return;
            }
            
            String cleaned = trimmed.replace(",", ".");
            double price = Double.parseDouble(cleaned);
            
            if (price <= 0) {
                System.out.printf("INVALID VALUE (%.2f)%n", price);
            } else {
                System.out.printf("SUCCESS (%.2f EUR → %.2f TND for 1 qty)%n", price, price * 5);
            }
            
        } catch (NumberFormatException ex) {
            System.out.printf("PARSE ERROR (%s)%n", ex.getMessage());
        }
    }
}
