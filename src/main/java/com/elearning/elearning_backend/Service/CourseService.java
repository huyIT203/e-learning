package com.elearning.elearning_backend.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elearning.elearning_backend.DTO.CourseStats;
import com.elearning.elearning_backend.DTO.TeacherCourseStats;
import com.elearning.elearning_backend.Enum.CourseStatus;
import com.elearning.elearning_backend.Enum.Role;
import com.elearning.elearning_backend.Model.Course;
import com.elearning.elearning_backend.Model.Exam;
import com.elearning.elearning_backend.Model.ExamSubmission;
import com.elearning.elearning_backend.Model.Lesson;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.CourseRepository;
import com.elearning.elearning_backend.Repository.ExamRepository;
import com.elearning.elearning_backend.Repository.ExamSubmissionRepository;
import com.elearning.elearning_backend.Repository.LessonProgressRepository;
import com.elearning.elearning_backend.Repository.LessonRepository;
import com.elearning.elearning_backend.Repository.QuestionRepository;
import com.elearning.elearning_backend.Repository.StudentAnswerRepository;
import com.elearning.elearning_backend.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final QuestionRepository questionRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public Course createCourse(Course course) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if teacherEmail is provided (for admin creating courses for other teachers)
        if (course.getTeacherEmail() != null && !course.getTeacherEmail().isEmpty()) {
            // Admin is creating course for a specific teacher
            User teacher = userRepository.findByEmail(course.getTeacherEmail())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên với email: " + course.getTeacherEmail()));
            
            if (teacher.getRole() != Role.TEACHER) {
                throw new RuntimeException("Người dùng không phải là giảng viên");
            }
            
            // Set instructor information from selected teacher
            course.setCreatedByUserId(teacher.getId());
            course.setInstructorName(teacher.getName());
        } else {
            // Teacher creating their own course OR admin didn't specify teacher
            // Set instructor information from current user
            course.setCreatedByUserId(currentUser.getId());
            course.setInstructorName(currentUser.getName());
            course.setTeacherEmail(currentUser.getEmail());
        }
        
        // Status logic: Check user role and course status to determine final status
        if (currentUser.getRole() == Role.ADMIN) {
            // Admin can set any status, but with specific rules:
            if (course.getStatus() == null || course.getStatus().isEmpty()) {
                course.setStatus(String.valueOf(CourseStatus.DRAFT));
            }
            
            // Status handling for Admin:
            if ("DRAFT".equalsIgnoreCase(course.getStatus())) {
                // DRAFT courses need approval, so set to PENDING
                course.setStatus(String.valueOf(CourseStatus.PENDING));
            } else if ("PUBLISHED".equalsIgnoreCase(course.getStatus())) {
                // PUBLISHED courses don't need approval, keep as PUBLISHED
                course.setStatus(String.valueOf(CourseStatus.PUBLISHED));
            }
            // Other statuses (ARCHIVED, etc.) keep as is
        } else {
            // Non-admin users (teachers) must go through approval process
            course.setStatus(String.valueOf(CourseStatus.PENDING));
        }
        
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        
        // Populate lesson data and teacher email for each course
        for (Course course : courses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return courses;
    }

    public List<Course> getPublishedCourses() {
        List<Course> courses = courseRepository.findAll();
        
        // Filter only published courses
        List<Course> publishedCourses = courses.stream()
                .filter(course -> "PUBLISHED".equalsIgnoreCase(course.getStatus()))
                .collect(Collectors.toList());
        
        // Populate lesson data and teacher email for each course
        for (Course course : publishedCourses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return publishedCourses;
    }

    public List<Course> getCoursesByInstructor(String userId) {
        List<Course> courses = courseRepository.findByCreatedByUserId(userId);
        
        // Populate lesson data and teacher email for each course
        for (Course course : courses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return courses;
    }

    public Course getCourseById(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));
        
        // Populate lesson data
        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        course.setLessons(lessons);
        
        return course;
    }

    public Course updateCourse(String id, Course updatedCourse) {
        Course course = getCourseById(id);

        course.setTitle(updatedCourse.getTitle());
        course.setDescription(updatedCourse.getDescription());
        course.setDuration(updatedCourse.getDuration());
        course.setImage(updatedCourse.getImage());
        course.setLevel(updatedCourse.getLevel());
        course.setStatus(updatedCourse.getStatus());
        course.setPrerequisites(updatedCourse.getPrerequisites());
        course.setObjectives(updatedCourse.getObjectives());
        
        // Update category fields
        course.setJobRole(updatedCourse.getJobRole());
        course.setSkillCategory(updatedCourse.getSkillCategory());
        
        // Update instructor if changed
        if (updatedCourse.getTeacherEmail() != null && !updatedCourse.getTeacherEmail().isEmpty()) {
            User teacher = userRepository.findByEmail(updatedCourse.getTeacherEmail())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));
            if (teacher.getRole() != Role.TEACHER) {
                throw new RuntimeException("Người dùng không phải là giảng viên");
            }
            course.setCreatedByUserId(teacher.getId());
            course.setInstructorName(teacher.getName());
        }
        
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(String id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khóa học với ID: " + id);
        }
        
        System.out.println("🗑️ Starting cascading delete for course: " + id);
        
        try {
            // 1. Get all lessons for this course
            List<Lesson> lessons = lessonRepository.findByCourseId(id);
            System.out.println("📚 Found " + lessons.size() + " lessons to delete");
            
            // 2. Delete all lesson progress for this course
            lessonProgressRepository.deleteByCourseId(id);
            System.out.println("📊 Deleted lesson progress for course: " + id);
            
            // 3. Get all exams for this course
            List<Exam> exams = examRepository.findByCourseId(id);
            System.out.println("📝 Found " + exams.size() + " exams to delete");
            
            // 4. For each exam, delete related data
            for (Exam exam : exams) {
                String examId = exam.getId();
                
                // Get all submissions for this exam
                List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(examId);
                System.out.println("📋 Found " + submissions.size() + " submissions for exam: " + examId);
                
                // Delete student answers for each submission
                for (ExamSubmission submission : submissions) {
                    studentAnswerRepository.deleteBySubmissionId(submission.getId());
                    System.out.println("✅ Deleted student answers for submission: " + submission.getId());
                }
                
                // Delete exam submissions
                examSubmissionRepository.deleteByExamId(examId);
                System.out.println("✅ Deleted submissions for exam: " + examId);
                
                // Delete questions for this exam
                questionRepository.deleteByExamId(examId);
                System.out.println("✅ Deleted questions for exam: " + examId);
            }
            
            // 5. Delete all exams for this course
            examRepository.deleteByCourseId(id);
            System.out.println("✅ Deleted exams for course: " + id);
            
            // 6. Delete all lessons for this course
            lessonRepository.deleteByCourseId(id);
            System.out.println("✅ Deleted lessons for course: " + id);
            
            // 7. Finally, delete the course itself
            courseRepository.deleteById(id);
            System.out.println("✅ Successfully deleted course: " + id);
            
        } catch (Exception e) {
            System.err.println("❌ Error during cascading delete for course " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể xóa khóa học và dữ liệu liên quan: " + e.getMessage());
        }
    }

    public List<User> getAllTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }

    public CourseStats getCourseStats() {
        CourseStats stats = new CourseStats();
        stats.setTotalCourses(courseRepository.count());
        stats.setTotalTeachers(userRepository.countByRole(Role.TEACHER));
        stats.setTotalStudents(userRepository.countByRole(Role.STUDENT));
        
        return stats;
    }
    public TeacherCourseStats getStatsForTeacher(String teacherId) {
        System.out.println("🔍 Getting stats for teacher ID: " + teacherId);
        
        List<Course> allCourses = getCoursesByInstructor(teacherId);
        System.out.println("📚 Found " + allCourses.size() + " courses for teacher");
        
        // Debug: Print all courses and their statuses
        for (Course course : allCourses) {
            System.out.println("📖 Course: " + course.getTitle() + " | Status: '" + course.getStatus() + "' | Students: " + 
                (course.getEnrolledUserIds() != null ? course.getEnrolledUserIds().size() : 0) + 
                " | Lessons: " + (course.getLessons() != null ? course.getLessons().size() : 0));
        }

        // Count approved courses (handle different status formats)
        int totalCourses = (int) allCourses.stream()
                .filter(course -> {
                    String status = course.getStatus();
                    boolean isApproved = status != null && (
                        "PUBLISHED".equalsIgnoreCase(status) || 
                        "APPROVED".equalsIgnoreCase(status) ||
                        "ACTIVE".equalsIgnoreCase(status)
                    );
                    if (isApproved) {
                        System.out.println("✅ Approved course: " + course.getTitle());
                    }
                    return isApproved;
                })
                .count();

        int totalStudents = allCourses.stream()
                .mapToInt(course -> course.getEnrolledUserIds() != null ? course.getEnrolledUserIds().size() : 0)
                .sum();

        int totalLessons = allCourses.stream()
                .mapToInt(course -> course.getLessons() != null ? course.getLessons().size() : 0)
                .sum();

        // Count pending courses (handle different status formats)
        int pendingCourses = (int) allCourses.stream()
                .filter(course -> {
                    String status = course.getStatus();
                    boolean isPending = status != null && "PENDING".equalsIgnoreCase(status);
                    if (isPending) {
                        System.out.println("⏳ Pending course: " + course.getTitle());
                    }
                    return isPending;
                })
                .count();

        System.out.println("📊 Final stats - Total Courses: " + totalCourses + 
                          " | Total Students: " + totalStudents + 
                          " | Total Lessons: " + totalLessons + 
                          " | Pending Courses: " + pendingCourses);

        TeacherCourseStats stats = new TeacherCourseStats();
        stats.setTotalCourses(totalCourses);
        stats.setTotalStudents(totalStudents);
        stats.setTotalLessons(totalLessons);
        stats.setPendingCourses(pendingCourses);

        return stats;
    }

    public List<Course> getCoursesByInstructorEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Course> courses = courseRepository.findByCreatedByUserId(user.getId());
        
        // Populate lesson data for each course
        for (Course course : courses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
        }
        
        return courses;
    }
    public Course updateStatus(String id, CourseStatus status) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        course.setStatus(String.valueOf(status));
        return courseRepository.save(course);
    }

    public List<Course> getCoursesByStatus(CourseStatus status) {
        return courseRepository.findByStatus(status);
    }
    public Course getCourseWithModulesAndLessons(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return course;
    }

    public Course enrollUserInCourse(String courseId, String userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getEnrolledUserIds() == null) {
            course.setEnrolledUserIds(new ArrayList<>());
        }
        
        if (!course.getEnrolledUserIds().contains(userId)) {
            course.getEnrolledUserIds().add(userId);
            courseRepository.save(course);
        }
        
        return course;
    }
    
    public List<Course> getEnrolledCourses(String userId) {
        List<Course> enrolledCourses = courseRepository.findByEnrolledUserIdsContaining(userId);
        
        // Populate lesson data and teacher email for each course
        for (Course course : enrolledCourses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return enrolledCourses;
    }
    
    public boolean isUserEnrolledInCourse(String courseId, String userId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        return course != null && course.getEnrolledUserIds() != null && 
               course.getEnrolledUserIds().contains(userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // Category-based search methods
    public List<Course> getCoursesByJobRole(String jobRole) {
        List<Course> courses = courseRepository.findByJobRole(jobRole);
        
        // Filter only published courses
        List<Course> publishedCourses = courses.stream()
                .filter(course -> "PUBLISHED".equalsIgnoreCase(course.getStatus()))
                .collect(Collectors.toList());
        
        // Populate lesson data and teacher email for each course
        for (Course course : publishedCourses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return publishedCourses;
    }
    
    public List<Course> getCoursesBySkillCategory(String skillCategory) {
        List<Course> courses = courseRepository.findBySkillCategory(skillCategory);
        
        // Filter only published courses
        List<Course> publishedCourses = courses.stream()
                .filter(course -> "PUBLISHED".equalsIgnoreCase(course.getStatus()))
                .collect(Collectors.toList());
        
        // Populate lesson data and teacher email for each course
        for (Course course : publishedCourses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return publishedCourses;
    }
    
    public List<Course> getCoursesByJobRoleAndSkill(String jobRole, String skillCategory) {
        List<Course> courses = courseRepository.findByJobRoleAndSkillCategory(jobRole, skillCategory);
        
        // Filter only published courses
        List<Course> publishedCourses = courses.stream()
                .filter(course -> "PUBLISHED".equalsIgnoreCase(course.getStatus()))
                .collect(Collectors.toList());
        
        // Populate lesson data and teacher email for each course
        for (Course course : publishedCourses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            course.setLessons(lessons);
            
            // Populate teacher email if teacherId exists
            if (course.getCreatedByUserId() != null) {
                User teacher = userRepository.findById(course.getCreatedByUserId()).orElse(null);
                if (teacher != null) {
                    course.setTeacherEmail(teacher.getEmail());
                    course.setInstructorName(teacher.getName());
                }
            }
        }
        
        return publishedCourses;
    }
}
