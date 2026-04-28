package server.handler;

import java.nio.file.Path;

public class UploadTarget {
    private final Path path;
    private final String storedFileName;

    public UploadTarget(Path path, String storedFileName) {
        this.path = path;
        this.storedFileName = storedFileName;
    }

    public Path getPath() {
        return path;
    }

    public String getStoredFileName() {
        return storedFileName;
    }
}
