/**
 * 共享停车位平台 - 业务逻辑模块
 * 处理认证、停车位、预订、支付等操作的API调用
 */

// ======================== 认证模块 ========================

/**
 * 用户登录
 */
window.loginUser = async function(email, password) {
    const data = await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });
    if (data.token) {
        localStorage.setItem('auth_token', data.token);
    }
    if (data.user) {
        localStorage.setItem('user_info', JSON.stringify(data.user));
    }
    return data;
};

/**
 * 用户注册
 */
window.registerUser = async function(userData) {
    const data = await apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData)
    });
    if (data.token) {
        localStorage.setItem('auth_token', data.token);
    }
    if (data.user) {
        localStorage.setItem('user_info', JSON.stringify(data.user));
    }
    return data;
};

/**
 * 发送验证码
 */
window.sendCaptcha = async function(phone, type) {
    return await apiRequest('/auth/send-captcha', {
        method: 'POST',
        body: JSON.stringify({ phone, type: type || 'register' })
    });
};

/**
 * 验证验证码
 */
window.verifyCode = async function(phone, code, type) {
    return await apiRequest('/auth/verify-code', {
        method: 'POST',
        body: JSON.stringify({ phone, code, type: type || 'register' })
    });
};

/**
 * 退出登录
 */
window.logoutUser = function() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');
    window.location.href = '/';
};

/**
 * 获取当前用户信息
 */
window.getCurrentUser = async function() {
    try {
        const data = await apiRequest('/auth/me');
        return data.data || data.user;
    } catch (e) {
        return null;
    }
};

/**
 * 获取用户资料
 */
window.getUserProfile = async function() {
    const data = await apiRequest('/users/profile');
    return data.data;
};

/**
 * 更新用户资料
 */
window.updateUserProfile = async function(profileData) {
    return await apiRequest('/users/profile', {
        method: 'PUT',
        body: JSON.stringify(profileData)
    });
};

// ======================== 停车位模块 ========================

/**
 * 搜索停车位
 */
window.searchParkingSpots = async function(filters) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            params.append(key, value);
        }
    });
    const data = await apiRequest(`/parking/spots?${params.toString()}`);
    return data.data;
};

/**
 * 获取停车位详情
 */
window.getParkingSpotDetail = async function(spotId) {
    const data = await apiRequest(`/parking/spots/${spotId}`);
    return data.data;
};

/**
 * 创建停车位
 */
window.createParkingSpot = async function(spotData) {
    return await apiRequest('/parking/spots', {
        method: 'POST',
        body: JSON.stringify(spotData)
    });
};

/**
 * 更新停车位
 */
window.updateParkingSpot = async function(spotId, spotData) {
    return await apiRequest(`/parking/spots/${spotId}`, {
        method: 'PUT',
        body: JSON.stringify(spotData)
    });
};

/**
 * 删除停车位
 */
window.deleteParkingSpot = async function(spotId) {
    return await apiRequest(`/parking/spots/${spotId}`, {
        method: 'DELETE'
    });
};

/**
 * 获取我的车位列表
 */
window.getMySpots = async function(page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/parking/my?${params.toString()}`);
    return data.data;
};

/**
 * 检查车位可用性
 */
window.checkAvailability = async function(spotId, startTime, endTime) {
    const params = new URLSearchParams({ start_time: startTime, end_time: endTime });
    const data = await apiRequest(`/parking/availability/${spotId}?${params.toString()}`);
    return data.data;
};

/**
 * 获取车位评价
 */
window.getSpotReviews = async function(spotId, page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/reviews/spot/${spotId}?${params.toString()}`);
    return data.data;
};

// ======================== 收藏模块 ========================

/**
 * 切换收藏（添加/取消）
 */
window.toggleFavorite = async function(spotId) {
    return await apiRequest(`/favorites/${spotId}`, {
        method: 'POST'
    });
};

/**
 * 获取收藏列表
 */
window.getFavorites = async function(page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/favorites?${params.toString()}`);
    return data.data;
};

/**
 * 取消收藏
 */
window.removeFavorite = async function(spotId) {
    return await apiRequest(`/favorites/${spotId}`, {
        method: 'DELETE'
    });
};

// ======================== 预订模块 ========================

/**
 * 创建预订
 */
window.createBooking = async function(bookingData) {
    return await apiRequest('/bookings', {
        method: 'POST',
        body: JSON.stringify(bookingData)
    });
};

/**
 * 获取预订详情
 */
window.getBookingDetail = async function(bookingId) {
    const data = await apiRequest(`/bookings/${bookingId}`);
    return data.data;
};

/**
 * 获取我的预订列表
 */
window.getMyBookings = async function(page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/bookings?${params.toString()}`);
    return data.data;
};

/**
 * 取消预订
 */
window.cancelBooking = async function(bookingId, reason) {
    return await apiRequest(`/bookings/${bookingId}/cancel`, {
        method: 'PUT',
        body: JSON.stringify({ reason: reason || '' })
    });
};

// ======================== 支付模块 ========================

/**
 * 创建支付
 */
window.createPayment = async function(bookingId, paymentMethod) {
    return await apiRequest('/payments/create', {
        method: 'POST',
        body: JSON.stringify({ booking_id: bookingId, payment_method: paymentMethod || 'wallet' })
    });
};

/**
 * 获取支付详情
 */
window.getPaymentDetail = async function(paymentId) {
    const data = await apiRequest(`/payments/${paymentId}`);
    return data.data;
};

/**
 * 获取预订的支付记录
 */
window.getBookingPayment = async function(bookingId) {
    const data = await apiRequest(`/payments/booking/${bookingId}`);
    return data.data;
};

/**
 * 获取我的支付记录
 */
window.getMyPayments = async function(page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/payments?${params.toString()}`);
    return data.data;
};

/**
 * 申请退款
 */
window.requestRefund = async function(paymentId, reason) {
    return await apiRequest(`/payments/${paymentId}/refund`, {
        method: 'POST',
        body: JSON.stringify({ refund_reason: reason || '' })
    });
};

// ======================== 消息模块 ========================

/**
 * 获取收件箱
 */
window.getInbox = async function(page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/messages?${params.toString()}`);
    return data.data;
};

/**
 * 获取发件箱
 */
window.getOutbox = async function(page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    const data = await apiRequest(`/messages/outbox?${params.toString()}`);
    return data.data;
};

/**
 * 获取与某用户的对话
 */
window.getConversation = async function(otherUserId, page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 50 });
    const data = await apiRequest(`/messages/conversation/${otherUserId}?${params.toString()}`);
    return data.data;
};

/**
 * 发送消息
 */
window.sendMessage = async function(receiverId, content, subject, bookingId) {
    return await apiRequest('/messages', {
        method: 'POST',
        body: JSON.stringify({
            receiver_id: receiverId,
            content: content,
            subject: subject || '',
            booking_id: bookingId || null
        })
    });
};

/**
 * 标记消息为已读
 */
window.markMessageRead = async function(messageId) {
    return await apiRequest(`/messages/${messageId}/read`, {
        method: 'PUT'
    });
};

/**
 * 标记所有消息为已读
 */
window.markAllMessagesRead = async function() {
    return await apiRequest('/messages', {
        method: 'PUT'
    });
};

/**
 * 获取未读消息数
 */
window.getUnreadCount = async function() {
    const data = await apiRequest('/messages/unread');
    return data.data.unread_count;
};

/**
 * 删除消息
 */
window.deleteMessage = async function(messageId) {
    return await apiRequest(`/messages/${messageId}`, {
        method: 'DELETE'
    });
};

// ======================== 评价模块 ========================

/**
 * 创建评价
 */
window.createReview = async function(bookingId, rating, title, content) {
    return await apiRequest('/reviews', {
        method: 'POST',
        body: JSON.stringify({ booking_id: bookingId, rating: rating, title: title || '', content: content || '' })
    });
};

/**
 * 回复评价
 */
window.replyReview = async function(reviewId, reply) {
    return await apiRequest(`/reviews/reply/${reviewId}`, {
        method: 'PUT',
        body: JSON.stringify({ reply: reply })
    });
};

// ======================== 管理模块 ========================

/**
 * 获取仪表盘统计
 */
window.getAdminStats = async function() {
    const data = await apiRequest('/admin/stats');
    return data.data;
};

/**
 * 获取所有停车位（管理）
 */
window.getAdminSpots = async function(filters, page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    Object.entries(filters || {}).forEach(([k, v]) => { if (v !== '') params.append(k, v); });
    const data = await apiRequest(`/admin/spots?${params.toString()}`);
    return data.data;
};

/**
 * 获取所有用户（管理）
 */
window.getAdminUsers = async function(filters, page, limit) {
    const params = new URLSearchParams({ page: page || 1, limit: limit || 20 });
    Object.entries(filters || {}).forEach(([k, v]) => { if (v !== '') params.append(k, v); });
    const data = await apiRequest(`/admin/users?${params.toString()}`);
    return data.data;
};

/**
 * 审核停车位
 */
window.approveSpot = async function(spotId, approved, notes) {
    return await apiRequest(`/admin/spots/${spotId}/approve`, {
        method: 'PUT',
        body: JSON.stringify({ is_approved: approved ? 1 : 0, approval_notes: notes || '' })
    });
};

/**
 * 管理用户状态
 */
window.manageUser = async function(userId, data) {
    return await apiRequest(`/admin/users/${userId}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
};

// ======================== 文件上传 ========================

/**
 * 上传文件
 */
window.uploadFile = async function(file, type) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type || 'spot');

    const token = localStorage.getItem('auth_token');
    const response = await fetch(`${window.API_BASE_URL}/upload`, {
        method: 'POST',
        headers: token ? { 'Authorization': `Bearer ${token}` } : {},
        body: formData
    });

    const data = await response.json();
    if (!data.success) {
        throw new Error(data.error?.message || '上传失败');
    }
    return data.data;
};

// ======================== 页面初始化 ========================

/**
 * 检查登录状态并更新UI
 */
window.initAuthUI = function() {
    const token = localStorage.getItem('auth_token');
    if (token) {
        document.body.classList.add('logged-in');
        const userInfo = localStorage.getItem('user_info');
        if (userInfo) {
            try {
                const user = JSON.parse(userInfo);
                const userEl = document.querySelector('.user-name');
                if (userEl) userEl.textContent = user.username || user.nickname;
            } catch (e) {}
        }
    }
};

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    initAuthUI();

    // 绑定登录表单
    const loginForm = document.querySelector('#login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const email = document.getElementById('email')?.value;
            const password = document.getElementById('password')?.value;
            if (!email || !password) {
                showErrorToast('请填写邮箱和密码');
                return;
            }
            try {
                const btn = loginForm.querySelector('button[type="submit"]');
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 登录中...';
                const result = await loginUser(email, password);
                showSuccessToast('登录成功！');
                setTimeout(() => { window.location.href = '/dashboard'; }, 500);
            } catch (e) {
                const msg = e.message || '登录失败';
                showErrorToast(msg);
                const btn = loginForm.querySelector('button[type="submit"]');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-sign-in-alt"></i> 登录';
            }
        });
    }

    // 绑定注册表单
    const registerForm = document.querySelector('#register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const username = document.getElementById('username')?.value;
            const email = document.getElementById('email')?.value;
            const phone = document.getElementById('phone')?.value;
            const password = document.getElementById('password')?.value;
            if (!username || !email || !phone || !password) {
                showErrorToast('请填写所有必填字段');
                return;
            }
            try {
                const btn = registerForm.querySelector('button[type="submit"]');
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 注册中...';
                await registerUser({ username, email, phone, password });
                showSuccessToast('注册成功！');
                setTimeout(() => { window.location.href = '/dashboard'; }, 500);
            } catch (e) {
                showErrorToast(e.message || '注册失败');
                const btn = registerForm.querySelector('button[type="submit"]');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-user-plus"></i> 注册';
            }
        });
    }

    // 绑定退出登录
    const logoutBtn = document.querySelector('#logout-btn, .logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logoutUser();
        });
    }

    // 绑定发送验证码
    const captchaBtn = document.querySelector('#send-captcha-btn');
    if (captchaBtn) {
        captchaBtn.addEventListener('click', async function(e) {
            e.preventDefault();
            const phone = document.getElementById('phone')?.value;
            if (!phone) { showErrorToast('请先填写手机号'); return; }
            try {
                captchaBtn.disabled = true;
                await sendCaptcha(phone, 'register');
                showSuccessToast('验证码已发送');
                let countdown = 60;
                const timer = setInterval(() => {
                    captchaBtn.textContent = `${countdown}s`;
                    if (--countdown < 0) {
                        clearInterval(timer);
                        captchaBtn.textContent = '重新发送';
                        captchaBtn.disabled = false;
                    }
                }, 1000);
            } catch (e) {
                showErrorToast(e.message || '发送失败');
                captchaBtn.disabled = false;
            }
        });
    }
});

/**
 * 成功提示
 */
function showSuccessToast(message) {
    showToast(message, 'success');
}

/**
 * Toast消息提示
 */
function showToast(message, type) {
    const existing = document.querySelector('.toast-notification');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type || 'info'}`;
    toast.innerHTML = `
        <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
        <span>${message}</span>
    `;

    const style = document.createElement('style');
    style.textContent = `
        .toast-notification {
            position: fixed; top: 80px; right: 20px; padding: 12px 20px;
            background: white; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex; align-items: center; gap: 10px; z-index: 3000;
            border-left: 4px solid #4CAF50; min-width: 200px;
            animation: slideIn 0.3s ease;
        }
        .toast-error { border-left-color: #f44336; }
        .toast-info { border-left-color: #2196F3; }
        @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    `;
    document.head.appendChild(style);
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
