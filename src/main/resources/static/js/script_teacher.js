// Sidebar active
document.addEventListener('DOMContentLoaded', function () {
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
});

// Toggle sidebar
const menuBar = document.querySelector('#content nav .bx.bx-menu');
const sidebar = document.getElementById('sidebar');
menuBar.addEventListener('click', () => sidebar.classList.toggle('hide'));

// Search responsive
const searchButton = document.querySelector('#content nav form .form-input button');
const searchButtonIcon = document.querySelector('#content nav form .form-input button .bx');
const searchForm = document.querySelector('#content nav form');

searchButton.addEventListener('click', function (e) {
	if (window.innerWidth < 576) {
		e.preventDefault();
		searchForm.classList.toggle('show');
		searchButtonIcon.classList.toggle('bx-search');
		searchButtonIcon.classList.toggle('bx-x');
	}
});

if (window.innerWidth < 768) {
	sidebar.classList.add('hide');
} else if (window.innerWidth > 576) {
	searchButtonIcon.classList.replace('bx-x', 'bx-search');
	searchForm.classList.remove('show');
}

window.addEventListener('resize', function () {
	if (this.innerWidth > 576) {
		searchButtonIcon.classList.replace('bx-x', 'bx-search');
		searchForm.classList.remove('show');
	}
});

// DARK MODE FUNCTIONALITY
const switchMode = document.getElementById('switch-mode');

// Load dark mode preference from localStorage
const isDarkMode = localStorage.getItem('darkMode') === 'true';
if (isDarkMode) {
    document.body.classList.add('dark');
    if (switchMode) {
        switchMode.checked = true;
    }
}

// Handle dark mode toggle
if (switchMode) {
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
}

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

// Logout
function handleLogout() {
	fetch('/api/auth/logout', { method: 'POST' }).finally(() => window.location.href = "/");
}

// ============================
// Image Upload Handling
// ============================

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
		document.getElementById('editImagePreview').src = e.target.result;
		document.getElementById('editImagePreviewContainer').style.display = 'block';
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
				console.log('Create modal - Image uploaded successfully:', uploadedImageUrl);
			} else {
				editUploadedImageUrl = data.fileUrl;
				console.log('Edit modal - Image uploaded successfully:', editUploadedImageUrl);
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
	document.getElementById('editImagePreviewContainer').style.display = 'none';
	document.getElementById('editImagePreview').src = '';
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

// ============================
// Khóa học - TEACHER
// ============================


// Load khóa học của giáo viên
document.addEventListener('DOMContentLoaded', function () {
	loadTeacherCourses();
	loadCourseStats();
	setupSearchAndFilter();
	
	// Image upload handling for create modal
	const imageUpload = document.getElementById('courseImageUpload');
	if (imageUpload) {
		imageUpload.addEventListener('change', handleImageUpload);
	}
	
	// Image upload handling for edit modal
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

// Global variable to store all courses for filtering
let allCourses = [];

function setupSearchAndFilter() {
	const searchInput = document.getElementById('searchInput');
	const statusFilter = document.getElementById('statusFilter');
	
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
		
		// Add placeholder animation
		let placeholders = [
			'Tìm kiếm khóa học...',
			'Nhập tên khóa học...',
			'Tìm theo mô tả...',
			'Tìm theo giảng viên...'
		];
		let currentPlaceholder = 0;
		
		setInterval(() => {
			if (!searchInput.value && document.activeElement !== searchInput) {
				searchInput.placeholder = placeholders[currentPlaceholder];
				currentPlaceholder = (currentPlaceholder + 1) % placeholders.length;
			}
		}, 3000);
	}
	
	if (statusFilter) {
		statusFilter.addEventListener('change', filterCourses);
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
		if (e.key === 'Escape' && (searchInput?.value || statusFilter?.value)) {
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
	
	console.log('🔍 Filtering courses:', { searchTerm, statusFilter });
	
	let filteredCourses = allCourses.filter(course => {
		// Search filter
		const matchesSearch = !searchTerm || 
			course.title?.toLowerCase().includes(searchTerm) ||
			course.description?.toLowerCase().includes(searchTerm) ||
			course.instructorName?.toLowerCase().includes(searchTerm);
		
		// Status filter
		const matchesStatus = !statusFilter || 
			course.status?.toUpperCase() === statusFilter.toUpperCase();
		
		return matchesSearch && matchesStatus;
	});
	
	console.log(`📊 Filtered ${filteredCourses.length} out of ${allCourses.length} courses`);
	
	renderFilteredCourses(filteredCourses);
	updateFilterInfo(filteredCourses.length, searchTerm, statusFilter);
}

function renderFilteredCourses(courses) {
	const courseList = document.getElementById('courseList');
	if (!courseList) return;
	
	if (courses.length === 0) {
		courseList.innerHTML = `
			<tr>
				<td colspan="6" class="text-center">
					<div style="padding: 2rem; color: #6c757d;">
						<i class="fas fa-search" style="font-size: 3rem; margin-bottom: 1rem;"></i>
						<h5>Không tìm thấy khóa học nào</h5>
						<p>Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc</p>
						<button class="btn btn-outline-primary" onclick="clearFilters()">
							<i class="fas fa-times"></i> Xóa bộ lọc
						</button>
					</div>
				</td>
			</tr>
		`;
		return;
	}
	
	courseList.innerHTML = courses.map(course => {
		const studentCount = course.enrolledUserIds?.length || 0;
		const lessonCount = course.lessons?.length || 0;
		const status = course.status || 'UNKNOWN';
		
		return `
			<tr class="course-row" data-course-id="${course.id}">
				<td>
					<div class="course-title-cell" style="display: flex; align-items: center;">
						<img src="${course.image || 'https://via.placeholder.com/50'}" 
						     style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover; margin-right: 12px;">
						<div>
							<strong>${highlightSearchTerm(course.title || 'Không có tiêu đề')}</strong>
							<br>
							<small class="text-muted">${highlightSearchTerm(course.description ? course.description.substring(0, 80) + '...' : 'Không có mô tả')}</small>
							<br>
							<small class="text-primary" style="font-weight: 500;">
								${course.skillCategory ? course.skillCategory : ''}
								${course.skillCategory && course.jobRole ? ' | ' : ''}
								${course.jobRole ? course.jobRole : ''}
								${!course.skillCategory && !course.jobRole ? 'General' : ''}
							</small>
						</div>
					</div>
				</td>
				<td>
					<span class="badge ${studentCount > 0 ? 'bg-success' : 'bg-secondary'}">
						<i class="fas fa-users"></i> ${studentCount}
					</span>
				</td>
				<td>
					<span class="badge ${lessonCount > 0 ? 'bg-info' : 'bg-secondary'}">
						<i class="fas fa-book"></i> ${lessonCount}
					</span>
				</td>
				<td>${calculateAverageLessonsCompleted(course)}</td>
				<td>
					<div class="btn-group" role="group">
						<button class="btn btn-sm btn-primary" onclick="editCourse('${course.id}')" title="Chỉnh sửa">
							<i class="fas fa-edit"></i>
						</button>
						
						<button class="btn btn-sm btn-success" onclick="goToLessons('${course.id}')" title="Quản lý bài học">
							<i class="fas fa-list"></i>
						</button>
					</div>
				</td>
				<td>${renderStatus(status)}</td>
			</tr>
		`;
	}).join('');
	
	// Add hover effects
	document.querySelectorAll('.course-row').forEach(row => {
		row.addEventListener('mouseenter', function() {
			this.style.backgroundColor = '#f8f9fa';
		});
		row.addEventListener('mouseleave', function() {
			this.style.backgroundColor = '';
		});
	});
}

function highlightSearchTerm(text) {
	const searchTerm = document.getElementById('searchInput')?.value.toLowerCase() || '';
	if (!searchTerm || !text) return text;
	
	const regex = new RegExp(`(${searchTerm})`, 'gi');
	return text.replace(regex, '<mark>$1</mark>');
}

function updateFilterInfo(count, searchTerm, statusFilter) {
	const filterInfo = document.getElementById('filterInfo');
	const filterResultCount = document.getElementById('filterResultCount');
	
	if (!filterInfo || !filterResultCount) return;
	
	if (searchTerm || statusFilter) {
		filterInfo.style.display = 'block';
		filterResultCount.textContent = count;
		
		// Add detailed filter information
		let filterDetails = [];
		if (searchTerm) {
			filterDetails.push(`tìm kiếm: "${searchTerm}"`);
		}
		if (statusFilter) {
			const statusText = getStatusText(statusFilter);
			filterDetails.push(`trạng thái: ${statusText}`);
		}
		
		const detailsText = filterDetails.length > 0 ? ` (${filterDetails.join(', ')})` : '';
		filterInfo.querySelector('small').innerHTML = `
			<span id="filterResultCount">${count}</span> khóa học được tìm thấy${detailsText}
			<button class="btn btn-link btn-sm p-0 ms-2" onclick="clearFilters()">
				<i class='bx bx-x'></i> Xóa bộ lọc
			</button>
		`;
	} else {
		filterInfo.style.display = 'none';
	}
}

function clearFilters() {
	const searchInput = document.getElementById('searchInput');
	const statusFilter = document.getElementById('statusFilter');
	
	if (searchInput) searchInput.value = '';
	if (statusFilter) statusFilter.value = '';
	
	// Re-render all courses
	renderFilteredCourses(allCourses);
	updateFilterInfo(allCourses.length, '', '');
	
	console.log('🧹 Filters cleared');
}

function loadTeacherCourses() {
	const courseList = document.getElementById('courseList');
	if (!courseList) return;

	// Show loading state
	courseList.innerHTML = `
		<tr>
			<td colspan="6" class="text-center">
				<i class="fas fa-spinner fa-spin"></i> Đang tải danh sách khóa học...
			</td>
		</tr>
	`;

	fetch('/api/courses/teacher')
		.then(res => {
			if (!res.ok) {
				throw new Error(`HTTP ${res.status}: Không thể lấy danh sách khóa học của giáo viên`);
			}
			return res.json();
		})
		.then(courses => {
			console.log('📚 Teacher courses loaded:', courses);

			if (!Array.isArray(courses)) {
				throw new Error('Phản hồi không hợp lệ từ server');
			}

			// Store all courses for filtering
			allCourses = courses;

			if (courses.length === 0) {
				courseList.innerHTML = `
					<tr>
						<td colspan="6" class="text-center">
							<div style="padding: 2rem; color: #6c757d;">
								<i class="fas fa-book-open" style="font-size: 3rem; margin-bottom: 1rem;"></i>
								<h5>Chưa có khóa học nào</h5>
								<p>Bạn chưa tạo khóa học nào. Hãy tạo khóa học đầu tiên của bạn!</p>
								<button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#createCourseModal">
									<i class="fas fa-plus"></i> Tạo khóa học mới
								</button>
							</div>
						</td>
					</tr>
				`;
				return;
			}

			// Use the new render function
			renderFilteredCourses(courses);
		})
		.catch(err => {
			console.error('Lỗi khi tải khóa học của giáo viên:', err);
			
			courseList.innerHTML = `
				<tr>
					<td colspan="6" class="text-center">
						<div style="padding: 2rem; color: #dc3545;">
							<i class="fas fa-exclamation-triangle" style="font-size: 3rem; margin-bottom: 1rem;"></i>
							<h5>Không thể tải danh sách khóa học</h5>
							<p>${err.message}</p>
							<button class="btn btn-outline-primary" onclick="loadTeacherCourses()">
								<i class="fas fa-redo"></i> Thử lại
						</button>
						</div>
					</td>
				</tr>
			`;
		});
}

function renderStatus(status) {
	if (!status) {
		return '<span class="badge bg-secondary"><i class="fas fa-question"></i> Không xác định</span>';
	}

	const statusConfig = {
		'APPROVED': {
			class: 'bg-success',
			icon: 'fas fa-check-circle',
			text: 'Đã duyệt'
		},
		'PENDING': {
			class: 'bg-warning text-dark',
			icon: 'fas fa-clock',
			text: 'Chờ duyệt'
		},
		'REJECTED': {
			class: 'bg-danger',
			icon: 'fas fa-times-circle',
			text: 'Bị từ chối'
		},
		'DRAFT': {
			class: 'bg-secondary',
			icon: 'fas fa-edit',
			text: 'Bản nháp'
		},
		'SUSPENDED': {
			class: 'bg-dark',
			icon: 'fas fa-pause-circle',
			text: 'Tạm ngưng'
		}
	};

	const config = statusConfig[status.toUpperCase()] || {
		class: 'bg-secondary',
		icon: 'fas fa-question',
		text: status
	};

	return `<span class="badge ${config.class}" title="Trạng thái: ${config.text}">
		<i class="${config.icon}"></i> ${config.text}
	</span>`;
}

// Thống kê khóa học


// Tính trung bình bài học hoàn thành
function calculateAverageLessonsCompleted(course) {
    if (!course.lessons || course.lessons.length === 0) {
        return '<span style="color: #6c757d;">Chưa có bài học</span>';
    }
    
    if (!course.enrolledUserIds || course.enrolledUserIds.length === 0) {
        return '<span style="color: #6c757d;">Chưa có học viên</span>';
    }
    
    // For now, return a placeholder since we need backend support for this calculation
    // This should be replaced with actual progress data from the backend
    const totalLessons = course.lessons.length;
    const totalStudents = course.enrolledUserIds.length;
    
    return `<span style="color: #28a745;" title="Cần cập nhật logic tính toán từ backend">
        <i class="fas fa-chart-line"></i> ${totalLessons} bài học / ${totalStudents} học viên
    </span>`;
}

// Tạo khóa học (PENDING by default)
function createCourse() {
	const form = document.getElementById('createCourseForm');
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
		image: imageUrl,
		level: formData.get('level'),
		prerequisites: formData.get('prerequisites'),
		objectives: formData.get('objectives'),
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
		bootstrap.Modal.getInstance(document.getElementById('createCourseModal')).hide();
		form.reset();
		form.classList.remove('was-validated');
		clearImagePreview(); // Clear image preview after successful creation
		loadTeacherCourses();
		loadCourseStats();
	})
	.catch(err => {
		console.error('Lỗi tạo khóa học:', err);
		alert('Tạo khóa học thất bại');
	});
}


// Chỉnh sửa khóa học → yêu cầu phê duyệt lại
function editCourse(courseId) {
	fetch(`/api/courses/${courseId}`)
		.then(res => res.json())
		.then(course => {
			const form = document.getElementById('editCourseForm');
			form.querySelector('[name="courseId"]').value = course.id;
			form.querySelector('[name="title"]').value = course.title;
			form.querySelector('[name="description"]').value = course.description;
			form.querySelector('[name="duration"]').value = course.duration;
			form.querySelector('[name="image"]').value = course.image || '';
			form.querySelector('[name="level"]').value = course.level || '';
			form.querySelector('[name="prerequisites"]').value = course.prerequisites;
			form.querySelector('[name="objectives"]').value = course.objectives;
			form.querySelector('[name="jobRole"]').value = course.jobRole || '';
			form.querySelector('[name="skillCategory"]').value = course.skillCategory || '';
			
			// Display current course image if exists
			const editImagePreview = document.getElementById('editImagePreview');
			const editImagePreviewContainer = document.getElementById('editImagePreviewContainer');
			
			if (course.image && course.image.trim() !== '') {
				editImagePreview.src = course.image;
				editImagePreviewContainer.style.display = 'block';
			} else {
				editImagePreviewContainer.style.display = 'none';
			}
			
			new bootstrap.Modal(document.getElementById('editCourseModal')).show();
		})
		.catch(err => console.error('Error loading course for edit:', err));
}

// Gửi chỉnh sửa khóa học (→ quay về trạng thái PENDING)
function updateCourse() {
	const form = document.getElementById('editCourseForm');
	const courseId = form.querySelector('[name="courseId"]').value;
	
	// Use uploaded image URL if available, otherwise use URL input
	const imageUrl = editUploadedImageUrl || form.querySelector('[name="image"]').value;
	
	const courseData = {
		title: form.querySelector('[name="title"]').value,
		description: form.querySelector('[name="description"]').value,
		duration: parseInt(form.querySelector('[name="duration"]').value),
		image: imageUrl,
		level: form.querySelector('[name="level"]').value,
		prerequisites: form.querySelector('[name="prerequisites"]').value,
		objectives: form.querySelector('[name="objectives"]').value,
		jobRole: form.querySelector('[name="jobRole"]').value || null,
		skillCategory: form.querySelector('[name="skillCategory"]').value || null
	};

	fetch(`/api/courses/${courseId}`, {
		method: 'PUT',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(courseData)
	})
		.then(res => res.json())
		.then(() => {
			bootstrap.Modal.getInstance(document.getElementById('editCourseModal')).hide();
			
			// Clear uploaded image URL for edit modal
			editUploadedImageUrl = null;
			
			loadTeacherCourses();
			loadCourseStats();
		})
		.catch(err => console.error('Error updating course:', err));
}

function viewCourseDetails(courseId) {
	if (!courseId) {
		console.error('Course ID is required for viewing details');
		alert('Lỗi: Không tìm thấy ID khóa học');
		return;
	}

	console.log('👁️ Viewing course details for:', courseId);
	
	// You can implement a modal or navigate to a details page
	// For now, let's navigate to the course page
	window.location.href = `/courses/${courseId}`;
}

function getStatusText(status) {
	const map = {
		'PENDING': 'Chờ phê duyệt',
		'PUBLISHED': 'Đã xuất bản',
		'APPROVED': 'Đã duyệt',
		'DRAFT': 'Bản nháp',
		'ARCHIVED': 'Đã lưu trữ',
		'REJECTED': 'Bị từ chối',
		'SUSPENDED': 'Tạm ngưng'
	};
	return map[status?.toUpperCase()] || status;
}
function loadCourseStats() {
    console.log('🔄 Starting to load teacher course stats...');
    
    // Show loading state
    const statsElements = ['totalCourses', 'totalStudents', 'totalLessons', 'pendingCourses'];
    statsElements.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            console.log(` Found element: ${id}`);
        } else {
            console.warn(`Element not found: ${id}`);
        }
    });

    console.log('🌐 Making fetch request to /api/courses/teacher/stats');
    
    fetch('/api/courses/teacher/stats', {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
        .then(res => {
            console.log('📡 Response status:', res.status);
            console.log('📡 Response headers:', res.headers);
            
            if (!res.ok) {
                throw new Error(`HTTP ${res.status}: ${res.statusText}`);
            }
            return res.json();
        })
        .then(stats => {
            console.log('📊 Teacher stats loaded successfully:', stats);
            console.log('📊 Stats details:', {
                totalCourses: stats.totalCourses,
                totalStudents: stats.totalStudents,
                totalLessons: stats.totalLessons,
                pendingCourses: stats.pendingCourses
            });
            
            // Update statistics with proper number formatting
            const totalCoursesEl = document.getElementById('totalCourses');
            if (totalCoursesEl) {
                const value = (stats.totalCourses || 0).toLocaleString();
                totalCoursesEl.textContent = value;
                console.log(' Updated totalCourses:', value);
            } else {
                console.error('totalCourses element not found');
            }
            
            const totalStudentsEl = document.getElementById('totalStudents');
            if (totalStudentsEl) {
                const value = (stats.totalStudents || 0).toLocaleString();
                totalStudentsEl.textContent = value;
                console.log(' Updated totalStudents:', value);
            } else {
                console.error('totalStudents element not found');
            }
            
            const totalLessonsEl = document.getElementById('totalLessons');
            if (totalLessonsEl) {
                const value = (stats.totalLessons || 0).toLocaleString();
                totalLessonsEl.textContent = value;
                console.log(' Updated totalLessons:', value);
            } else {
                console.error('totalLessons element not found');
            }
            
            const pendingCoursesEl = document.getElementById('pendingCourses');
            if (pendingCoursesEl) {
                const value = (stats.pendingCourses || 0).toLocaleString();
                pendingCoursesEl.textContent = value;
                console.log(' Updated pendingCourses:', value);
            } else {
                console.error('pendingCourses element not found');
            }
            
            // Add visual feedback for empty states
            if (stats.totalCourses === 0) {
                totalCoursesEl.style.color = '#6c757d';
                totalCoursesEl.title = 'Bạn chưa có khóa học nào được phê duyệt';
                console.log('ℹ️ No approved courses found');
            }
            
            if (stats.pendingCourses > 0) {
                pendingCoursesEl.style.color = '#ffc107';
                pendingCoursesEl.title = `Có ${stats.pendingCourses} khóa học đang chờ phê duyệt`;
                console.log(`⏳ ${stats.pendingCourses} pending courses found`);
            }
            
            console.log('🎉 Stats update completed successfully!');
        })
        .catch(err => {
            console.error('Lỗi tải thống kê giáo viên:', err);
            console.error('Error details:', {
                message: err.message,
                stack: err.stack
            });
            
            // Show error state
            statsElements.forEach(id => {
                const element = document.getElementById(id);
                if (element) {
                    element.innerHTML = '<i class="fas fa-exclamation-triangle" style="color: #dc3545;" title="Lỗi tải dữ liệu"></i>';
                }
            });
            
            // Show user-friendly error message
            const errorMessage = err.message.includes('HTTP') ? 
                'Không thể kết nối đến server' : 
                'Có lỗi xảy ra khi tải thống kê';
                
            console.error('📢 User-friendly error:', errorMessage);
            
            // Show alert for debugging
            alert(`Lỗi tải thống kê: ${err.message}\nVui lòng kiểm tra console để biết thêm chi tiết.`);
        });
}




//LESSON
function goToLessons(courseId) {
	if (!courseId) {
		console.error('Course ID is required for navigation');
		alert('Lỗi: Không tìm thấy ID khóa học');
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

	// Navigate to lesson management page - Fixed endpoint for teacher
	window.location.href = `/teacher/courses/${courseId}/lessons`;
}
