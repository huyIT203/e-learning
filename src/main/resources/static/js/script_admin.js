// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
	// Sidebar menu items
	const allSideMenu = document.querySelectorAll('#sidebar .side-menu.top li a');
	const currentPath = window.location.pathname;

	if (allSideMenu) {
		allSideMenu.forEach(item => {
			const li = item.parentElement;
			const linkPath = item.getAttribute('href');

			// Remove active class from all items first
			li.classList.remove('active');

			// Check if the current path matches the link path or starts with the link path
			if (currentPath === linkPath || (linkPath !== '/' && currentPath.startsWith(linkPath))) {
				li.classList.add('active');
			}
		});
	}

	// Toggle sidebar
	const menuBar = document.querySelector('#content nav .bx.bx-menu');
	const sidebar = document.getElementById('sidebar');
	if (menuBar && sidebar) {
		menuBar.addEventListener('click', function () {
			sidebar.classList.toggle('hide');
		});
	}

	// Search functionality
	const searchButton = document.querySelector('#content nav form .form-input button');
	const searchButtonIcon = document.querySelector('#content nav form .form-input button .bx');
	const searchForm = document.querySelector('#content nav form');
	if (searchButton && searchButtonIcon && searchForm) {
		searchButton.addEventListener('click', function (e) {
			if (window.innerWidth < 576) {
				e.preventDefault();
				searchForm.classList.toggle('show');
				if (searchForm.classList.contains('show')) {
					searchButtonIcon.classList.replace('bx-search', 'bx-x');
				} else {
					searchButtonIcon.classList.replace('bx-x', 'bx-search');
				}
			}
		});
	}

	// Responsive sidebar
	if (window.innerWidth < 768 && sidebar) {
		sidebar.classList.add('hide');
	} else if (window.innerWidth > 576 && searchButtonIcon && searchForm) {
		searchButtonIcon.classList.replace('bx-x', 'bx-search');
		searchForm.classList.remove('show');
	}

	// Dark mode toggle
	const switchMode = document.getElementById('switch-mode');

	// Handle dark mode toggle
		switchMode.addEventListener('change', function () {
			if (this.checked) {
				document.body.classList.add('dark');
			localStorage.setItem('darkMode', 'true');
			showModeNotification('Đã chuyển sang chế độ tối');
			} else {
				document.body.classList.remove('dark');
			localStorage.setItem('darkMode', 'false');
			showModeNotification('Đã chuyển sang chế độ sáng');
		}
	});

	// Function to show mode change notification
	function showModeNotification(message) {
		// Create notification element
		const notification = document.createElement('div');
		notification.textContent = message;
		notification.style.cssText = `
			position: fixed;
			top: 20px;
			right: 20px;
			background: var(--blue);
			color: white;
			padding: 12px 20px;
			border-radius: 25px;
			font-size: 14px;
			font-weight: 500;
			z-index: 10000;
			opacity: 0;
			transform: translateX(100px);
			transition: all 0.3s ease;
			box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
		`;
		
		document.body.appendChild(notification);
		
		// Animate in
		setTimeout(() => {
			notification.style.opacity = '1';
			notification.style.transform = 'translateX(0)';
		}, 100);
		
		// Animate out and remove
		setTimeout(() => {
			notification.style.opacity = '0';
			notification.style.transform = 'translateX(100px)';
			setTimeout(() => {
				if (document.body.contains(notification)) {
					document.body.removeChild(notification);
				}
			}, 300);
		}, 2000);
	}

	// Initialize dark mode on page load
	const savedDarkMode = localStorage.getItem('darkMode') === 'true';
	if (savedDarkMode) {
		document.body.classList.add('dark');
		if (switchMode) {
			switchMode.checked = true;
		}
	}

	// Auto-detect system dark mode preference
	function detectSystemDarkMode() {
		if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
			if (localStorage.getItem('darkMode') === null) {
				document.body.classList.add('dark');
				if (switchMode) {
					switchMode.checked = true;
				}
				localStorage.setItem('darkMode', 'true');
			}
		}
	}

	// Call detect system dark mode
	detectSystemDarkMode();

	// Listen for system dark mode changes
	if (window.matchMedia) {
		window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function(e) {
			if (localStorage.getItem('darkMode') === null) {
				if (e.matches) {
					document.body.classList.add('dark');
					if (switchMode) {
						switchMode.checked = true;
					}
				} else {
					document.body.classList.remove('dark');
					if (switchMode) {
						switchMode.checked = false;
					}
				}
			}
		});
	}

	// Load initial data
	loadCourses();
	loadTeachers();
	loadCourseStats();
	setupSearchAndFilter();

	// Image upload handling
	const imageUpload = document.getElementById('courseImageUpload');
	if (imageUpload) {
		imageUpload.addEventListener('change', handleImageUpload);
	}

	// Edit image upload handling
	const editImageUpload = document.getElementById('editCourseImageUpload');
	if (editImageUpload) {
		editImageUpload.addEventListener('change', handleEditImageUpload);
	}

	// Reset form when create modal is closed
	const createModal = document.getElementById('createCourseModal');
	if (createModal) {
		createModal.addEventListener('hidden.bs.modal', resetCreateCourseForm);
	}

	// Reset form when edit modal is closed
	const editModal = document.getElementById('editCourseModal');
	if (editModal) {
		editModal.addEventListener('hidden.bs.modal', resetEditCourseForm);
	}
});

// Window resize handler
window.addEventListener('resize', function () {
	const searchButtonIcon = document.querySelector('#content nav form .form-input button .bx');
	const searchForm = document.querySelector('#content nav form');
	if (searchButtonIcon && searchForm) {
		if (this.innerWidth > 576) {
			searchButtonIcon.classList.replace('bx-x', 'bx-search');
			searchForm.classList.remove('show');
		}
	}
});

function handleLogout() {
	fetch('/api/auth/logout', {
		method: 'POST'
	}).finally(() => {
		// Sau khi xóa token và context → reload về index
		window.location.href = "/";
	});
}

// Global variables for filtering
let allCourses = [];
let allTeachers = [];

// Load all courses
function loadCourses() {
	console.log('🔄 Loading courses...');
	
	fetch('/api/courses')
		.then(response => {
			if (!response.ok) {
				throw new Error('Không thể tải danh sách khóa học');
			}
			return response.json();
		})
		.then(courses => {
			console.log('📚 Courses loaded:', courses.length);
			
			// Debug: Log teacher emails
			courses.forEach((course, index) => {
				console.log(`📚 Course ${index + 1}: "${course.title}" - teacherEmail: "${course.teacherEmail}", instructorName: "${course.instructorName}"`);
			});
			
			allCourses = courses;
			renderFilteredCourses(courses);
			updateCourseStats(courses);
		})
		.catch(error => {
			console.error('Error loading courses:', error);
			showEmptyState('error', 'Không thể tải danh sách khóa học', error.message);
		});
}

// Load teachers for dropdown
function loadTeachers() {
	console.log('🔄 Loading teachers...');
	
	fetch('/api/courses/teachers')
		.then(response => {
			if (!response.ok) {
				throw new Error('Không thể tải danh sách giảng viên');
			}
			return response.json();
		})
		.then(teachers => {
			console.log('👨‍🏫 Teachers loaded:', teachers);
			
			// Store teachers in global variable
			allTeachers = teachers;
			
			// Update create/edit form dropdowns
			const teacherSelects = document.querySelectorAll('select[name="teacherEmail"]');
			const options = teachers.map(teacher => `
				<option value="${teacher.email}">${teacher.name}</option>
			`).join('');
			
			teacherSelects.forEach(select => {
				select.innerHTML = '<option value="">Chọn giảng viên</option>' + options;
			});
			
			// Update teacher filter dropdown
			const teacherFilter = document.getElementById('teacherFilter');
			if (teacherFilter) {
				const filterOptions = teachers.map(teacher => `
					<option value="${teacher.email}">${teacher.name}</option>
				`).join('');
				teacherFilter.innerHTML = '<option value="">Tất cả giảng viên</option>' + filterOptions;
			}
		})
		.catch(error => {
			console.error('Error loading teachers:', error);
			showNotification('error', 'Không thể tải danh sách giảng viên');
		});
}

function loadCourseStats() {
	console.log('🔄 Loading course stats...');
	
    fetch('/api/courses/stats')
        .then(response => {
            if (!response.ok) throw new Error('Không thể tải thống kê');
            return response.json();
        })
        .then(stats => {
			console.log('📊 Stats loaded:', stats);
			
			// Update stats with animation
			animateNumber('totalTeachers', stats.totalTeachers);
			animateNumber('totalStudents', stats.totalStudents);
		})
		.catch(error => {
			console.error('Error loading course stats:', error);
			showNotification('error', 'Không thể tải thống kê');
		});
}

// Update course statistics
function updateCourseStats(courses) {
	const totalCourses = courses.length;
	const pendingCourses = courses.filter(course => course.status === 'PENDING').length;

	// Update DOM with animation
	animateNumber('totalCourses', totalCourses);
	animateNumber('pendingCount', pendingCourses);
}

// Animate number changes
function animateNumber(elementId, targetNumber) {
	const element = document.getElementById(elementId);
	if (!element) return;
	
	const startNumber = parseInt(element.textContent) || 0;
	const increment = (targetNumber - startNumber) / 20;
	let currentNumber = startNumber;
	
	const timer = setInterval(() => {
		currentNumber += increment;
		if ((increment > 0 && currentNumber >= targetNumber) || 
			(increment < 0 && currentNumber <= targetNumber)) {
			currentNumber = targetNumber;
			clearInterval(timer);
		}
		element.textContent = Math.floor(currentNumber).toLocaleString();
	}, 50);
}

// Setup search and filter functionality
function setupSearchAndFilter() {
	const searchInput = document.getElementById('searchInput');
	const statusFilter = document.getElementById('statusFilter');
	const teacherFilter = document.getElementById('teacherFilter');
	
	if (searchInput) {
		searchInput.addEventListener('input', debounce(filterCourses, 300));
		
		// Add keyboard shortcuts
		searchInput.addEventListener('keydown', function(e) {
			if (e.key === 'Enter') {
				e.preventDefault();
				filterCourses();
			} else if (e.key === 'Escape') {
				e.preventDefault();
				clearFilters();
				this.blur();
			}
		});
	}
	
	if (statusFilter) {
		statusFilter.addEventListener('change', filterCourses);
	}
	
	if (teacherFilter) {
		teacherFilter.addEventListener('change', filterCourses);
	}
	
	// Add global keyboard shortcuts
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
		if (e.key === 'Escape' && (searchInput?.value || statusFilter?.value || teacherFilter?.value)) {
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
	const searchTerm = document.getElementById('searchInput')?.value.toLowerCase() || '';
	const statusFilter = document.getElementById('statusFilter')?.value || '';
	const teacherFilter = document.getElementById('teacherFilter')?.value || '';
	
	console.log('🔍 Filtering courses:', { searchTerm, statusFilter, teacherFilter });
	
	let filteredCourses = allCourses.filter(course => {
		// Search filter
		const matchesSearch = !searchTerm || 
			course.title?.toLowerCase().includes(searchTerm) ||
			course.description?.toLowerCase().includes(searchTerm) ||
			course.instructorName?.toLowerCase().includes(searchTerm);
		
		// Status filter
		const matchesStatus = !statusFilter || 
			course.status?.toUpperCase() === statusFilter.toUpperCase();
			
		// Teacher filter - Debug log
		const matchesTeacher = !teacherFilter || 
			course.teacherEmail === teacherFilter;
		
		// Debug log for teacher filter
		if (teacherFilter) {
			console.log(`🔍 Course "${course.title}": teacherEmail="${course.teacherEmail}", filter="${teacherFilter}", matches=${matchesTeacher}`);
		}
		
		return matchesSearch && matchesStatus && matchesTeacher;
	});
	
	console.log(`📊 Filtered ${filteredCourses.length} out of ${allCourses.length} courses`);
	
	renderFilteredCourses(filteredCourses);
	updateFilterInfo(filteredCourses.length, searchTerm, statusFilter, teacherFilter);
}

function renderFilteredCourses(courses) {
	const courseList = document.getElementById('courseList');
	if (!courseList) {
		console.error('courseList element not found');
		return;
	}

	console.log('🎨 Rendering', courses.length, 'courses');
	
	// Add fade-in animation
	courseList.classList.add('fade-in');
	
	if (courses.length === 0) {
		showEmptyState('search', 'Không tìm thấy khóa học nào', 'Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc');
		return;
	}
	
	courseList.innerHTML = courses.map((course, index) => {
		console.log(`📚 Rendering course ${index + 1}:`, course.title, 'ID:', course.id);
		
		return `
		<div class="course-card" data-course-id="${course.id}">
			<div class="course-card-header">
				<span class="course-status ${course.status?.toLowerCase()}">${getStatusText(course.status)}</span>
				<h3 class="course-title">${highlightSearchTerm(course.title || 'Không có tiêu đề')}</h3>
				<div class="course-instructor">
					<i class="fas fa-user"></i>
					${highlightSearchTerm(course.instructorName || 'Chưa có giảng viên')}
				</div>
				<div class="course-category" style="margin-top: 8px; font-size: 12px; color: #007bff;">
					${course.skillCategory ? course.skillCategory : ''}
					${course.skillCategory && course.jobRole ? ' | ' : ''}
					${course.jobRole ? course.jobRole : ''}
					${!course.skillCategory && !course.jobRole ? 'General' : ''}
				</div>
			</div>
			<div class="course-card-body">
				<p class="course-description">
					${highlightSearchTerm(course.description ? course.description.substring(0, 120) + '...' : 'Chưa có mô tả')}
				</p>
				<div class="course-stats">
					<div class="course-stat">
						<span class="course-stat-number">${course.enrolledUserIds ? course.enrolledUserIds.length : 0}</span>
						<span class="course-stat-label">Học viên</span>
					</div>
					<div class="course-stat">
						<span class="course-stat-number">${course.lessons ? course.lessons.length : 0}</span>
						<span class="course-stat-label">Bài học</span>
					</div>
					<div class="course-stat">
						<span class="course-stat-number">${course.duration || 0}h</span>
						<span class="course-stat-label">Thời lượng</span>
					</div>
				</div>
				<div class="course-actions">
					<button class="btn btn-info btn-action" onclick="viewCourseDetails('${course.id}')" title="Xem chi tiết">
						<i class="fas fa-eye"></i>
						<span>Chi tiết</span>
					</button>
					<button class="btn btn-success btn-action" onclick="goToLessons('${course.id}')" title="Quản lý bài học">
						<i class="fas fa-list"></i>
						<span>Bài học</span>
					</button>
					<button class="btn btn-primary btn-action" onclick="editCourse('${course.id}')" title="Chỉnh sửa">
						<i class="fas fa-edit"></i>
						<span>Sửa</span>
					</button>
					${course.status === 'PENDING' ? `
						<button class="btn btn-success btn-action" onclick="approveCourse('${course.id}')" title="Phê duyệt">
							<i class="fas fa-check"></i>
							<span>Duyệt</span>
						</button>
						<button class="btn btn-warning btn-action" onclick="rejectCourse('${course.id}')" title="Từ chối">
							<i class="fas fa-times"></i>
							<span>Từ chối</span>
						</button>
					` : ''}
					<button class="btn btn-danger btn-action delete-btn" data-course-id="${course.id}" onclick="deleteCourse('${course.id}')" title="Xóa">
						<i class="fas fa-trash"></i>
						<span>Xóa</span>
					</button>
				</div>
			</div>
		</div>
	`;
	}).join('');
	
	// Add event listeners after rendering (fallback method)
	setTimeout(() => {
		const deleteButtons = courseList.querySelectorAll('.delete-btn');
		console.log('🔘 Found', deleteButtons.length, 'delete buttons after rendering');
		
		deleteButtons.forEach((button, index) => {
			const courseId = button.getAttribute('data-course-id');
			console.log(`🔘 Delete button ${index + 1} - Course ID:`, courseId);
			
			// Add additional event listener as fallback
			button.addEventListener('click', function(e) {
				e.preventDefault();
				e.stopPropagation();
				console.log('🖱️ Delete button clicked via event listener for course:', courseId);
				deleteCourse(courseId);
			});
		});
	}, 100);
	
	console.log(' Courses rendered successfully');
}

function showEmptyState(type, title, message) {
	const courseList = document.getElementById('courseList');
	if (!courseList) return;
	
	const iconMap = {
		'search': 'fas fa-search',
		'error': 'fas fa-exclamation-triangle',
		'empty': 'fas fa-inbox'
	};
	
	const icon = iconMap[type] || iconMap['empty'];
	
	courseList.innerHTML = `
		<div class="empty-state">
			<i class="${icon} empty-state-icon"></i>
			<h3 class="empty-state-title">${title}</h3>
			<p class="empty-state-text">${message}</p>
			${type === 'search' ? `
				<button class="btn btn-primary" onclick="clearFilters()">
					<i class="fas fa-times"></i> Xóa bộ lọc
				</button>
			` : type === 'error' ? `
				<button class="btn btn-outline-primary" onclick="loadCourses()">
					<i class="fas fa-redo"></i> Thử lại
				</button>
			` : ''}
		</div>
	`;
}

function highlightSearchTerm(text) {
	const searchTerm = document.getElementById('searchInput')?.value.toLowerCase() || '';
	if (!searchTerm || !text) return text;
	
	const regex = new RegExp(`(${searchTerm})`, 'gi');
	return text.replace(regex, '<mark>$1</mark>');
}

function updateFilterInfo(count, searchTerm, statusFilter, teacherFilter) {
	const filterInfo = document.getElementById('filterInfo');
	const filterResultText = document.getElementById('filterResultText');
	const totalCoursesCount = document.getElementById('totalCoursesCount');
	
	// Update total courses count
	if (totalCoursesCount) {
		totalCoursesCount.textContent = allCourses.length;
	}
	
	if (!filterInfo || !filterResultText) return;
	
	if (searchTerm || statusFilter || teacherFilter) {
		filterInfo.classList.remove('hidden');
		
		// Build filter details
		let filterDetails = [];
		if (searchTerm) {
			filterDetails.push(`tìm kiếm: "${searchTerm}"`);
		}
		if (statusFilter) {
			filterDetails.push(`trạng thái: ${getStatusText(statusFilter)}`);
		}
		if (teacherFilter) {
			const teacher = allTeachers.find(t => t.email === teacherFilter);
			if (teacher) {
				filterDetails.push(`giảng viên: ${teacher.name}`);
			}
		}
		
		const detailsText = filterDetails.length > 0 ? ` (${filterDetails.join(', ')})` : '';
		filterResultText.textContent = `Tìm thấy ${count} khóa học${detailsText}`;
	} else {
		filterInfo.classList.add('hidden');
	}
}

function clearFilters() {
	const searchInput = document.getElementById('searchInput');
	const statusFilter = document.getElementById('statusFilter');
	const teacherFilter = document.getElementById('teacherFilter');
	
	if (searchInput) searchInput.value = '';
	if (statusFilter) statusFilter.value = '';
	if (teacherFilter) teacherFilter.value = '';
	
	// Re-render all courses
	renderFilteredCourses(allCourses);
	updateFilterInfo(allCourses.length, '', '', '');
	
	console.log('🧹 Filters cleared');
}

// Helper function to get status text
function getStatusText(status) {
	const statusMap = {
		'DRAFT': 'Bản nháp',
		'PUBLISHED': 'Đã xuất bản',
		'ARCHIVED': 'Đã lưu trữ',
		'PENDING': 'Chờ phê duyệt',
		'REJECTED': 'Bị từ chối'
	};
	return statusMap[status] || status;
}

// Helper function to get status badge class
function getStatusBadgeClass(status) {
	const statusMap = {
		'DRAFT': 'bg-secondary',
		'PUBLISHED': 'bg-success',
		'ARCHIVED': 'bg-warning',
		'PENDING': 'bg-info text-dark',
		'REJECTED': 'bg-danger'
	};
	return statusMap[status] || 'bg-secondary';
}

// Show notification
function showNotification(type, message) {
	// Create notification element
	const notification = document.createElement('div');
	notification.className = `alert alert-${type === 'error' ? 'danger' : 'success'} alert-dismissible fade show position-fixed`;
	notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
	notification.innerHTML = `
		<i class="fas fa-${type === 'error' ? 'exclamation-triangle' : 'check-circle'} me-2"></i>
		${message}
		<button type="button" class="btn-close" data-bs-dismiss="alert"></button>
	`;
	
	document.body.appendChild(notification);
	
	// Auto remove after 5 seconds
	setTimeout(() => {
		if (notification.parentNode) {
			notification.remove();
		}
	}, 5000);
}

// Navigation function
function goToLessons(courseId) {
	if (!courseId) {
		console.error('Course ID is required for navigation');
		showNotification('error', 'Lỗi: Không tìm thấy ID khóa học');
		return;
	}

	console.log('🔗 Navigating to lessons for course:', courseId);
	
	// Show loading indicator
	const button = event?.target?.closest('button');
	if (button) {
		const originalHTML = button.innerHTML;
		button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
		button.disabled = true;
		
		// Reset button after navigation
		setTimeout(() => {
			button.innerHTML = originalHTML;
			button.disabled = false;
		}, 1000);
	}

	// Store current page URL for back button
	localStorage.setItem('courseReferrer', window.location.href);
	console.log('Stored courseReferrer from admin:', window.location.href);

	// Navigate to lesson management page - Using general endpoint that supports admin access
	window.location.href = `/course/${courseId}/lessons`;
}

// Create new course
function createCourse(event) {
	if (event) event.preventDefault();
	
	const form = document.getElementById('createCourseForm');
	
	// Validate form
	if (!form.checkValidity()) {
		form.classList.add('was-validated');
		return;
	}

	const formData = new FormData(form);
	
	// Use uploaded image URL if available, otherwise use URL input
	const imageUrl = uploadedImageUrl || formData.get('image');
	
	const courseData = {
		title: formData.get('title'),
		description: formData.get('description'),
		duration: parseInt(formData.get('duration')),
		teacherEmail: formData.get('teacherEmail'),
		status: formData.get('status'),
		prerequisites: formData.get('prerequisites'),
		objectives: formData.get('objectives'),
		image: imageUrl,
		level: formData.get('level'),
		jobRole: formData.get('jobRole') || null,
		skillCategory: formData.get('skillCategory') || null
	};

	fetch('/api/courses', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(courseData)
	})
	.then(res => {
		if (!res.ok) throw new Error('Không thể tạo khóa học');
		return res.json();
	})
	.then(() => {
		const modal = bootstrap.Modal.getInstance(document.getElementById('createCourseModal'));
		modal.hide();
		form.reset();
		form.classList.remove('was-validated');
		
		// Clear uploaded image
		uploadedImageUrl = null;
		clearImagePreview();
		
		// Reload courses to update the display
		loadCourses();
		loadCourseStats();
		
		// Show success message
		showNotification('success', 'Tạo khóa học thành công!');
	})
	.catch(err => {
		console.error('Error creating course:', err);
		showNotification('error', 'Tạo khóa học thất bại: ' + err.message);
	});
}

// Edit course
function editCourse(courseId) {
	fetch(`/api/courses/${courseId}`)
		.then(response => {
			if (!response.ok) {
				return response.text().then(text => {
					throw new Error(text || 'Không thể tải thông tin khóa học');
				});
			}
			return response.json();
		})
		.then(course => {
			const form = document.getElementById('editCourseForm');
			
			// Populate form fields using name attributes
			form.querySelector('input[name="courseId"]').value = course.id;
			form.querySelector('input[name="title"]').value = course.title || '';
			form.querySelector('textarea[name="description"]').value = course.description || '';
			form.querySelector('select[name="teacherEmail"]').value = course.teacherEmail || '';
			form.querySelector('input[name="duration"]').value = course.duration || '';
			form.querySelector('select[name="status"]').value = course.status || '';
			form.querySelector('textarea[name="prerequisites"]').value = course.prerequisites || '';
			form.querySelector('textarea[name="objectives"]').value = course.objectives || '';
			form.querySelector('input[name="image"]').value = course.image || '';
			form.querySelector('select[name="level"]').value = course.level || '';
			form.querySelector('select[name="jobRole"]').value = course.jobRole || '';
			form.querySelector('select[name="skillCategory"]').value = course.skillCategory || '';

			// Display current course image if exists
			const editImagePreview = document.getElementById('editImagePreview');
			const editPreviewImg = document.getElementById('editPreviewImg');
			
			if (course.image && course.image.trim() !== '') {
				editPreviewImg.src = course.image;
				editImagePreview.style.display = 'block';
			} else {
				editImagePreview.style.display = 'none';
			}

			// Show the modal
			const modal = new bootstrap.Modal(document.getElementById('editCourseModal'));
			modal.show();
		})
		.catch(error => {
			console.error('Error loading course:', error);
			showNotification('error', error.message || 'Không thể tải thông tin khóa học');
		});
}

// Update course
function updateCourse(event) {
	if (event) event.preventDefault();
	
	const form = document.getElementById('editCourseForm');
	const formData = new FormData(form);
	const courseId = formData.get('courseId');
	
	// Use uploaded image URL if available, otherwise use manual URL input
	const imageUrl = editUploadedImageUrl || formData.get('image') || '';
	
	const courseData = {
		title: formData.get('title'),
		description: formData.get('description'),
		image: imageUrl,
		level: formData.get('level'),
		teacherEmail: formData.get('teacherEmail'),
		duration: parseInt(formData.get('duration')),
		status: formData.get('status'),
		prerequisites: formData.get('prerequisites'),
		objectives: formData.get('objectives'),
		jobRole: formData.get('jobRole') || null,
		skillCategory: formData.get('skillCategory') || null
	};

	fetch(`/api/courses/${courseId}`, {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(courseData)
	})
	.then(response => {
		if (!response.ok) throw new Error('Không thể cập nhật khóa học');
		return response.json();
	})
	.then(() => {
		const modal = bootstrap.Modal.getInstance(document.getElementById('editCourseModal'));
		modal.hide();
		
		// Clear uploaded image URL for edit modal
		editUploadedImageUrl = null;
		
		// Reload courses to update the display
		loadCourses();
		loadCourseStats();
		
		showNotification('success', 'Cập nhật khóa học thành công!');
	})
	.catch(error => {
		console.error('Error updating course:', error);
		showNotification('error', 'Không thể cập nhật khóa học: ' + error.message);
	});
}

// View course details
function viewCourseDetails(courseId) {
	fetch(`/api/courses/${courseId}`)
		.then(response => response.json())
		.then(course => {
			const modal = document.getElementById('courseDetailsModal');
			modal.querySelector('.modal-title').innerHTML = `
				<i class="fas fa-eye me-2"></i>
				${course.title}
			`;
			
			const detailsContent = modal.querySelector('.modal-body');
			detailsContent.innerHTML = `
				<div class="course-details">
					<div class="row mb-4">
						<div class="col-md-6">
							<div class="card h-100">
								<div class="card-body">
									<h5 class="card-title">
										<i class="fas fa-info-circle text-primary me-2"></i>
										Thông tin cơ bản
									</h5>
									<ul class="list-unstyled">
										<li class="mb-2">
											<i class="fas fa-user text-primary me-2"></i>
											<strong>Giảng viên:</strong> ${course.instructorName}
										</li>
										<li class="mb-2">
											<i class="fas fa-clock text-primary me-2"></i>
											<strong>Thời lượng:</strong> ${course.duration || 0} giờ
										</li>
										<li class="mb-2">
											<i class="fas fa-tag text-primary me-2"></i>
											<strong>Trạng thái:</strong> 
											<span class="badge ${getStatusBadgeClass(course.status)}">
												${getStatusText(course.status)}
											</span>
										</li>
										<li class="mb-2">
											<i class="fas fa-users text-primary me-2"></i>
											<strong>Số học viên:</strong> ${course.enrolledUserIds ? course.enrolledUserIds.length : 0}
										</li>
										<li class="mb-2">
											<i class="fas fa-book text-primary me-2"></i>
											<strong>Số bài học:</strong> ${course.lessons ? course.lessons.length : 0}
										</li>
									</ul>
								</div>
							</div>
						</div>
						<div class="col-md-6">
							<div class="card h-100">
								<div class="card-body">
									<h5 class="card-title">
										<i class="fas fa-align-left text-primary me-2"></i>
										Mô tả
									</h5>
									<p class="card-text">${course.description || 'Chưa có mô tả'}</p>
								</div>
							</div>
						</div>
					</div>
					<div class="row mb-4">
						<div class="col-md-6">
							<div class="card h-100">
								<div class="card-body">
									<h5 class="card-title">
										<i class="fas fa-list-check text-primary me-2"></i>
										Yêu cầu đầu vào
									</h5>
									<p class="card-text">${course.prerequisites || 'Không có yêu cầu đầu vào'}</p>
								</div>
							</div>
						</div>
						<div class="col-md-6">
							<div class="card h-100">
								<div class="card-body">
									<h5 class="card-title">
										<i class="fas fa-target text-primary me-2"></i>
										Mục tiêu khóa học
									</h5>
									<p class="card-text">${course.objectives || 'Chưa có mục tiêu'}</p>
								</div>
							</div>
						</div>
					</div>
					${course.lessons && course.lessons.length > 0 ? `
						<div class="card">
							<div class="card-body">
								<h5 class="card-title">
									<i class="fas fa-list text-primary me-2"></i>
									Danh sách bài học
								</h5>
								<div class="table-responsive">
									<table class="table table-hover">
										<thead class="table-light">
											<tr>
												<th>STT</th>
												<th>Tên bài học</th>
												<th>Thời lượng</th>
												<th>Trạng thái</th>
											</tr>
										</thead>
										<tbody>
											${course.lessons.map((lesson, index) => `
												<tr>
													<td>${index + 1}</td>
													<td>${lesson.title}</td>
													<td>${lesson.duration || 0} phút</td>
													<td>
														<span class="badge ${lesson.isPublished ? 'bg-success' : 'bg-warning'}">
															${lesson.isPublished ? 'Đã xuất bản' : 'Bản nháp'}
														</span>
													</td>
												</tr>
											`).join('')}
										</tbody>
									</table>
								</div>
							</div>
						</div>
					` : ''}
				</div>
			`;
			
			new bootstrap.Modal(modal).show();
		})
		.catch(error => {
			console.error('Error loading course details:', error);
			showNotification('error', 'Không thể tải chi tiết khóa học');
		});
}

// Delete course
function deleteCourse(courseId) {
	console.log('🗑️ deleteCourse called with ID:', courseId);
	
	// Validate courseId
	if (!courseId || courseId.trim() === '') {
		console.error('Invalid courseId:', courseId);
		showNotification('error', 'Lỗi: ID khóa học không hợp lệ');
		return;
	}

	// Create custom confirmation modal
	showDeleteConfirmModal(courseId);
}

// Custom modal for delete confirmation
function showDeleteConfirmModal(courseId) {
	// Remove existing modal if any
	const existingModal = document.getElementById('deleteConfirmModal');
	if (existingModal) {
		existingModal.remove();
	}

	// Create modal HTML
	const modalHTML = `
		<div id="deleteConfirmModal" class="modal-overlay" style="
			position: fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: rgba(0, 0, 0, 0.5);
			display: flex;
			justify-content: center;
			align-items: center;
			z-index: 10000;
			animation: fadeIn 0.3s ease;
		">
			<div class="modal-content" style="
				background: white;
				padding: 30px;
				border-radius: 12px;
				max-width: 500px;
				width: 90%;
				box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
				text-align: center;
				animation: slideIn 0.3s ease;
			">
				<div style="color: #dc3545; font-size: 48px; margin-bottom: 20px;">
					⚠️
				</div>
				<h3 style="color: #dc3545; margin-bottom: 20px; font-size: 24px;">
					CẢNH BÁO: Xóa khóa học vĩnh viễn!
				</h3>
				<div style="text-align: left; margin-bottom: 25px; background: #f8f9fa; padding: 20px; border-radius: 8px;">
					<p style="margin-bottom: 15px; font-weight: bold;">Hành động này sẽ XÓA VĨNH VIỄN:</p>
					<ul style="margin: 0; padding-left: 20px; line-height: 1.6;">
						<li>• Khóa học và tất cả thông tin</li>
						<li>• Tất cả bài học trong khóa học</li>
						<li>• Toàn bộ tiến độ học tập của học viên</li>
						<li>• Tất cả bài kiểm tra và câu hỏi</li>
						<li>• Tất cả kết quả thi và bài làm</li>
						<li>• Tất cả dữ liệu liên quan khác</li>
					</ul>
					<p style="margin-top: 15px; color: #dc3545; font-weight: bold;">
						KHÔNG THỂ HOÀN TÁC sau khi xóa!
					</p>
				</div>
				<div style="display: flex; gap: 15px; justify-content: center;">
					<button id="cancelDelete" style="
						background: #6c757d;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
					">
						Hủy bỏ
					</button>
					<button id="confirmDelete" style="
						background: #dc3545;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
					">
						🗑️ Xác nhận xóa
					</button>
				</div>
			</div>
		</div>
		<style>
			@keyframes fadeIn {
				from { opacity: 0; }
				to { opacity: 1; }
			}
			@keyframes slideIn {
				from { transform: translateY(-50px); opacity: 0; }
				to { transform: translateY(0); opacity: 1; }
			}
			#cancelDelete:hover {
				background: #5a6268 !important;
				transform: translateY(-2px);
			}
			#confirmDelete:hover {
				background: #c82333 !important;
				transform: translateY(-2px);
			}
		</style>
	`;

	// Add modal to page
	document.body.insertAdjacentHTML('beforeend', modalHTML);

	// Add event listeners
	document.getElementById('cancelDelete').addEventListener('click', () => {
		console.log('🚫 User cancelled deletion');
		document.getElementById('deleteConfirmModal').remove();
	});

	document.getElementById('confirmDelete').addEventListener('click', () => {
		document.getElementById('deleteConfirmModal').remove();
		showSecondConfirmModal(courseId);
	});

	// Close on outside click
	document.getElementById('deleteConfirmModal').addEventListener('click', (e) => {
		if (e.target.id === 'deleteConfirmModal') {
			console.log('🚫 User cancelled deletion (outside click)');
			document.getElementById('deleteConfirmModal').remove();
		}
	});

	// Close on Escape key
	document.addEventListener('keydown', function escapeHandler(e) {
		if (e.key === 'Escape') {
			const modal = document.getElementById('deleteConfirmModal');
			if (modal) {
				console.log('🚫 User cancelled deletion (Escape key)');
				modal.remove();
				document.removeEventListener('keydown', escapeHandler);
			}
		}
	});
}

// Second confirmation modal
function showSecondConfirmModal(courseId) {
	const modalHTML = `
		<div id="secondConfirmModal" class="modal-overlay" style="
			position: fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: rgba(0, 0, 0, 0.7);
			display: flex;
			justify-content: center;
			align-items: center;
			z-index: 10001;
			animation: fadeIn 0.3s ease;
		">
			<div class="modal-content" style="
				background: white;
				padding: 30px;
				border-radius: 12px;
				max-width: 400px;
				width: 90%;
				box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
				text-align: center;
				animation: slideIn 0.3s ease;
				border: 3px solid #dc3545;
			">
				<div style="color: #dc3545; font-size: 60px; margin-bottom: 20px;">
					🔴
				</div>
				<h3 style="color: #dc3545; margin-bottom: 20px; font-size: 22px;">
					XÁC NHẬN LẦN CUỐI
				</h3>
				<p style="margin-bottom: 25px; font-size: 18px; line-height: 1.5;">
					Bạn <strong>THỰC SỰ</strong> muốn xóa khóa học này và <strong>TẤT CẢ</strong> dữ liệu liên quan?
				</p>
				<p style="color: #dc3545; font-weight: bold; margin-bottom: 25px; font-size: 16px;">
					Đây là hành động <strong>KHÔNG THỂ HOÀN TÁC!</strong>
				</p>
				<div style="display: flex; gap: 15px; justify-content: center;">
					<button id="finalCancel" style="
						background: #28a745;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
					">
						 Giữ lại khóa học
					</button>
					<button id="finalConfirm" style="
						background: #dc3545;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
						animation: pulse 2s infinite;
					">
						💀 XÓA VĨNH VIỄN
					</button>
				</div>
			</div>
		</div>
		<style>
			@keyframes pulse {
				0% { transform: scale(1); }
				50% { transform: scale(1.05); }
				100% { transform: scale(1); }
			}
			#finalCancel:hover {
				background: #218838 !important;
				transform: translateY(-2px);
			}
			#finalConfirm:hover {
				background: #c82333 !important;
				transform: translateY(-2px);
			}
		</style>
	`;

	document.body.insertAdjacentHTML('beforeend', modalHTML);

	document.getElementById('finalCancel').addEventListener('click', () => {
		console.log('🚫 User cancelled final confirmation');
		document.getElementById('secondConfirmModal').remove();
	});

	document.getElementById('finalConfirm').addEventListener('click', () => {
		console.log(' User confirmed deletion, proceeding...');
		document.getElementById('secondConfirmModal').remove();
		executeDelete(courseId);
	});

	// Close on outside click
	document.getElementById('secondConfirmModal').addEventListener('click', (e) => {
		if (e.target.id === 'secondConfirmModal') {
			console.log('🚫 User cancelled final confirmation (outside click)');
			document.getElementById('secondConfirmModal').remove();
		}
	});
}

// Execute the actual delete operation
function executeDelete(courseId) {
	// Show loading state
	const deleteButton = event?.target?.closest('button');
	let originalButtonContent = '';
	
	if (deleteButton) {
		originalButtonContent = deleteButton.innerHTML;
		deleteButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xóa...';
		deleteButton.disabled = true;
		console.log('🔄 Button state updated to loading');
	}

	// Show loading notification
	showNotification('info', '🗑️ Đang xóa khóa học và tất cả dữ liệu liên quan...');

	console.log('🗑️ Starting cascading delete for course:', courseId);
	console.log('📡 Making DELETE request to:', `/api/courses/${courseId}`);

	// Get JWT token from localStorage or sessionStorage
	const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
	console.log('🔑 JWT Token found:', token ? 'Yes' : 'No');

	const headers = {
		'Content-Type': 'application/json',
		'X-Requested-With': 'XMLHttpRequest'
	};

	// Add Authorization header if token exists
	if (token) {
		headers['Authorization'] = `Bearer ${token}`;
		console.log('🔑 Authorization header added');
	}

		fetch(`/api/courses/${courseId}`, {
		method: 'DELETE',
		headers: headers,
		credentials: 'same-origin'
		})
		.then(response => {
		console.log('📡 Response status:', response.status);
		console.log('📡 Response headers:', response.headers);
		
			if (!response.ok) {
				return response.text().then(text => {
				console.error('Server error response:', text);
				throw new Error(text || `HTTP ${response.status}: Không thể xóa khóa học`);
			});
		}
		
		console.log(' Course and all related data deleted successfully');
		
		// Success notification
		showNotification('success', ' Đã xóa khóa học và tất cả dữ liệu liên quan thành công!');
		
		// Reload data to reflect changes
		console.log('🔄 Reloading courses and stats...');
			loadCourses();
			loadCourseStats();
		
		// Additional success message
		setTimeout(() => {
			showNotification('info', '📊 Dữ liệu đã được cập nhật. Khóa học không còn hiển thị ở bất kỳ role nào.');
		}, 2000);
		
		})
		.catch(error => {
		console.error('Error deleting course:', error);
		console.error('Full error object:', error);
		
		// Check if it's an authentication error and try fallback
		if (error.message.includes('401') || error.message.includes('403') || 
			error.message.includes('Unauthorized') || error.message.includes('Forbidden')) {
			console.log('🔄 Authentication error detected, trying fallback method...');
			showNotification('info', '🔄 Thử phương thức xóa khác...');
			
			// Try fallback method
			deleteCourseFallback(courseId);
			return;
		}
		
		// Detailed error notification
		let errorMessage = 'Không thể xóa khóa học';
		if (error.message.includes('403') || error.message.includes('Forbidden')) {
			errorMessage = 'Bạn không có quyền xóa khóa học. Chỉ Admin mới có thể thực hiện hành động này.';
		} else if (error.message.includes('404')) {
			errorMessage = 'Khóa học không tồn tại hoặc đã được xóa.';
		} else if (error.message.includes('500')) {
			errorMessage = 'Lỗi server khi xóa khóa học. Vui lòng thử lại sau.';
		} else if (error.message.includes('NetworkError') || error.message.includes('Failed to fetch')) {
			errorMessage = 'Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet và thử lại.';
		} else if (error.message) {
			errorMessage = error.message;
		}
		
		showNotification('error', '' + errorMessage);
	})
	.finally(() => {
		console.log('🔄 Restoring button state...');
		// Restore button state
		if (deleteButton && originalButtonContent) {
			deleteButton.innerHTML = originalButtonContent;
			deleteButton.disabled = false;
			console.log(' Button state restored');
		}
	});
}

// Approve course
function approveCourse(courseId) {
	showApproveConfirmModal(courseId);
}

// Custom modal for approve confirmation
function showApproveConfirmModal(courseId) {
	// Remove existing modal if any
	const existingModal = document.getElementById('approveConfirmModal');
	if (existingModal) {
		existingModal.remove();
	}

	// Create modal HTML
	const modalHTML = `
		<div id="approveConfirmModal" class="modal-overlay" style="
			position: fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: rgba(0, 0, 0, 0.5);
			display: flex;
			justify-content: center;
			align-items: center;
			z-index: 10000;
			animation: fadeIn 0.3s ease;
		">
			<div class="modal-content" style="
				background: white;
				padding: 30px;
				border-radius: 12px;
				max-width: 400px;
				width: 90%;
				box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
				text-align: center;
				animation: slideIn 0.3s ease;
				border: 3px solid #28a745;
			">
				<div style="color: #28a745; font-size: 48px; margin-bottom: 20px;">
					
						</div>
				<h3 style="color: #28a745; margin-bottom: 20px; font-size: 22px;">
					Phê duyệt khóa học
				</h3>
				<p style="margin-bottom: 25px; font-size: 16px; line-height: 1.5;">
					Bạn có chắc chắn muốn <strong>phê duyệt</strong> khóa học này?
				</p>
				<p style="color: #6c757d; margin-bottom: 25px; font-size: 14px;">
					Khóa học sẽ được xuất bản và hiển thị cho học viên.
				</p>
				<div style="display: flex; gap: 15px; justify-content: center;">
					<button id="cancelApprove" style="
						background: #6c757d;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
					">
						Hủy bỏ
								</button>
					<button id="confirmApprove" style="
						background: #28a745;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
					">
						 Phê duyệt
								</button>
						</div>
					</div>
				</div>
	`;

	// Add modal to page
	document.body.insertAdjacentHTML('beforeend', modalHTML);

	// Add event listeners
	document.getElementById('cancelApprove').addEventListener('click', () => {
		console.log('🚫 User cancelled approval');
		document.getElementById('approveConfirmModal').remove();
	});

	document.getElementById('confirmApprove').addEventListener('click', () => {
		console.log(' User confirmed approval');
		document.getElementById('approveConfirmModal').remove();
		executeApprove(courseId);
	});

	// Close on outside click
	document.getElementById('approveConfirmModal').addEventListener('click', (e) => {
		if (e.target.id === 'approveConfirmModal') {
			console.log('🚫 User cancelled approval (outside click)');
			document.getElementById('approveConfirmModal').remove();
		}
	});

	// Close on Escape key
	document.addEventListener('keydown', function escapeHandler(e) {
		if (e.key === 'Escape') {
			const modal = document.getElementById('approveConfirmModal');
			if (modal) {
				console.log('🚫 User cancelled approval (Escape key)');
				modal.remove();
				document.removeEventListener('keydown', escapeHandler);
			}
		}
	});
}

// Execute the actual approve operation
function executeApprove(courseId) {
		fetch(`/api/courses/${courseId}/approve`, {
			method: 'PUT'
		})
	.then(response => {
		if (!response.ok) throw new Error('Không thể phê duyệt khóa học');
		return response.json();
		})
		.then(() => {
		showNotification('success', ' Phê duyệt khóa học thành công!');
			loadCourses();
		loadCourseStats();
	})
	.catch(error => {
		console.error('Error approving course:', error);
		showNotification('error', 'Không thể phê duyệt khóa học: ' + error.message);
	});
}

// Reject course
function rejectCourse(courseId) {
	showRejectConfirmModal(courseId);
}

// Custom modal for reject confirmation
function showRejectConfirmModal(courseId) {
	// Remove existing modal if any
	const existingModal = document.getElementById('rejectConfirmModal');
	if (existingModal) {
		existingModal.remove();
	}

	// Create modal HTML
	const modalHTML = `
		<div id="rejectConfirmModal" class="modal-overlay" style="
			position: fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: rgba(0, 0, 0, 0.5);
			display: flex;
			justify-content: center;
			align-items: center;
			z-index: 10000;
			animation: fadeIn 0.3s ease;
		">
			<div class="modal-content" style="
				background: white;
				padding: 30px;
				border-radius: 12px;
				max-width: 400px;
				width: 90%;
				box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
				text-align: center;
				animation: slideIn 0.3s ease;
				border: 3px solid #ffc107;
			">
				<div style="color: #ffc107; font-size: 48px; margin-bottom: 20px;">
					⚠️
				</div>
				<h3 style="color: #ffc107; margin-bottom: 20px; font-size: 22px;">
					Từ chối khóa học
				</h3>
				<p style="margin-bottom: 25px; font-size: 16px; line-height: 1.5;">
					Bạn có chắc chắn muốn <strong>từ chối</strong> khóa học này?
				</p>
				<p style="color: #6c757d; margin-bottom: 25px; font-size: 14px;">
					Khóa học sẽ được đánh dấu là bị từ chối và không hiển thị cho học viên.
				</p>
				<div style="display: flex; gap: 15px; justify-content: center;">
					<button id="cancelReject" style="
						background: #6c757d;
						color: white;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
					">
						Hủy bỏ
					</button>
					<button id="confirmReject" style="
						background: #ffc107;
						color: #212529;
						border: none;
						padding: 12px 24px;
						border-radius: 6px;
						cursor: pointer;
						font-size: 16px;
						transition: all 0.3s ease;
						font-weight: bold;
					">
						⚠️ Từ chối
					</button>
				</div>
			</div>
		</div>
	`;

	// Add modal to page
	document.body.insertAdjacentHTML('beforeend', modalHTML);

	// Add event listeners
	document.getElementById('cancelReject').addEventListener('click', () => {
		console.log('🚫 User cancelled rejection');
		document.getElementById('rejectConfirmModal').remove();
	});

	document.getElementById('confirmReject').addEventListener('click', () => {
		console.log('⚠️ User confirmed rejection');
		document.getElementById('rejectConfirmModal').remove();
		executeReject(courseId);
	});

	// Close on outside click
	document.getElementById('rejectConfirmModal').addEventListener('click', (e) => {
		if (e.target.id === 'rejectConfirmModal') {
			console.log('🚫 User cancelled rejection (outside click)');
			document.getElementById('rejectConfirmModal').remove();
		}
	});

	// Close on Escape key
	document.addEventListener('keydown', function escapeHandler(e) {
		if (e.key === 'Escape') {
			const modal = document.getElementById('rejectConfirmModal');
			if (modal) {
				console.log('🚫 User cancelled rejection (Escape key)');
				modal.remove();
				document.removeEventListener('keydown', escapeHandler);
			}
		}
	});
}

// Execute the actual reject operation
function executeReject(courseId) {
		fetch(`/api/courses/${courseId}/reject`, {
			method: 'PUT'
		})
	.then(response => {
		if (!response.ok) throw new Error('Không thể từ chối khóa học');
		return response.json();
		})
		.then(() => {
		showNotification('success', '⚠️ Từ chối khóa học thành công!');
			loadCourses();
		loadCourseStats();
	})
	.catch(error => {
		console.error('Error rejecting course:', error);
		showNotification('error', 'Không thể từ chối khóa học: ' + error.message);
	});
}

// Test function to debug delete functionality
function testDeleteFunction() {
	console.log('🧪 Testing delete function...');
	
	// Check if deleteCourse function exists
	if (typeof deleteCourse === 'function') {
		console.log(' deleteCourse function exists');
	} else {
		console.error('deleteCourse function not found');
	}
	
	// Check if DOM elements exist
	const courseList = document.getElementById('courseList');
	if (courseList) {
		console.log(' courseList element found');
		
		const deleteButtons = courseList.querySelectorAll('.delete-btn');
		console.log('🔘 Found', deleteButtons.length, 'delete buttons');
		
		deleteButtons.forEach((button, index) => {
			const courseId = button.getAttribute('data-course-id');
			console.log(`Button ${index + 1}: Course ID = ${courseId}`);
		});
	} else {
		console.error('courseList element not found');
	}
	
	// Check global variables
	console.log('📊 allCourses length:', allCourses.length);
	console.log('👥 allTeachers length:', allTeachers.length);
	
	// Test API endpoint
	console.log('🔗 Testing API connection...');
    fetch('/api/courses')
		.then(response => {
			console.log('📡 API Response status:', response.status);
			return response.json();
		})
        .then(courses => {
			console.log('📚 API returned', courses.length, 'courses');
		})
		.catch(error => {
			console.error('API Error:', error);
		});
}

// Add to window for console access
window.testDeleteFunction = testDeleteFunction;
window.deleteCourse = deleteCourse;

console.log('🎉 Admin script loaded successfully');
console.log('💡 Type testDeleteFunction() in console to debug');
console.log('💡 Type deleteCourse("courseId") in console to test delete directly');

// Alternative delete method using form submission
function deleteCourseFallback(courseId) {
	console.log('🔄 Using fallback delete method for course:', courseId);
	
	// Create a hidden form for DELETE request
	const form = document.createElement('form');
	form.method = 'POST';
	form.action = `/api/courses/${courseId}`;
	form.style.display = 'none';
	
	// Add method override for DELETE
	const methodInput = document.createElement('input');
	methodInput.type = 'hidden';
	methodInput.name = '_method';
	methodInput.value = 'DELETE';
	form.appendChild(methodInput);
	
	document.body.appendChild(form);
	
	// Submit form
	form.submit();
	
	// Clean up
	setTimeout(() => {
		document.body.removeChild(form);
	}, 100);
}

// Image upload handling
let uploadedImageUrl = null;
let editUploadedImageUrl = null;

function handleImageUpload(event) {
	const file = event.target.files[0];
	if (!file) return;

	// Validate file type
	if (!file.type.startsWith('image/')) {
		alert('Vui lòng chọn file ảnh hợp lệ!');
		return;
	}

	// Validate file size (5MB)
	if (file.size > 5 * 1024 * 1024) {
		alert('Kích thước file không được vượt quá 5MB!');
		return;
	}

	// Show preview
	const reader = new FileReader();
	reader.onload = function(e) {
		document.getElementById('previewImg').src = e.target.result;
		document.getElementById('imagePreview').style.display = 'block';
	};
	reader.readAsDataURL(file);

	// Upload file
	uploadImage(file, 'create');
}

function handleEditImageUpload(event) {
	const file = event.target.files[0];
	if (!file) return;

	// Validate file type
	if (!file.type.startsWith('image/')) {
		alert('Vui lòng chọn file ảnh hợp lệ!');
		return;
	}

	// Validate file size (5MB)
	if (file.size > 5 * 1024 * 1024) {
		alert('Kích thước file không được vượt quá 5MB!');
		return;
	}

	// Show preview for edit modal
	const reader = new FileReader();
	reader.onload = function(e) {
		document.getElementById('editPreviewImg').src = e.target.result;
		document.getElementById('editImagePreview').style.display = 'block';
	};
	reader.readAsDataURL(file);

	// Upload file
	uploadImage(file, 'edit');
}

function uploadImage(file, modalType) {
	const formData = new FormData();
	formData.append('file', file);

	fetch('/api/upload/course-image', {
		method: 'POST',
		body: formData
	})
	.then(response => response.json())
	.then(data => {
		if (data.success) {
			if (modalType === 'create') {
				uploadedImageUrl = data.fileUrl;
				console.log('Create modal image uploaded successfully:', uploadedImageUrl);
			} else {
				editUploadedImageUrl = data.fileUrl;
				console.log('Edit modal image uploaded successfully:', editUploadedImageUrl);
			}
		} else {
			alert('Lỗi upload ảnh: ' + data.message);
			if (modalType === 'create') {
				clearImagePreview();
			} else {
				clearEditImagePreview();
			}
		}
	})
	.catch(error => {
		console.error('Error uploading image:', error);
		alert('Lỗi upload ảnh!');
		if (modalType === 'create') {
			clearImagePreview();
		} else {
			clearEditImagePreview();
            }
        });
}

function clearImagePreview() {
	document.getElementById('imagePreview').style.display = 'none';
	document.getElementById('previewImg').src = '';
	document.getElementById('courseImageUpload').value = '';
	uploadedImageUrl = null;
}

function clearEditImagePreview() {
	document.getElementById('editImagePreview').style.display = 'none';
	document.getElementById('editPreviewImg').src = '';
	document.getElementById('editCourseImageUpload').value = '';
	editUploadedImageUrl = null;
}

// Reset form when modal is closed
function resetCreateCourseForm() {
    document.getElementById('createCourseForm').reset();
    clearImagePreview();
    document.getElementById('createCourseForm').classList.remove('was-validated');
}

function resetEditCourseForm() {
    document.getElementById('editCourseForm').reset();
    clearEditImagePreview();
    document.getElementById('editCourseForm').classList.remove('was-validated');
}
