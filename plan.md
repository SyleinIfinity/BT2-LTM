Bạn là một lập trình viên Java senior. Hãy xây dựng một hệ thống TCP Chat theo mô hình Client-Server với các yêu cầu sau: ===================== I. TỔNG QUAN ===================== - Ngôn ngữ: Java (chuẩn OOP, clean code) - Giao thức: TCP Socket - Kiến trúc: Client - Server - Server: single-threaded (không dùng multi-thread) - Thư mục gốc: BTTHS3 ===================== II. CẤU TRÚC THƯ MỤC ===================== Tạo cấu trúc project như sau: BTTHS3/ │ ├── server/ │ ├── core/ │ ├── handler/ │ ├── model/ │ └── ServerMain.java │ ├── client/ │ ├── ui/ │ ├── network/ │ ├── model/ │ └── ClientMain.java - Server tham khảo cấu trúc giống backend (MVC nhẹ) - Client tham khảo app JavaFX client ===================== III. YÊU CẦU SERVER ===================== 1. Xây dựng TCP Chat Server: - Lắng nghe kết nối từ client - Nhận và gửi tin nhắn text - Broadcast tin nhắn đến tất cả client đang kết nối - Single-threaded: xử lý tuần tự (có thể dùng select loop hoặc queue) 2. Nhận file ảnh từ client: - Nhận file dưới dạng byte stream - Lưu vào thư mục: /server/uploads/ - Đặt tên file tránh trùng (timestamp + original name) 3. Bảo mật: - Validate dữ liệu đầu vào - Giới hạn kích thước file (ví dụ: max 5MB) - Chỉ cho phép định dạng ảnh (jpg, png) - Không cho phép path traversal - Handle exception đầy đủ 4. Code phải: - Có comment rõ ràng từng phần - Tách class hợp lý (Server, ClientHandler, FileHandler,...) ===================== IV. YÊU CẦU CLIENT ===================== 1. TCP Client: - Kết nối đến server qua socket - Gửi/nhận tin nhắn 2. Giao diện (JavaFX): - Khung chat: + TextArea hiển thị tin nhắn + TextField nhập tin nhắn + Button gửi - Button chọn ảnh: + Mở FileChooser + Gửi ảnh lên server 3. Khi gửi ảnh: - Encode file thành byte[] - Gửi kèm metadata: + tên file + kích thước + loại file 4. Code: - Tách UI và network logic - Có comment đầy đủ ===================== V. GIAO THỨC TRAO ĐỔI (QUAN TRỌNG) ===================== Thiết kế protocol đơn giản: - Message text: TEXT|<username>|<message> - Gửi ảnh: IMAGE|<username>|<filename>|<filesize> (sau đó gửi byte stream) ===================== VI. OUTPUT YÊU CẦU ===================== - Viết đầy đủ code cho: + Server + Client - Đảm bảo chạy được - Có hướng dẫn chạy (main class) - Có comment giải thích logic quan trọng ===================== VII. LƯU Ý ===================== - Không dùng framework ngoài (chỉ Java core + JavaFX) - Code rõ ràng, dễ hiểu - Không viết quá tối giản, phải mang tính học tậpBạn là một lập trình viên Java senior. Hãy xây dựng một hệ thống TCP Chat theo mô hình Client-Server với các yêu cầu sau:

===================== I. TỔNG QUAN =====================
- Ngôn ngữ: Java (chuẩn OOP, clean code)
- Giao thức: TCP Socket
- Kiến trúc: Client - Server
- Server: Single-threaded đồng thời (BẮT BUỘC dùng Java NIO với Selector, SocketChannel, không dùng Thread hay ExecutorService).
- Thư mục gốc: BTTHS3

===================== II. CẤU TRÚC THƯ MỤC =====================
Tạo cấu trúc project như sau:
BTTHS3/
├── server/
│   ├── core/
│   ├── handler/
│   ├── model/
│   └── ServerMain.java
├── client/
│   ├── ui/
│   ├── network/
│   ├── model/
│   └── ClientMain.java
- Server: Cấu trúc MVC nhẹ.
- Client: Chuẩn JavaFX (có đủ Controller, FXML và CSS).

===================== III. YÊU CẦU SERVER =====================
1. Xây dựng TCP Chat Server (Java NIO):
- Sử dụng Selector loop để lắng nghe OP_ACCEPT và OP_READ.
- Quản lý danh sách client đang kết nối và broadcast tin nhắn đến tất cả.
2. Xử lý File ảnh:
- Nhận byte stream file chính xác theo kích thước (tránh TCP sticky packet).
- Lưu vào: /server/uploads/ (tạo thư mục nếu chưa có).
- Đặt tên: timestamp + "_" + original_name.
3. Bảo mật:
- Chặn tuyệt đối Path Traversal khi lưu file.
- Max file size: 5MB (vượt quá thì từ chối gói tin).
- Định dạng cho phép: .jpg, .png.

===================== IV. YÊU CẦU CLIENT (JAVAFX) =====================
1. Network Logic: 
- Dùng SocketChannel (non-blocking) hoặc Socket truyền thống chạy trên luồng riêng biệt (Task/Service) để không làm đơ UI.
- Gửi/Nhận dữ liệu an toàn bằng DataOutputStream/DataInputStream.
2. Giao diện (UI/UX) - Bắt buộc cung cấp file .fxml và .css:
- Thiết kế hiện đại, lấy cảm hứng từ Messenger.
- Tin nhắn hiển thị dạng "floating card" (bo góc, có padding, background khác màu giữa tin nhắn mình gửi và người khác gửi).
- Phối màu: Sử dụng Sky Blue (xanh da trời) làm chủ đạo và Sunset Orange (cam) cho các nút hành động (như nút Gửi tin, Chọn ảnh).
- Có ScrollPane tự động cuộn xuống khi có tin nhắn mới.
3. Chức năng Gửi ảnh:
- Mở FileChooser. Kiểm tra dung lượng (<5MB) trước khi gửi.

===================== V. GIAO THỨC TRAO ĐỔI (QUAN TRỌNG) =====================
Sử dụng DataOutputStream.writeUTF() cho metadata và write() cho byte stream.
- Message text: TEXT|<username>|<message>
- Gửi ảnh: Bước 1 gửi metadata: IMAGE|<username>|<filename>|<filesize>
           Bước 2: Gửi chính xác <filesize> byte của file ảnh.

===================== VI. OUTPUT YÊU CẦU =====================
- In ra TOÀN BỘ MÃ NGUỒN từ dòng đầu đến dòng cuối cho tất cả các file. KHÔNG được viết tóm tắt hay dùng từ "tương tự như trên".
- Có comment tiếng Việt giải thích logic NIO và xử lý byte stream.