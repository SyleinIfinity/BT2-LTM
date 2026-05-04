package common.security;

/**
 * Centralized security limits shared by server & client.
 * <p>
 * NOTE: These limits are intentionally conservative to mitigate simple DoS vectors
 * (unbounded line buffers / outbound queues / oversized files).
 */
public final class Limits {
    private Limits() {
    }

    public static final int MAX_USERNAME_LENGTH = 24;
    public static final int MAX_MESSAGE_LENGTH = 1000;

    /**
     * Maximum bytes allowed for a single protocol line (header). Prevents memory DoS
     * when a client never sends '\n'.
     */
    public static final int MAX_LINE_BYTES = 4096;

    /**
     * Maximum allowed file size for image uploads.
     */
    public static final long MAX_FILE_SIZE = 5L * 1024L * 1024L; // 5MB

    /**
     * Maximum queued outbound bytes per client session before the server should start
     * applying back-pressure policies (e.g. disconnect slow clients).
     */
    public static final long MAX_OUTBOUND_BYTES_PER_SESSION = 1024L * 1024L; // 1MB

    /**
     * Idle timeout for a client session (server-side).
     */
    public static final long IDLE_TIMEOUT_MS = 120_000L; // 2 minutes

    /**
     * Upload timeout (server-side). If the client sends IMAGE header but does not
     * complete the byte stream within this window, the server cancels the upload.
     */
    public static final long UPLOAD_TIMEOUT_MS = 30_000L; // 30 seconds
}
