// Dark Mode Global Handler
(function() {
    'use strict';
    
    // Initialize dark mode on page load
    function initDarkMode() {
        const isDarkMode = localStorage.getItem('darkMode') === 'true';
        const switchMode = document.getElementById('switch-mode');
        const body = document.body;
        
        // Apply dark mode state
        if (isDarkMode) {
            body.classList.add('dark');
            if (switchMode) {
                switchMode.checked = true;
            }
        } else {
            body.classList.remove('dark');
            if (switchMode) {
                switchMode.checked = false;
            }
        }
        
        // Show notification when switching
        if (window.darkModeInitialized) {
            showDarkModeNotification(isDarkMode);
        }
        window.darkModeInitialized = true;
    }
    
    // Toggle dark mode
    function toggleDarkMode() {
        const body = document.body;
        const isDarkMode = body.classList.contains('dark');
        const newDarkMode = !isDarkMode;
        
        // Toggle class
        body.classList.toggle('dark', newDarkMode);
        
        // Save to localStorage
        localStorage.setItem('darkMode', newDarkMode.toString());
        
        // Update all switch elements on the page
        const switches = document.querySelectorAll('#switch-mode');
        switches.forEach(switchElement => {
            switchElement.checked = newDarkMode;
        });
        
        // Show notification
        showDarkModeNotification(newDarkMode);
        
        // Dispatch custom event for other scripts
        window.dispatchEvent(new CustomEvent('darkModeChanged', { 
            detail: { isDarkMode: newDarkMode } 
        }));
    }
    
    // Show notification when switching modes
    function showDarkModeNotification(isDarkMode) {
        // Remove existing notification
        const existingNotification = document.querySelector('.dark-mode-notification');
        if (existingNotification) {
            existingNotification.remove();
        }
        
        // Create notification
        const notification = document.createElement('div');
        notification.className = 'dark-mode-notification';
        notification.innerHTML = `
            <div style="
                position: fixed;
                top: 20px;
                right: 20px;
                background: ${isDarkMode ? '#2c3e50' : '#fff'};
                color: ${isDarkMode ? '#fff' : '#333'};
                padding: 12px 20px;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                z-index: 10000;
                font-size: 14px;
                border: 1px solid ${isDarkMode ? '#34495e' : '#e0e0e0'};
                display: flex;
                align-items: center;
                gap: 8px;
                animation: slideInRight 0.3s ease-out;
            ">
                <span style="font-size: 16px;">${isDarkMode ? '🌙' : '☀️'}</span>
                <span>Đã chuyển sang chế độ ${isDarkMode ? 'tối' : 'sáng'}</span>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 2 seconds
        setTimeout(() => {
            if (notification && notification.parentNode) {
                notification.style.animation = 'slideOutRight 0.3s ease-in forwards';
                setTimeout(() => {
                    if (notification && notification.parentNode) {
                        notification.remove();
                    }
                }, 300);
            }
        }, 2000);
    }
    
    // Add CSS animations
    function addAnimationStyles() {
        if (document.querySelector('#dark-mode-animations')) return;
        
        const style = document.createElement('style');
        style.id = 'dark-mode-animations';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            
            @keyframes slideOutRight {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
    
    // Keyboard shortcut (Ctrl/Cmd + D)
    function setupKeyboardShortcut() {
        document.addEventListener('keydown', function(e) {
            if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
                e.preventDefault();
                toggleDarkMode();
            }
        });
    }
    
    // Auto-detect system dark mode preference
    function detectSystemDarkMode() {
        if (localStorage.getItem('darkMode') === null) {
            const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
            localStorage.setItem('darkMode', prefersDark.toString());
        }
        
        // Listen for system theme changes
        if (window.matchMedia) {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
            mediaQuery.addListener(function(e) {
                if (localStorage.getItem('darkMode') === null) {
                    localStorage.setItem('darkMode', e.matches.toString());
                    initDarkMode();
                }
            });
        }
    }
    
    // Setup event listeners
    function setupEventListeners() {
        // Handle switch toggle
        document.addEventListener('change', function(e) {
            if (e.target && e.target.id === 'switch-mode') {
                toggleDarkMode();
            }
        });
        
        // Handle label clicks (in case switch is hidden)
        document.addEventListener('click', function(e) {
            if (e.target && e.target.matches('label[for="switch-mode"]')) {
                toggleDarkMode();
            }
        });
    }
    
    // Initialize everything when DOM is ready
    function init() {
        addAnimationStyles();
        detectSystemDarkMode();
        initDarkMode();
        setupEventListeners();
        setupKeyboardShortcut();
    }
    
    // Run initialization
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
    
    // Also run on page navigation (for SPAs)
    window.addEventListener('pageshow', initDarkMode);
    
    // Expose global functions
    window.toggleDarkMode = toggleDarkMode;
    window.initDarkMode = initDarkMode;
    
})(); 