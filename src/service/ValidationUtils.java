package service;


public class ValidationUtils {

    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate that an entity is not null.
     * @param entity The entity to validate
     * @param entityName The name of the entity for error messages
     * @throws IllegalArgumentException if entity is null
     */
    public static void validateNotNull(Object entity, String entityName) {
        if (entity == null) {
            throw new IllegalArgumentException(entityName + " cannot be null");
        }
    }

    /**
     * Validate that an ID is positive.
     * @param id The ID to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if ID is not positive
     */
    public static void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validate that a string is not empty.
     * @param value The string to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if string is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    /**
     * Validate that a string length doesn't exceed maximum.
     * @param value The string to validate
     * @param maxLength The maximum allowed length
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if string exceeds max length
     */
    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }

    /**
     * Validate that a numeric value is positive.
     * @param value The value to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if value is not positive
     */
    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validate that a numeric value is non-negative.
     * @param value The value to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if value is negative
     */
    public static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    /**
     * Validate that a string has exact length (useful for phone numbers).
     * @param value The string to validate
     * @param exactLength The required length
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if string doesn't have exact length
     */
    public static void validateExactLength(String value, int exactLength, String fieldName) {
        if (value != null && value.length() != exactLength) {
            throw new IllegalArgumentException(fieldName + " must be exactly " + exactLength + " characters");
        }
    }

    /**
     * Validate that a phone number is exactly 8 digits and starts with 2, 5, or 9.
     * @param phoneNumber The phone number to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if phone number is invalid
     */
    public static void validatePhoneNumber(String phoneNumber, String fieldName) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String trimmed = phoneNumber.trim();
        if (trimmed.length() != 8) {
            throw new IllegalArgumentException(fieldName + " must be exactly 8 digits");
        }
        if (!trimmed.matches("\\d{8}")) {
            throw new IllegalArgumentException(fieldName + " must contain only digits");
        }
        if (!trimmed.matches("[259]\\d{7}")) {
            throw new IllegalArgumentException(fieldName + " must start with 2, 5, or 9");
        }
    }
}
