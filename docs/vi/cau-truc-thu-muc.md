# Cấu trúc thư mục

```text
src/main/java/com/elearning/elearning_backend/
├── Config/                 # Cấu hình Web, WebSocket
├── Controller/             # REST + MVC Controllers (Admin/Teacher/Student/UI)
├── DTO/                    # Data Transfer Objects (payloads)
├── Enum/                   # Enum dùng trong domain
├── Model/                  # Domain models (MongoDB/JPA)
├── Repository/             # Layer truy xuất dữ liệu (Mongo + JPA)
├── Security/               # Cấu hình bảo mật (JWT, Security)
└── ElearningBackendApplication.java

src/main/resources/
├── static/                 # Tài nguyên tĩnh (CSS/JS/Images/Uploads)
├── templates/              # Giao diện Thymeleaf (admin/teacher/student)
└── application.properties(.example)

uploads/                     # Thư mục upload ngoài (được map bởi WebConfig)
```

Các controller chính:
- `AuthController` (`/api/auth/**`)
- `CourseController` (`/api/courses/**`)
- `LessonController` (`/api/**` cho lesson)
- `QuizController` (`/api/quiz/**`), `EmbeddedQuizController` (`/api/embedded-quiz/**`)
- `CommentController` (`/api/comments/**`)
- `ChatController` (REST: `/api/chat/**`, WS: `/app/*` → `/topic`/`/queue`/`/user`)
- `AnalyticsController` (`/api/analytics/**`)
- `UserController` (`/api/admin/**`), `UserProfileController` (`/api/profile/**`)
- `AdminViewController` (`/admin/**`), `TeacherViewController` (`/teacher/**`), `StudentViewController` (`/student/**`)