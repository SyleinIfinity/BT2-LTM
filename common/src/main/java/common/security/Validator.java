package common.security;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Validation and sanitization utilities.
 * <p>
 * Security mindset: do not trust network input. Always validate lengths, formats,
 * and refuse suspicious values (e.g. path traversal attempts).
 */
public final class Validator {
    private Validator() {
    }

    public static String sanitizeProtocolField(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", " ")
                .replace("\n", " ")
                .replace("|", " ")
                .trim();
    }

    public static String validateUsername(String username) throws ValidationException {
        String clean = sanitizeProtocolField(username);
        if (clean.isEmpty()) {
            throw new ValidationException("Username is empty.");
        }
        if (clean.length() > Limits.MAX_USERNAME_LENGTH) {
            throw new ValidationException("Username too long.");
        }
        return clean;
    }

    public static String validateMessage(String message) throws ValidationException {
        String clean = sanitizeProtocolField(message);
        if (clean.isEmpty()) {
            throw new ValidationException("Message is empty.");
        }
        if (clean.length() > Limits.MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Message too long.");
        }
        return clean;
    }

    /**
     * Extract a safe file name (no directories) and replace suspicious characters.
     * This mitigates path traversal and weird filesystem characters.
     */
    public static String sanitizeFileName(String originalName) throws ValidationException {
        if (originalName == null) {
            throw new ValidationException("File name is null.");
        }
        String trimmed = originalName.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("File name is empty.");
        }

        String baseName = Paths.get(trimmed).getFileName().toString();
        String safe = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safe.isEmpty()) {
            throw new ValidationException("File name is invalid.");
        }
        return safe;
    }

    public static String fileExtensionLower(String safeFileName) {
        int dot = safeFileName.lastIndexOf('.');
        if (dot < 0 || dot == safeFileName.length() - 1) {
            return "";
        }
        return safeFileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static boolean isAllowedImageExtension(String extensionLower) {
        return "jpg".equals(extensionLower) || "jpeg".equals(extensionLower) || "png".equals(extensionLower);
    }

    public static void validateFileSize(long size) throws ValidationException {
        if (size <= 0) {
            throw new ValidationException("Invalid file size.");
        }
        if (size > Limits.MAX_FILE_SIZE) {
            throw new ValidationException("File too large (max 5MB).");
        }
    }

    /**
     * Ensure a resolved target path stays within its base directory.
     */
    public static void ensureInsideBaseDir(Path baseDir, Path resolved) throws ValidationException {
        Path baseAbs = baseDir.toAbsolutePath().normalize();
        Path resolvedAbs = resolved.toAbsolutePath().normalize();
        if (!resolvedAbs.startsWith(baseAbs)) {
            throw new ValidationException("Invalid file path.");
        }
    }
}
