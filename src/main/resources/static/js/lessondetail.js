// Dark Mode functionality for Lesson Detail page
document.addEventListener('DOMContentLoaded', function() {
    // Initialize dark mode
    initializeDarkMode();
    
    // Add smooth transitions
    addSmoothTransitions();
    
    // Initialize other features
    initializeCodeCopyButtons();
    initializeSidebarToggle();
});

// Dark Mode Functions
function initializeDarkMode() {
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
                
                // Add smooth transition effect
                document.body.style.transition = 'all 0.3s ease';
                setTimeout(() => {
                    document.body.style.transition = '';
                }, 300);
                
                // Show notification
                showModeNotification('Đã chuyển sang chế độ tối');
            } else {
                document.body.classList.remove('dark');
                localStorage.setItem('darkMode', 'false');
                
                // Add smooth transition effect
                document.body.style.transition = 'all 0.3s ease';
                setTimeout(() => {
                    document.body.style.transition = '';
                }, 300);
                
                // Show notification
                showModeNotification('Đã chuyển sang chế độ sáng');
            }
        });
    }

    // Auto-detect system dark mode preference
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
}

function detectSystemDarkMode() {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        if (localStorage.getItem('darkMode') === null) {
            document.body.classList.add('dark');
            const switchMode = document.getElementById('switch-mode');
            if (switchMode) {
                switchMode.checked = true;
            }
            localStorage.setItem('darkMode', 'true');
        }
    }
}

function showModeNotification(message) {
    // Create notification element
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: var(--accent-color);
        color: white;
        padding: 12px 20px;
        border-radius: 25px;
        font-size: 14px;
        font-weight: 500;
        z-index: 10000;
        opacity: 0;
        transform: translateX(100px);
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px var(--shadow);
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

function addSmoothTransitions() {
    // Add CSS for smooth transitions
    const style = document.createElement('style');
    style.textContent = `
        * {
            transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease !important;
        }
        
        /* Smooth transition for dark mode elements */
        body, .header, .sidebar, .main-content, .lesson-content-wrapper,
        .code-block, .check-skills, .instructions, .sidebar-tab,
        .search-input, .lesson-link, .module-header {
            transition: all 0.3s ease !important;
        }
        
        /* Enhanced hover effects */
        .lesson-link:hover {
            transform: translateX(5px);
        }
        
        .sidebar-tab:hover {
            background: var(--hover-bg);
        }
        
        .module-header:hover {
            background: var(--hover-bg);
        }
        
        /* Custom scrollbar animations */
        ::-webkit-scrollbar-thumb {
            transition: background-color 0.3s ease;
        }
    `;
    document.head.appendChild(style);
}

// Code copy functionality
function initializeCodeCopyButtons() {
    const copyButtons = document.querySelectorAll('.copy-btn');
    
    copyButtons.forEach(button => {
        button.addEventListener('click', function() {
            const codeBlock = this.closest('.code-block');
            const code = codeBlock.querySelector('pre code');
            
            if (code) {
                // Create a temporary textarea to copy text
                const textarea = document.createElement('textarea');
                textarea.value = code.textContent;
                document.body.appendChild(textarea);
                textarea.select();
                
                try {
                    document.execCommand('copy');
                    
                    // Show success feedback
                    const originalText = this.textContent;
                    this.textContent = 'Đã sao chép!';
                    this.style.background = 'var(--accent-dark)';
                    
                    setTimeout(() => {
                        this.textContent = originalText;
                        this.style.background = 'var(--accent-color)';
                    }, 2000);
                    
                } catch (err) {
                    console.error('Không thể sao chép code:', err);
                    
                    // Show error feedback
                    const originalText = this.textContent;
                    this.textContent = 'Lỗi!';
                    this.style.background = '#ff4757';
                    
                    setTimeout(() => {
                        this.textContent = originalText;
                        this.style.background = 'var(--accent-color)';
                    }, 2000);
                }
                
                document.body.removeChild(textarea);
            }
        });
    });
}

// Sidebar toggle functionality for mobile
function initializeSidebarToggle() {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');
    
    // Create toggle button for mobile
    if (window.innerWidth <= 768) {
        const toggleButton = document.createElement('button');
        toggleButton.innerHTML = '☰';
        toggleButton.style.cssText = `
            position: fixed;
            top: 15px;
            left: 15px;
            z-index: 1001;
            background: var(--accent-color);
            color: white;
            border: none;
            padding: 10px;
            border-radius: 5px;
            font-size: 18px;
            cursor: pointer;
            display: none;
        `;
        
        document.body.appendChild(toggleButton);
        
        // Show/hide toggle button based on screen size
        function handleResize() {
            if (window.innerWidth <= 768) {
                toggleButton.style.display = 'block';
                sidebar.style.transform = 'translateX(-100%)';
            } else {
                toggleButton.style.display = 'none';
                sidebar.style.transform = 'translateX(0)';
            }
        }
        
        // Toggle sidebar
        toggleButton.addEventListener('click', function() {
            const isHidden = sidebar.style.transform === 'translateX(-100%)';
            sidebar.style.transform = isHidden ? 'translateX(0)' : 'translateX(-100%)';
            sidebar.style.transition = 'transform 0.3s ease';
        });
        
        // Handle window resize
        window.addEventListener('resize', handleResize);
        handleResize();
    }
}

// Search functionality
function initializeSearch() {
    const searchInput = document.querySelector('.search-input');
    const lessonLinks = document.querySelectorAll('.lesson-link');
    
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            
            lessonLinks.forEach(link => {
                const text = link.textContent.toLowerCase();
                const listItem = link.closest('li');
                
                if (text.includes(searchTerm)) {
                    listItem.style.display = 'block';
                } else {
                    listItem.style.display = 'none';
                }
            });
        });
    }
}

// Initialize search on page load
document.addEventListener('DOMContentLoaded', function() {
    initializeSearch();
});

// Smooth scroll for anchor links
document.addEventListener('DOMContentLoaded', function() {
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
});

// Enhanced keyboard navigation
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + D to toggle dark mode
    if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
        e.preventDefault();
        const switchMode = document.getElementById('switch-mode');
        if (switchMode) {
            switchMode.click();
        }
    }
    
    // Escape to close mobile sidebar
    if (e.key === 'Escape' && window.innerWidth <= 768) {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar && sidebar.style.transform === 'translateX(0px)') {
            sidebar.style.transform = 'translateX(-100%)';
        }
    }
});

// Progress tracking (optional feature)
function trackReadingProgress() {
    const content = document.querySelector('.lesson-content-wrapper');
    const progressBar = document.createElement('div');
    
    progressBar.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 0%;
        height: 3px;
        background: var(--accent-color);
        z-index: 1000;
        transition: width 0.3s ease;
    `;
    
    document.body.appendChild(progressBar);
    
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset;
        const docHeight = document.documentElement.scrollHeight - window.innerHeight;
        const scrollPercent = (scrollTop / docHeight) * 100;
        
        progressBar.style.width = scrollPercent + '%';
    });
}

// Initialize progress tracking
document.addEventListener('DOMContentLoaded', function() {
    trackReadingProgress();
}); 