# Esoclusty E-Learning Platform

A modern and feature-rich e-learning platform built with Spring Boot and MongoDB, designed to provide an interactive learning experience for students, teachers, and administrators.

## Features

- **User Management**
  - Role-based authentication (Admin, Teacher, Student)
  - JWT-based secure authentication
  - Password recovery system
  - User profile management

- **Course Management**
  - Course creation and management
  - Rich content support with markdown
  - Video lesson integration
  - Course categorization by job roles and skills
  - Course progress tracking
  - Interactive quizzes and assessments

- **Interactive Learning**
  - Real-time chat system
  - Course comments and ratings
  - Progress tracking
  - Interactive quizzes
  - Course completion certificates

- **Administrative Features**
  - User management
  - Course oversight
  - Analytics and reporting
  - Category management
  - System monitoring

- **Teacher Features**
  - Course creation and management
  - Student progress monitoring
  - Quiz creation and management
  - Content management
  - Student interaction

## Architecture and Technologies

### Backend
- **Framework**: Spring Boot 3.4.5
- **Security**: Spring Security with JWT
- **Database**: 
  - MongoDB (Primary database)
  - MySQL (Comments and additional data)
- **Real-time Communication**: WebSocket with STOMP
- **Email Service**: Spring Mail
- **Template Engine**: Thymeleaf
- **Documentation**: Spring REST Docs

### Frontend
- **Template Engine**: Thymeleaf
- **Styling**: Custom CSS with responsive design
- **JavaScript**: Vanilla JS with WebSocket integration
- **Rich Text Editor**: Quill.js
- **Icons**: Font Awesome, Boxicons

## Installation

1. **Prerequisites**
   - Java 17 or higher
   - Maven 3.9+
   - MongoDB 4.4+
   - MySQL 8.0+

2. **Clone the Repository**
   ```bash
   git clone [repository-url]
   cd elearning-backend
   ```

3. **Configure Environment**
   - Copy `src/main/resources/application.properties.example` to `application.properties`
   - Update the following properties:
     - MongoDB connection string
     - MySQL connection details
     - JWT secret
     - SMTP settings
     - File upload configurations

4. **Build and Run**
   ```bash
   mvn clean install
   java -jar target/elearning-backend-0.0.1-SNAPSHOT.jar
   ```

## Configuration

### Application Properties
```properties
# Database Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/elearning
spring.datasource.url=jdbc:mysql://localhost:3306/elearning

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=3600000

# File Upload Configuration
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email
spring.mail.password=your-password
```

## Folder Structure

```
src/main/java/com/elearning/elearning_backend/
├── Config/                 # Configuration classes
├── Controller/            # REST and MVC controllers
├── DTO/                   # Data Transfer Objects
├── Enum/                  # Enumerations
├── Model/                 # Domain models
├── Repository/           # Data access layer
├── Security/             # Security configurations
├── Service/              # Business logic
└── ElearningBackendApplication.java

src/main/resources/
├── static/               # Static resources
├── templates/            # Thymeleaf templates
└── application.properties
```

## API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/forgot-password` - Password recovery

### Course Endpoints
- `GET /api/courses` - List all courses
- `GET /api/courses/{id}` - Get course details
- `POST /api/courses` - Create new course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

### User Endpoints
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Analytics Endpoints
- `GET /api/analytics/user/{userId}` - User statistics
- `GET /api/analytics/teacher/{teacherId}/courses` - Teacher course statistics
- `GET /api/analytics/admin/overview` - System overview

## State Management

- JWT-based authentication state
- Session management using Spring Security
- WebSocket session handling for real-time features
- Course progress tracking using MongoDB

## Styling

- Custom CSS framework
- Responsive design
- Dark mode support
- Custom components
- Bootstrap integration

## Caching

- Spring Cache abstraction
- In-memory caching for frequently accessed data
- Cache management for course content
- Session caching

## Authentication

- JWT-based authentication
- Role-based access control
- Secure cookie handling
- CSRF protection
- Password encryption using BCrypt

## Other Third-party Libraries

- `org.projectlombok:lombok` - Reduce boilerplate code
- `io.jsonwebtoken:jjwt` - JWT handling
- `org.apache.poi:poi-ooxml` - Excel file handling
- `com.vladsch.flexmark:flexmark-all` - Markdown processing
- `org.springframework.boot:spring-boot-starter-websocket` - WebSocket support

## Documentation
- Vietnamese docs: `docs/vi/README.md`

## Contact

For any inquiries or support, please contact:
- Email: phamquangnamhuy1908@gmail.com
-Linkedin: https://www.linkedin.com/in/ph%E1%BA%A1m-quang-huy-324952359/

 