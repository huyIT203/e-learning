// Teacher Profile Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all functionality
    initializeAvatarUpload();
    initializeFormHandlers();
    initializePasswordStrength();
    loadTeacherStats();
    loadActivityLog();
});

// Initialize Avatar Upload
function initializeAvatarUpload() {
    const avatarInput = document.getElementById('avatarInput');
    const profileAvatar = document.getElementById('profileAvatar');
    
    if (avatarInput && profileAvatar) {
        avatarInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                // Validate file type
                if (!file.type.startsWith('image/')) {
                    showNotification('Vui lòng chọn file hình ảnh hợp lệ', 'error');
                    return;
                }
                
                // Validate file size (max 5MB)
                if (file.size > 5 * 1024 * 1024) {
                    showNotification('Kích thước file không được vượt quá 5MB', 'error');
                    return;
                }
                
                // Preview image
                const reader = new FileReader();
                reader.onload = function(e) {
                    profileAvatar.src = e.target.result;
                };
                reader.readAsDataURL(file);
                
                // Upload avatar
                uploadAvatar(file);
            }
        });
    }
}

// Upload Avatar
async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('avatar', file);
    
    try {
        const response = await fetch('/teacher/api/profile/avatar', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Cập nhật avatar thành công', 'success');
            // Update all avatar images on the page
            if (result.avatarUrl) {
                document.querySelectorAll('img[alt="Profile"], img[alt="Avatar"], .profile-avatar').forEach(img => {
                    img.src = result.avatarUrl;
                });
            }
        } else {
            showNotification(result.message || 'Cập nhật avatar thất bại', 'error');
        }
    } catch (error) {
        console.error('Error uploading avatar:', error);
        showNotification('Có lỗi xảy ra khi tải lên avatar', 'error');
    }
}

// Initialize Form Handlers
function initializeFormHandlers() {
    // Personal Information Form
    const personalForm = document.getElementById('personalInfoForm');
    if (personalForm) {
        personalForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleFormSubmit(this, '/teacher/api/profile/personal');
        });
    }
    
    // Contact Information Form
    const contactForm = document.getElementById('contactInfoForm');
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleFormSubmit(this, '/teacher/api/profile/contact');
        });
    }
    
    // Security Form
    const securityForm = document.getElementById('securityForm');
    if (securityForm) {
        securityForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handlePasswordChange(this);
        });
    }
}

// Handle Form Submit
async function handleFormSubmit(form, endpoint) {
    const formData = new FormData(form);
    const submitButton = form.querySelector('button[type="submit"]');
    const originalText = submitButton.innerHTML;
    
    // Show loading state
    submitButton.innerHTML = '<span class="spinner"></span> Đang xử lý...';
    submitButton.disabled = true;
    form.classList.add('loading');
    
    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification(result.message || 'Cập nhật thành công', 'success');
        } else {
            showNotification(result.message || 'Cập nhật thất bại', 'error');
        }
    } catch (error) {
        console.error('Error submitting form:', error);
        showNotification('Có lỗi xảy ra khi cập nhật thông tin', 'error');
    } finally {
        // Reset loading state
        submitButton.innerHTML = originalText;
        submitButton.disabled = false;
        form.classList.remove('loading');
    }
}

// Handle Password Change
async function handlePasswordChange(form) {
    const formData = new FormData(form);
    const newPassword = formData.get('newPassword');
    const confirmPassword = formData.get('confirmPassword');
    
    // Validate passwords match
    if (newPassword !== confirmPassword) {
        showNotification('Mật khẩu xác nhận không khớp', 'error');
        return;
    }
    
    // Validate password strength
    if (newPassword.length < 6) {
        showNotification('Mật khẩu phải có ít nhất 6 ký tự', 'error');
        return;
    }
    
    const submitButton = form.querySelector('button[type="submit"]');
    const originalText = submitButton.innerHTML;
    
    // Show loading state
    submitButton.innerHTML = '<span class="spinner"></span> Đang xử lý...';
    submitButton.disabled = true;
    form.classList.add('loading');
    
    try {
        const response = await fetch('/teacher/api/profile/password', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification(result.message || 'Đổi mật khẩu thành công', 'success');
            form.reset();
        } else {
            showNotification(result.message || 'Đổi mật khẩu thất bại', 'error');
        }
    } catch (error) {
        console.error('Error changing password:', error);
        showNotification('Có lỗi xảy ra khi đổi mật khẩu', 'error');
    } finally {
        // Reset loading state
        submitButton.innerHTML = originalText;
        submitButton.disabled = false;
        form.classList.remove('loading');
    }
}

// Initialize Password Strength
function initializePasswordStrength() {
    const newPasswordInput = document.getElementById('newPassword');
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');
    
    if (newPasswordInput && strengthFill && strengthText) {
        newPasswordInput.addEventListener('input', function() {
            const password = this.value;
            const strength = calculatePasswordStrength(password);
            
            // Update strength bar
            strengthFill.style.width = strength.percentage + '%';
            strengthFill.style.backgroundColor = strength.color;
            strengthText.textContent = strength.text;
            strengthText.style.color = strength.color;
        });
    }
}

// Calculate Password Strength
function calculatePasswordStrength(password) {
    let score = 0;
    let feedback = [];
    
    if (password.length >= 8) score += 25;
    else feedback.push('ít nhất 8 ký tự');
    
    if (/[a-z]/.test(password)) score += 25;
    else feedback.push('chữ thường');
    
    if (/[A-Z]/.test(password)) score += 25;
    else feedback.push('chữ hoa');
    
    if (/[0-9]/.test(password)) score += 25;
    else feedback.push('số');
    
    if (/[^A-Za-z0-9]/.test(password)) score += 10;
    
    let result = {
        percentage: Math.min(score, 100),
        color: '#dc3545',
        text: 'Yếu'
    };
    
    if (score >= 80) {
        result.color = '#28a745';
        result.text = 'Mạnh';
    } else if (score >= 60) {
        result.color = '#ffc107';
        result.text = 'Trung bình';
    } else if (score >= 40) {
        result.color = '#fd7e14';
        result.text = 'Yếu';
    }
    
    if (feedback.length > 0 && password.length > 0) {
        result.text += ' (cần: ' + feedback.join(', ') + ')';
    }
    
    return result;
}

// Load Teacher Stats
async function loadTeacherStats() {
    try {
        const response = await fetch('/teacher/api/stats');
        const result = await response.json();
        
        if (result.success) {
            const stats = result.stats;
            
            // Update header stats
            updateCounter('totalCourses', stats.totalCourses);
            updateCounter('totalStudents', stats.totalStudents);
            updateCounter('completionRate', stats.completionRate);
            
            // Update overview cards
            updateCounter('activeCourses', stats.activeCourses);
            updateCounter('newEnrollments', stats.newEnrollments);
            document.getElementById('averageRating').textContent = stats.averageRating;
            document.getElementById('monthlyRevenue').textContent = stats.monthlyRevenue;
        }
    } catch (error) {
        console.error('Error loading teacher stats:', error);
    }
}

// Load Activity Log
async function loadActivityLog() {
    try {
        const response = await fetch('/teacher/api/activity-log');
        const result = await response.json();
        
        if (result.success) {
            const logContainer = document.getElementById('activityLog');
            if (logContainer) {
                logContainer.innerHTML = '';
                
                result.activities.forEach(activity => {
                    const logItem = createLogItem(activity);
                    logContainer.appendChild(logItem);
                });
            }
        }
    } catch (error) {
        console.error('Error loading activity log:', error);
    }
}

// Create Log Item
function createLogItem(activity) {
    const logItem = document.createElement('div');
    logItem.className = 'log-item';
    
    logItem.innerHTML = `
        <div class="log-icon ${activity.type}">
            <i class="fas ${getActivityIcon(activity.type)}"></i>
        </div>
        <div class="log-details">
            <div class="log-action">${activity.action}</div>
            <div class="log-time">${formatTime(activity.timestamp)}</div>
        </div>
    `;
    
    return logItem;
}

// Get Activity Icon
function getActivityIcon(type) {
    const icons = {
        'create': 'fa-plus',
        'update': 'fa-edit',
        'delete': 'fa-trash',
        'login': 'fa-sign-in-alt',
        'course': 'fa-book',
        'lesson': 'fa-play',
        'student': 'fa-user-graduate'
    };
    return icons[type] || 'fa-info';
}

// Format Time
function formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('vi-VN');
}

// Show Notification
function showNotification(message, type = 'info') {
    const toast = document.getElementById('toast');
    if (toast) {
        toast.textContent = message;
        toast.className = `toast ${type} show`;
        
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }
}

// Update Counter with Animation
function updateCounter(elementId, targetValue) {
    const element = document.getElementById(elementId);
    if (element) {
        const currentValue = parseInt(element.textContent) || 0;
        const increment = (targetValue - currentValue) / 20;
        let current = currentValue;
        
        const timer = setInterval(() => {
            current += increment;
            if ((increment > 0 && current >= targetValue) || (increment < 0 && current <= targetValue)) {
                current = targetValue;
                clearInterval(timer);
            }
            element.textContent = Math.floor(current);
        }, 50);
    }
}

// Update User Info in UI
function updateUserInfo(user) {
    // Update name
    const nameElements = document.querySelectorAll('[data-user="name"]');
    nameElements.forEach(el => el.textContent = user.name);
    
    // Update email
    const emailElements = document.querySelectorAll('[data-user="email"]');
    emailElements.forEach(el => el.textContent = user.email);
    
    // Update avatar
    if (user.avatarUrl) {
        document.querySelectorAll('img[alt="Profile"], img[alt="Avatar"]').forEach(img => {
            img.src = user.avatarUrl;
        });
    }
}

// Auto-refresh stats every 60 seconds
setInterval(loadTeacherStats, 60000);

// Sidebar integration (if exists)
if (typeof window.sidebarHandler !== 'undefined') {
    window.sidebarHandler.setActivePage('profile');
} 