package server.core;

import common.protocol.messages.ErrMessage;
import common.security.Limits;
import server.handler.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {
    private final int port;
    private final Set<ClientSession> sessions = new HashSet<>();
    private final ClientHandler clientHandler;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public ChatServer(int port) {
        this.port = port;
        this.clientHandler = new ClientHandler(this);
    }

    public void start() {
        try {
            init();
            System.out.println("Chat server started on port " + port);
            // Single-threaded selector loop for all clients.
            loop();
        } catch (IOException ex) {
            System.err.println("Server error: " + ex.getMessage());
        } finally {
            shutdown();
        }
    }

    private void init() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void loop() throws IOException {
        while (true) {
            selector.select(1000);
            long now = System.currentTimeMillis();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }
                try {
                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException ex) {
                    ClientSession session = (ClientSession) key.attachment();
                    disconnect(session);
                }
            }
            disconnectTimedOutSessions(now);
        }
    }

    private void disconnectTimedOutSessions(long nowMillis) {
        for (ClientSession session : new HashSet<>(sessions)) {
            long idleMs = nowMillis - session.getLastActivityMillis();
            if (idleMs > Limits.IDLE_TIMEOUT_MS) {
                disconnect(session);
                continue;
            }

            if (session.isReceivingFile() && session.getUploadStartMillis() > 0) {
                long uploadMs = nowMillis - session.getUploadStartMillis();
                if (uploadMs > Limits.UPLOAD_TIMEOUT_MS) {
                    sendLine(session, new ErrMessage("IMAGE", "UPLOAD_TIMEOUT", "Upload timed out.").toProtocolLine());
                    disconnect(session);
                }
            }
        }
    }

    private void handleAccept() throws IOException {
        // Accept new client connection.
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            return;
        }
        clientChannel.configureBlocking(false);
        SelectionKey key = clientChannel.register(selector, SelectionKey.OP_READ);
        ClientSession session = new ClientSession(key, clientChannel);
        key.attach(session);
        sessions.add(session);
        sendLine(session, "TEXT|SERVER|Connected to chat server.");
    }

    private void handleRead(SelectionKey key) throws IOException {
        // Read incoming data (text lines or file bytes).
        ClientSession session = (ClientSession) key.attachment();
        if (session == null) {
            key.cancel();
            return;
        }
        SocketChannel channel = session.getChannel();
        if (!session.readFromChannel(channel)) {
            disconnect(session);
            return;
        }
        clientHandler.consume(session);
    }

    private void handleWrite(SelectionKey key) throws IOException {
        // Flush pending outbound messages.
        ClientSession session = (ClientSession) key.attachment();
        if (session == null) {
            key.cancel();
            return;
        }
        SocketChannel channel = session.getChannel();
        session.writeToChannel(channel);
        updateInterestOps(session);
    }

    public void sendLine(ClientSession session, String line) {
        // Queue a single line to one client.
        if (session == null) {
            return;
        }
        if (!session.queueLine(line)) {
            disconnect(session);
            return;
        }
        updateInterestOps(session);
    }

    public void broadcastLine(String line) {
        // Broadcast a line to all connected clients.
        for (ClientSession session : new HashSet<>(sessions)) {
            if (!session.queueLine(line)) {
                disconnect(session);
                continue;
            }
            updateInterestOps(session);
        }
    }

    public void disconnect(ClientSession session) {
        if (session == null) {
            return;
        }
        sessions.remove(session);

        SelectionKey key = session.getSelectionKey();
        SocketChannel channel = session.getChannel();
        session.close();

        if (key != null) {
            key.cancel();
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void updateInterestOps(ClientSession session) {
        SelectionKey key = session.getSelectionKey();
        if (key == null || !key.isValid()) {
            return;
        }
        int ops = key.interestOps();
        if (session.hasPendingWrites()) {
            ops |= SelectionKey.OP_WRITE;
        } else {
            ops &= ~SelectionKey.OP_WRITE;
        }
        key.interestOps(ops);
    }

    private void shutdown() {
        for (ClientSession session : new HashSet<>(sessions)) {
            disconnect(session);
        }
        if (serverChannel != null) {
            try {
                serverChannel.close();
            } catch (IOException ignored) {
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException ignored) {
            }
        }
    }
}
