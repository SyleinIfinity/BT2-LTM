package server.handler;

import java.nio.file.Path;

public class UploadTarget {
    private final Path finalPath;
    private final Path tempPath;
    private final String storedFileName;
    private final String extensionLower;

    public UploadTarget(Path finalPath, Path tempPath, String storedFileName, String extensionLower) {
        this.finalPath = finalPath;
        this.tempPath = tempPath;
        this.storedFileName = storedFileName;
        this.extensionLower = extensionLower;
    }

    public Path getFinalPath() {
        return finalPath;
    }

    public Path getTempPath() {
        return tempPath;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getExtensionLower() {
        return extensionLower;
    }
}
