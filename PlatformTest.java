import model.*;
import dao.*;

public class PlatformTest {
    public static void main(String[] args) {
        System.out.println("=== Testing Platform Feature ===");
        
        // Test 1: Platform Enum
        System.out.println("\n1. Testing Platform Enum:");
        for (Platform platform : Platform.values()) {
            System.out.println("  " + platform.name() + " -> " + platform.getDisplayName());
        }
        
        // Test 2: Platform String Parsing
        System.out.println("\n2. Testing Platform String Parsing:");
        System.out.println("  'Shein' -> " + Platform.fromString("Shein"));
        System.out.println("  'temu' -> " + Platform.fromString("temu"));
        System.out.println("  'invalid' -> " + Platform.fromString("invalid"));
        System.out.println("  null -> " + Platform.fromString(null));
        
        // Test 3: Platform Display Names
        System.out.println("\n3. Testing Platform Display Names:");
        String[] displayNames = Platform.getDisplayNames();
        for (String name : displayNames) {
            System.out.println("  " + name);
        }
        
        // Test 4: Order with Platform
        System.out.println("\n4. Testing Order with Platform:");
        Order order = new Order();
        order.setPlatform(Platform.SHEIN);
        System.out.println("  Set platform to SHEIN");
        System.out.println("  Retrieved platform: " + order.getPlatform().getDisplayName());
        
        // Test 5: OrderDAO compilation
        System.out.println("\n5. Testing OrderDAO instantiation:");
        try {
            OrderDAO orderDAO = new OrderDAO();
            System.out.println("  OrderDAO created successfully");
        } catch (Exception e) {
            System.out.println("  OrderDAO error: " + e.getMessage());
        }
        
        System.out.println("\n=== Platform Feature Test Complete ===");
    }
}
