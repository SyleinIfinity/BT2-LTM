package server.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClientSession {
    private final SelectionKey selectionKey;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private final Queue<ByteBuffer> outbound = new ArrayDeque<>();

    // File receiving state.
    private boolean receivingFile;
    private boolean discardingFile;
    private long remainingFileBytes;
    private OutputStream fileOut;
    private String uploadUser;
    private String storedFileName;

    public ClientSession(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public boolean readFromChannel(SocketChannel channel) throws IOException {
        int read = channel.read(readBuffer);
        return read != -1;
    }

    public void queueLine(String line) {
        // Queue outbound text line in UTF-8.
        byte[] data = (line + "\n").getBytes(StandardCharsets.UTF_8);
        outbound.add(ByteBuffer.wrap(data));
    }

    public boolean hasPendingWrites() {
        return !outbound.isEmpty();
    }

    public void writeToChannel(SocketChannel channel) throws IOException {
        // Write pending buffers until the socket blocks.
        while (!outbound.isEmpty()) {
            ByteBuffer buffer = outbound.peek();
            channel.write(buffer);
            if (buffer.hasRemaining()) {
                break;
            }
            outbound.poll();
        }
    }

    public void appendLineByte(byte b) {
        lineBuffer.write(b);
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

    public void startReceivingFile(OutputStream outputStream, long size, String username, String storedName) {
        this.receivingFile = true;
        this.discardingFile = false;
        this.remainingFileBytes = size;
        this.fileOut = outputStream;
        this.uploadUser = username;
        this.storedFileName = storedName;
    }

    public void startDiscardingFile(long size) {
        this.receivingFile = true;
        this.discardingFile = true;
        this.remainingFileBytes = size;
        this.fileOut = null;
        this.uploadUser = null;
        this.storedFileName = null;
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
    }

    public void close() {
        if (fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException ignored) {
            }
        }
    }
}
