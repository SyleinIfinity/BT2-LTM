package common.protocol.messages;

import common.protocol.MessageType;

public final class ImageRequest implements ProtocolMessage {
    private final String username;
    private final String fileName;
    private final long size;
    public ImageRequest(String username, String fileName, long size) { this.username = username; this.fileName = fileName; this.size = size; }
    @Override public MessageType type() { return MessageType.IMAGE; }
    public String username() { return username; }
    public String fileName() { return fileName; }
    public long size() { return size; }
    public String toProtocolLine() { return "IMAGE|" + username + "|" + fileName + "|" + size; }
}
