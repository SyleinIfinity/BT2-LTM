package client.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ChatClient {
    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private final String host;
    private final int port;
    private final String username;

    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private MessageListener listener;
    private Thread readerThread;

    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = sanitize(username, 24);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
        // Background reader to keep UI responsive.
        startReaderThread();
    }

    public void sendText(String message) throws IOException {
        if (out == null) {
            throw new IOException("Not connected.");
        }
        String cleanMessage = sanitize(message, 1000);
        if (cleanMessage.isEmpty()) {
            return;
        }
        String line = "TEXT|" + username + "|" + cleanMessage + "\n";
        out.write(line.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public void sendImage(File file) throws IOException {
        if (out == null) {
            throw new IOException("Not connected.");
        }
        if (file == null || !file.exists()) {
            throw new IOException("File not found.");
        }
        String fileName = file.getName();
        String extension = getExtension(fileName);
        if (!isAllowedExtension(extension)) {
            throw new IOException("Only JPG or PNG files are allowed.");
        }
        long size = file.length();
        if (size <= 0 || size > MAX_FILE_SIZE) {
            throw new IOException("File size must be <= 5MB.");
        }

        String header = "IMAGE|" + username + "|" + fileName + "|" + size + "\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));

        // Send raw bytes after the header.
        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        out.flush();
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = readLine()) != null) {
                    if (listener != null) {
                        listener.onMessage(line);
                    }
                }
            } catch (IOException ex) {
                if (listener != null) {
                    listener.onMessage("TEXT|SERVER|Disconnected from server.");
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private String readLine() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                buffer.write(b);
            }
        }
        if (b == -1 && buffer.size() == 0) {
            return null;
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
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
