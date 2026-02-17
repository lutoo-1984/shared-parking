<?php
/**
 * 前端入口文件
 * 主页面控制器
 */

// 设置错误报告
error_reporting(E_ALL);
ini_set('display_errors', '1');

// 定义常量
define('WEB_ROOT', dirname(__FILE__));
define('ROOT_PATH', dirname(WEB_ROOT));
define('API_URL', '/api');

// 自动加载配置
require_once ROOT_PATH . '/api/config/constants.php';

// 会话管理
session_start();

// 当前用户信息（模拟，后续由认证系统提供）
$currentUser = isset($_SESSION['user']) ? $_SESSION['user'] : null;

// 路由处理
$request = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$basePath = '/';

// 移除基础路径
if (strpos($request, $basePath) === 0) {
    $request = substr($request, strlen($basePath));
}

// 分割路径
$pathSegments = explode('/', trim($request, '/'));
$page = $pathSegments[0] ?: 'home';

// 定义允许的页面
$allowedPages = [
    'home' => '首页',
    'login' => '登录',
    'register' => '注册',
    'parking' => '停车位',
    'booking' => '预订',
    'dashboard' => '仪表板',
    'profile' => '个人资料',
    'messages' => '消息',
    'admin' => '管理',
];

// 检查页面是否存在
if (!array_key_exists($page, $allowedPages) && $page !== 'home') {
    $page = '404';
}

// 设置页面标题
$pageTitle = isset($allowedPages[$page]) ? $allowedPages[$page] . ' - ' . APP_NAME : APP_NAME;

// 渲染页面
renderPage($page, $pageTitle, $currentUser);

/**
 * 渲染页面
 */
function renderPage($page, $pageTitle, $currentUser) {
    // 公共头部
    includeHeader($pageTitle, $currentUser);

    // 页面内容
    switch ($page) {
        case 'home':
            includeHome();
            break;
        case 'login':
            includeLogin();
            break;
        case 'register':
            includeRegister();
            break;
        case 'parking':
            includeParking();
            break;
        case 'booking':
            includeBooking();
            break;
        case 'dashboard':
            includeDashboard();
            break;
        case 'profile':
            includeProfile();
            break;
        case 'messages':
            includeMessages();
            break;
        case 'admin':
            includeAdmin();
            break;
        case '404':
            includeNotFound();
            break;
        default:
            includeHome();
    }

    // 公共底部
    includeFooter();
}

/**
 * 包含头部模板
 */
function includeHeader($pageTitle, $currentUser) {
    ?>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($pageTitle); ?></title>
    <link rel="stylesheet" href="/assets/css/style.css">
    <link rel="stylesheet" href="/assets/css/responsive.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <!-- 百度地图API -->
    <script src="https://api.map.baidu.com/api?v=3.0&ak=<?php echo BAIDU_MAP_AK; ?>"></script>
</head>
<body>
    <header class="header">
        <nav class="navbar">
            <div class="container">
                <div class="logo">
                    <a href="/">
                        <i class="fas fa-parking"></i>
                        <span><?php echo APP_NAME; ?></span>
                    </a>
                </div>

                <div class="nav-menu">
                    <a href="/parking" class="<?php echo isActive('parking'); ?>">
                        <i class="fas fa-search"></i>
                        <span>查找车位</span>
                    </a>

                    <?php if ($currentUser): ?>
                        <a href="/parking/create" class="<?php echo isActive('parking/create'); ?>">
                            <i class="fas fa-plus-circle"></i>
                            <span>发布车位</span>
                        </a>
                        <a href="/dashboard" class="<?php echo isActive('dashboard'); ?>">
                            <i class="fas fa-tachometer-alt"></i>
                            <span>我的仪表板</span>
                        </a>
                        <a href="/messages" class="<?php echo isActive('messages'); ?>">
                            <i class="fas fa-envelope"></i>
                            <span>消息</span>
                        </a>
                    <?php endif; ?>
                </div>

                <div class="user-actions">
                    <?php if ($currentUser): ?>
                        <div class="user-menu">
                            <button class="user-btn">
                                <i class="fas fa-user-circle"></i>
                                <span><?php echo htmlspecialchars($currentUser['username']); ?></span>
                                <i class="fas fa-chevron-down"></i>
                            </button>
                            <div class="dropdown-menu">
                                <a href="/profile"><i class="fas fa-user"></i> 个人资料</a>
                                <a href="/dashboard/bookings"><i class="fas fa-calendar-check"></i> 我的预订</a>
                                <a href="/dashboard/spots"><i class="fas fa-map-marker-alt"></i> 我的车位</a>
                                <hr>
                                <?php if ($currentUser['is_admin']): ?>
                                    <a href="/admin"><i class="fas fa-cog"></i> 管理后台</a>
                                    <hr>
                                <?php endif; ?>
                                <a href="/logout"><i class="fas fa-sign-out-alt"></i> 退出登录</a>
                            </div>
                        </div>
                    <?php else: ?>
                        <a href="/login" class="btn btn-outline">登录</a>
                        <a href="/register" class="btn btn-primary">注册</a>
                    <?php endif; ?>
                </div>

                <button class="mobile-menu-btn">
                    <i class="fas fa-bars"></i>
                </button>
            </div>
        </nav>
    </header>

    <main class="main-content">
    <?php
}

/**
 * 包含底部模板
 */
function includeFooter() {
    ?>
    </main>

    <footer class="footer">
        <div class="container">
            <div class="footer-content">
                <div class="footer-section">
                    <h3><i class="fas fa-parking"></i> <?php echo APP_NAME; ?></h3>
                    <p>共享停车位，让停车更简单</p>
                    <div class="social-links">
                        <a href="#"><i class="fab fa-weixin"></i></a>
                        <a href="#"><i class="fab fa-weibo"></i></a>
                        <a href="#"><i class="fab fa-qq"></i></a>
                    </div>
                </div>

                <div class="footer-section">
                    <h4>快速链接</h4>
                    <ul>
                        <li><a href="/parking">查找车位</a></li>
                        <li><a href="/parking/create">发布车位</a></li>
                        <li><a href="/about">关于我们</a></li>
                        <li><a href="/contact">联系我们</a></li>
                    </ul>
                </div>

                <div class="footer-section">
                    <h4>帮助中心</h4>
                    <ul>
                        <li><a href="/help">使用指南</a></li>
                        <li><a href="/faq">常见问题</a></li>
                        <li><a href="/terms">服务条款</a></li>
                        <li><a href="/privacy">隐私政策</a></li>
                    </ul>
                </div>

                <div class="footer-section">
                    <h4>联系我们</h4>
                    <ul>
                        <li><i class="fas fa-phone"></i> 400-123-4567</li>
                        <li><i class="fas fa-envelope"></i> support@shared-parking.com</li>
                        <li><i class="fas fa-map-marker-alt"></i> 北京市朝阳区共享停车大厦</li>
                    </ul>
                </div>
            </div>

            <div class="footer-bottom">
                <p>&copy; <?php echo date('Y'); ?> <?php echo APP_NAME; ?> 版权所有</p>
                <p>京ICP备12345678号</p>
            </div>
        </div>
    </footer>

    <script src="/assets/js/main.js"></script>
    <script src="/assets/js/map.js"></script>
</body>
</html>
    <?php
}

/**
 * 检查当前页面是否激活
 */
function isActive($page) {
    $current = $_SERVER['REQUEST_URI'];
    return strpos($current, $page) !== false ? 'active' : '';
}

/**
 * 首页内容
 */
function includeHome() {
    ?>
    <section class="hero">
        <div class="container">
            <div class="hero-content">
                <h1>轻松找到理想停车位</h1>
                <p>共享停车位平台连接车位主和车主，让停车更简单、更经济</p>

                <div class="search-box">
                    <div class="search-input">
                        <i class="fas fa-map-marker-alt"></i>
                        <input type="text" placeholder="输入地址或区域搜索停车位" id="search-address">
                    </div>
                    <div class="search-input">
                        <i class="fas fa-calendar"></i>
                        <input type="datetime-local" id="search-time">
                    </div>
                    <button class="btn btn-primary btn-lg" onclick="searchParking()">
                        <i class="fas fa-search"></i> 搜索车位
                    </button>
                </div>

                <div class="hero-stats">
                    <div class="stat">
                        <h3>1,000+</h3>
                        <p>可用车位</p>
                    </div>
                    <div class="stat">
                        <h3>5,000+</h3>
                        <p>满意用户</p>
                    </div>
                    <div class="stat">
                        <h3>24/7</h3>
                        <p>全天候服务</p>
                    </div>
                    <div class="stat">
                        <h3>99%</h3>
                        <p>满意度</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="features">
        <div class="container">
            <h2 class="section-title">为什么选择我们？</h2>
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-map-marked-alt"></i>
                    </div>
                    <h3>精准定位</h3>
                    <p>基于百度地图的精确定位，轻松找到附近可用车位</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-shield-alt"></i>
                    </div>
                    <h3>安全可靠</h3>
                    <p>严格的车位审核和用户验证，保障交易安全</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-money-bill-wave"></i>
                    </div>
                    <h3>灵活支付</h3>
                    <p>支持支付宝、微信支付，支付便捷安全</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <h3>实时预订</h3>
                    <p>随时查看车位状态，即时预订确认</p>
                </div>
            </div>
        </div>
    </section>

    <section class="how-it-works">
        <div class="container">
            <h2 class="section-title">如何使用？</h2>
            <div class="steps">
                <div class="step">
                    <div class="step-number">1</div>
                    <h3>注册账户</h3>
                    <p>快速注册，完成实名验证</p>
                </div>
                <div class="step">
                    <div class="step-number">2</div>
                    <h3>搜索车位</h3>
                    <p>按位置、时间、价格筛选合适车位</p>
                </div>
                <div class="step">
                    <div class="step-number">3</div>
                    <h3>在线预订</h3>
                    <p>选择时间，确认预订信息</p>
                </div>
                <div class="step">
                    <div class="step-number">4</div>
                    <h3>支付停车</h3>
                    <p>在线支付，按时停车</p>
                </div>
            </div>
        </div>
    </section>

    <section class="map-preview">
        <div class="container">
            <h2 class="section-title">附近可用车位</h2>
            <div id="map-container" style="height: 400px; border-radius: 10px;"></div>
        </div>
    </section>

    <script>
    function searchParking() {
        const address = document.getElementById('search-address').value;
        const time = document.getElementById('search-time').value;

        if (!address) {
            alert('请输入搜索地址');
            return;
        }

        // 跳转到停车位搜索页面
        window.location.href = `/parking?address=${encodeURIComponent(address)}&time=${encodeURIComponent(time)}`;
    }

    // 初始化地图
    document.addEventListener('DOMContentLoaded', function() {
        if (typeof ParkingMap !== 'undefined') {
            const map = new ParkingMap('map-container');
            map.initMap(39.909186, 116.397389); // 默认北京坐标

            // 模拟数据
            const sampleSpots = [
                {id: 1, title: '市中心地下停车场', latitude: 39.909186, longitude: 116.397389,
                 price_per_hour: 15, address: '北京市朝阳区建国门外大街1号'},
                {id: 2, title: '商业区停车位', latitude: 39.912345, longitude: 116.401234,
                 price_per_hour: 12, address: '北京市朝阳区光华路'},
                {id: 3, title: '小区露天车位', latitude: 39.907654, longitude: 116.395678,
                 price_per_hour: 10, address: '北京市朝阳区建国里小区'},
            ];

            sampleSpots.forEach(spot => map.addParkingSpot(spot));
        }
    });
    </script>
    <?php
}

/**
 * 登录页面
 */
function includeLogin() {
    ?>
    <div class="auth-container">
        <div class="auth-card">
            <h2><i class="fas fa-sign-in-alt"></i> 用户登录</h2>

            <form id="login-form" class="auth-form">
                <div class="form-group">
                    <label for="email">
                        <i class="fas fa-envelope"></i> 邮箱地址
                    </label>
                    <input type="email" id="email" name="email" required placeholder="请输入邮箱地址">
                </div>

                <div class="form-group">
                    <label for="password">
                        <i class="fas fa-lock"></i> 密码
                    </label>
                    <input type="password" id="password" name="password" required placeholder="请输入密码">
                </div>

                <div class="form-options">
                    <label class="checkbox">
                        <input type="checkbox" name="remember"> 记住我
                    </label>
                    <a href="/forgot-password">忘记密码？</a>
                </div>

                <button type="submit" class="btn btn-primary btn-block">
                    <i class="fas fa-sign-in-alt"></i> 登录
                </button>
            </form>

            <div class="auth-divider">
                <span>或</span>
            </div>

            <div class="social-login">
                <button class="btn btn-social btn-wechat">
                    <i class="fab fa-weixin"></i> 微信登录
                </button>
                <button class="btn btn-social btn-alipay">
                    <i class="fab fa-alipay"></i> 支付宝登录
                </button>
            </div>

            <div class="auth-footer">
                还没有账户？ <a href="/register">立即注册</a>
            </div>
        </div>
    </div>

    <script>
    document.getElementById('login-form').addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = {
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        };

        // 调用API登录
        fetch('/api/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // 登录成功，跳转到首页
                window.location.href = '/';
            } else {
                alert('登录失败: ' + (data.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('登录错误:', error);
            alert('登录请求失败，请检查网络连接');
        });
    });
    </script>
    <?php
}

/**
 * 注册页面
 */
function includeRegister() {
    ?>
    <div class="auth-container">
        <div class="auth-card">
            <h2><i class="fas fa-user-plus"></i> 用户注册</h2>

            <form id="register-form" class="auth-form">
                <div class="form-group">
                    <label for="reg-username">
                        <i class="fas fa-user"></i> 用户名
                    </label>
                    <input type="text" id="reg-username" name="username" required placeholder="请输入用户名">
                </div>

                <div class="form-group">
                    <label for="reg-email">
                        <i class="fas fa-envelope"></i> 邮箱地址
                    </label>
                    <input type="email" id="reg-email" name="email" required placeholder="请输入邮箱地址">
                </div>

                <div class="form-group">
                    <label for="reg-phone">
                        <i class="fas fa-phone"></i> 手机号码
                    </label>
                    <input type="tel" id="reg-phone" name="phone" required placeholder="请输入手机号码">
                </div>

                <div class="form-group">
                    <label for="reg-password">
                        <i class="fas fa-lock"></i> 密码
                    </label>
                    <input type="password" id="reg-password" name="password" required placeholder="请输入密码（至少6位）">
                </div>

                <div class="form-group">
                    <label for="reg-confirm-password">
                        <i class="fas fa-lock"></i> 确认密码
                    </label>
                    <input type="password" id="reg-confirm-password" name="confirm_password" required placeholder="请再次输入密码">
                </div>

                <div class="form-group">
                    <label class="checkbox">
                        <input type="checkbox" name="agree_terms" required>
                        我已阅读并同意 <a href="/terms">服务条款</a> 和 <a href="/privacy">隐私政策</a>
                    </label>
                </div>

                <div class="form-group">
                    <div class="captcha-container">
                        <input type="text" id="reg-captcha" name="captcha" required placeholder="请输入验证码">
                        <button type="button" class="btn btn-outline" id="send-captcha-btn">发送验证码</button>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-block">
                    <i class="fas fa-user-plus"></i> 注册
                </button>
            </form>

            <div class="auth-footer">
                已有账户？ <a href="/login">立即登录</a>
            </div>
        </div>
    </div>

    <script>
    document.getElementById('register-form').addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = {
            username: document.getElementById('reg-username').value,
            email: document.getElementById('reg-email').value,
            phone: document.getElementById('reg-phone').value,
            password: document.getElementById('reg-password').value,
            confirm_password: document.getElementById('reg-confirm-password').value,
            captcha: document.getElementById('reg-captcha').value
        };

        // 密码验证
        if (formData.password !== formData.confirm_password) {
            alert('两次输入的密码不一致');
            return;
        }

        if (formData.password.length < 6) {
            alert('密码长度至少6位');
            return;
        }

        // 调用API注册
        fetch('/api/auth/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('注册成功！请登录您的邮箱完成验证。');
                window.location.href = '/login';
            } else {
                alert('注册失败: ' + (data.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('注册错误:', error);
            alert('注册请求失败，请检查网络连接');
        });
    });

    // 发送验证码
    document.getElementById('send-captcha-btn').addEventListener('click', function() {
        const phone = document.getElementById('reg-phone').value;
        if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
            alert('请输入有效的手机号码');
            return;
        }

        // 调用API发送验证码
        fetch('/api/auth/send-captcha', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({phone: phone})
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('验证码已发送到您的手机');
                // 开始倒计时
                startCountdown();
            } else {
                alert('发送验证码失败: ' + (data.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('发送验证码错误:', error);
            alert('发送验证码失败，请检查网络连接');
        });
    });

    function startCountdown() {
        const btn = document.getElementById('send-captcha-btn');
        let countdown = 60;
        btn.disabled = true;
        btn.textContent = `${countdown}秒后重新发送`;

        const timer = setInterval(() => {
            countdown--;
            if (countdown <= 0) {
                clearInterval(timer);
                btn.disabled = false;
                btn.textContent = '发送验证码';
            } else {
                btn.textContent = `${countdown}秒后重新发送`;
            }
        }, 1000);
    }
    </script>
    <?php
}

/**
 * 停车位页面
 */
function includeParking() {
    ?>
    <div class="parking-container">
        <div class="container">
            <h1 class="page-title">
                <i class="fas fa-map-marker-alt"></i> 查找停车位
            </h1>

            <div class="parking-layout">
                <!-- 侧边栏筛选 -->
                <aside class="parking-sidebar">
                    <div class="filter-card">
                        <h3><i class="fas fa-filter"></i> 筛选条件</h3>

                        <div class="filter-group">
                            <h4>位置范围</h4>
                            <input type="text" placeholder="输入地址" id="filter-address" value="<?php echo $_GET['address'] ?? ''; ?>">
                            <div class="range-slider">
                                <label>半径: <span id="radius-value">5</span>公里</label>
                                <input type="range" id="filter-radius" min="1" max="20" value="5">
                            </div>
                        </div>

                        <div class="filter-group">
                            <h4>停车时间</h4>
                            <input type="datetime-local" id="filter-start-time" value="<?php echo $_GET['time'] ?? date('Y-m-d\TH:i'); ?>">
                            <div class="duration-selector">
                                <label>停车时长</label>
                                <select id="filter-duration">
                                    <option value="1">1小时</option>
                                    <option value="2">2小时</option>
                                    <option value="4">4小时</option>
                                    <option value="8">8小时</option>
                                    <option value="24">24小时</option>
                                </select>
                            </div>
                        </div>

                        <div class="filter-group">
                            <h4>价格范围</h4>
                            <div class="price-range">
                                <input type="number" id="filter-min-price" placeholder="最低价" min="0" value="0">
                                <span>至</span>
                                <input type="number" id="filter-max-price" placeholder="最高价" min="0" value="50">
                            </div>
                        </div>

                        <div class="filter-group">
                            <h4>车辆尺寸</h4>
                            <div class="vehicle-size">
                                <label>高度(米)</label>
                                <input type="number" id="filter-height" placeholder="最大高度" step="0.1" min="1" max="3">

                                <label>宽度(米)</label>
                                <input type="number" id="filter-width" placeholder="最大宽度" step="0.1" min="1" max="3">
                            </div>
                        </div>

                        <div class="filter-group">
                            <h4>设施要求</h4>
                            <div class="facilities">
                                <label class="checkbox">
                                    <input type="checkbox" name="facility" value="security"> 安保监控
                                </label>
                                <label class="checkbox">
                                    <input type="checkbox" name="facility" value="covered"> 有顶棚
                                </label>
                                <label class="checkbox">
                                    <input type="checkbox" name="facility" value="lighting"> 照明良好
                                </label>
                                <label class="checkbox">
                                    <input type="checkbox" name="facility" value="charging"> 充电桩
                                </label>
                            </div>
                        </div>

                        <button class="btn btn-primary btn-block" onclick="applyFilters()">
                            <i class="fas fa-search"></i> 应用筛选
                        </button>
                        <button class="btn btn-outline btn-block" onclick="resetFilters()">
                            <i class="fas fa-redo"></i> 重置筛选
                        </button>
                    </div>
                </aside>

                <!-- 主内容区域 -->
                <main class="parking-main">
                    <div class="parking-header">
                        <div class="sort-options">
                            <span>排序方式:</span>
                            <select id="sort-by">
                                <option value="distance">距离最近</option>
                                <option value="price_asc">价格最低</option>
                                <option value="price_desc">价格最高</option>
                                <option value="rating">评分最高</option>
                            </select>
                        </div>

                        <div class="view-toggle">
                            <button class="view-btn active" data-view="list">
                                <i class="fas fa-list"></i>
                            </button>
                            <button class="view-btn" data-view="grid">
                                <i class="fas fa-th-large"></i>
                            </button>
                            <button class="view-btn" data-view="map">
                                <i class="fas fa-map"></i>
                            </button>
                        </div>
                    </div>

                    <!-- 地图视图 -->
                    <div id="parking-map-view" class="parking-view" style="display: none;">
                        <div id="parking-map" style="height: 500px;"></div>
                    </div>

                    <!-- 列表/网格视图 -->
                    <div id="parking-list-view" class="parking-view">
                        <div class="parking-grid" id="parking-grid">
                            <!-- 停车位卡片将通过JavaScript动态加载 -->
                            <div class="loading-spinner">
                                <i class="fas fa-spinner fa-spin"></i> 加载中...
                            </div>
                        </div>

                        <div class="pagination" id="pagination">
                            <!-- 分页控件将通过JavaScript动态生成 -->
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </div>

    <script>
    // 停车位数据
    let parkingSpots = [];
    let currentPage = 1;
    let pageSize = 12;

    // 页面加载时获取停车位数据
    document.addEventListener('DOMContentLoaded', function() {
        loadParkingSpots();

        // 视图切换
        document.querySelectorAll('.view-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.view-btn').forEach(b => b.classList.remove('active'));
                this.classList.add('active');

                const view = this.dataset.view;
                document.querySelectorAll('.parking-view').forEach(v => v.style.display = 'none');

                if (view === 'map') {
                    document.getElementById('parking-map-view').style.display = 'block';
                    initParkingMap();
                } else {
                    document.getElementById('parking-list-view').style.display = 'block';
                    document.getElementById('parking-grid').className = `parking-${view}`;
                }
            });
        });

        // 半径滑块
        const radiusSlider = document.getElementById('filter-radius');
        const radiusValue = document.getElementById('radius-value');
        radiusSlider.addEventListener('input', function() {
            radiusValue.textContent = this.value;
        });
    });

    // 加载停车位数据
    function loadParkingSpots() {
        // 模拟API调用
        setTimeout(() => {
            // 模拟数据
            parkingSpots = [
                {
                    id: 1,
                    title: '市中心地下停车场',
                    description: '位于市中心商业区的地下停车场，24小时监控，安全可靠',
                    address: '北京市朝阳区建国门外大街1号',
                    latitude: 39.909186,
                    longitude: 116.397389,
                    price_per_hour: 15,
                    price_per_day: 120,
                    max_vehicle_height: 2.2,
                    max_vehicle_width: 2.0,
                    amenities: ['security', 'cctv', 'lighting', 'covered'],
                    is_available: true,
                    owner_username: 'testuser',
                    avg_rating: 4.5,
                    review_count: 128,
                    distance: 0.5
                },
                // 可以添加更多模拟数据
            ];

            renderParkingSpots();
            renderPagination();
        }, 500);
    }

    // 渲染停车位卡片
    function renderParkingSpots() {
        const grid = document.getElementById('parking-grid');
        if (!grid) return;

        if (parkingSpots.length === 0) {
            grid.innerHTML = '<div class="no-results">未找到符合条件的停车位</div>';
            return;
        }

        const start = (currentPage - 1) * pageSize;
        const end = start + pageSize;
        const pageSpots = parkingSpots.slice(start, end);

        grid.innerHTML = pageSpots.map(spot => `
            <div class="parking-card">
                <div class="parking-card-image">
                    <img src="/assets/images/parking${spot.id % 3 + 1}.jpg" alt="${spot.title}">
                    <div class="parking-card-badge">
                        <span class="badge badge-success">可用</span>
                        <span class="badge badge-info">${spot.distance}km</span>
                    </div>
                </div>
                <div class="parking-card-content">
                    <h3>${spot.title}</h3>
                    <p class="parking-address">
                        <i class="fas fa-map-marker-alt"></i> ${spot.address}
                    </p>
                    <div class="parking-features">
                        ${spot.amenities && spot.amenities.includes('security') ?
                            '<span class="feature"><i class="fas fa-shield-alt"></i> 安保</span>' : ''}
                        ${spot.amenities && spot.amenities.includes('covered') ?
                            '<span class="feature"><i class="fas fa-umbrella"></i> 有顶棚</span>' : ''}
                        ${spot.amenities && spot.amenities.includes('lighting') ?
                            '<span class="feature"><i class="fas fa-lightbulb"></i> 照明</span>' : ''}
                        ${spot.max_vehicle_height ?
                            `<span class="feature"><i class="fas fa-arrows-alt-v"></i> ${spot.max_vehicle_height}m</span>` : ''}
                    </div>
                    <div class="parking-rating">
                        <div class="stars">
                            ${'★'.repeat(Math.floor(spot.avg_rating || 0))}${'☆'.repeat(5 - Math.floor(spot.avg_rating || 0))}
                            <span>${spot.avg_rating?.toFixed(1) || '0.0'} (${spot.review_count || 0}条评价)</span>
                        </div>
                    </div>
                    <div class="parking-price">
                        <div class="price-hour">
                            <strong>${spot.price_per_hour}元/小时</strong>
                            ${spot.price_per_day ? `<span class="price-day">${spot.price_per_day}元/天</span>` : ''}
                        </div>
                        <a href="/parking/${spot.id}" class="btn btn-primary">
                            查看详情
                        </a>
                    </div>
                </div>
            </div>
        `).join('');
    }

    // 渲染分页控件
    function renderPagination() {
        const pagination = document.getElementById('pagination');
        if (!pagination) return;

        const totalPages = Math.ceil(parkingSpots.length / pageSize);
        if (totalPages <= 1) {
            pagination.innerHTML = '';
            return;
        }

        let html = '';

        // 上一页按钮
        html += `<button class="page-btn ${currentPage <= 1 ? 'disabled' : ''}"
                 onclick="changePage(${currentPage - 1})" ${currentPage <= 1 ? 'disabled' : ''}>
                 <i class="fas fa-chevron-left"></i> 上一页</button>`;

        // 页码
        for (let i = 1; i <= totalPages; i++) {
            if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
                html += `<button class="page-btn ${i === currentPage ? 'active' : ''}"
                         onclick="changePage(${i})">${i}</button>`;
            } else if (i === currentPage - 3 || i === currentPage + 3) {
                html += '<span class="page-dots">...</span>';
            }
        }

        // 下一页按钮
        html += `<button class="page-btn ${currentPage >= totalPages ? 'disabled' : ''}"
                 onclick="changePage(${currentPage + 1})" ${currentPage >= totalPages ? 'disabled' : ''}>
                 下一页 <i class="fas fa-chevron-right"></i></button>`;

        pagination.innerHTML = html;
    }

    // 切换页码
    function changePage(page) {
        if (page < 1 || page > Math.ceil(parkingSpots.length / pageSize)) return;
        currentPage = page;
        renderParkingSpots();
        renderPagination();
        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    // 应用筛选条件
    function applyFilters() {
        // 这里应该调用API应用筛选条件
        alert('筛选条件已应用');
        loadParkingSpots();
    }

    // 重置筛选条件
    function resetFilters() {
        document.getElementById('filter-address').value = '';
        document.getElementById('filter-radius').value = 5;
        document.getElementById('radius-value').textContent = '5';
        document.getElementById('filter-start-time').value = '';
        document.getElementById('filter-duration').value = '1';
        document.getElementById('filter-min-price').value = '0';
        document.getElementById('filter-max-price').value = '50';
        document.getElementById('filter-height').value = '';
        document.getElementById('filter-width').value = '';
        document.querySelectorAll('input[name="facility"]').forEach(cb => cb.checked = false);

        loadParkingSpots();
    }

    // 初始化停车位地图
    function initParkingMap() {
        if (typeof BMap === 'undefined') {
            console.error('百度地图API未加载');
            return;
        }

        const map = new BMap.Map('parking-map');
        const point = new BMap.Point(116.397389, 39.909186);
        map.centerAndZoom(point, 13);
        map.enableScrollWheelZoom(true);

        // 添加停车位标记
        parkingSpots.forEach(spot => {
            const point = new BMap.Point(spot.longitude, spot.latitude);
            const marker = new BMap.Marker(point);

            const infoWindow = new BMap.InfoWindow(`
                <div style="padding: 10px; max-width: 250px;">
                    <h4 style="margin: 0 0 10px 0;">${spot.title}</h4>
                    <p style="margin: 0 0 5px 0; color: #666;">${spot.address}</p>
                    <p style="margin: 0 0 5px 0; color: #f60; font-weight: bold;">${spot.price_per_hour}元/小时</p>
                    <a href="/parking/${spot.id}" style="color: #06c; text-decoration: none;">查看详情 →</a>
                </div>
            `);

            marker.addEventListener('click', function() {
                map.openInfoWindow(infoWindow, point);
            });

            map.addOverlay(marker);
        });
    }
    </script>
    <?php
}

/**
 * 其他页面函数（简化实现）
 */
function includeBooking() {
    echo '<div class="container"><h1>预订页面</h1><p>功能开发中...</p></div>';
}

function includeDashboard() {
    echo '<div class="container"><h1>用户仪表板</h1><p>功能开发中...</p></div>';
}

function includeProfile() {
    echo '<div class="container"><h1>个人资料</h1><p>功能开发中...</p></div>';
}

function includeMessages() {
    echo '<div class="container"><h1>消息中心</h1><p>功能开发中...</p></div>';
}

function includeAdmin() {
    echo '<div class="container"><h1>管理后台</h1><p>功能开发中...</p></div>';
}

function includeNotFound() {
    echo '<div class="container" style="text-align: center; padding: 100px 20px;">
            <h1 style="font-size: 120px; color: #ddd; margin: 0;">404</h1>
            <h2>页面未找到</h2>
            <p>抱歉，您访问的页面不存在或已被移除。</p>
            <a href="/" class="btn btn-primary">返回首页</a>
          </div>';
}
?>