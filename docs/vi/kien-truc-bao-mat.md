# Kiến trúc & Bảo mật

## Tổng quan kiến trúc
- Spring Boot 3.4.5 làm nền tảng backend
- MongoDB: database chính
- MySQL: bình luận và dữ liệu bổ sung
- Giao diện Thymeleaf (SSR) + static assets
- Realtime: WebSocket + STOMP (`/ws`) với broker nội bộ (`/topic`, `/queue`, `/user`)

## Bảo mật (Spring Security + JWT)
- Config: `SecurityConfig`
- Bộ lọc JWT: `JwtFilter`
- Mã hóa mật khẩu: `BCryptPasswordEncoder`

### Vai trò (roles)
- `ADMIN`: truy cập `/admin/**`, `/api/admin/**`, quản trị người dùng, hệ thống
- `TEACHER`: truy cập `/teacher/**`, quản lý khóa học/lesson/quiz
- `STUDENT`: truy cập `/student/**`, làm bài học/quiz/exam

### Chính sách truy cập (chính)
- Công khai: `/api/auth/**`, `/`, `/login`, `/register`, `/forgot-password`, `/error`, `/courses`, `/welcome`, static (`/css/**`, `/js/**`, `/images/**`, `/uploads/**`, `/favicon.ico`), WebSocket endpoint `/ws`
- Admin: `/admin/**`, `/api/admin/**`
- Teacher: `/teacher/**`, `/api/courses/**`, `/api/courses/*/exams`, `/api/exams/*/questions`, `/api/exams/*/submissions`
- Xác thực: `/api/quiz/**`, `/api/embedded-quiz/**`, `/api/chat/**`, `/dashboard`
- Student: `/student/**`, `/api/exams/*/submit`, `/api/exams/*/submission`

### JWT
- Lấy token từ `Authorization: Bearer <token>` hoặc cookie `auth_token`
- Trích `email`, `role` từ token và ánh xạ GrantedAuthority `ROLE_<role>`
- Nếu token không hợp lệ với request UI (không phải `/api/**`), sẽ redirect tới `/session-expired`

## CORS
- Cấu hình trong `WebConfig` cho:
  - `/api/**`, `/admin/api/**`, `/teacher/api/**`, `/student/api/**`
  - Cho phép origins: `*`, methods: GET/POST/PUT/DELETE/OPTIONS/HEAD/PATCH

## WebSocket (STOMP)
- Endpoint: `/ws` (cho phép mọi origin, có SockJS)
- Application destination prefix: `/app`
- Broker: `/topic`, `/queue`; user prefix: `/user`
- Ví dụ mapping:
  - Client gửi tới: `/app/chat.sendMessage`, `/app/chat.addUser`, `/app/chat.typing`, `/app/chat.stopTyping`
  - Server phát: `/topic/...`, gửi riêng tới user: `/user/{id}/queue/...`

## Lưu ý triển khai
- `SecurityConfig` có cấu hình `requiresSecure()` khi header `X-Forwarded-Proto` tồn tại. Khi đặt sau reverse proxy (Nginx/Ingress), đảm bảo chuyển tiếp header này để cưỡng bức HTTPS đúng cách.