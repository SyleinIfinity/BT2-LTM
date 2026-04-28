package server.core;

import server.handler.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatServer {
    private final int port;
    private final Map<SocketChannel, ClientSession> sessions = new HashMap<>();
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
            selector.select();
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
                    closeKey(key);
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
        ClientSession session = new ClientSession(key);
        sessions.put(clientChannel, session);
        sendLine(session, "TEXT|SERVER|Connected to chat server.");
    }

    private void handleRead(SelectionKey key) throws IOException {
        // Read incoming data (text lines or file bytes).
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = sessions.get(channel);
        if (session == null) {
            closeKey(key);
            return;
        }
        if (!session.readFromChannel(channel)) {
            disconnect(channel);
            return;
        }
        clientHandler.consume(session);
    }

    private void handleWrite(SelectionKey key) throws IOException {
        // Flush pending outbound messages.
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = sessions.get(channel);
        if (session == null) {
            closeKey(key);
            return;
        }
        session.writeToChannel(channel);
        updateInterestOps(session);
    }

    public void sendLine(ClientSession session, String line) {
        // Queue a single line to one client.
        if (session == null) {
            return;
        }
        session.queueLine(line);
        updateInterestOps(session);
    }

    public void broadcastLine(String line) {
        // Broadcast a line to all connected clients.
        for (ClientSession session : sessions.values()) {
            session.queueLine(line);
            updateInterestOps(session);
        }
    }

    public void disconnect(ClientSession session) {
        if (session == null) {
            return;
        }
        SelectionKey key = session.getSelectionKey();
        if (key != null) {
            closeKey(key);
        }
    }

    private void disconnect(SocketChannel channel) {
        ClientSession session = sessions.remove(channel);
        if (session != null) {
            session.close();
        }
        try {
            channel.close();
        } catch (IOException ignored) {
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

    private void closeKey(SelectionKey key) {
        if (key == null) {
            return;
        }
        try {
            key.channel().close();
        } catch (IOException ignored) {
        }
        key.cancel();
        if (key.channel() instanceof SocketChannel) {
            sessions.remove((SocketChannel) key.channel());
        }
    }

    private void shutdown() {
        for (SocketChannel channel : sessions.keySet()) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
        sessions.clear();
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
