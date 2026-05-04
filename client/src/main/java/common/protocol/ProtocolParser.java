package common.protocol;

import common.protocol.messages.ErrMessage;
import common.protocol.messages.ImageRequest;
import common.protocol.messages.OkMessage;
import common.protocol.messages.ProtocolMessage;
import common.protocol.messages.TextMessage;

public final class ProtocolParser {
    public ProtocolMessage parseLine(String line) {
        if (line == null) return null;
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.startsWith("TEXT|")) return parseText(trimmed);
        if (trimmed.startsWith("IMAGE|")) return parseImage(trimmed);
        if (trimmed.startsWith("OK|")) return parseOk(trimmed);
        if (trimmed.startsWith("ERR|")) return parseErr(trimmed);
        return null;
    }

    private TextMessage parseText(String line) {
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        if (first < 0 || second < 0) return null;
        String username = line.substring(first + 1, second);
        String message = line.substring(second + 1);
        return new TextMessage(username, message);
    }

    private ImageRequest parseImage(String line) {
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        int third = line.indexOf('|', second + 1);
        if (first < 0 || second < 0 || third < 0) return null;
        String username = line.substring(first + 1, second);
        String filename = line.substring(second + 1, third);
        String sizeText = line.substring(third + 1);
        long size;
        try { size = Long.parseLong(sizeText); } catch (NumberFormatException ex) { return null; }
        return new ImageRequest(username, filename, size);
    }

    private OkMessage parseOk(String line) {
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        if (first < 0 || second < 0) return null;
        String context = line.substring(first + 1, second);
        String detail = line.substring(second + 1);
        return new OkMessage(context, detail);
    }

    private ErrMessage parseErr(String line) {
        int first = line.indexOf('|');
        int second = line.indexOf('|', first + 1);
        int third = line.indexOf('|', second + 1);
        if (first < 0 || second < 0 || third < 0) return null;
        String context = line.substring(first + 1, second);
        String code = line.substring(second + 1, third);
        String message = line.substring(third + 1);
        return new ErrMessage(context, code, message);
    }
}
