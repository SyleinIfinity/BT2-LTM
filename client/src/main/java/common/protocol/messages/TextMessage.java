package common.protocol.messages;

import common.protocol.MessageType;

public final class TextMessage implements ProtocolMessage {
    private final String username;
    private final String message;

    public TextMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

    @Override
    public MessageType type() { return MessageType.TEXT; }

    public String username() { return username; }
    public String message() { return message; }

    public String toProtocolLine() { return "TEXT|" + username + "|" + message; }
}
