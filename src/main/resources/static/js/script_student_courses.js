// ============================
// STUDENT COURSES MANAGEMENT
// ============================

// Global variables
let allCourses = [];
let filteredCourses = [];
let userEnrollments = [];
let currentUser = null;

// DOM Elements
const studentSidebar = document.getElementById('studentSidebar');
const mainContent = document.getElementById('mainContent');
const menuToggle = document.getElementById('menuToggle');
const loadingSpinner = document.getElementById('loadingSpinner');
const coursesGrid = document.getElementById('coursesGrid');
const emptyState = document.getElementById('emptyState');

// Search and Filter Elements
const globalSearch = document.getElementById('globalSearch');
const searchInput = document.getElementById('searchInput');
const categoryFilter = document.getElementById('categoryFilter');
const levelFilter = document.getElementById('levelFilter');
const priceFilter = document.getElementById('priceFilter');
const statusFilter = document.getElementById('statusFilter');

// Statistics Elements
const totalCoursesCount = document.getElementById('totalCoursesCount');
const myEnrolledCount = document.getElementById('myEnrolledCount');
const inProgressCount = document.getElementById('inProgressCount');
const completedCount = document.getElementById('completedCount');
const coursesCount = document.getElementById('coursesCount');

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Initializing student courses page...');
    
    // Setup sidebar and navigation
    setupSidebar();
    setupNavigation();
    
    // Setup search and filters
    setupSearchAndFilters();
    
    // Load user data and courses
    loadUserData();
    loadCourses();
    loadUserEnrollments();
});

// ============================
// SIDEBAR AND NAVIGATION
// ============================
function setupSidebar() {
    // Menu toggle functionality
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            studentSidebar.classList.toggle('hide');
            mainContent.classList.toggle('expanded');
            
            // Save sidebar state
            localStorage.setItem('sidebarHidden', studentSidebar.classList.contains('hide'));
        });
    }
    
    // Restore sidebar state
    const sidebarHidden = localStorage.getItem('sidebarHidden') === 'true';
    if (sidebarHidden) {
        studentSidebar.classList.add('hide');
        mainContent.classList.add('expanded');
    }
    
    // Set active menu item
    const currentPath = window.location.pathname;
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        const href = item.getAttribute('href');
        if (currentPath === href || currentPath.startsWith(href)) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

function setupNavigation() {
    // Responsive sidebar for mobile
    function handleResize() {
        if (window.innerWidth <= 768) {
            studentSidebar.classList.add('hide');
            mainContent.classList.add('expanded');
        } else {
            const sidebarHidden = localStorage.getItem('sidebarHidden') === 'true';
            if (!sidebarHidden) {
                studentSidebar.classList.remove('hide');
                mainContent.classList.remove('expanded');
            }
        }
    }
    
    window.addEventListener('resize', handleResize);
    handleResize(); // Initial check
    
    // Click outside sidebar to close on mobile
    document.addEventListener('click', function(e) {
        if (window.innerWidth <= 768) {
            if (!studentSidebar.contains(e.target) && !menuToggle.contains(e.target)) {
                studentSidebar.classList.add('hide');
                mainContent.classList.add('expanded');
            }
        }
    });
}

// ============================
// SEARCH AND FILTERS
// ============================
function setupSearchAndFilters() {
    // Global search in navbar
    if (globalSearch) {
        globalSearch.addEventListener('input', debounce(function() {
            if (searchInput) {
                searchInput.value = this.value;
            }
            filterCourses();
        }, 300));
    }
    
    // Search input in filters
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function() {
            if (globalSearch) {
                globalSearch.value = this.value;
            }
            filterCourses();
        }, 300));
    }
    
    // Filter dropdowns
    [categoryFilter, levelFilter, priceFilter, statusFilter].forEach(filter => {
        if (filter) {
            filter.addEventListener('change', filterCourses);
        }
    });
    
    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + F to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            if (globalSearch) {
                globalSearch.focus();
                globalSearch.select();
            }
        }
        
        // Escape to clear filters
        if (e.key === 'Escape') {
            clearFilters();
        }
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function filterCourses() {
    const searchTerm = (searchInput?.value || globalSearch?.value || '').toLowerCase();
    const category = categoryFilter?.value || '';
    const level = levelFilter?.value || '';
    const price = priceFilter?.value || '';
    const status = statusFilter?.value || '';
    
    console.log('🔍 Filtering courses:', { searchTerm, category, level, price, status });
    
    filteredCourses = allCourses.filter(course => {
        // Search filter
        const matchesSearch = !searchTerm || 
            course.title?.toLowerCase().includes(searchTerm) ||
            course.description?.toLowerCase().includes(searchTerm) ||
            course.instructorName?.toLowerCase().includes(searchTerm);
        
        // Category filter
        const matchesCategory = !category || course.category === category;
        
        // Level filter
        const matchesLevel = !level || course.level === level;
        
        // Price filter
        const matchesPrice = !price || 
            (price === 'free' && (course.price === 0 || course.price === null)) ||
            (price === 'paid' && course.price > 0);
        
        // Status filter
        const isEnrolled = userEnrollments.some(enrollment => enrollment.courseId === course.id);
        const matchesStatus = !status ||
            (status === 'available' && !isEnrolled) ||
            (status === 'enrolled' && isEnrolled);
        
        return matchesSearch && matchesCategory && matchesLevel && matchesPrice && matchesStatus;
    });
    
    console.log(`📊 Filtered ${filteredCourses.length} out of ${allCourses.length} courses`);
    
    renderCourses(filteredCourses);
    updateCoursesCount();
}

function clearFilters() {
    if (searchInput) searchInput.value = '';
    if (globalSearch) globalSearch.value = '';
    if (categoryFilter) categoryFilter.value = '';
    if (levelFilter) levelFilter.value = '';
    if (priceFilter) priceFilter.value = '';
    if (statusFilter) statusFilter.value = '';
    
    filteredCourses = [...allCourses];
    renderCourses(filteredCourses);
    updateCoursesCount();
}

// ============================
// DATA LOADING
// ============================
function loadUserData() {
    console.log('👤 Loading user data...');
    
    fetch('/api/auth/me', {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(user => {
        console.log(' User data loaded:', user);
        currentUser = user;
        updateUserInfo(user);
    })
    .catch(error => {
        console.error('Error loading user data:', error);
        // Set default user info
        updateUserInfo({ name: 'Học viên', email: 'student@example.com' });
    });
}

function loadCourses() {
    console.log('📚 Loading courses...');
    showLoading();
    
    fetch('/api/courses', {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('📡 Courses response status:', response.status);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(courses => {
        console.log(' Courses loaded successfully:', courses);
        
        allCourses = courses.map(course => ({
            ...course,
            category: course.category || 'programming',
            level: course.level || 'beginner',
            price: course.price || 0,
            instructorName: course.instructorName || 'Giảng viên',
            lessonsCount: course.lessons ? course.lessons.length : 0,
            duration: calculateCourseDuration(course.lessons),
            enrollmentCount: course.enrollmentCount || 0
        }));
        
        filteredCourses = [...allCourses];
        
        renderCourses(filteredCourses);
        updateStatistics();
        updateCoursesCount();
        hideLoading();
    })
    .catch(error => {
        console.error('Error loading courses:', error);
        showError('Không thể tải danh sách khóa học: ' + error.message);
        hideLoading();
    });
}

function loadUserEnrollments() {
    if (!currentUser) {
        console.log('⏳ Waiting for user data...');
        setTimeout(loadUserEnrollments, 1000);
        return;
    }
    
    console.log('📋 Loading user enrollments...');
    
    fetch('/api/student/enrollments', {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 404) {
                console.log('📝 No enrollments found');
                return [];
            }
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(enrollments => {
        console.log(' Enrollments loaded:', enrollments);
        userEnrollments = enrollments || [];
        
        // Re-render courses with enrollment status
        if (filteredCourses.length > 0) {
            renderCourses(filteredCourses);
        }
        
        updateStatistics();
        updateSidebarBadges();
    })
    .catch(error => {
        console.error('Error loading enrollments:', error);
        userEnrollments = [];
    });
}

// ============================
// UI RENDERING
// ============================
function showLoading() {
    if (loadingSpinner) loadingSpinner.style.display = 'flex';
    if (coursesGrid) coursesGrid.style.display = 'none';
    if (emptyState) emptyState.style.display = 'none';
}

function hideLoading() {
    if (loadingSpinner) loadingSpinner.style.display = 'none';
}

function renderCourses(courses) {
    if (!coursesGrid) return;
    
    if (courses.length === 0) {
        showEmptyState();
        return;
    }
    
    coursesGrid.innerHTML = courses.map(course => {
        const isEnrolled = userEnrollments.some(enrollment => enrollment.courseId === course.id);
        const enrollment = userEnrollments.find(enrollment => enrollment.courseId === course.id);
        
        return `
            <div class="course-card" data-course-id="${course.id}">
                <div class="course-image" style="background: linear-gradient(135deg, #667eea, #764ba2), url('${course.imageUrl || ''}') center/cover;">
                </div>
                
                <div class="course-content">
                    <span class="course-category">${getCategoryText(course.category)}</span>
                    
                    <h3 class="course-title">${course.title}</h3>
                    
                    <p class="course-description">${truncateText(course.description || '', 120)}</p>
                    
                    <div class="course-meta">
                        <span>
                            <i class='bx bx-book'></i>
                            ${course.lessonsCount} bài học
                        </span>
                        <span>
                            <i class='bx bx-time'></i>
                            ${course.duration}
                        </span>
                        <span>
                            <i class='bx bx-user'></i>
                            ${course.enrollmentCount} học viên
                        </span>
                    </div>
                    
                    <div class="course-instructor">
                        <img src="${course.instructorAvatar || 'https://via.placeholder.com/35'}" 
                             alt="${course.instructorName}" class="instructor-avatar">
                        <div class="instructor-info">
                            <p class="instructor-name">${course.instructorName}</p>
                            <p class="instructor-title">Giảng viên</p>
                        </div>
                    </div>
                    
                    <div class="course-footer">
                        <div class="course-price ${course.price === 0 ? 'free' : ''}">
                            ${course.price === 0 ? 'Miễn phí' : formatPrice(course.price)}
                        </div>
                        
                        ${isEnrolled ? 
                            `<span class="enrolled-badge">
                                <i class='bx bx-check'></i> Đã đăng ký
                            </span>` :
                            `<button class="enroll-btn" onclick="enrollInCourse('${course.id}')">
                                <i class='bx bx-plus'></i> Đăng ký
                            </button>`
                        }
                    </div>
                </div>
            </div>
        `;
    }).join('');
    
    if (coursesGrid) coursesGrid.style.display = 'grid';
    if (emptyState) emptyState.style.display = 'none';
}

function showEmptyState() {
    if (coursesGrid) coursesGrid.style.display = 'none';
    if (emptyState) emptyState.style.display = 'block';
}

function showError(message) {
    if (coursesGrid) {
        coursesGrid.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: #dc3545;">
                <i class='bx bx-error' style="font-size: 4rem; margin-bottom: 1rem;"></i>
                <h3>Có lỗi xảy ra</h3>
                <p>${message}</p>
                <button class="enroll-btn" onclick="loadCourses()" style="margin-top: 1rem;">
                    <i class='bx bx-refresh'></i> Thử lại
                </button>
            </div>
        `;
        coursesGrid.style.display = 'grid';
    }
    
    if (emptyState) emptyState.style.display = 'none';
}

// ============================
// COURSE ENROLLMENT
// ============================
function enrollInCourse(courseId) {
    const course = allCourses.find(c => c.id === courseId);
    if (!course) {
        console.error('Course not found:', courseId);
        return;
    }
    
    // Show confirmation dialog
    const confirmed = confirm(`Bạn có muốn đăng ký khóa học "${course.title}" không?`);
    if (!confirmed) {
        return;
    }
    
    console.log('📝 Enrolling in course:', course.title);
    
    // Find the enroll button and show loading state
    const enrollBtn = document.querySelector(`[data-course-id="${courseId}"] .enroll-btn`);
    if (enrollBtn) {
        const originalText = enrollBtn.innerHTML;
        enrollBtn.innerHTML = '<i class="bx bx-loader-alt bx-spin"></i> Đang đăng ký...';
        enrollBtn.disabled = true;
    }
    
    fetch('/api/student/enroll', {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ courseId: courseId })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(enrollment => {
        console.log(' Enrollment successful:', enrollment);
        
        // Add to user enrollments
        userEnrollments.push(enrollment);
        
        // Update course enrollment count
        const courseIndex = allCourses.findIndex(c => c.id === courseId);
        if (courseIndex !== -1) {
            allCourses[courseIndex].enrollmentCount = (allCourses[courseIndex].enrollmentCount || 0) + 1;
        }
        
        // Re-render courses to show enrolled status
        renderCourses(filteredCourses);
        
        // Update statistics
        updateStatistics();
        updateSidebarBadges();
        
        // Show success message
        showSuccessMessage(`Đã đăng ký thành công khóa học "${course.title}"`);
    })
    .catch(error => {
        console.error('Error enrolling in course:', error);
        
        // Reset button
        if (enrollBtn) {
            enrollBtn.innerHTML = '<i class="bx bx-plus"></i> Đăng ký';
            enrollBtn.disabled = false;
        }
        
        // Show error message
        alert('Có lỗi xảy ra khi đăng ký khóa học: ' + error.message);
    });
}

// ============================
// STATISTICS AND UI UPDATES
// ============================
function updateUserInfo(user) {
    const userName = document.getElementById('userName');
    const userAvatar = document.getElementById('userAvatar');
    
    if (userName) {
        userName.textContent = user.name || 'Học viên';
    }
    
    if (userAvatar) {
        userAvatar.src = user.avatarUrl || 'https://via.placeholder.com/50';
    }
    
    // Update navbar avatar too
    const navbarAvatar = document.querySelector('.navbar-right .user-avatar');
    if (navbarAvatar) {
        navbarAvatar.src = user.avatarUrl || 'https://via.placeholder.com/36';
    }
}

function updateStatistics() {
    const stats = {
        totalCourses: allCourses.length,
        enrolled: userEnrollments.length,
        inProgress: userEnrollments.filter(e => e.status === 'in_progress').length,
        completed: userEnrollments.filter(e => e.status === 'completed').length
    };
    
    console.log('📊 Course statistics:', stats);
    
    if (totalCoursesCount) totalCoursesCount.textContent = stats.totalCourses;
    if (myEnrolledCount) myEnrolledCount.textContent = stats.enrolled;
    if (inProgressCount) inProgressCount.textContent = stats.inProgress;
    if (completedCount) completedCount.textContent = stats.completed;
}

function updateSidebarBadges() {
    const availableCoursesCount = document.getElementById('availableCoursesCount');
    const enrolledCoursesCount = document.getElementById('enrolledCoursesCount');
    const certificatesCount = document.getElementById('certificatesCount');
    
    if (availableCoursesCount) {
        availableCoursesCount.textContent = allCourses.length;
    }
    
    if (enrolledCoursesCount) {
        enrolledCoursesCount.textContent = userEnrollments.length;
    }
    
    if (certificatesCount) {
        const completedCount = userEnrollments.filter(e => e.status === 'completed').length;
        certificatesCount.textContent = completedCount;
    }
}

function updateCoursesCount() {
    if (coursesCount) {
        coursesCount.textContent = `${filteredCourses.length} khóa học`;
    }
}

// ============================
// UTILITY FUNCTIONS
// ============================
function getCategoryText(category) {
    const categories = {
        'programming': 'Lập trình',
        'design': 'Thiết kế',
        'business': 'Kinh doanh',
        'marketing': 'Marketing',
        'language': 'Ngoại ngữ'
    };
    return categories[category] || 'Khác';
}

function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

function calculateCourseDuration(lessons) {
    if (!lessons || lessons.length === 0) return '0 phút';
    
    const totalMinutes = lessons.reduce((total, lesson) => {
        return total + (lesson.duration || 30); // Default 30 minutes per lesson
    }, 0);
    
    if (totalMinutes < 60) {
        return `${totalMinutes} phút`;
    } else {
        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;
        return minutes > 0 ? `${hours}h ${minutes}m` : `${hours} giờ`;
    }
}

// ============================
// SUCCESS MESSAGE
// ============================
function showSuccessMessage(message) {
    // Create success toast
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 12px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        z-index: 9999;
        animation: slideInRight 0.3s ease-out;
        max-width: 400px;
    `;
    toast.innerHTML = `
        <div style="display: flex; align-items: center; gap: 0.5rem;">
            <i class="bx bx-check-circle" style="font-size: 1.25rem;"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    // Remove after 4 seconds
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease-in';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 4000);
}

// ============================
// LOGOUT
// ============================
function handleLogout() {
    const confirmed = confirm('Bạn có chắc chắn muốn đăng xuất không?');
    if (!confirmed) return;
    
    fetch('/api/auth/logout', { 
        method: 'POST',
        credentials: 'same-origin'
    })
    .finally(() => {
        window.location.href = "/";
    });
}

// ============================
// CSS ANIMATIONS
// ============================
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    .course-card {
        transition: all 0.3s ease;
    }
    
    .course-card:hover {
        transform: translateY(-5px);
    }
    
    .enroll-btn:disabled {
        opacity: 0.7;
        cursor: not-allowed;
    }
`;
document.head.appendChild(style); 