# 🎤 NỘI DUNG THUYẾT TRÌNH – VIECHAT
**Môn:** Quản lý Dự án CNTT | **Nhóm:** 01 | **Thời gian:** ~10 phút

---

## 📊 BẢNG PHÂN CÔNG

| Người | Slide | Nội dung | Thời gian |
|---|---|---|---|
| Phan Văn Khánh | Slide 1 – 4 | Giới thiệu + Tổng quan + Tổ chức + Các bên liên quan | ~2 phút |
| Nguyễn Thành Mạnh | Slide 5 – 8 | Vai trò & Trách nhiệm + Agile Scrum + Cấu trúc Sprint + Công cụ | ~2 phút |
| Nguyễn Hữu Hoàng | Slide 9 – 12 | Công nghệ + Văn bản dự án + Ước lượng + WBS Tổng quan | ~2 phút |
| Nguyễn Hoài Nam | Slide 13 – 16 | WBS Giai đoạn 1-2-3 + Giai đoạn 4-5 + Giai đoạn 6 + Nguồn lực | ~2 phút |
| Lê Gia Anh Quân | Slide 17 – 22 | RACI + Lịch trình + Tài chính + Tóm tắt + Kết luận | ~2 phút |

---

## 👤 NGƯỜI 1 – Phan Văn Khánh | Slide 1 → 4

---

**[Slide 1 – Trang bìa]**

Kính chào thầy và các bạn. Nhóm 01 xin phép bắt đầu phần báo cáo cuối kỳ môn Quản lý Dự án CNTT.

Đề tài nhóm mình thực hiện là **VieChat – Nền tảng Chat và Hồng Bao**, được hướng dẫn bởi thầy PGS.TS. Võ Trung Hùng. Nhóm gồm 5 thành viên: Phan Văn Khánh, Nguyễn Thành Mạnh, Nguyễn Hoài Nam, Nguyễn Hữu Hoàng và Lê Gia Anh Quân.

---

**[Slide 2 – Tổng quan dự án]**

VieChat là một ứng dụng PWA – tức là Progressive Web App – kết hợp nhắn tin và giao dịch hồng bao số. Ý tưởng xuất phát từ thực tế người dùng phải chuyển qua lại giữa app chat và app ngân hàng mỗi khi muốn lì xì hay chuyển tiền – rất bất tiện.

Về mục tiêu, dự án hướng đến **10.000 người dùng đăng ký** và **500 người online cùng lúc** trong vòng 3 tháng đầu sau khi ra mắt. Thời gian thực hiện là **12 tháng**, từ tháng 1 đến tháng 12 năm 2026. Tổng ngân sách là **5 tỷ đồng**.

Về phạm vi, dự án tập trung vào 4 tính năng chính: chat nhóm và cá nhân theo thời gian thực, hồng bao ngẫu nhiên và chia đều, tích hợp thanh toán MoMo và ZaloPay, và màn hình quản trị Admin. Các thứ như native app, gọi điện, hay marketplace sẽ không nằm trong phiên bản này.

---

**[Slide 3 – Tổ chức dự án]**

Nhóm chọn mô hình **Pure-Project Organization** – toàn bộ 5 thành viên làm việc full-time, tập trung hoàn toàn cho dự án trong suốt 12 tháng. Không có ai kiêm nhiệm dự án khác song song.

Sơ đồ tổ chức rất gọn: anh Khánh ở trên với vai trò PM & QA, bên dưới là 4 vị trí chuyên môn gồm Frontend, Backend, UI/UX và DevOps. Ngoài ra từ tháng 7 đến tháng 11 có thêm nhân sự outsource cho kiểm thử bảo mật và marketing.

---

**[Slide 4 – Các bên liên quan]**

Dự án có 6 nhóm bên liên quan chính. **Ban Giám đốc và Sponsor** phê duyệt ngân sách và định hướng chiến lược. **Người dùng cuối** là nhóm Early Adopters sẽ dùng thử sớm và cho phản hồi thực tế. **Đội dự án** là 5 thành viên full-time. **Đối tác thanh toán** MoMo và ZaloPay cung cấp API cho tính năng hồng bao. **SMS Provider** hỗ trợ gửi OTP và thông báo. Và cuối cùng là **đơn vị outsource** gồm nhóm pentest bảo mật và agency marketing.

---

## 👤 NGƯỜI 2 – Nguyễn Thành Mạnh | Slide 5 → 8

---

**[Slide 5 – Vai trò và Trách nhiệm]**

Mỗi thành viên trong nhóm đều có vai trò rõ ràng. **Anh Khánh** là Project Manager kiêm QA – chịu trách nhiệm lập kế hoạch, quản lý tiến độ và kiểm thử chất lượng trước mỗi lần ra mắt. **Mình – Mạnh** phụ trách Frontend, phát triển giao diện PWA bằng ReactJS, TypeScript và Tailwind CSS. **Anh Hoàng** phụ trách Backend – xây dựng API bằng NestJS, kết nối MongoDB và PostgreSQL theo kiến trúc microservices. **Anh Nam** làm UI/UX Designer, từ wireframe đến thiết kế chi tiết trên Figma. **Anh Quân** phụ trách DevOps – xây dựng CI/CD pipeline, quản lý hạ tầng AWS hoặc GCP và theo dõi hệ thống.

---

**[Slide 6 – Mô hình Agile Scrum]**

Nhóm chọn **Agile Scrum** làm mô hình phát triển sau khi so sánh với Waterfall và Kanban. Lý do chính là đội nhỏ 5 người, yêu cầu dễ thay đổi theo phản hồi thực tế, nên cần mô hình linh hoạt.

Có 4 điểm nổi bật: **thứ nhất**, thích nghi nhanh khi yêu cầu thay đổi. **Thứ hai**, đội nhỏ tự tổ chức theo Sprint 2 tuần. **Thứ ba**, bàn giao và kiểm thử sau mỗi Sprint thay vì chờ cuối dự án. **Thứ tư**, Retrospective sau mỗi Sprint giúp phát hiện vấn đề sớm. Một điểm quan trọng là phạm vi được **đóng băng sau tháng 2** – không thêm tính năng mới để tránh trễ tiến độ.

---

**[Slide 7 – Cấu trúc Sprint]**

Mỗi Sprint 2 tuần gồm 5 bước liên tiếp. **Sprint Planning** – chọn các User Story từ Backlog để làm trong Sprint đó. **Daily Scrum** – họp 15 phút mỗi sáng để đồng bộ tiến độ. **Sprint Review** – cuối Sprint demo tính năng cho stakeholder xem. **Sprint Retro** – rút kinh nghiệm để Sprint sau làm tốt hơn. **Backlog Refinement** – cập nhật và ước lượng lại các story chuẩn bị cho Sprint tiếp theo.

Velocity dự kiến mỗi Sprint là 30 đến 40 story points. Một task được coi là **hoàn thành** khi: qua code review, unit test đạt ≥ 80%, demo thành công, tài liệu cập nhật và được merge vào nhánh chính.

---

**[Slide 8 – Phương pháp & Công cụ]**

Nhóm sử dụng bộ công cụ khá đầy đủ theo từng mảng. **Quản lý dự án** dùng Jira với Kanban Board và Confluence để lưu tài liệu. **Mã nguồn** quản lý trên GitHub Enterprise, tự động hóa qua GitHub Actions. **Thiết kế** dùng Figma cho UI/UX và Storybook để quản lý component. **Giao tiếp** hàng ngày qua Slack, Discord và Google Meet. **Kiểm thử** dùng Postman cho API, Jest cho unit test, k6 cho load test và SonarQube để kiểm tra chất lượng code. **Giám sát** hệ thống qua Grafana, Prometheus và AWS CloudWatch.

---

## 👤 NGƯỜI 3 – Nguyễn Hữu Hoàng | Slide 9 → 12

---

**[Slide 9 – Công nghệ phát triển]**

Nhóm lựa chọn bộ công nghệ hiện đại và phù hợp với yêu cầu của sản phẩm. **Frontend** dùng ReactJS kết hợp TypeScript và Tailwind CSS, hỗ trợ PWA để chạy được trên mọi thiết bị như một ứng dụng thực sự. **Giao tiếp thời gian thực** dùng Socket.io theo giao thức WebSocket, đảm bảo tin nhắn gửi đi gần như tức thì. **Backend** dùng NestJS theo kiến trúc Microservices, tức là mỗi chức năng chạy độc lập, dễ mở rộng và bảo trì. **Database** tách làm hai: MongoDB lưu dữ liệu chat vì linh hoạt với dữ liệu phi cấu trúc, PostgreSQL lưu dữ liệu tài chính vì cần độ chính xác cao. **Hỗ trợ thêm** có Redis để cache và RabbitMQ làm message queue. **Hạ tầng** triển khai trên AWS hoặc GCP với auto-scaling, CDN và S3.

---

**[Slide 10 – Ban hành các văn bản cần thiết]**

Trong suốt dự án, nhóm ban hành tổng cộng **12 loại văn bản** theo từng mốc thời gian. Tháng 1 có Kế hoạch dự án tổng thể do PM phụ trách. Tháng 2 ra 3 tài liệu cùng lúc: đặc tả yêu cầu SRS, thiết kế kiến trúc hệ thống và sơ đồ database ERD. Tháng 3 có tài liệu API trên Swagger. Tháng 6 có kế hoạch kiểm thử. Tháng 8 là báo cáo pentest từ đội outsource. Tháng 11 hoàn thiện tài liệu hướng dẫn sử dụng. Tháng 12 là biên bản nghiệm thu và bàn giao. Ngoài ra còn 3 văn bản chạy liên tục: phiếu yêu cầu thay đổi, sổ đăng ký rủi ro cập nhật mỗi Sprint, và biên bản họp họp 2 tuần một lần.

---

**[Slide 11 – Ước lượng nỗ lực]**

Toàn dự án kéo dài **12 tháng**, với **5 người** làm full-time, tổng cộng khoảng **48 người-tháng** tương đương **2.880 người-giờ** và **24 Sprint** theo chu kỳ 2 tuần.

Nhóm dùng **3 phương pháp ước lượng** kết hợp. **Analogous Estimation** – dựa trên kinh nghiệm từ các dự án tương tự để ước lượng thời gian tổng thể. **Story Points với Planning Poker** – cả nhóm cùng ước lượng độ phức tạp từng User Story theo thang Fibonacci 1, 2, 3, 5, 8, 13. **PERT 3 điểm** – tính theo công thức E = (O + 4M + P) chia 6, kết hợp ba kịch bản: tối ưu, thực tế và bi quan để ra con số tin cậy hơn.

---

**[Slide 12 – WBS Tổng quan]**

Toàn bộ công việc của dự án được phân rã thành **6 giai đoạn lớn** trong WBS. **Giai đoạn 1** là Quản lý dự án, chạy xuyên suốt từ tháng 1 đến tháng 12. **Giai đoạn 2** là Phân tích và Thiết kế, tập trung vào tháng 1 và 2. **Giai đoạn 3** là Phát triển MVP từ tháng 3 đến tháng 6. **Giai đoạn 4** là Kiểm thử và Tối ưu trong tháng 7 và 8. **Giai đoạn 5** là Tiếp thị và Mở rộng từ tháng 9 đến tháng 11. **Giai đoạn 6** là Bàn giao và Đóng dự án vào tháng 12. Mình sẽ đi vào chi tiết từng giai đoạn ở phần tiếp theo.

---

## 👤 NGƯỜI 4 – Nguyễn Hoài Nam | Slide 13 → 16

---

**[Slide 13 – WBS Giai đoạn 1 & 2]**

**Giai đoạn 1 – Quản lý dự án** chạy xuyên suốt 12 tháng gồm 6 nhóm công việc: lập kế hoạch và khởi động, quản lý Sprint Agile, giám sát tiến độ, kiểm soát thay đổi, quản lý rủi ro và báo cáo định kỳ. Đây là nền tảng đảm bảo dự án đi đúng hướng.

**Giai đoạn 2 – Phân tích và Thiết kế** chỉ kéo dài tháng 1 và 2 nhưng rất quan trọng vì đây là nền tảng cho toàn bộ phần phát triển sau. Nhóm thực hiện 6 việc: thu thập và phân tích yêu cầu, thiết kế wireframe sơ bộ, thiết kế giao diện chi tiết trên Figma, thiết kế kiến trúc hệ thống, thiết kế database theo ERD, và đặc tả API. Cuối giai đoạn này phạm vi được đóng băng hoàn toàn.

---

**[Slide 14 – WBS Giai đoạn 3: Phát triển MVP]**

**Giai đoạn 3** từ tháng 3 đến tháng 6 là giai đoạn phát triển chính, chia thành 6 nhóm công việc lớn. **Đầu tiên** là thiết lập hạ tầng DevOps và CI/CD pipeline – đây là nền tảng để toàn đội làm việc song song. **Tiếp theo** là phát triển Backend API cho các chức năng cốt lõi: quản lý người dùng, xác thực, chat và hồng bao. **Song song đó** là phát triển Frontend PWA – bao gồm cấu hình manifest, service worker và giao diện chat. **Bước thứ tư** là tích hợp WebSocket và RabbitMQ để đảm bảo tin nhắn được truyền đi theo thời gian thực và không bị mất. **Thứ năm** là tích hợp API thanh toán của MoMo và ZaloPay vào luồng hồng bao. **Cuối cùng** là xây dựng Admin Dashboard để quản trị và theo dõi hệ thống.

---

**[Slide 15 – WBS Giai đoạn 4 & 5]**

**Giai đoạn 4 – Kiểm thử và Tối ưu** trong tháng 7 và 8 gồm 6 hạng mục. Kiểm thử chức năng để đảm bảo các tính năng hoạt động đúng. Kiểm thử giao diện trên nhiều thiết bị và trình duyệt. Kiểm thử bảo mật do đội outsource thực hiện – đây là bước quan trọng vì sản phẩm liên quan đến tiền. Kiểm thử hiệu năng dùng k6 để mô phỏng tải lớn. Sau đó sửa lỗi và tối ưu hóa. Cuối cùng là kiểm thử hồi quy và UAT để người dùng thực xác nhận.

**Giai đoạn 5 – Tiếp thị và Mở rộng** từ tháng 9 đến tháng 11 gồm: triển khai lên môi trường production, mở Open Beta cho người dùng sớm, chạy chiến dịch marketing qua agency outsource, thu thập phản hồi và tối ưu tiếp, rồi báo cáo KPI về số user, CCU và số giao dịch.

---

**[Slide 16 – WBS Giai đoạn 6 & Kế hoạch nguồn lực]**

**Giai đoạn 6 – Bàn giao và Đóng dự án** vào tháng 12 gồm 5 bước. Tổ chức sự kiện ra mắt chính thức. Bàn giao toàn bộ source code và tài liệu kỹ thuật. Đánh giá sau triển khai để đo lường kết quả thực tế so với kế hoạch. Rút kinh nghiệm cho dự án tiếp theo. Và cuối cùng là nghiệm thu chính thức, giải phóng nguồn lực.

Về **kế hoạch nguồn lực** theo tháng: từ tháng 1 đến 6 là 5 người core team. Tháng 7 đến 8 tăng lên 7 người, thêm 2 người pentest. Tháng 9 đến 11 là 8 người, thêm 3 người marketing. Tháng 12 trở lại 5 người core team để bàn giao.

---

## 👤 NGƯỜI 5 – Lê Gia Anh Quân | Slide 17 → 22 (Slide 17 đã nói ở trên, bắt đầu từ slide 18)

---

**[Slide 17 – Ma trận RACI]**

Để phân công rõ ràng hơn, nhóm sử dụng **ma trận RACI** với 4 mức: R là người thực hiện trực tiếp, A là người chịu trách nhiệm cuối, C là người được tham vấn, và I là người được thông báo.

Nhìn vào ma trận, **anh Khánh** đứng ở vị trí A hầu hết mọi công việc – tức là chịu trách nhiệm cuối. Với kiểm thử chức năng và bàn giao, anh Khánh đảm nhận cả R lẫn A. **Mình – Mạnh** là R cho phần Frontend. **Anh Hoàng** là R cho Backend và API. **Anh Nam** là R cho thiết kế UI/UX. **Anh Quân** là R cho hạ tầng DevOps. **Đội outsource** thực hiện pentest và marketing.

---

**[Slide 18 – Kế hoạch lịch trình]**

Nhìn vào timeline 12 tháng, dự án chia làm 5 pha rõ ràng. **Tháng 1–2** tập trung phân tích yêu cầu, thiết kế UI/UX, thiết kế hệ thống và API, kết thúc bằng việc đóng băng phạm vi. **Tháng 3–6** là phát triển MVP – hạ tầng, backend, frontend, tích hợp thanh toán và admin dashboard. **Tháng 7–8** là kiểm thử toàn diện – pentest, load test và sửa lỗi. **Tháng 9–11** triển khai production, chạy Open Beta và chiến dịch marketing. **Tháng 12** ra mắt chính thức, bàn giao và đóng dự án.

---

**[Slide 19 – Kế hoạch tài chính]**

Tổng ngân sách là **5 tỷ đồng**, tương đương khoảng **200.000 USD**, được phân bổ theo 4 nhóm. **60% – tức 3 tỷ** dành cho nhân sự, bao gồm lương 5 thành viên full-time trong 12 tháng. **25% – tức 1,25 tỷ** cho hạ tầng và công nghệ: cloud, CDN, license công cụ. **10% – tức 500 triệu** cho marketing và outsource pentest. **5% – tức 250 triệu** là quỹ dự phòng để xử lý rủi ro phát sinh. Toàn bộ ngân sách được kiểm soát theo phương pháp **Earned Value Management** để theo dõi hiệu quả chi tiêu theo tiến độ thực tế.

---

**[Slide 20 – Tóm tắt kế hoạch dự án]**

Tóm lại, kế hoạch dự án VieChat được xây dựng dựa trên 6 yếu tố cốt lõi. **Mô hình Agile Scrum** với Sprint 2 tuần, liên tục cải tiến. **Thời gian 12 tháng** từ tháng 1 đến tháng 12 năm 2026. **Nhân sự 5 FTE** và hỗ trợ từ outsource. **WBS 6 giai đoạn** phân rã chi tiết từ phân tích đến bàn giao. **Ngân sách 5 tỷ** phân bổ theo tỉ lệ 60/25/10/5. **Công nghệ PWA và Microservices** đảm bảo hệ thống thời gian thực, có thể mở rộng và bảo mật tốt.

---

**[Slide 21 – Kết luận]**

Để kết lại phần báo cáo, nhóm xin nhấn mạnh 5 điểm. Kế hoạch dự án VieChat được xây dựng **chi tiết và khả thi**. Agile Scrum giúp **quản trị chặt chẽ và linh hoạt** khi có thay đổi. WBS 6 giai đoạn **phân rã đầy đủ** từ phân tích đến bàn giao. Ngân sách **được phân bổ hợp lý** và có quỹ dự phòng. Và nhóm **cam kết bàn giao sản phẩm chất lượng** vào tháng 12/2026.

---

**[Slide 22 – Trang kết]**

Đó là toàn bộ nội dung báo cáo của Nhóm 01. Nhóm mình xin chân thành cảm ơn thầy và các bạn đã lắng nghe. Nhóm rất sẵn sàng nhận câu hỏi ạ.
