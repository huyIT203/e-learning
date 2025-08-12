# Triển khai

## Đóng gói và chạy
```bash
mvn clean package -DskipTests
java -jar target/elearning-backend-0.0.1-SNAPSHOT.jar
```

## Reverse proxy (Nginx) gợi ý
```nginx
server {
  listen 80;
  server_name your.domain.com;

  location / {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }

  location /ws {
    proxy_pass http://127.0.0.1:8081;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}
```

Lưu ý: `SecurityConfig` yêu cầu `requiresSecure()` khi có header `X-Forwarded-Proto`. Đảm bảo chuyển tiếp header này khi dùng HTTPS sau reverse proxy để tránh lỗi redirect/CSRF.

## Biến môi trường
- Tùy chọn: quản lý `jwt.secret`, thông tin DB và SMTP qua biến môi trường hoặc externalized config.