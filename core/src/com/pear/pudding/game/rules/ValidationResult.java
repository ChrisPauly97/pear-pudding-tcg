package com.pear.pudding.game.rules;

/**
 * Result of a validation check.
 */
public class ValidationResult {
    public final boolean valid;
    public final String reason;

    private ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason);
    }
}
