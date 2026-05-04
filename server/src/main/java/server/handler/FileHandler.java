package server.handler;

import common.security.ValidationException;
import common.security.Validator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileHandler {
    private final Path uploadDir;

    public FileHandler(Path uploadDir) {
        // FIX: Chuẩn hóa đường dẫn base upload để chặn bypass qua path tricks.
        this.uploadDir = uploadDir.toAbsolutePath().normalize();
    }

    public UploadTarget createUploadTarget(String originalName, long size)
            throws ValidationException, IOException {
        // Validate file size and name before saving.
        Validator.validateFileSize(size);
        String safeName = Validator.sanitizeFileName(originalName);
        String extension = Validator.fileExtensionLower(safeName);
        if (!Validator.isAllowedImageExtension(extension)) {
            throw new ValidationException("Only JPG or PNG files are allowed.");
        }

        // Ensure upload directory exists.
        Files.createDirectories(uploadDir);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String storedName = timestamp + "_" + safeName;
        Path finalPath = uploadDir.resolve(storedName).normalize();
        Validator.ensureInsideBaseDir(uploadDir, finalPath);
        Path tempPath = uploadDir.resolve(storedName + ".part").normalize();
        Validator.ensureInsideBaseDir(uploadDir, tempPath);

        return new UploadTarget(finalPath, tempPath, storedName, extension);
    }

    public OutputStream openTempUploadStream(UploadTarget target) throws IOException {
        return Files.newOutputStream(target.getTempPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public void commitUpload(UploadTarget target) throws IOException {
        Files.move(target.getTempPath(), target.getFinalPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void abortUpload(UploadTarget target) {
        try {
            Files.deleteIfExists(target.getTempPath());
        } catch (IOException ignored) {
        }
    }

    public boolean isMagicBytesValid(String extensionLower, byte[] probe, int len) {
        if (len <= 0) {
            return false;
        }
        if ("png".equals(extensionLower)) {
            return len >= 4
                    && (probe[0] & 0xFF) == 0x89
                    && (probe[1] & 0xFF) == 0x50
                    && (probe[2] & 0xFF) == 0x4E
                    && (probe[3] & 0xFF) == 0x47;
        }
        if ("jpg".equals(extensionLower) || "jpeg".equals(extensionLower)) {
            return len >= 2
                    && (probe[0] & 0xFF) == 0xFF
                    && (probe[1] & 0xFF) == 0xD8;
        }
        return false;
    }
}
