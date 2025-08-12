# Tham chiếu API (tóm tắt)

Lưu ý: Nhiều API yêu cầu JWT. Gửi `Authorization: Bearer <token>` hoặc cookie `auth_token`.

## Auth (`/api/auth`)
- `POST /register`: đăng ký
- `POST /login`: đăng nhập (trả JWT)
- `POST /forgot-password`: quên mật khẩu

## Người dùng & Hồ sơ
- `GET /api/admin/users` (ADMIN): danh sách user
- `DELETE /api/admin/users/{id}` (ADMIN): xóa user
- `PUT /api/admin/users/{id}/role` (ADMIN): đổi vai trò
- `PUT /api/admin/users/{id}/status` (ADMIN): khóa/mở user
- `PUT /api/admin/profile` (AUTH): cập nhật hồ sơ
- `PUT /api/admin/profile/avatar` (AUTH): cập nhật avatar
- `GET /api/auth/me` (AUTH): thông tin người dùng hiện tại

## Khóa học (`/api/courses`)
- `GET /api/courses`: danh sách khóa học
- `GET /api/courses/{id}`: chi tiết
- `POST /api/courses` (ADMIN/TEACHER): tạo
- `PUT /api/courses/{id}` (ADMIN/TEACHER): cập nhật
- `DELETE /api/courses/{id}` (ADMIN/TEACHER): xóa

## Bài học (Lesson)
- `POST /api/courses/{courseId}/lessons` (TEACHER/ADMIN): tạo
- `GET /api/courses/{courseId}/lessons`: list theo khóa học
- `GET /api/lessons/{id}`: chi tiết
- `PUT /api/lessons/{id}` (TEACHER/ADMIN): cập nhật
- `DELETE /api/lessons/{id}` (TEACHER/ADMIN): xóa
- `POST /api/lessons/{lessonId}/upload-body`: upload nội dung

## Quiz thường (`/api/quiz`)
- `POST /lessons/{lessonId}`: tạo quiz
- `POST /lessons/{lessonId}/position/{position}`: chèn vị trí
- `GET /lessons/{lessonId}`: list quiz theo lesson
- `GET /{quizId}`: chi tiết
- `PUT /{quizId}`: cập nhật
- `DELETE /{quizId}`: xóa
- `POST /{quizId}/check`: kiểm tra câu trả lời
- `PUT /{quizId}/position/{newPosition}`: đổi vị trí
- `GET /lessons/{lessonId}/next-position`: vị trí kế tiếp
- `POST /{quizId}/start`: bắt đầu hoặc lấy attempt hiện tại
- `POST /attempts/{attemptId}/save`: lưu tạm (autosave)
- `POST /attempts/{attemptId}/submit`: nộp bài
- `GET /{quizId}/result`: kết quả
- `POST /{quizId}/reset`: làm lại
- `GET /{quizId}/stats` (TEACHER): thống kê
- `GET /lessons/{lessonId}/attempts`: attempts của user cho lesson

## Quiz nhúng (`/api/embedded-quiz`)
- `POST /create-quick`: tạo nhanh theo template
- `POST /create-custom`: tạo tùy chỉnh
- `GET /{quizId}`: lấy quiz + trạng thái attempt
- `POST /{quizId}/start`: bắt đầu
- `POST /attempts/{attemptId}/save`: lưu tạm
- `POST /attempts/{attemptId}/submit`: nộp
- `GET /{quizId}/result`: kết quả chi tiết
- `POST /{quizId}/retry`: làm lại
- `GET /lessons/{lessonId}/embedded`: danh sách embedded quiz trong lesson

## Bình luận (`/api/comments`)
- CRUD bình luận theo khóa học/bài học; có hỗ trợ realtime qua WebSocket

## Chat (`/api/chat` + WebSocket)
- REST: `GET /contacts`, `GET /unread-count`, `GET /unread-messages`, `GET /conversation`, `POST /markAsRead`, `DELETE /conversation`
- WS (STOMP): xem [Realtime/WebSocket](./realtime-websocket.md)

## Thông báo (`/api/notifications`)
- `GET /` (list), `PUT /{id}/read` (đánh dấu đã đọc), `POST /` (tạo - ADMIN)

## Phân tích (`/api/analytics`)
- `GET /user/{userId}`: thống kê user
- `GET /teacher/{teacherId}/courses`: thống kê theo khóa học của giảng viên
- `GET /admin/overview` (ADMIN): tổng quan hệ thống

## Kỳ thi (Exam)
- `POST /api/courses/{courseId}/exams` (TEACHER/ADMIN): tạo
- `POST /api/exams/{examId}/questions` (TEACHER/ADMIN): thêm câu hỏi
- `POST /api/exams/{examId}/submissions` (STUDENT): nộp bài

## Upload (`/api/upload`)
- `POST /course-image` (ADMIN/TEACHER): upload ảnh khóa học
- `POST /notification-attachment` (ADMIN): upload tệp đính kèm thông báo

Ghi chú: Một số endpoint MVC trả về trang Thymeleaf (ví dụ: `/admin/**`, `/teacher/**`, `/student/**`).