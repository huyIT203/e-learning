# Hướng dẫn sử dụng Postman Collection - Embedded Quiz API

## 🚀 Cách sử dụng

### Bước 1: Import Collection
1. Mở Postman
2. Click **Import** → **File** → Chọn file `Embedded_Quiz_API.postman_collection.json`
3. Collection sẽ xuất hiện với tên "**Embedded Quiz API - Updated**"

### Bước 2: Thiết lập Environment Variables
Collection đã có sẵn các biến, bạn chỉ cần kiểm tra:
- `base_url`: `http://localhost:8081` ✅
- `lesson_id`: `64a1b2c3d4e5f6789012345` ✅
- `auth_token`: Sẽ được tự động cập nhật khi login ✅

### Bước 3: Test theo thứ tự

## 🔐 **1. Authentication**
### Đăng ký user test (nếu chưa có)
```
POST /api/auth/register
```
**Body:**
```json
{
  "email": "test@example.com",
  "password": "password123",
  "name": "Test User"
}
```

### Login và lấy JWT token
```
POST /api/auth/login
```
**Body:**
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
**✅ Tự động lưu token vào biến `auth_token`**

---

## 🎯 **2. Quiz Testing Flow**

### Bước 1: Tạo Quiz
```
POST /api/embedded-quiz/create-quick
```
**✅ Tự động lưu `quiz_id` và `question_id`**

### Bước 2: Xem Quiz (Confirm Question ID)
```
GET /api/embedded-quiz/{{quiz_id}}
```
**✅ Confirm và update `question_id`**

### Bước 3: Bắt đầu Quiz
```
POST /api/embedded-quiz/{{quiz_id}}/start?lessonId={{lesson_id}}
```
**✅ Tự động lưu `attempt_id`**

### Bước 4: Lưu câu trả lời (Auto-save) ⭐
```
POST /api/embedded-quiz/attempts/{{attempt_id}}/save
```
**Body:**
```json
{
  "questionId": "{{question_id}}",
  "answer": "1"
}
```
**✅ Đây là step quan trọng nhất - test Save Answer**

### Bước 5: Submit Quiz
```
POST /api/embedded-quiz/attempts/{{attempt_id}}/submit
```

### Bước 6: Xem kết quả
```
GET /api/embedded-quiz/{{quiz_id}}/result
```

---

## 🔧 **3. Test các loại câu trả lời khác**

### True/False Answer:
```json
{
  "questionId": "{{question_id}}",
  "answer": "0"  // 0 = True, 1 = False
}
```

### Fill Blank Answer:
```json
{
  "questionId": "{{question_id}}",
  "answer": "hướng đối tượng"  // Text answer
}
```

---

## 🧪 **4. Tạo các loại Quiz khác**

### True/False Quiz:
- Chạy "Create True/False Quiz"
- Sẽ tự động lưu `tf_quiz_id` và `tf_question_id`

### Fill Blank Quiz:
- Chạy "Create Fill Blank Quiz"
- Sẽ tự động lưu `fb_quiz_id` và `fb_question_id`

---

## 🔍 **5. Debug & Troubleshooting**

### Kiểm tra server:
```
GET /api/auth/check
```

### Xem tất cả variables:
- Chạy "Get All Variables"
- Xem Console để thấy tất cả biến hiện tại

---

## 📊 **Automatic Variable Updates**

Collection này có **JavaScript scripts** tự động update variables:

### Sau khi Login:
```javascript
pm.environment.set("auth_token", jsonData.token);
```

### Sau khi tạo Quiz:
```javascript
pm.environment.set("quiz_id", jsonData.id);
pm.environment.set("question_id", jsonData.questions[0].id);
```

### Sau khi start Quiz:
```javascript
pm.environment.set("attempt_id", jsonData.attempt.id);
```

---

## ✅ **Test Flow hoàn chỉnh**

1. **🔐 Authentication** → **Login** (lấy token)
2. **🎯 Quiz Testing Flow** → **1. Create Quick Quiz** (lấy quiz_id, question_id)
3. **🎯 Quiz Testing Flow** → **2. Get Quiz** (confirm question_id)
4. **🎯 Quiz Testing Flow** → **3. Start Quiz** (lấy attempt_id)
5. **🎯 Quiz Testing Flow** → **4. Save Answer** ⭐ (test chính)
6. **🎯 Quiz Testing Flow** → **5. Submit Quiz**
7. **🎯 Quiz Testing Flow** → **6. Get Quiz Result**

---

## 🚨 **Lỗi thường gặp**

### 404 Not Found khi Save Answer:
- ❌ Sai `attempt_id` hoặc `question_id`
- ✅ Chạy "Get All Variables" để kiểm tra
- ✅ Đảm bảo đã start quiz trước khi save answer

### 401 Unauthorized:
- ❌ Chưa login hoặc token hết hạn
- ✅ Chạy lại "Login" để refresh token

### 400 Bad Request:
- ❌ Sai format JSON
- ✅ Kiểm tra `questionId` và `answer` format

---

## 🎉 **Kết quả mong đợi**

Khi **Save Answer** thành công:
```json
{
  "success": true,
  "message": "Answer saved automatically",
  "timestamp": "2024-01-01T10:00:00"
}
```

Console sẽ hiển thị:
```
✅ Answer saved successfully!
Message: Answer saved automatically
Timestamp: 2024-01-01T10:00:00
```

---

## 📝 **Lưu ý quan trọng**

1. **Chạy theo thứ tự** - các request phụ thuộc vào nhau
2. **Kiểm tra Console** - tất cả thông tin debug được log ra
3. **Variables tự động update** - không cần copy/paste IDs
4. **Server phải chạy trên port 8081** - đã được cấu hình sẵn
5. **JWT token tự động lưu** - không cần manual setup

---

## 🔄 **Retry nếu lỗi**

Nếu có lỗi ở bất kỳ step nào:
1. Kiểm tra server có đang chạy không
2. Chạy "Get All Variables" để xem variables hiện tại
3. Chạy lại từ step "Login" nếu cần
4. Check Console để xem error details 