package common.security;

/**
 * Checked exception for validation failures (untrusted input).
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
