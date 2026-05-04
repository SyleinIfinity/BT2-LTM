package common.protocol.messages;

import common.protocol.MessageType;

public final class ErrMessage implements ProtocolMessage {
    private final String context;
    private final String code;
    private final String message;

    public ErrMessage(String context, String code, String message) {
        this.context = context;
        this.code = code;
        this.message = message;
    }

    @Override
    public MessageType type() {
        return MessageType.ERR;
    }

    public String context() {
        return context;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public String toProtocolLine() {
        return "ERR|" + context + "|" + code + "|" + message;
    }
}
