# Tài liệu dự án Esoclusty E-Learning Platform

Dự án e-learning hiện đại dựa trên Spring Boot 3, MongoDB và MySQL, hỗ trợ JWT, WebSocket (STOMP) và giao diện Thymeleaf.

## Mục lục nhanh
- [Cài đặt và chạy dự án](./cai-dat.md)
- [Cấu hình ứng dụng](./cau-hinh.md)
- [Cấu trúc thư mục](./cau-truc-thu-muc.md)
- [Kiến trúc & Bảo mật](./kien-truc-bao-mat.md)
- [Tham chiếu API](./api-tham-chieu.md)
- [Realtime/WebSocket](./realtime-websocket.md)
- [Triển khai](./trien-khai.md)

## Thông tin nhanh
- Backend: Spring Boot 3.4.5, Spring Security (JWT)
- DB: MongoDB (chính), MySQL (bình luận và dữ liệu bổ sung)
- Giao tiếp realtime: WebSocket + STOMP (`/ws`, topic `/topic`, `/queue`, user `/user`)
- Giao diện: Thymeleaf + static assets (CSS/JS/Images)

Nếu bạn mới bắt đầu, xem ngay: [Cài đặt và chạy dự án](./cai-dat.md).