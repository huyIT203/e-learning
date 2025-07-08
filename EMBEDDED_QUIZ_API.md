# Embedded Quiz System API Documentation

## Tổng quan
Hệ thống Quiz Nhúng cho phép tạo và quản lý các câu hỏi tương tác trực tiếp trong nội dung bài học. Hệ thống hỗ trợ tự động lưu câu trả lời, chấm điểm và hiển thị kết quả ngay lập tức.

## Models

### 1. UserQuizAttempt
- **Collection**: `user_quiz_attempts`
- **Mục đích**: Lưu trữ kết quả làm quiz của học viên
- **Fields chính**:
  - `userId`: ID của học viên
  - `quizId`: ID của quiz
  - `lessonId`: ID của bài học
  - `userAnswers`: Map lưu câu trả lời của user
  - `results`: Kết quả chi tiết từng câu
  - `score`, `percentage`, `correctAnswers`: Điểm số và thống kê
  - `completed`: Trạng thái hoàn thành
  - `startedAt`, `completedAt`: Thời gian bắt đầu và hoàn thành

### 2. Quiz (Cập nhật)
- **Fields mới**:
  - `embedded`: Quiz có được nhúng trong nội dung không
  - `showResult`: Hiển thị kết quả ngay sau khi làm
  - `allowRetry`: Cho phép làm lại
- **QuizType mới**:
  - `MULTIPLE_CHOICE`: Trắc nghiệm nhúng
  - `TRUE_FALSE`: Đúng/Sai nhúng  
  - `FILL_BLANK`: Điền vào chỗ trống nhúng

## API Endpoints

### A. EmbeddedQuizController (`/api/embedded-quiz`)

#### 1. Tạo Quiz Nhúng Nhanh
```http
POST /api/embedded-quiz/create-quick
```

**Request Body:**
```json
{
  "lessonId": "lesson123",
  "title": "Quiz về Java Basics",
  "description": "Quiz kiểm tra kiến thức cơ bản",
  "type": "MULTIPLE_CHOICE" // hoặc "TRUE_FALSE", "FILL_BLANK"
}
```

**Response:**
```json
{
  "id": "quiz123",
  "lessonId": "lesson123",
  "title": "Quiz về Java Basics",
  "type": "MULTIPLE_CHOICE",
  "embedded": true,
  "questions": [
    {
      "id": "q1",
      "questionText": "Câu hỏi mẫu - Hãy chọn đáp án đúng:",
      "type": "MULTIPLE_CHOICE",
      "options": [
        {"id": "0", "text": "Đáp án A", "isCorrect": false},
        {"id": "1", "text": "Đáp án B (Đúng)", "isCorrect": true}
      ],
      "points": 10
    }
  ]
}
```

#### 2. Tạo Quiz Nhúng Tùy Chỉnh
```http
POST /api/embedded-quiz/create-custom
```

**Request Body:**
```json
{
  "lessonId": "lesson123",
  "title": "Quiz tùy chỉnh",
  "description": "Quiz với câu hỏi tự tạo",
  "type": "MULTIPLE_CHOICE",
  "questions": [
    {
      "questionText": "Java là gì?",
      "type": "MULTIPLE_CHOICE",
      "options": [
        {"text": "Ngôn ngữ lập trình", "isCorrect": true},
        {"text": "Cơ sở dữ liệu", "isCorrect": false}
      ],
      "correctAnswer": "0",
      "explanation": "Java là ngôn ngữ lập trình hướng đối tượng",
      "points": 10
    }
  ]
}
```

#### 3. Lấy Quiz Nhúng
```http
GET /api/embedded-quiz/{quizId}
```

**Response:**
```json
{
  "quiz": { /* Quiz object */ },
  "userAttempt": { /* UserQuizAttempt nếu user đã làm */ },
  "hasCompleted": false
}
```

#### 4. Bắt Đầu Làm Quiz
```http
POST /api/embedded-quiz/{quizId}/start?lessonId=lesson123
```

**Response:**
```json
{
  "attempt": {
    "id": "attempt123",
    "userId": "user123",
    "quizId": "quiz123",
    "lessonId": "lesson123",
    "completed": false,
    "startedAt": "2024-01-01T10:00:00"
  },
  "message": "Quiz started successfully"
}
```

#### 5. Lưu Câu Trả Lời (Auto-save)
```http
POST /api/embedded-quiz/attempts/{attemptId}/save
```

**Request Body:**
```json
{
  "questionId": "q1",
  "answer": "1" // hoặc "true", "false", "text answer"
}
```

#### 6. Nộp Bài Quiz
```http
POST /api/embedded-quiz/attempts/{attemptId}/submit
```

**Request Body:**
```json
{
  "q1": "1",
  "q2": "true",
  "q3": "hướng đối tượng"
}
```

**Response:**
```json
{
  "attempt": { /* UserQuizAttempt object */ },
  "score": 85,
  "percentage": 85.0,
  "passed": true,
  "totalQuestions": 3,
  "correctAnswers": 2,
  "message": "Quiz submitted successfully"
}
```

#### 7. Lấy Kết Quả Quiz
```http
GET /api/embedded-quiz/{quizId}/result
```

#### 8. Làm Lại Quiz
```http
POST /api/embedded-quiz/{quizId}/retry?lessonId=lesson123
```

#### 9. Lấy Danh Sách Quiz Nhúng Trong Bài Học
```http
GET /api/embedded-quiz/lessons/{lessonId}/embedded
```

### B. QuizController - Endpoints Mở Rộng (`/api/quiz`)

#### 1. Bắt Đầu Quiz
```http
POST /api/quiz/{quizId}/start?lessonId=lesson123
```

#### 2. Lưu Câu Trả Lời
```http
POST /api/quiz/attempts/{attemptId}/save
```

#### 3. Nộp Bài Quiz
```http
POST /api/quiz/attempts/{attemptId}/submit
```

#### 4. Lấy Kết Quả
```http
GET /api/quiz/{quizId}/result
```

#### 5. Reset Quiz
```http
POST /api/quiz/{quizId}/reset?lessonId=lesson123
```

#### 6. Thống Kê Quiz (Cho Giảng Viên)
```http
GET /api/quiz/{quizId}/stats
```

**Response:**
```json
{
  "totalAttempts": 25,
  "completedAttempts": 20,
  "averageScore": 78.5,
  "maxScore": 95,
  "minScore": 45,
  "passRate": 80.0
}
```

#### 7. Lấy Attempts Của User Trong Bài Học
```http
GET /api/quiz/lessons/{lessonId}/attempts
```

## Services

### 1. UserQuizAttemptService
- `startOrGetAttempt()`: Bắt đầu hoặc lấy attempt hiện tại
- `saveAnswer()`: Lưu câu trả lời tạm thời
- `submitQuiz()`: Nộp bài và tính điểm
- `hasUserCompletedQuiz()`: Kiểm tra user đã hoàn thành chưa
- `getQuizStats()`: Lấy thống kê quiz

### 2. EmbeddedQuizService
- `createEmbeddedQuizWithSample()`: Tạo quiz với câu hỏi mẫu
- `createCustomEmbeddedQuiz()`: Tạo quiz với câu hỏi tùy chỉnh

## Tính Năng Chính

### 1. Auto-save
- Tự động lưu câu trả lời khi user nhập
- Không mất dữ liệu khi refresh trang

### 2. Instant Feedback
- Hiển thị kết quả ngay sau khi nộp bài
- Hiển thị đáp án đúng và giải thích

### 3. Retry Support
- Cho phép làm lại quiz nhiều lần
- Lưu lịch sử các lần làm

### 4. Flexible Integration
- Có thể nhúng vào bất kỳ vị trí nào trong nội dung
- Hỗ trợ nhiều loại câu hỏi

### 5. Progress Tracking
- Theo dõi tiến độ học viên
- Thống kê chi tiết cho giảng viên

## Authentication
Tất cả endpoints yêu cầu user đăng nhập (trừ GET quiz public). Sử dụng JWT token trong header:
```
Authorization: Bearer <jwt_token>
```

## Error Handling
Tất cả endpoints trả về error format chuẩn:
```json
{
  "error": "Error message",
  "status": 400
}
```

## Sử Dụng Frontend
1. Tạo quiz bằng `/create-quick` hoặc `/create-custom`
2. Nhúng quiz vào nội dung bài học
3. User bắt đầu làm bằng `/start`
4. Auto-save câu trả lời bằng `/save`
5. Nộp bài bằng `/submit`
6. Hiển thị kết quả từ response 