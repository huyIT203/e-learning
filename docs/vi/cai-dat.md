# Cài đặt và chạy dự án

## Yêu cầu
- Java 17+
- Maven 3.9+
- MongoDB 4.4+
- MySQL 8.0+

## Bắt đầu nhanh
```bash
# Clone mã nguồn
git clone <repository-url>
cd elearning-backend

# Biên dịch
mvn clean install

# Chạy ứng dụng (cách 1)
mvn spring-boot:run

# Hoặc chạy JAR (cách 2)
java -jar target/elearning-backend-0.0.1-SNAPSHOT.jar
```

Ứng dụng mặc định chạy tại: `http://localhost:8081` (cấu hình trong `server.port`).

## Cấu hình ban đầu
1) Sao chép file mẫu cấu hình:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
2) Cập nhật các giá trị sau trong `application.properties`:
- `spring.data.mongodb.uri` (MongoDB)
- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` (MySQL)
- `jwt.secret`, `jwt.expiration`
- Cấu hình SMTP email (`spring.mail.*`)
- Cấu hình upload file (`spring.servlet.multipart.*`)

## Dữ liệu phát triển (tùy chọn)
- Tạo DB MongoDB: `elearning`
- Tạo DB MySQL: `elearning`
- Tạo user/role mặc định: thông qua API đăng ký/đăng nhập hoặc seed thủ công nếu cần.

## Lỗi thường gặp
- Không kết nối được DB: kiểm tra URI/username/password và DB đã chạy.
- 401 Unauthorized: chưa gửi JWT (header `Authorization: Bearer <token>` hoặc cookie `auth_token`).
- Tài nguyên tĩnh không tải được: kiểm tra cấu hình `WebConfig` và đường dẫn `/css`, `/js`, `/images`, `/uploads`.