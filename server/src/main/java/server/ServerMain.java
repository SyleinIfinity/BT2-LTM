package server;

import server.core.ChatServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 5555;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid port, using default 5555.");
            }
        }

        ChatServer server = new ChatServer(port);
        server.start();
    }
}
