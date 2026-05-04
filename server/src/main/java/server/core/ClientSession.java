package server.core;

import common.security.Limits;
import server.config.ServerConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClientSession {
    private final SelectionKey selectionKey;
    private final SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private final Queue<ByteBuffer> outbound = new ArrayDeque<>();
    private long pendingOutboundBytes;

    private long lastActivityMillis = System.currentTimeMillis();

    // FIX: Theo dõi tần suất command để chặn abuse/spam protocol.
    private long commandWindowStartMillis = System.currentTimeMillis();
    private int commandCountInWindow;

    // File receiving state.
    private boolean receivingFile;
    private boolean discardingFile;
    private long remainingFileBytes;
    private OutputStream fileOut;
    private String uploadUser;
    private String storedFileName;
    private Path uploadFinalPath;
    private Path uploadTempPath;
    private String uploadExtensionLower;
    private boolean magicVerified;
    private final ByteArrayOutputStream preMagicBuffer = new ByteArrayOutputStream();
    private long uploadStartMillis;

    public ClientSession(SelectionKey selectionKey, SocketChannel channel) {
        this.selectionKey = selectionKey;
        this.channel = channel;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public boolean readFromChannel(SocketChannel channel) throws IOException {
        int read = channel.read(readBuffer);
        if (read > 0) {
            touch();
        }
        return read != -1;
    }

    public boolean queueLine(String line) {
        // Queue outbound text line in UTF-8.
        byte[] data = (line + "\n").getBytes(StandardCharsets.UTF_8);
        long nextPending = pendingOutboundBytes + data.length;
        if (nextPending > Limits.MAX_OUTBOUND_BYTES_PER_SESSION) {
            return false;
        }
        pendingOutboundBytes = nextPending;
        outbound.add(ByteBuffer.wrap(data));
        return true;
    }

    public boolean hasPendingWrites() {
        return !outbound.isEmpty();
    }

    public void writeToChannel(SocketChannel channel) throws IOException {
        // Write pending buffers until the socket blocks.
        while (!outbound.isEmpty()) {
            ByteBuffer buffer = outbound.peek();
            int written = channel.write(buffer);
            if (written > 0) {
                touch();
            }
            if (buffer.hasRemaining()) {
                break;
            }
            outbound.poll();
            pendingOutboundBytes -= buffer.capacity();
        }
    }

    public boolean appendLineByte(byte b) {
        if (lineBuffer.size() + 1 > ServerConfig.getMaxLineBytes()) {
            return false;
        }
        lineBuffer.write(b);
        return true;
    }

    public String consumeLine() {
        String line = new String(lineBuffer.toByteArray(), StandardCharsets.UTF_8);
        lineBuffer.reset();
        return line;
    }

    public boolean isReceivingFile() {
        return receivingFile;
    }

    public boolean isDiscardingFile() {
        return discardingFile;
    }

    public long getRemainingFileBytes() {
        return remainingFileBytes;
    }

    public OutputStream getFileOut() {
        return fileOut;
    }

    public String getUploadUser() {
        return uploadUser;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public Path getUploadFinalPath() {
        return uploadFinalPath;
    }

    public Path getUploadTempPath() {
        return uploadTempPath;
    }

    public String getUploadExtensionLower() {
        return uploadExtensionLower;
    }

    public boolean isMagicVerified() {
        return magicVerified;
    }

    public int getPreMagicSize() {
        return preMagicBuffer.size();
    }

    public void startReceivingFile(Path finalPath, Path tempPath, String extensionLower, long size, String username, String storedName) {
        this.receivingFile = true;
        this.discardingFile = false;
        this.remainingFileBytes = size;
        this.fileOut = null;
        this.uploadUser = username;
        this.storedFileName = storedName;
        this.uploadFinalPath = finalPath;
        this.uploadTempPath = tempPath;
        this.uploadExtensionLower = extensionLower;
        this.magicVerified = false;
        this.preMagicBuffer.reset();
        this.uploadStartMillis = System.currentTimeMillis();
        touch();
    }

    public void startDiscardingFile(long size) {
        this.receivingFile = true;
        this.discardingFile = true;
        this.remainingFileBytes = size;
        this.fileOut = null;
        this.uploadUser = null;
        this.storedFileName = null;
        this.uploadFinalPath = null;
        this.uploadTempPath = null;
        this.uploadExtensionLower = null;
        this.magicVerified = false;
        this.preMagicBuffer.reset();
        this.uploadStartMillis = System.currentTimeMillis();
        touch();
    }

    public void consumeFileBytes(int count) {
        remainingFileBytes -= count;
    }

    public void finishFile() {
        if (fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException ignored) {
            }
        }
        fileOut = null;
        receivingFile = false;
        discardingFile = false;
        remainingFileBytes = 0;
        uploadUser = null;
        storedFileName = null;
        uploadFinalPath = null;
        uploadTempPath = null;
        uploadExtensionLower = null;
        magicVerified = false;
        preMagicBuffer.reset();
        uploadStartMillis = 0;
    }

    public void close() {
        if (uploadTempPath != null) {
            try {
                Files.deleteIfExists(uploadTempPath);
            } catch (IOException ignored) {
            }
        }
        if (fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException ignored) {
            }
        }
        fileOut = null;
        receivingFile = false;
        discardingFile = false;
        remainingFileBytes = 0;
        uploadUser = null;
        storedFileName = null;
        uploadFinalPath = null;
        uploadTempPath = null;
        uploadExtensionLower = null;
        magicVerified = false;
        preMagicBuffer.reset();
        uploadStartMillis = 0;

        lineBuffer.reset();
        outbound.clear();
        pendingOutboundBytes = 0;
    }

    public void setFileOut(OutputStream out) {
        this.fileOut = out;
    }

    public void markMagicVerified() {
        this.magicVerified = true;
    }

    public void bufferPreMagicBytes(byte[] src, int offset, int length) {
        preMagicBuffer.write(src, offset, length);
    }

    public byte[] drainPreMagicBytes() {
        byte[] data = preMagicBuffer.toByteArray();
        preMagicBuffer.reset();
        return data;
    }

    public long getLastActivityMillis() {
        return lastActivityMillis;
    }

    public long getUploadStartMillis() {
        return uploadStartMillis;
    }

    public boolean allowCommandRate() {
        long now = System.currentTimeMillis();
        if (now - commandWindowStartMillis > ServerConfig.getCommandRateWindowMs()) {
            commandWindowStartMillis = now;
            commandCountInWindow = 0;
        }
        commandCountInWindow++;
        return commandCountInWindow <= ServerConfig.getMaxCommandsPerWindow();
    }

    private void touch() {
        lastActivityMillis = System.currentTimeMillis();
    }
}
