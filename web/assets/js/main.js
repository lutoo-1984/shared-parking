/**
 * 共享停车位平台 - 主JavaScript文件
 * 版本: 1.0.0
 */

// DOM加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化移动端菜单
    initMobileMenu();

    // 初始化下拉菜单
    initDropdowns();

    // 初始化表单验证
    initFormValidation();

    // 初始化工具提示
    initTooltips();

    // 初始化图片懒加载
    initLazyLoading();

    // 初始化回到顶部按钮
    initBackToTop();

    // 初始化通知系统
    initNotifications();

    // 设置API基础URL
    window.API_BASE_URL = '/api';

    // 全局错误处理
    window.addEventListener('error', handleGlobalError);
    window.addEventListener('unhandledrejection', handlePromiseRejection);

    console.log('共享停车位平台已初始化');
});

/**
 * 初始化移动端菜单
 */
function initMobileMenu() {
    const menuBtn = document.querySelector('.mobile-menu-btn');
    const navMenu = document.querySelector('.nav-menu');

    if (!menuBtn || !navMenu) return;

    menuBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        navMenu.classList.toggle('show');
        menuBtn.classList.toggle('active');
    });

    // 点击其他地方关闭菜单
    document.addEventListener('click', function(e) {
        if (!navMenu.contains(e.target) && !menuBtn.contains(e.target)) {
            navMenu.classList.remove('show');
            menuBtn.classList.remove('active');
        }
    });

    // 点击菜单项关闭菜单（移动端）
    navMenu.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', function() {
            navMenu.classList.remove('show');
            menuBtn.classList.remove('active');
        });
    });
}

/**
 * 初始化下拉菜单
 */
function initDropdowns() {
    const dropdowns = document.querySelectorAll('.user-menu');

    dropdowns.forEach(dropdown => {
        const btn = dropdown.querySelector('.user-btn');
        const menu = dropdown.querySelector('.dropdown-menu');

        if (!btn || !menu) return;

        // 点击按钮切换下拉菜单
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
        });

        // 点击其他地方关闭下拉菜单
        document.addEventListener('click', function() {
            menu.style.display = 'none';
        });

        // 防止菜单内部点击关闭
        menu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });
}

/**
 * 初始化表单验证
 */
function initFormValidation() {
    const forms = document.querySelectorAll('form[data-validate]');

    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
                return false;
            }
        });

        // 实时验证
        form.querySelectorAll('input, select, textarea').forEach(input => {
            input.addEventListener('blur', function() {
                validateField(this);
            });
        });
    });
}

/**
 * 验证整个表单
 */
function validateForm(form) {
    let isValid = true;
    const inputs = form.querySelectorAll('input, select, textarea');

    inputs.forEach(input => {
        if (!validateField(input)) {
            isValid = false;
        }
    });

    return isValid;
}

/**
 * 验证单个字段
 */
function validateField(field) {
    const value = field.value.trim();
    const errorElement = field.parentElement.querySelector('.error-message') ||
                         document.createElement('div');

    // 移除旧的错误消息
    errorElement.className = 'error-message';
    field.classList.remove('error');

    // 检查必填字段
    if (field.hasAttribute('required') && !value) {
        showFieldError(field, errorElement, '此字段为必填项');
        return false;
    }

    // 检查邮箱格式
    if (field.type === 'email' && value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            showFieldError(field, errorElement, '请输入有效的邮箱地址');
            return false;
        }
    }

    // 检查手机号格式（中国）
    if (field.name === 'phone' && value) {
        const phoneRegex = /^1[3-9]\d{9}$/;
        if (!phoneRegex.test(value)) {
            showFieldError(field, errorElement, '请输入有效的手机号码');
            return false;
        }
    }

    // 检查密码强度
    if (field.type === 'password' && value) {
        if (value.length < 6) {
            showFieldError(field, errorElement, '密码长度至少6位');
            return false;
        }
    }

    // 检查确认密码
    if (field.name === 'confirm_password' && value) {
        const passwordField = field.form.querySelector('input[name="password"]');
        if (passwordField && value !== passwordField.value) {
            showFieldError(field, errorElement, '两次输入的密码不一致');
            return false;
        }
    }

    return true;
}

/**
 * 显示字段错误
 */
function showFieldError(field, errorElement, message) {
    field.classList.add('error');
    errorElement.textContent = message;

    if (!errorElement.parentElement) {
        errorElement.className = 'error-message';
        field.parentElement.appendChild(errorElement);
    }
}

/**
 * 初始化工具提示
 */
function initTooltips() {
    const tooltipElements = document.querySelectorAll('[data-tooltip]');

    tooltipElements.forEach(element => {
        const tooltipText = element.getAttribute('data-tooltip');
        const tooltip = document.createElement('div');
        tooltip.className = 'tooltip';
        tooltip.textContent = tooltipText;

        element.appendChild(tooltip);

        element.addEventListener('mouseenter', function() {
            tooltip.style.opacity = '1';
            tooltip.style.visibility = 'visible';
        });

        element.addEventListener('mouseleave', function() {
            tooltip.style.opacity = '0';
            tooltip.style.visibility = 'hidden';
        });
    });
}

/**
 * 初始化图片懒加载
 */
function initLazyLoading() {
    if ('IntersectionObserver' in window) {
        const lazyImages = document.querySelectorAll('img[data-src]');

        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.getAttribute('data-src');
                    img.removeAttribute('data-src');
                    imageObserver.unobserve(img);
                }
            });
        });

        lazyImages.forEach(img => imageObserver.observe(img));
    } else {
        // 回退方案：直接加载所有图片
        document.querySelectorAll('img[data-src]').forEach(img => {
            img.src = img.getAttribute('data-src');
            img.removeAttribute('data-src');
        });
    }
}

/**
 * 初始化回到顶部按钮
 */
function initBackToTop() {
    const backToTopBtn = document.createElement('button');
    backToTopBtn.className = 'back-to-top';
    backToTopBtn.innerHTML = '<i class="fas fa-chevron-up"></i>';
    backToTopBtn.setAttribute('aria-label', '回到顶部');
    document.body.appendChild(backToTopBtn);

    // 样式
    const style = document.createElement('style');
    style.textContent = `
        .back-to-top {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 50px;
            height: 50px;
            background-color: var(--primary-color);
            color: white;
            border: none;
            border-radius: 50%;
            font-size: 20px;
            cursor: pointer;
            opacity: 0;
            visibility: hidden;
            transition: all 0.3s ease;
            z-index: 1000;
            box-shadow: var(--shadow-md);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .back-to-top.visible {
            opacity: 1;
            visibility: visible;
        }

        .back-to-top:hover {
            background-color: var(--primary-dark);
            transform: translateY(-3px);
            box-shadow: var(--shadow-lg);
        }
    `;
    document.head.appendChild(style);

    // 滚动显示/隐藏按钮
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopBtn.classList.add('visible');
        } else {
            backToTopBtn.classList.remove('visible');
        }
    });

    // 点击回到顶部
    backToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

/**
 * 初始化通知系统
 */
function initNotifications() {
    // 检查是否有未读消息
    checkUnreadMessages();

    // 定期检查更新
    setInterval(checkUnreadMessages, 5 * 60 * 1000); // 5分钟
}

/**
 * 检查未读消息
 */
function checkUnreadMessages() {
    // 这里应该调用API检查未读消息
    // 暂时模拟
    const hasUnread = Math.random() > 0.5;

    if (hasUnread) {
        showMessageNotification();
    }
}

/**
 * 显示消息通知
 */
function showMessageNotification() {
    // 避免重复通知
    if (document.querySelector('.message-notification')) return;

    const notification = document.createElement('div');
    notification.className = 'message-notification';
    notification.innerHTML = `
        <i class="fas fa-envelope"></i>
        <span>您有新的消息</span>
        <button class="notification-close"><i class="fas fa-times"></i></button>
    `;

    // 样式
    const style = document.createElement('style');
    style.textContent = `
        .message-notification {
            position: fixed;
            top: 80px;
            right: 20px;
            background-color: white;
            border-radius: var(--radius-md);
            padding: var(--spacing-md);
            box-shadow: var(--shadow-lg);
            display: flex;
            align-items: center;
            gap: var(--spacing-md);
            z-index: 2000;
            animation: slideIn 0.3s ease;
            border-left: 4px solid var(--primary-color);
        }

        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        .message-notification i {
            color: var(--primary-color);
            font-size: 20px;
        }

        .notification-close {
            background: none;
            border: none;
            color: var(--text-light);
            cursor: pointer;
            padding: var(--spacing-xs);
            margin-left: var(--spacing-sm);
        }

        .notification-close:hover {
            color: var(--text-primary);
        }
    `;
    document.head.appendChild(style);

    document.body.appendChild(notification);

    // 关闭按钮
    notification.querySelector('.notification-close').addEventListener('click', function() {
        notification.remove();
    });

    // 自动关闭
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);

    // 点击通知跳转到消息页面
    notification.addEventListener('click', function(e) {
        if (!e.target.closest('.notification-close')) {
            window.location.href = '/messages';
        }
    });
}

/**
 * 全局错误处理
 */
function handleGlobalError(event) {
    console.error('全局错误:', event.error);
    showErrorToast('发生了一个错误，请稍后重试');
}

/**
 * Promise拒绝处理
 */
function handlePromiseRejection(event) {
    console.error('未处理的Promise拒绝:', event.reason);
    showErrorToast('操作失败，请稍后重试');
}

/**
 * 显示错误提示
 */
function showErrorToast(message) {
    const toast = document.createElement('div');
    toast.className = 'error-toast';
    toast.textContent = message;

    // 样式
    const style = document.createElement('style');
    style.textContent = `
        .error-toast {
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background-color: var(--danger-color);
            color: white;
            padding: var(--spacing-md) var(--spacing-lg);
            border-radius: var(--radius-md);
            box-shadow: var(--shadow-lg);
            z-index: 3000;
            animation: fadeInOut 3s ease;
        }

        @keyframes fadeInOut {
            0% { opacity: 0; transform: translateX(-50%) translateY(-20px); }
            10% { opacity: 1; transform: translateX(-50%) translateY(0); }
            90% { opacity: 1; transform: translateX(-50%) translateY(0); }
            100% { opacity: 0; transform: translateX(-50%) translateY(-20px); }
        }
    `;
    document.head.appendChild(style);

    document.body.appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 3000);
}

/**
 * API请求函数
 */
window.apiRequest = async function(endpoint, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };

    const token = localStorage.getItem('auth_token');
    if (token) {
        defaultOptions.headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    try {
        const response = await fetch(`${window.API_BASE_URL}${endpoint}`, config);

        if (!response.ok) {
            if (response.status === 401) {
                // 未授权，跳转到登录页面
                localStorage.removeItem('auth_token');
                window.location.href = '/login';
                return;
            }

            throw new Error(`请求失败: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();

        if (!data.success) {
            throw new Error(data.message || '操作失败');
        }

        return data;
    } catch (error) {
        console.error('API请求错误:', error);
        showErrorToast(error.message || '网络请求失败');
        throw error;
    }
};

/**
 * 工具函数：格式化日期
 */
window.formatDate = function(date, format = 'YYYY-MM-DD HH:mm:ss') {
    const d = new Date(date);

    const pad = (n) => n.toString().padStart(2, '0');

    const replacements = {
        'YYYY': d.getFullYear(),
        'MM': pad(d.getMonth() + 1),
        'DD': pad(d.getDate()),
        'HH': pad(d.getHours()),
        'mm': pad(d.getMinutes()),
        'ss': pad(d.getSeconds())
    };

    return format.replace(/YYYY|MM|DD|HH|mm|ss/g, match => replacements[match]);
};

/**
 * 工具函数：格式化价格
 */
window.formatPrice = function(price, currency = '¥') {
    return `${currency}${parseFloat(price).toFixed(2)}`;
};

/**
 * 工具函数：计算距离
 */
window.calculateDistance = function(lat1, lon1, lat2, lon2) {
    const R = 6371; // 地球半径（公里）
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // 返回距离（公里）
};

/**
 * 工具函数：防抖
 */
window.debounce = function(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
};

/**
 * 工具函数：节流
 */
window.throttle = function(func, limit) {
    let inThrottle;
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
};

/**
 * 工具函数：深拷贝
 */
window.deepClone = function(obj) {
    if (obj === null || typeof obj !== 'object') return obj;
    if (obj instanceof Date) return new Date(obj);
    if (obj instanceof Array) return obj.map(item => deepClone(item));
    if (typeof obj === 'object') {
        const cloned = {};
        for (const key in obj) {
            if (obj.hasOwnProperty(key)) {
                cloned[key] = deepClone(obj[key]);
            }
        }
        return cloned;
    }
};

// 导出全局函数
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        apiRequest: window.apiRequest,
        formatDate: window.formatDate,
        formatPrice: window.formatPrice,
        calculateDistance: window.calculateDistance,
        debounce: window.debounce,
        throttle: window.throttle,
        deepClone: window.deepClone
    };
}