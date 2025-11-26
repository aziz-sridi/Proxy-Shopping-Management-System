package model;

/**
 * Enum representing the different e-commerce platforms
 * where products can be ordered from.
 */
public enum Platform {
    SHEIN("Shein"),
    TEMU("Temu"), 
    ALIEXPRESS("AliExpress"),
    ALIBABA("Alibaba"),
    OTHER("Other");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parse a string to get the corresponding Platform enum value
     * @param value the string representation
     * @return the Platform enum or OTHER if not found
     */
    public static Platform fromString(String value) {
        if (value == null) return OTHER;
        
        for (Platform platform : Platform.values()) {
            if (platform.displayName.equalsIgnoreCase(value.trim())) {
                return platform;
            }
        }
        return OTHER;
    }

    /**
     * Get all platform display names as string array for ComboBox
     * @return array of platform display names
     */
    public static String[] getDisplayNames() {
        return new String[] {
            SHEIN.displayName,
            TEMU.displayName, 
            ALIEXPRESS.displayName,
            ALIBABA.displayName,
            OTHER.displayName
        };
    }
}
