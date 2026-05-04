package server;

import server.core.ChatServer;
import server.config.ServerConfig;
import server.util.LoggerUtil;

public class ServerMain {
    public static void main(String[] args) {
        // PACK: Load external configuration and initialize logging before starting server.
        ServerConfig.load();
        LoggerUtil.setup();

        int port = ServerConfig.getPort();
        if (args.length > 0) {
            try {
                int argPort = Integer.parseInt(args[0]);
                if (argPort >= 1 && argPort <= 65535) {
                    port = argPort;
                } else {
                    // FIX: Chặn cổng không hợp lệ để tránh lỗi runtime/abuse cấu hình.
                    System.out.println("Invalid port range, using configured/default port.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid port argument, using configured/default port.");
            }
        }

        ChatServer server = new ChatServer(port);
        server.start();
    }
}
