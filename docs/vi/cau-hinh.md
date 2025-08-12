# Cấu hình ứng dụng

Chỉnh trong `src/main/resources/application.properties`.

## Cơ sở dữ liệu
```properties
spring.data.mongodb.uri=mongodb://127.0.0.1:27017/elearning

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/elearning
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.maximum-pool-size=10
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.open-in-view=false
```
- MongoDB lưu trữ dữ liệu chính (khóa học, lesson, quiz...).
- MySQL dùng cho bình luận và một số dữ liệu bổ sung.

## Server & Logging
```properties
server.port=8081
logging.level.root=INFO
logging.level.org.springframework=INFO
logging.level.com.elearning=DEBUG
```

## JWT
```properties
jwt.secret=<bắt buộc>
jwt.expiration=3600000 # 1 giờ (ms)
```
- JWT có thể được gửi qua header `Authorization: Bearer <token>` hoặc cookie `auth_token`.

## CORS
```properties
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=*
```
Cấu hình CORS chi tiết được bổ sung trong `WebConfig` (áp dụng cho `/api/**`, `/admin/api/**`, `/teacher/api/**`, `/student/api/**`).

## Email (SMTP)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<email>
spring.mail.password=<app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.default-encoding=UTF-8
```

## Thymeleaf & Static
```properties
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false
spring.thymeleaf.servlet.content-type=text/html

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
spring.servlet.multipart.enabled=true

spring.web.resources.static-locations=classpath:/static/
spring.web.resources.add-mappings=true
```