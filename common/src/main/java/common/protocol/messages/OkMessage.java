package common.protocol.messages;

import common.protocol.MessageType;

public final class OkMessage implements ProtocolMessage {
    private final String context;
    private final String detail;

    public OkMessage(String context, String detail) {
        this.context = context;
        this.detail = detail;
    }

    @Override
    public MessageType type() {
        return MessageType.OK;
    }

    public String context() {
        return context;
    }

    public String detail() {
        return detail;
    }

    public String toProtocolLine() {
        return "OK|" + context + "|" + detail;
    }
}
