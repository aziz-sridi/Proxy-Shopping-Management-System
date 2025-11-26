public class QuickTest {
    public static void main(String[] args) {
        System.out.println("Testing '90' parsing:");
        try {
            String test = "90";
            double result = Double.parseDouble(test);
            System.out.println("SUCCESS: " + test + " → " + result);
            
            // Test calculation
            double selling = result * 5.0 * 1;
            System.out.println("Selling: " + selling);
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}
