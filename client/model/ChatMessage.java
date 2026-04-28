package client.model;

public class ChatMessage {
    private final String username;
    private final String message;

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public static ChatMessage fromProtocol(String line) {
        if (line == null || !line.startsWith("TEXT|")) {
            return null;
        }
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        if (first < 0 || second < 0) {
            return null;
        }
        String username = line.substring(first + 1, second);
        String message = line.substring(second + 1);
        return new ChatMessage(username, message);
    }
}
