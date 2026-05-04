package common.protocol.messages;

import common.protocol.MessageType;

/**
 * Marker interface for parsed protocol messages.
 */
public interface ProtocolMessage {
    MessageType type();
}
