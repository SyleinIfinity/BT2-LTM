package server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileHandler {
    public static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private final Path uploadDir;

    public FileHandler(Path uploadDir) {
        this.uploadDir = uploadDir;
    }

    public UploadTarget createUploadTarget(String originalName, long size)
            throws ValidationException, IOException {
        // Validate file size and name before saving.
        if (size > MAX_FILE_SIZE) {
            throw new ValidationException("File too large. Max 5MB.");
        }
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new ValidationException("File name is empty.");
        }

        String safeName = sanitizeFileName(originalName);
        String extension = getExtension(safeName);
        if (!isAllowedExtension(extension)) {
            throw new ValidationException("Only JPG or PNG files are allowed.");
        }

        // Ensure upload directory exists.
        Files.createDirectories(uploadDir);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String storedName = timestamp + "_" + safeName;
        Path targetPath = uploadDir.resolve(storedName).normalize();
        if (!targetPath.toAbsolutePath().startsWith(uploadDir.toAbsolutePath())) {
            throw new ValidationException("Invalid file path.");
        }

        return new UploadTarget(targetPath, storedName);
    }

    public OutputStream openUploadStream(UploadTarget target) throws IOException {
        return Files.newOutputStream(target.getPath());
    }

    private String sanitizeFileName(String name) {
        String baseName = Paths.get(name).getFileName().toString();
        return baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedExtension(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension);
    }
}
