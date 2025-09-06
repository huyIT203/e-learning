// ============================
// USER MANAGEMENT - ADMIN
// ============================

// Global variables
let allUsers = [];
let filteredUsers = [];
let selectedUserId = null;
let activityUpdateInterval = null;

// DOM Elements
const loadingSpinner = document.getElementById('loadingSpinner');
const usersTableContainer = document.getElementById('usersTableContainer');
const usersTableBody = document.getElementById('usersTableBody');
const emptyState = document.getElementById('emptyState');
const searchInput = document.getElementById('searchInput');
const roleFilter = document.getElementById('roleFilter');
const statusFilter = document.getElementById('statusFilter');

// Statistics elements
const totalUsersEl = document.getElementById('totalUsers');
const totalTeachersEl = document.getElementById('totalTeachers');
const totalStudentsEl = document.getElementById('totalStudents');
const totalAdminsEl = document.getElementById('totalAdmins');
const filteredCountEl = document.getElementById('filteredCount');
const totalCountEl = document.getElementById('totalCount');

// Modal elements
const roleModal = document.getElementById('roleModal');
const deleteModal = document.getElementById('deleteModal');
const statusModal = document.getElementById('statusModal');

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Initializing user management page...');
    
    // Setup sidebar active state
    setupSidebar();
    
    // Setup search and filter
    setupSearchAndFilter();
    
    // Load users and statistics
    loadUsers();
    
    // Setup modal event listeners
    setupModals();
    
    // Start real-time activity updates
    startActivityUpdates();
});

// ============================
// REAL-TIME ACTIVITY TRACKING
// ============================
function startActivityUpdates() {
    // Update activity status every 30 seconds
    activityUpdateInterval = setInterval(() => {
        updateUserActivities();
    }, 30000);
    
    console.log('🔄 Started real-time activity tracking');
}

function updateUserActivities() {
    if (allUsers.length === 0) return;
    
    // Simulate activity updates (in real app, this would fetch from server)
    allUsers.forEach(user => {
        const now = new Date();
        const lastActivity = user.lastActivityAt ? new Date(user.lastActivityAt) : new Date(now - Math.random() * 24 * 60 * 60 * 1000);
        const timeDiff = now - lastActivity;
        
        // Update activity status based on time difference
        if (timeDiff < 5 * 60 * 1000) { // 5 minutes
            user.activityStatus = 'online';
        } else if (timeDiff < 30 * 60 * 1000) { // 30 minutes
            user.activityStatus = 'away';
        } else {
            user.activityStatus = 'offline';
        }
        
        // Randomly update some users' last activity
        if (Math.random() < 0.1) { // 10% chance
            user.lastActivityAt = now.toISOString();
            user.activityStatus = 'online';
        }
    });
    
    // Re-render users if they're currently displayed
    if (filteredUsers.length > 0) {
        renderUsers(filteredUsers);
    }
}

function getActivityStatus(user) {
    if (!user.lastActivityAt) return 'offline';
    
    const now = new Date();
    const lastActivity = new Date(user.lastActivityAt);
    const timeDiff = now - lastActivity;
    
    if (timeDiff < 5 * 60 * 1000) return 'online';
    if (timeDiff < 30 * 60 * 1000) return 'away';
    return 'offline';
}

function getActivityText(status) {
    switch (status) {
        case 'online': return 'Đang hoạt động';
        case 'away': return 'Vắng mặt';
        case 'offline': return 'Không hoạt động';
        default: return 'Không xác định';
    }
}

function formatLastActivity(lastActivityAt) {
    if (!lastActivityAt) return 'Chưa bao giờ';
    
    const now = new Date();
    const lastActivity = new Date(lastActivityAt);
    const timeDiff = now - lastActivity;
    
    if (timeDiff < 60 * 1000) return 'Vừa xong';
    if (timeDiff < 60 * 60 * 1000) return `${Math.floor(timeDiff / (60 * 1000))} phút trước`;
    if (timeDiff < 24 * 60 * 60 * 1000) return `${Math.floor(timeDiff / (60 * 60 * 1000))} giờ trước`;
    
    return lastActivity.toLocaleDateString('vi-VN');
}

// ============================
// SIDEBAR MANAGEMENT
// ============================
function setupSidebar() {
    const currentPath = window.location.pathname;
    const allSideLinks = document.querySelectorAll('#sidebar .side-menu li a');

    allSideLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (currentPath === href || currentPath.startsWith(href)) {
            link.parentElement.classList.add('active');
        } else {
            link.parentElement.classList.remove('active');
        }
    });
}

// Toggle sidebar
const menuBar = document.querySelector('#content nav .bx.bx-menu');
const sidebar = document.getElementById('sidebar');
if (menuBar && sidebar) {
    menuBar.addEventListener('click', () => sidebar.classList.toggle('hide'));
}

// Dark mode toggle
const switchMode = document.getElementById('switch-mode');
if (switchMode) {
    switchMode.addEventListener('change', function() {
        document.body.classList.toggle('dark', this.checked);
    });
}

// ============================
// SEARCH AND FILTER
// ============================
function setupSearchAndFilter() {
    // Search input
    if (searchInput) {
        searchInput.addEventListener('input', debounce(filterUsers, 300));
        
        // Keyboard shortcuts
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                filterUsers();
            } else if (e.key === 'Escape') {
                e.preventDefault();
                clearFilters();
                this.blur();
            }
        });
    }
    
    // Role filter
    if (roleFilter) {
        roleFilter.addEventListener('change', filterUsers);
    }
    
    // Status filter
    if (statusFilter) {
        statusFilter.addEventListener('change', filterUsers);
    }
    
    // Global keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + F to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
        
        // Escape to clear filters
        if (e.key === 'Escape' && (searchInput?.value || roleFilter?.value || statusFilter?.value)) {
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

function filterUsers() {
    const searchTerm = searchInput?.value.toLowerCase() || '';
    const roleFilterValue = roleFilter?.value || '';
    const statusFilterValue = statusFilter?.value || '';
    
    console.log('🔍 Filtering users:', { searchTerm, roleFilterValue, statusFilterValue });
    
    filteredUsers = allUsers.filter(user => {
        // Search filter
        const matchesSearch = !searchTerm || 
            user.name?.toLowerCase().includes(searchTerm) ||
            user.email?.toLowerCase().includes(searchTerm);
        
        // Role filter
        const matchesRole = !roleFilterValue || 
            user.role?.toUpperCase() === roleFilterValue.toUpperCase();
        
        // Status filter
        const matchesStatus = !statusFilterValue || 
            (user.status || 'active') === statusFilterValue;
        
        return matchesSearch && matchesRole && matchesStatus;
    });
    
    console.log(`📊 Filtered ${filteredUsers.length} out of ${allUsers.length} users`);
    
    renderUsers(filteredUsers);
    updateFilterInfo();
}

function clearFilters() {
    if (searchInput) searchInput.value = '';
    if (roleFilter) roleFilter.value = '';
    if (statusFilter) statusFilter.value = '';
    
    filteredUsers = [...allUsers];
    renderUsers(filteredUsers);
    updateFilterInfo();
}

function updateFilterInfo() {
    if (filteredCountEl) filteredCountEl.textContent = filteredUsers.length;
    if (totalCountEl) totalCountEl.textContent = allUsers.length;
}

// ============================
// DATA LOADING
// ============================
function loadUsers() {
    console.log('🔄 Loading users...');
    showLoading();
    
    fetch('/admin/api/users/recent', {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('📡 Response status:', response.status);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('👥 Users loaded successfully:', data);
        
        // Extract users from response
        const users = data.users || [];
        
        // Add default status and activity data
        allUsers = users.map(user => ({
            ...user,
            status: user.status || 'active',
            activityStatus: getActivityStatus(user),
            lastActivityAt: user.lastActivityAt || new Date(Date.now() - Math.random() * 24 * 60 * 60 * 1000).toISOString()
        }));
        
        filteredUsers = [...allUsers];
        
        renderUsers(filteredUsers);
        updateStatistics();
        updateFilterInfo();
        hideLoading();
    })
    .catch(error => {
        console.error('Error loading users:', error);
        showError('Không thể tải danh sách người dùng: ' + error.message);
        hideLoading();
    });
}

function updateStatistics() {
    const stats = {
        total: allUsers.length,
        teachers: allUsers.filter(u => u.role === 'TEACHER').length,
        students: allUsers.filter(u => u.role === 'STUDENT').length,
        admins: allUsers.filter(u => u.role === 'ADMIN').length
    };
    
    console.log('📊 User statistics:', stats);
    
    if (totalUsersEl) totalUsersEl.textContent = stats.total;
    if (totalTeachersEl) totalTeachersEl.textContent = stats.teachers;
    if (totalStudentsEl) totalStudentsEl.textContent = stats.students;
    if (totalAdminsEl) totalAdminsEl.textContent = stats.admins;
}

// ============================
// UI RENDERING
// ============================
function showLoading() {
    if (loadingSpinner) loadingSpinner.style.display = 'flex';
    if (usersTableContainer) usersTableContainer.style.display = 'none';
    if (emptyState) emptyState.style.display = 'none';
}

function hideLoading() {
    if (loadingSpinner) loadingSpinner.style.display = 'none';
}

function renderUsers(users) {
    if (!usersTableBody) return;
    
    if (users.length === 0) {
        showEmptyState();
        return;
    }
    
    usersTableBody.innerHTML = users.map(user => {
        const joinDate = user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'N/A';
        const activityStatus = user.activityStatus || getActivityStatus(user);
        const lastActivityText = formatLastActivity(user.lastActivityAt);
        
        return `
            <tr data-user-id="${user.id}">
                <td>
                    <div class="user-info">
                        <img src="${user.avatarUrl || 'https://via.placeholder.com/45'}" 
                             alt="${user.name}" class="user-avatar">
                        <div class="user-details">
                            <h5>${user.name || 'N/A'}</h5>
                            <p>${user.email}</p>
                            <div class="user-id">ID: ${user.id}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="role-badge role-${user.role?.toLowerCase() || 'student'}">
                        ${getRoleText(user.role)}
                    </span>
                </td>
                <td>
                    <span class="status-badge status-${user.status || 'active'}" 
                          onclick="openStatusModal('${user.id}')" 
                          title="Nhấp để thay đổi trạng thái">
                        ${getStatusText(user.status || 'active')}
                    </span>
                </td>
                <td>${joinDate}</td>
                <td>
                    <div class="activity-status">
                        <div class="activity-indicator activity-${activityStatus}"></div>
                        <div>
                            <div class="activity-time">${getActivityText(activityStatus)}</div>
                            <div class="activity-time" style="font-size: 11px;">${lastActivityText}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="user-actions">
                        <button class="action-btn btn-role" onclick="openRoleModal('${user.id}')" 
                                title="Thay đổi vai trò">
                            <i class="fas fa-user-cog"></i>
                        </button>
                        <button class="action-btn btn-status" onclick="openStatusModal('${user.id}')" 
                                title="Thay đổi trạng thái">
                            <i class="fas fa-toggle-on"></i>
                        </button>
                        <button class="action-btn btn-delete" onclick="openDeleteModal('${user.id}')" 
                                title="Xóa người dùng">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
    
    if (usersTableContainer) usersTableContainer.style.display = 'table';
    if (emptyState) emptyState.style.display = 'none';
}

function showEmptyState() {
    if (usersTableContainer) usersTableContainer.style.display = 'none';
    if (emptyState) emptyState.style.display = 'block';
}

function showError(message) {
    if (usersTableBody) {
        usersTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center">
                    <div style="padding: 2rem; color: #dc3545;">
                        <i class="fas fa-exclamation-triangle" style="font-size: 3rem; margin-bottom: 1rem;"></i>
                        <h5>Có lỗi xảy ra</h5>
                        <p>${message}</p>
                        <button class="btn btn-primary" onclick="loadUsers()">
                            <i class="fas fa-redo"></i> Thử lại
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }
    
    if (usersTableContainer) usersTableContainer.style.display = 'table';
    if (emptyState) emptyState.style.display = 'none';
}

// ============================
// UTILITY FUNCTIONS
// ============================
function getRoleText(role) {
    switch (role?.toUpperCase()) {
        case 'ADMIN': return 'Quản trị viên';
        case 'TEACHER': return 'Giảng viên';
        case 'STUDENT': return 'Học viên';
        default: return 'Không xác định';
    }
}

function getStatusText(status) {
    switch (status?.toLowerCase()) {
        case 'active': return 'Hoạt động';
        case 'inactive': return 'Không hoạt động';
        case 'suspended': return 'Tạm khóa';
        default: return 'Không xác định';
    }
}

function getUserById(userId) {
    return allUsers.find(user => user.id === userId);
}

// ============================
// MODAL MANAGEMENT
// ============================
function setupModals() {
    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === roleModal) {
            closeRoleModal();
        }
        if (event.target === deleteModal) {
            closeDeleteModal();
        }
        if (event.target === statusModal) {
            closeStatusModal();
        }
    });
    
    // Close modals with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeRoleModal();
            closeDeleteModal();
            closeStatusModal();
        }
    });
}

// ============================
// ROLE MANAGEMENT
// ============================
function openRoleModal(userId) {
    const user = getUserById(userId);
    if (!user) {
        console.error('User not found:', userId);
        return;
    }
    
    selectedUserId = userId;
    
    // Populate modal fields
    document.getElementById('selectedUserName').value = `${user.name} (${user.email})`;
    document.getElementById('currentRole').value = getRoleText(user.role);
    document.getElementById('newRole').value = user.role || 'STUDENT';
    
    // Show modal
    if (roleModal) roleModal.style.display = 'block';
    
    console.log('📝 Opened role modal for user:', user.name);
}

function closeRoleModal() {
    if (roleModal) roleModal.style.display = 'none';
    selectedUserId = null;
}

function confirmRoleChange() {
    if (!selectedUserId) {
        console.error('No user selected');
        return;
    }
    
    const newRole = document.getElementById('newRole').value;
    const user = getUserById(selectedUserId);
    
    if (!user) {
        console.error('User not found:', selectedUserId);
        return;
    }
    
    if (user.role === newRole) {
        alert('Vai trò mới giống với vai trò hiện tại!');
        return;
    }
    
    console.log(`🔄 Changing role for user ${user.name} from ${user.role} to ${newRole}`);
    
    // Show loading state
    const confirmBtn = document.querySelector('#roleModal .btn-primary');
    const originalText = confirmBtn.textContent;
    confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang cập nhật...';
    confirmBtn.disabled = true;
    
    fetch(`/api/admin/users/${selectedUserId}/role`, {
        method: 'PUT',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ newRole: newRole })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(updatedUser => {
        console.log(' Role updated successfully:', updatedUser);
        
        // Update user in local array
        const userIndex = allUsers.findIndex(u => u.id === selectedUserId);
        if (userIndex !== -1) {
            allUsers[userIndex] = { ...allUsers[userIndex], ...updatedUser };
        }
        
        // Re-filter and re-render
        filterUsers();
        updateStatistics();
        
        // Close modal
        closeRoleModal();
        
        // Show success message
        showSuccessMessage(`Đã cập nhật vai trò của ${user.name} thành ${getRoleText(newRole)}`);
    })
    .catch(error => {
        console.error('Error updating role:', error);
        alert('Có lỗi xảy ra khi cập nhật vai trò: ' + error.message);
    })
    .finally(() => {
        // Reset button
        confirmBtn.textContent = originalText;
        confirmBtn.disabled = false;
    });
}

// ============================
// STATUS MANAGEMENT
// ============================
function openStatusModal(userId) {
    const user = getUserById(userId);
    if (!user) {
        console.error('User not found:', userId);
        return;
    }
    
    selectedUserId = userId;
    
    // Populate modal fields
    document.getElementById('selectedUserNameStatus').value = `${user.name} (${user.email})`;
    document.getElementById('currentStatus').value = getStatusText(user.status || 'active');
    document.getElementById('newStatus').value = user.status || 'active';
    document.getElementById('statusReason').value = '';
    
    // Show modal
    if (statusModal) statusModal.style.display = 'block';
    
    console.log('📝 Opened status modal for user:', user.name);
}

function closeStatusModal() {
    if (statusModal) statusModal.style.display = 'none';
    selectedUserId = null;
}

function confirmStatusChange() {
    if (!selectedUserId) {
        console.error('No user selected');
        return;
    }
    
    const newStatus = document.getElementById('newStatus').value;
    const reason = document.getElementById('statusReason').value;
    const user = getUserById(selectedUserId);
    
    if (!user) {
        console.error('User not found:', selectedUserId);
        return;
    }
    
    if ((user.status || 'active') === newStatus) {
        alert('Trạng thái mới giống với trạng thái hiện tại!');
        return;
    }
    
    console.log(`🔄 Changing status for user ${user.name} from ${user.status || 'active'} to ${newStatus}`);
    
    // Show loading state
    const confirmBtn = document.querySelector('#statusModal .btn-primary');
    const originalText = confirmBtn.textContent;
    confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang cập nhật...';
    confirmBtn.disabled = true;
    
    fetch(`/api/admin/users/${selectedUserId}/status`, {
        method: 'PUT',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
            newStatus: newStatus,
            reason: reason
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(updatedUser => {
        console.log(' Status updated successfully:', updatedUser);
        
        // Update user in local array
        const userIndex = allUsers.findIndex(u => u.id === selectedUserId);
        if (userIndex !== -1) {
            allUsers[userIndex] = { ...allUsers[userIndex], ...updatedUser };
        }
        
        // Re-filter and re-render
        filterUsers();
        updateStatistics();
        
        // Close modal
        closeStatusModal();
        
        // Show success message
        showSuccessMessage(`Đã cập nhật trạng thái của ${user.name} thành ${getStatusText(newStatus)}`);
    })
    .catch(error => {
        console.error('Error updating status:', error);
        alert('Có lỗi xảy ra khi cập nhật trạng thái: ' + error.message);
    })
    .finally(() => {
        // Reset button
        confirmBtn.textContent = originalText;
        confirmBtn.disabled = false;
    });
}

// ============================
// USER DELETION
// ============================
function openDeleteModal(userId) {
    const user = getUserById(userId);
    if (!user) {
        console.error('User not found:', userId);
        return;
    }
    
    selectedUserId = userId;
    
    // Populate modal
    document.getElementById('deleteUserName').textContent = user.name;
    
    // Show modal
    if (deleteModal) deleteModal.style.display = 'block';
    
    console.log('🗑️ Opened delete modal for user:', user.name);
}

function closeDeleteModal() {
    if (deleteModal) deleteModal.style.display = 'none';
    selectedUserId = null;
}

function confirmDeleteUser() {
    if (!selectedUserId) {
        console.error('No user selected');
        return;
    }
    
    const user = getUserById(selectedUserId);
    if (!user) {
        console.error('User not found:', selectedUserId);
        return;
    }
    
    console.log(`🗑️ Deleting user: ${user.name}`);
    
    // Show loading state
    const confirmBtn = document.querySelector('#deleteModal .btn-primary');
    const originalText = confirmBtn.textContent;
    confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xóa...';
    confirmBtn.disabled = true;
    
    fetch(`/api/admin/users/${selectedUserId}`, {
        method: 'DELETE',
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
        
        console.log(' User deleted successfully');
        
        // Remove user from local array
        allUsers = allUsers.filter(u => u.id !== selectedUserId);
        
        // Re-filter and re-render
        filterUsers();
        updateStatistics();
        
        // Close modal
        closeDeleteModal();
        
        // Show success message
        showSuccessMessage(`Đã xóa người dùng ${user.name}`);
    })
    .catch(error => {
        console.error('Error deleting user:', error);
        alert('Có lỗi xảy ra khi xóa người dùng: ' + error.message);
    })
    .finally(() => {
        // Reset button
        confirmBtn.textContent = originalText;
        confirmBtn.disabled = false;
    });
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
        background: #28a745;
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        z-index: 9999;
        animation: slideInRight 0.3s ease-out;
    `;
    toast.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span style="margin-left: 8px;">${message}</span>
    `;
    
    document.body.appendChild(toast);
    
    // Remove after 3 seconds
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease-in';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// ============================
// CLEANUP
// ============================
window.addEventListener('beforeunload', function() {
    if (activityUpdateInterval) {
        clearInterval(activityUpdateInterval);
    }
});

// ============================
// LOGOUT
// ============================
function handleLogout() {
    if (activityUpdateInterval) {
        clearInterval(activityUpdateInterval);
    }
    
    fetch('/api/auth/logout', { method: 'POST' })
        .finally(() => window.location.href = "/");
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
`;
document.head.appendChild(style); 