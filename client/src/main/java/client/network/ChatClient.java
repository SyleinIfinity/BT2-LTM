package client.network;

import common.protocol.ProtocolParser;
import common.protocol.messages.ErrMessage;
import common.protocol.messages.OkMessage;
import common.protocol.messages.ProtocolMessage;
import common.security.Limits;
import common.security.ValidationException;
import common.security.Validator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private final String host;
    private final int port;
    private final String username;

    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private MessageListener listener;
    private Thread readerThread;

    private final Object writeLock = new Object();
    private final ProtocolParser protocolParser = new ProtocolParser();

    private PendingImage pendingImage;

    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = safeUsername(username);
    }

    public String getUsername() {
        return username;
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
        String cleanMessage = sanitizeAndClip(message, Limits.MAX_MESSAGE_LENGTH);
        if (cleanMessage.isEmpty()) {
            return;
        }
        synchronized (writeLock) {
            if (out == null) {
                throw new IOException("Not connected.");
            }
            String line = "TEXT|" + username + "|" + cleanMessage + "\n";
            out.write(line.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

    public void sendImage(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File not found.");
        }
        String fileName;
        try {
            fileName = Validator.sanitizeFileName(file.getName());
        } catch (ValidationException ex) {
            throw new IOException("Invalid file name.");
        }

        String extension = Validator.fileExtensionLower(fileName);
        if (!Validator.isAllowedImageExtension(extension)) {
            throw new IOException("Only JPG or PNG files are allowed.");
        }
        long size = file.length();
        try {
            Validator.validateFileSize(size);
        } catch (ValidationException ex) {
            throw new IOException(ex.getMessage());
        }

        synchronized (writeLock) {
            if (out == null) {
                throw new IOException("Not connected.");
            }
            if (pendingImage != null) {
                throw new IOException("Another image upload is in progress.");
            }

            // IMAGE handshake:
            // 1) Send IMAGE|... header.
            // 2) Wait server response on reader thread:
            //    - OK|IMAGE|storedName  => send bytes
            //    - ERR|IMAGE|code|msg   => abort
            pendingImage = new PendingImage(file, fileName, size);

            String header = "IMAGE|" + username + "|" + fileName + "|" + size + "\n";
            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
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
                    handleServerLine(line);
                    if (listener != null) {
                        listener.onMessage(line);
                    }
                }
            } catch (IOException ex) {
                if (listener != null) {
                    listener.onMessage("ERR|CONN|DISCONNECTED|Disconnected from server.");
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void handleServerLine(String line) {
        ProtocolMessage message = protocolParser.parseLine(line);
        if (message instanceof OkMessage okMessage) {
            if ("IMAGE".equalsIgnoreCase(okMessage.context())) {
                handleOkImage(okMessage.detail());
            }
            return;
        }
        if (message instanceof ErrMessage errMessage) {
            if ("IMAGE".equalsIgnoreCase(errMessage.context())) {
                synchronized (writeLock) {
                    pendingImage = null;
                }
            }
        }
    }

    private void handleOkImage(String storedName) {
        synchronized (writeLock) {
            PendingImage upload = pendingImage;
            if (upload == null) {
                return;
            }
            try {
                sendImageBytesLocked(upload.file());
                pendingImage = null;
            } catch (IOException ex) {
                pendingImage = null;
                if (listener != null) {
                    listener.onMessage("ERR|IMAGE|CLIENT_IO|" + ex.getMessage());
                }
            }
        }
    }

    private void sendImageBytesLocked(File file) throws IOException {
        if (out == null) {
            throw new IOException("Not connected.");
        }
        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        out.flush();
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

    private String safeUsername(String input) {
        String clean = sanitizeAndClip(input, Limits.MAX_USERNAME_LENGTH);
        if (clean.isEmpty()) {
            return "User";
        }
        return clean;
    }

    private String sanitizeAndClip(String value, int maxLength) {
        String clean = Validator.sanitizeProtocolField(value);
        if (clean.length() > maxLength) {
            clean = clean.substring(0, maxLength);
        }
        return clean;
    }

    private record PendingImage(File file, String fileName, long size) {
    }
}
