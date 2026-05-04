# Báo cáo - Dự án BTTHS3

## Tổng quan

Dự án là một hệ thống chat TCP gồm hai module chính: `client` và `server`. Mọi logic xử lý và lớp giao thức đã được gom vào `server`; `client` giữ phần giao diện JavaFX và gửi/nhận dữ liệu qua TCP.

## Chức năng chính

- Chat văn bản theo dòng: `TEXT|<username>|<message>`
- Upload ảnh với handshake (header + bytes): `IMAGE|<username>|<filename>|<size>` sau đó client gửi chính xác <size> bytes khi server trả `OK|IMAGE|<storedName>`; lỗi trả `ERR|IMAGE|<code>|<msg>`
- Server broadcast tin nhắn văn bản và thông báo ảnh tới tất cả client
- Kiểm tra và giới hạn: tên người dùng, độ dài tin nhắn, kích thước file, magic-bytes kiểm tra ảnh (PNG/JPEG), timeouts, và bảo vệ path traversal khi lưu file

## Giao thức (tóm tắt)

- TEXT: `TEXT|username|message` — gửi/nhận văn bản
- IMAGE: `IMAGE|username|filename|size` — client khởi tạo upload; server trả `OK|IMAGE|storedName` để client gửi bytes; server trả `ERR|IMAGE|...` nếu từ chối
- OK: `OK|context|detail`
- ERR: `ERR|context|code|message`

## Kiến trúc và các lớp quan trọng

- `server` (bao gồm cả các lớp giao thức/chiến lược xử lý):
  - `ProtocolParser` — phân tích dòng giao thức thành các message object
  - `messages/*` — `TextMessage`, `ImageRequest`, `OkMessage`, `ErrMessage`, `ProtocolMessage`
  - `security/Validator` và `Limits` — sanitize/validate và các hằng giới hạn (username max 24, message max 1000, file max 5MB, timeouts)
  - `core/ChatServer`, `handler/*` — logic server, upload và lưu file
- `client`:
  - `app/MainApp` — JavaFX entrypoint
  - `network/ChatClient`, `ui/ChatController` — giao diện và kết nối TCP
  - Có bản sao package `common.protocol` và `common.security` trong `client/src/main/java/common` để chạy độc lập với server

## Bảo mật & Ràng buộc (điểm nổi bật)

- Hạn chế kích thước và độ dài (Limits)
- `Validator.sanitizeFileName` loại bỏ ký tự nguy hiểm và ngăn path traversal bằng cách lấy `getFileName()`
- Server kiểm tra magic bytes ảnh trước khi thực sự ghi file; nếu không hợp lệ sẽ discard và gửi lỗi
- Timeouts: idle client (2 phút), upload timeout (30s)

## Hướng dẫn chạy (phiên bản hiện tại)

Hiện tại `client` và `server` là hai dự án Maven độc lập, mỗi thư mục có `pom.xml` riêng.

1. Build server:

```powershell
mvn -f server/pom.xml clean package -DskipTests
```

2. Chạy server (port mặc định 5555):

```powershell
mvn -f server/pom.xml exec:java
```

3. Build client:

```powershell
mvn -f client/pom.xml clean package -DskipTests
```

4. Chạy client JavaFX:

```powershell
mvn -f client/pom.xml javafx:run
```

Lưu ý:

- Chạy server trước, sau đó mới chạy client.
- Client mặc định kết nối `127.0.0.1:5555` (trong `MainApp`).
- Nếu không chạy được JavaFX bằng Maven trên máy cục bộ, có thể chạy `client.app.MainApp` trực tiếp từ IDE.

## Tệp tài nguyên giao diện

- `client/src/main/resources/ui/main.fxml` — layout UI
- `client/src/main/resources/ui/style.css` — style cho chat bubbles

## Ghi chú / Vấn đề tiềm năng

- JavaFX client yêu cầu runtime JavaFX; chạy từ IDE thường tiện nhất nếu Maven chưa cấu hình `javafx` plugin
- Không có script đóng gói client thành jar executable với JavaFX, nên hướng dẫn dùng IDE
- Thư mục `uploads/` trong repository gốc hiện có; server tạo `server/uploads` để lưu file upload

## Demo chạy thực tế

Đã test runtime theo luồng:

1. `mvn -f server/pom.xml exec:java` -> server in: `Chat server started on port 5555`.
2. Dùng TCP client PowerShell gửi: `TEXT|DemoClient2|Test sau khi fix`.
3. Nhận về:
   - `TEXT|SERVER|Connected to chat server.`
   - `TEXT|DemoClient2|Test sau khi fix`

Lỗi `CancelledKeyException` trong `ChatServer.loop()` đã được xử lý bằng cách:

- kiểm tra lại `key.isValid()` sau `handleAccept()` và `handleRead()`;
- bắt `CancelledKeyException` để disconnect session an toàn thay vì làm server dừng.

## Kết luận

Dự án hiện vận hành theo mô hình 2 thư mục độc lập (`client`, `server`), vẫn giữ đầy đủ chức năng chat text + upload ảnh, đồng thời đã vá lỗi runtime selector trên server.

-- Kết thúc báo cáo
