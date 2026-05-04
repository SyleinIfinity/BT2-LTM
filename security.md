# Security Audit Report

## Scope

- Server: Java NIO selector loop, session state, protocol handler, upload pipeline.
- Client: protocol reader/writer and input sanitization utilities.
- Protocol: line-delimited framing and command parsing.

## 1. Input Validation

- Issue:
  - Sanitization previously removed only `\r`, `\n`, `|`, missing other control chars (`\t`, `\0`).
  - Validation errors sometimes leaked detailed reason strings to clients.
- Fix:
  - Updated sanitization in both server and client validators to remove `\r`, `\n`, `\t`, `\0`, `|`.
  - Kept strict length checks: username <= 24, message <= 1000.
  - Returned generic error messages (`Invalid input.`) from server for validation failures.

## 2. Path Traversal

- Issue:
  - Upload logic already had traversal checks but base dir normalization could be stricter.
- Fix:
  - Normalized upload base directory to absolute normalized path in `FileHandler` constructor.
  - Continued enforcing `Paths.get(...).getFileName()` and `ensureInsideBaseDir(...)`.

## 3. TCP Framing / Sticky Packets

- Issue:
  - Protocol depends on delimiter framing and had a line-size cap server-side; client reader had no cap.
- Fix:
  - Preserved delimiter-based framing (`\n`) to avoid protocol break.
  - Added client-side line length bound using `Limits.MAX_LINE_BYTES` to reduce memory DoS risk.

## 4. File Upload Security

- Issue:
  - Need strict controls for extension, size, write location, and content type.
- Fix:
  - Enforced extension whitelist (jpg/jpeg/png), max size (5MB), and base-dir confinement.
  - Preserved magic-byte validation before writing final content.
  - Preserved temp-file write + commit/abort flow.

## 5. DoS Protection & Resource Control

- Issue:
  - No explicit connection cap and no command-level abuse control per client.
  - Idle timeout was relatively loose.
- Fix:
  - Added `MAX_CONNECTIONS` and reject new accepts over limit.
  - Added per-session command rate limiting (`MAX_COMMANDS_PER_WINDOW`, `COMMAND_RATE_WINDOW_MS`).
  - Tightened idle timeout to 60s.
  - Retained outbound queue cap (`MAX_OUTBOUND_BYTES_PER_SESSION`) and header line cap (`MAX_LINE_BYTES`).

## 6. NIO Resource Safety

- Issue:
  - Cancelled/invalid keys can race and cause runtime failures in selector flow.
- Fix:
  - Re-check key validity between operations in selector loop.
  - Catch `CancelledKeyException` and disconnect safely.
  - Guarded interest-op updates against cancelled keys.
  - Session/channel close + key cancel remain enforced on disconnect paths.

## 7. ByteBuffer Safety

- Issue:
  - Needed confirmation of safe lifecycle in read/parse loop.
- Fix:
  - Confirmed and preserved `flip()` -> consume -> `compact()` lifecycle in handler.
  - Added defensive malformed-command handling to avoid runtime parser failures escaping loop.

## 8. Error Handling & Information Disclosure

- Issue:
  - Server previously echoed detailed validation messages from exceptions.
- Fix:
  - Replaced with generic client-facing messages (`Invalid input.` / `Server error ...`) while keeping internal handling local.
  - Avoided returning stack traces to client protocol.

## 9. Protocol Abuse Protection

- Issue:
  - Unknown/malformed command abuse and command spam were weakly controlled.
- Fix:
  - Added per-session rate limit check before command processing.
  - Added explicit malformed-command handling and generic rejection.
  - Added explicit line-too-long rejection path.

## Files Hardened

- `server/src/main/java/common/security/Limits.java`
- `server/src/main/java/common/security/Validator.java`
- `server/src/main/java/server/core/ClientSession.java`
- `server/src/main/java/server/core/ChatServer.java`
- `server/src/main/java/server/handler/ClientHandler.java`
- `server/src/main/java/server/handler/FileHandler.java`
- `server/src/main/java/server/ServerMain.java`
- `client/src/main/java/common/security/Limits.java`
- `client/src/main/java/common/security/Validator.java`
- `client/src/main/java/client/network/ChatClient.java`

## Verification Notes

- Build check passed:
  - `mvn -f server/pom.xml clean package -DskipTests`
  - `mvn -f client/pom.xml clean package -DskipTests`
- Runtime smoke check passed (text send/receive).
- Oversized-line test no longer crashes server loop; client session is dropped safely.

## Optional Hardening (Bonus)

- TODO: Upgrade to TLS (SSLEngine) for encrypted transport.
- TODO: Add authentication/session token to prevent impersonation.

## Phase 2 - Hardening & Packaging

- Rate limiting added (per-session command window).
- Logging added (logs/server.log) and configurable logs directory.
- Config externalized to `config/server.properties` (port, limits, dirs).
- File upload secured (size limits, extension whitelist, magic-bytes, temp files, base-dir confinement).
