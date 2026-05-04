package server.handler;

import common.protocol.ProtocolParser;
import common.protocol.messages.ErrMessage;
import common.protocol.messages.ImageRequest;
import common.protocol.messages.OkMessage;
import common.protocol.messages.ProtocolMessage;
import common.protocol.messages.TextMessage;
import common.security.ValidationException;
import common.security.Validator;
import server.core.ChatServer;
import server.config.ServerConfig;
import server.core.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class ClientHandler {
    private final ChatServer server;
    private final FileHandler fileHandler;
    private final ProtocolParser protocolParser = new ProtocolParser();

    public ClientHandler(ChatServer server) {
        this.server = server;
        // PACK: use configured upload directory
        this.fileHandler = new FileHandler(ServerConfig.getUploadDir());
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
                if (!session.appendLineByte(b)) {
                    // FIX: Trả lỗi protocol rõ ràng trước khi đóng kết nối khi line quá dài.
                    server.sendLine(session, new ErrMessage("PROTO", "LINE_TOO_LONG", "Invalid input.").toProtocolLine());
                    server.disconnect(session);
                    return;
                }
            }
        }
    }

    private void processFileBytes(ClientSession session, ByteBuffer buffer) throws IOException {
        // Stream file bytes to disk with image hardening:
        // - probe magic bytes first (PNG/JPEG)
        // - only start writing after magic is verified
        long remaining = session.getRemainingFileBytes();
        int toRead = (int) Math.min(buffer.remaining(), remaining);
        byte[] chunk = new byte[toRead];
        buffer.get(chunk);

        if (!session.isDiscardingFile()) {
            handleUploadBytes(session, chunk);
        }

        session.consumeFileBytes(toRead);
        if (session.getRemainingFileBytes() == 0) {
            boolean discarded = session.isDiscardingFile();
            String user = session.getUploadUser();
            String storedName = session.getStoredFileName();
            UploadTarget target = null;
            if (!discarded && session.getUploadTempPath() != null && session.getUploadFinalPath() != null) {
                target = new UploadTarget(session.getUploadFinalPath(), session.getUploadTempPath(), storedName, session.getUploadExtensionLower());
            }
            session.finishFile();

            if (!discarded) {
                if (target != null) {
                    try {
                        fileHandler.commitUpload(target);
                    } catch (IOException ex) {
                        fileHandler.abortUpload(target);
                        server.sendLine(session, new ErrMessage("IMAGE", "SERVER_ERROR", "Failed to finalize upload.").toProtocolLine());
                        server.disconnect(session);
                        return;
                    }
                }
                server.broadcastLine("TEXT|SERVER|" + user + " sent image: " + storedName);
            }
        }
    }

    private void handleUploadBytes(ClientSession session, byte[] chunk) throws IOException {
        if (!session.isMagicVerified()) {
            session.bufferPreMagicBytes(chunk, 0, chunk.length);
            int needed = "png".equals(session.getUploadExtensionLower()) ? 4 : 2;
            if (session.getPreMagicSize() < needed) {
                return;
            }

            byte[] buffered = session.drainPreMagicBytes();
            boolean ok = fileHandler.isMagicBytesValid(session.getUploadExtensionLower(), buffered, buffered.length);
            if (!ok) {
                UploadTarget target = new UploadTarget(
                        session.getUploadFinalPath(),
                        session.getUploadTempPath(),
                        session.getStoredFileName(),
                        session.getUploadExtensionLower()
                );
                fileHandler.abortUpload(target);
                server.sendLine(session, new ErrMessage("IMAGE", "NOT_IMAGE", "Magic bytes validation failed.").toProtocolLine());
                server.disconnect(session);
                throw new IOException("Magic bytes validation failed.");
            }

            session.markMagicVerified();
            UploadTarget target = new UploadTarget(
                    session.getUploadFinalPath(),
                    session.getUploadTempPath(),
                    session.getStoredFileName(),
                    session.getUploadExtensionLower()
            );
            OutputStream out = fileHandler.openTempUploadStream(target);
            session.setFileOut(out);
            out.write(buffered);
            return;
        }

        OutputStream out = session.getFileOut();
        if (out == null) {
            UploadTarget target = new UploadTarget(
                    session.getUploadFinalPath(),
                    session.getUploadTempPath(),
                    session.getStoredFileName(),
                    session.getUploadExtensionLower()
            );
            out = fileHandler.openTempUploadStream(target);
            session.setFileOut(out);
        }
        out.write(chunk);
    }

    private void handleLine(ClientSession session, String line) throws IOException {
        if (!session.allowCommandRate()) {
            // FIX: Chặn spam command để giảm abuse protocol.
            server.sendLine(session, new ErrMessage("PROTO", "RATE_LIMIT", "Invalid input.").toProtocolLine());
            server.disconnect(session);
            return;
        }

        // Parse protocol command and route to handler.
        ProtocolMessage message;
        try {
            message = protocolParser.parseLine(line);
        } catch (RuntimeException ex) {
            // FIX: Không để parse lỗi runtime làm sập phiên.
            server.sendLine(session, new ErrMessage("PROTO", "MALFORMED", "Invalid input.").toProtocolLine());
            server.disconnect(session);
            return;
        }
        if (message instanceof TextMessage textMessage) {
            handleText(session, textMessage);
            return;
        }
        if (message instanceof ImageRequest imageRequest) {
            handleImage(session, imageRequest);
            return;
        }
        // FIX: Ẩn chi tiết nội bộ, chỉ trả thông báo lỗi chung.
        server.sendLine(session, new ErrMessage("PROTO", "UNKNOWN_COMMAND", "Invalid input.").toProtocolLine());
    }

    private void handleText(ClientSession session, TextMessage incoming) {
        // TEXT|username|message
        try {
            String username = Validator.validateUsername(incoming.username());
            String message = Validator.validateMessage(incoming.message());
            server.broadcastLine(new TextMessage(username, message).toProtocolLine());
        } catch (ValidationException ex) {
            // FIX: Không lộ chi tiết validation nội bộ cho client.
            server.sendLine(session, new ErrMessage("TEXT", "VALIDATION_FAILED", "Invalid input.").toProtocolLine());
        }
    }

    private void handleImage(ClientSession session, ImageRequest incoming) {
        // IMAGE handshake:
        // 1) Client sends IMAGE|username|filename|size
        // 2) Server validates and replies:
        //      OK|IMAGE|storedName
        //    or ERR|IMAGE|code|reason
        // 3) Client sends exactly <size> bytes only after OK.
        String username;
        try {
            username = Validator.validateUsername(incoming.username());
        } catch (ValidationException ex) {
            // FIX: Không lộ chi tiết validation nội bộ cho client.
            server.sendLine(session, new ErrMessage("IMAGE", "BAD_USERNAME", "Invalid input.").toProtocolLine());
            server.disconnect(session);
            return;
        }

        String filename = incoming.fileName();
        long size = incoming.size();

        try {
            Validator.validateFileSize(size);
        } catch (ValidationException ex) {
            // FIX: Không lộ chi tiết validation nội bộ cho client.
            server.sendLine(session, new ErrMessage("IMAGE", "BAD_FILE_SIZE", "Invalid input.").toProtocolLine());
            return;
        }

        try {
            UploadTarget target = fileHandler.createUploadTarget(filename, size);
            int minMagicBytes = "png".equals(target.getExtensionLower()) ? 4 : 2;
            if (size < minMagicBytes) {
                server.sendLine(session, new ErrMessage("IMAGE", "BAD_FILE_SIZE", "File too small.").toProtocolLine());
                return;
            }
            session.startReceivingFile(
                    target.getFinalPath(),
                    target.getTempPath(),
                    target.getExtensionLower(),
                    size,
                    username,
                    target.getStoredFileName()
            );
            server.sendLine(session, new OkMessage("IMAGE", target.getStoredFileName()).toProtocolLine());
        } catch (ValidationException ex) {
            // FIX: Không lộ chi tiết validation nội bộ cho client.
            server.sendLine(session, new ErrMessage("IMAGE", "VALIDATION_FAILED", "Invalid input.").toProtocolLine());
        } catch (IOException ex) {
            server.sendLine(session, new ErrMessage("IMAGE", "SERVER_ERROR", "Server error while saving file.").toProtocolLine());
        }
    }
}
