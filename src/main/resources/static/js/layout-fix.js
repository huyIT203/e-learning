/*
 * Layout Fix JavaScript
 * Handles sidebar toggle and ensures proper layout
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('🔧 Layout Fix JS loaded');
    
    // Get elements
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');
    const menuToggle = document.querySelector('#content nav .bx.bx-menu');
    
    if (!sidebar || !content) {
        console.warn('⚠️ Sidebar or content element not found');
        return;
    }
    
    console.log('✅ Sidebar and content elements found');
    
    // Handle menu toggle
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            console.log('🔄 Toggle sidebar');
            sidebar.classList.toggle('hide');
            
            // Force layout recalculation
            setTimeout(() => {
                window.dispatchEvent(new Event('resize'));
            }, 300);
        });
    }
    
    // Force proper layout on load
    function fixLayout() {
        console.log('🔧 Fixing layout...');
        
        // Ensure sidebar is properly positioned
        sidebar.style.position = 'fixed';
        sidebar.style.top = '0';
        sidebar.style.left = '0';
        sidebar.style.zIndex = '2000';
        
        // Ensure content is properly positioned
        const sidebarWidth = sidebar.classList.contains('hide') ? '60px' : '280px';
        content.style.marginLeft = sidebarWidth;
        content.style.width = `calc(100% - ${sidebarWidth})`;
        
        console.log(`📐 Layout fixed - sidebar: ${sidebarWidth}, content width: calc(100% - ${sidebarWidth})`);
    }
    
    // Fix layout on load
    fixLayout();
    
    // Fix layout on window resize
    window.addEventListener('resize', function() {
        setTimeout(fixLayout, 100);
    });
    
    // Monitor sidebar class changes
    if (window.MutationObserver) {
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    console.log('🔄 Sidebar class changed, fixing layout...');
                    setTimeout(fixLayout, 50);
                }
            });
        });
        
        observer.observe(sidebar, { attributes: true, attributeFilter: ['class'] });
    }
    
    // Debug function (can be called from console)
    window.debugLayout = function() {
        console.log('🐛 Layout Debug Info:');
        console.log('Sidebar element:', sidebar);
        console.log('Sidebar classes:', sidebar.className);
        console.log('Sidebar computed styles:', window.getComputedStyle(sidebar));
        console.log('Content element:', content);
        console.log('Content computed styles:', window.getComputedStyle(content));
    };
    
    // Add visual debug helper (temporary)
    if (localStorage.getItem('debug-layout') === 'true') {
        sidebar.style.border = '2px solid red';
        content.style.border = '2px solid blue';
        console.log('🐛 Debug mode enabled - borders added');
    }
});

// Utility functions
window.layoutFix = {
    enableDebug: function() {
        localStorage.setItem('debug-layout', 'true');
        location.reload();
    },
    
    disableDebug: function() {
        localStorage.removeItem('debug-layout');
        location.reload();
    },
    
    forceRefresh: function() {
        const sidebar = document.getElementById('sidebar');
        const content = document.getElementById('content');
        
        if (sidebar && content) {
            sidebar.style.transition = 'none';
            content.style.transition = 'none';
            
            setTimeout(() => {
                sidebar.style.transition = '';
                content.style.transition = '';
                window.dispatchEvent(new Event('resize'));
            }, 100);
        }
    }
}; 