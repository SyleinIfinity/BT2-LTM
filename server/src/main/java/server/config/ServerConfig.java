package server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * PACK: Central server configuration loaded from config/server.properties.
 * HARDEN: Externalize limits so packaging can tune values without recompiling.
 */
public final class ServerConfig {
    private ServerConfig() {
    }

    private static int port = 5555;
    private static int maxClients = 100;
    private static int maxMessageLength = 1000;
    private static long maxFileSize = 5L * 1024L * 1024L;
    private static long timeoutMs = 30_000L;
    private static long idleTimeoutMs = 60_000L;
    private static long uploadTimeoutMs = 30_000L;
    private static int maxLineBytes = 4096;
    private static int maxCommandsPerWindow = 25;
    private static long commandRateWindowMs = 1000L;
    private static Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    private static Path logsDir = Paths.get("logs").toAbsolutePath().normalize();

    public static void load() {
        File cfg = Paths.get("config", "server.properties").toFile();
        if (!cfg.exists()) {
            try {
                Files.createDirectories(cfg.toPath().getParent());
            } catch (IOException ignored) {
            }
            return;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(cfg)) {
            p.load(in);
            port = Integer.parseInt(p.getProperty("port", Integer.toString(port)).trim());
            maxClients = Integer.parseInt(p.getProperty("max_clients", Integer.toString(maxClients)).trim());
            maxMessageLength = Integer.parseInt(p.getProperty("max_message_length", Integer.toString(maxMessageLength)).trim());
            maxFileSize = Long.parseLong(p.getProperty("max_file_size", Long.toString(maxFileSize)).trim());
            timeoutMs = Long.parseLong(p.getProperty("timeout_ms", Long.toString(timeoutMs)).trim());
            idleTimeoutMs = Long.parseLong(p.getProperty("idle_timeout_ms", Long.toString(idleTimeoutMs)).trim());
            uploadTimeoutMs = Long.parseLong(p.getProperty("upload_timeout_ms", Long.toString(uploadTimeoutMs)).trim());
            maxLineBytes = Integer.parseInt(p.getProperty("max_line_bytes", Integer.toString(maxLineBytes)).trim());
            maxCommandsPerWindow = Integer.parseInt(p.getProperty("max_commands_per_window", Integer.toString(maxCommandsPerWindow)).trim());
            commandRateWindowMs = Long.parseLong(p.getProperty("command_rate_window_ms", Long.toString(commandRateWindowMs)).trim());
            String uploadPath = p.getProperty("upload_dir");
            if (uploadPath != null && !uploadPath.isBlank()) {
                uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            }
            String logsPath = p.getProperty("logs_dir");
            if (logsPath != null && !logsPath.isBlank()) {
                logsDir = Paths.get(logsPath).toAbsolutePath().normalize();
            }
            // Ensure directories exist.
            try {
                Files.createDirectories(uploadDir);
                Files.createDirectories(logsDir);
            } catch (IOException ignored) {
            }
        } catch (IOException | NumberFormatException ex) {
            // If config is malformed, use defaults. Do not crash on bad config.
        }
    }

    public static int getPort() {
        return port;
    }

    public static int getMaxClients() {
        return maxClients;
    }

    public static int getMaxMessageLength() {
        return maxMessageLength;
    }

    public static long getMaxFileSize() {
        return maxFileSize;
    }

    public static long getTimeoutMs() {
        return timeoutMs;
    }

    public static long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }

    public static long getUploadTimeoutMs() {
        return uploadTimeoutMs;
    }

    public static int getMaxLineBytes() {
        return maxLineBytes;
    }

    public static int getMaxCommandsPerWindow() {
        return maxCommandsPerWindow;
    }

    public static long getCommandRateWindowMs() {
        return commandRateWindowMs;
    }

    public static Path getUploadDir() {
        return uploadDir;
    }

    public static Path getLogsDir() {
        return logsDir;
    }
}
