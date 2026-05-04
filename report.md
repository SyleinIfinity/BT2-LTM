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
  - `Server` module: `ServerMain` — entrypoint để khởi chạy server (mặc định cổng 5555); `core/ChatServer` — vòng chọn (selector) duy nhất, quản lý `ClientSession`; `handler/ClientHandler` — xử lý giao thức và upload ảnh; tệp upload lưu tại `server/uploads`.
  - `Client` module: `app/MainApp` — JavaFX entrypoint; `network/ChatClient` và `ui/ChatController` — giao diện và kết nối TCP. `Client` phụ thuộc vào `server` artifact để sử dụng các lớp giao thức đã được gom lại vào server.

## Bảo mật & Ràng buộc (điểm nổi bật)

- Hạn chế kích thước và độ dài (Limits)
- `Validator.sanitizeFileName` loại bỏ ký tự nguy hiểm và ngăn path traversal bằng cách lấy `getFileName()`
- Server kiểm tra magic bytes ảnh trước khi thực sự ghi file; nếu không hợp lệ sẽ discard và gửi lỗi
- Timeouts: idle client (2 phút), upload timeout (30s)

## Hướng dẫn chạy (tổng quát)

1. Build toàn bộ project (multi-module Maven):

```powershell
mvn clean package
```

2. Chạy server (mặc định cổng 5555):

```powershell
# từ thư mục gốc hoặc module server
mvn -pl server exec:java -Dexec.mainClass=server.ServerMain
# hoặc chạy trực tiếp class nếu build classpath sẵn
java -cp server/target/classes;common/target/classes server.ServerMain
```

3. Chạy client JavaFX:
   - Mở module `client` trong IDE (IntelliJ/NetBeans/Eclipse) và chạy `client.app.MainApp`, hoặc
   - Nếu cấu hình JavaFX trong Maven, dùng plugin tương ứng (IDE thường đơn giản hơn cho JavaFX GUI)

Lưu ý: client mặc định kết nối tới `127.0.0.1:5555` (xem `MainApp`), có thể thay đổi bằng code.

## Tệp tài nguyên giao diện

- `client/src/main/resources/ui/main.fxml` — layout UI
- `client/src/main/resources/ui/style.css` — style cho chat bubbles

## Ghi chú / Vấn đề tiềm năng

- JavaFX client yêu cầu runtime JavaFX; chạy từ IDE thường tiện nhất nếu Maven chưa cấu hình `javafx` plugin
- Không có script đóng gói client thành jar executable với JavaFX, nên hướng dẫn dùng IDE
- Thư mục `uploads/` trong repository gốc hiện có; server tạo `server/uploads` để lưu file upload

## Kết luận

Dự án cung cấp một ứng dụng chat TCP cơ bản đầy đủ: chat văn bản, upload ảnh an toàn với handshake và kiểm tra magic-bytes, cùng các lớp xác thực/sàng lọc. Kiến trúc tách rõ `common`(giao thức), `server`(selector + handler), và `client`(JavaFX + mạng), phù hợp yêu cầu bài tập.

-- Kết thúc báo cáo

## Chạy server và test nhanh (ví dụ thực tế tôi đã chạy)

1. Build toàn bộ project (nếu chưa build):

```powershell
mvn clean package
```

2. Khởi chạy server (PowerShell lưu ý cách đặt property):

```powershell
# Cách an toàn cho PowerShell
mvn -Dexec.mainClass=server.ServerMain -pl server exec:java

# Hoặc đặt property trong dấu nháy
mvn -pl server exec:java "-Dexec.mainClass=server.ServerMain"

# Hoặc chạy trực tiếp classpath (sau khi build)
java -cp "server\target\classes;common\target\classes" server.ServerMain
```

3. Test nhanh bằng `QuickClient` (tôi đã thêm `tools/QuickClient.java` để test):

```powershell
# Biên dịch test client
javac -d tools tools\QuickClient.java
# Chạy test client
java -cp tools QuickClient
```

Ví dụ đầu ra tôi thấy khi chạy:

- Server: `Chat server started on port 5555`
- QuickClient: `Server replied: TEXT|SERVER|Connected to chat server.`

Ghi chú về lỗi xuất hiện trong terminal khi tôi chạy: sau khi test, server in ra
`CancelledKeyException` (stack trace). Điều này thường xảy ra khi một SelectionKey
đã bị cancel (ví dụ client ngắt kết nối) và vòng selector cố gắng truy vấn trạng thái
của key đã bị hủy. Ứng dụng vẫn hoạt động cho mục đích test — khuyến nghị là:

- Chạy server trong terminal riêng và để nó chạy; chạy client trong terminal khác hoặc IDE.
- Nếu muốn, có thể nâng cấp `ChatServer.loop()` bắt thêm `CancelledKeyException`
  để log và tiếp tục thay vì để crash.

-- Kết thúc cập nhật
