package server.handler;

import server.core.ChatServer;
import server.core.ClientSession;
import server.model.ChatMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class ClientHandler {
    private static final int MAX_USERNAME_LENGTH = 24;
    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ChatServer server;
    private final FileHandler fileHandler;

    public ClientHandler(ChatServer server) {
        this.server = server;
        this.fileHandler = new FileHandler(Paths.get("server", "uploads"));
    }

    public void consume(ClientSession session) throws IOException {
        // Decode incoming bytes into protocol lines or file chunks.
        ByteBuffer buffer = session.getReadBuffer();
        buffer.flip();
        while (buffer.hasRemaining()) {
            if (session.isReceivingFile()) {
                processFileBytes(session, buffer);
            } else {
                processTextBytes(session, buffer);
            }
        }
        buffer.compact();
    }

    private void processTextBytes(ClientSession session, ByteBuffer buffer) throws IOException {
        // Build text lines separated by newline.
        while (buffer.hasRemaining() && !session.isReceivingFile()) {
            byte b = buffer.get();
            if (b == '\n') {
                String line = session.consumeLine().trim();
                if (!line.isEmpty()) {
                    handleLine(session, line);
                }
            } else if (b != '\r') {
                session.appendLineByte(b);
            }
        }
    }

    private void processFileBytes(ClientSession session, ByteBuffer buffer) throws IOException {
        // Stream file bytes to disk (or discard on validation failure).
        long remaining = session.getRemainingFileBytes();
        int toRead = (int) Math.min(buffer.remaining(), remaining);
        byte[] chunk = new byte[toRead];
        buffer.get(chunk);

        if (!session.isDiscardingFile()) {
            OutputStream out = session.getFileOut();
            if (out != null) {
                out.write(chunk);
            }
        }

        session.consumeFileBytes(toRead);
        if (session.getRemainingFileBytes() == 0) {
            boolean discarded = session.isDiscardingFile();
            String user = session.getUploadUser();
            String storedName = session.getStoredFileName();
            session.finishFile();

            if (!discarded) {
                server.broadcastLine("TEXT|SERVER|" + user + " sent image: " + storedName);
            }
        }
    }

    private void handleLine(ClientSession session, String line) throws IOException {
        // Parse protocol command and route to handler.
        if (line.startsWith("TEXT|")) {
            handleText(session, line);
            return;
        }
        if (line.startsWith("IMAGE|")) {
            handleImage(session, line);
            return;
        }
        server.sendLine(session, "TEXT|SERVER|Invalid command.");
    }

    private void handleText(ClientSession session, String line) {
        // TEXT|username|message
        String[] parts = parseText(line);
        if (parts == null) {
            server.sendLine(session, "TEXT|SERVER|Invalid TEXT format.");
            return;
        }
        String username = sanitize(parts[0], MAX_USERNAME_LENGTH);
        String message = sanitize(parts[1], MAX_MESSAGE_LENGTH);
        if (username.isEmpty() || message.isEmpty()) {
            server.sendLine(session, "TEXT|SERVER|Username or message is empty.");
            return;
        }
        ChatMessage chatMessage = new ChatMessage(username, message);
        server.broadcastLine(chatMessage.toProtocolLine());
    }

    private void handleImage(ClientSession session, String line) {
        // IMAGE|username|filename|filesize + raw bytes.
        String[] parts = parseImage(line);
        if (parts == null) {
            server.sendLine(session, "TEXT|SERVER|Invalid IMAGE format.");
            server.disconnect(session);
            return;
        }
        String username = sanitize(parts[0], MAX_USERNAME_LENGTH);
        String filename = parts[1];
        String sizeText = parts[2];

        long size;
        try {
            size = Long.parseLong(sizeText);
        } catch (NumberFormatException ex) {
            server.sendLine(session, "TEXT|SERVER|Invalid file size.");
            server.disconnect(session);
            return;
        }

        if (size <= 0) {
            server.sendLine(session, "TEXT|SERVER|Invalid file size.");
            server.disconnect(session);
            return;
        }
        if (size > FileHandler.MAX_FILE_SIZE) {
            server.sendLine(session, "TEXT|SERVER|File too large (max 5MB).");
            server.disconnect(session);
            return;
        }

        try {
            UploadTarget target = fileHandler.createUploadTarget(filename, size);
            OutputStream outputStream = fileHandler.openUploadStream(target);
            session.startReceivingFile(outputStream, size, username, target.getStoredFileName());
            server.sendLine(session, "TEXT|SERVER|Uploading image: " + target.getStoredFileName());
        } catch (ValidationException ex) {
            server.sendLine(session, "TEXT|SERVER|Image rejected: " + ex.getMessage());
            session.startDiscardingFile(size);
        } catch (IOException ex) {
            server.sendLine(session, "TEXT|SERVER|Server error while saving file.");
            server.disconnect(session);
        }
    }

    private String[] parseText(String line) {
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        if (first < 0 || second < 0) {
            return null;
        }
        String username = line.substring(first + 1, second);
        String message = line.substring(second + 1);
        return new String[]{username, message};
    }

    private String[] parseImage(String line) {
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        int third = line.indexOf('|', second + 1);
        if (first < 0 || second < 0 || third < 0) {
            return null;
        }
        String username = line.substring(first + 1, second);
        String filename = line.substring(second + 1, third);
        String size = line.substring(third + 1);
        return new String[]{username, filename, size};
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String clean = value.replace("\r", " ").replace("\n", " ").replace("|", " ").trim();
        if (clean.length() > maxLength) {
            clean = clean.substring(0, maxLength);
        }
        return clean;
    }
}
