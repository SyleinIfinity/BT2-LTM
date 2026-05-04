package server.util;

import server.config.ServerConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * PACK: Lightweight logger setup that writes to logs/server.log and keeps console output.
 */
public final class LoggerUtil {
    private LoggerUtil() {
    }

    public static void setup() {
        try {
            Path logs = ServerConfig.getLogsDir();
            FileHandler fh = new FileHandler(logs.resolve("server.log").toString(), true);
            fh.setFormatter(new SimpleFormatter());
            Logger root = Logger.getLogger("");
            root.addHandler(fh);
            root.setLevel(Level.INFO);
        } catch (IOException ex) {
            System.err.println("Failed to setup file logger: " + ex.getMessage());
        }
    }
}
