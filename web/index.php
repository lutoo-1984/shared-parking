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

// 会话管理（必须在任何输出之前）
session_start();

// 自动加载配置（使用defined检查避免重复定义）
if (!defined('APP_NAME')) {
    require_once ROOT_PATH . '/api/config/constants.php';
}
if (!defined('API_URL')) {
    define('API_URL', '/api');
}

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
    <meta name="color-scheme" content="light dark">
    <title><?php echo htmlspecialchars($pageTitle); ?></title>
    <link rel="stylesheet" href="/assets/css/style.css">
    <link rel="stylesheet" href="/assets/css/responsive.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <!-- 高德地图API -->
    <script src="https://webapi.amap.com/maps?v=2.0&key=<?php echo AMAP_KEY; ?>"></script>
    <!-- 高德地图UI库 -->
    <script src="https://webapi.amap.com/ui/1.1/main.js"></script>
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
    <script src="/assets/js/app.js"></script>
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
                    <p>基于高德地图的精确定位，轻松找到附近可用车位</p>
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

            // 加载地图后显示示例停车位
            setTimeout(() => {
                if (map.map) {
                    const sampleSpots = [
                        {id: 1, title: '市中心地下停车场', latitude: 39.909186, longitude: 116.397389,
                         price_per_hour: 15, address: '北京市朝阳区建国门外大街1号', is_available: true},
                        {id: 2, title: '商业区停车位', latitude: 39.912345, longitude: 116.401234,
                         price_per_hour: 12, address: '北京市朝阳区光华路', is_available: true},
                        {id: 3, title: '小区露天车位', latitude: 39.907654, longitude: 116.395678,
                         price_per_hour: 10, address: '北京市朝阳区建国里小区', is_available: true},
                    ];
                    sampleSpots.forEach(spot => map.addParkingSpotMarker(spot));
                }
            }, 500);
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
 * 停车位页面（含详情和创建子页面）
 */
function includeParking() {
    global $pathSegments;
    $subPage = $pathSegments[1] ?? '';

    // 停车位创建页面
    if ($subPage === 'create') {
        includeParkingCreate();
        return;
    }

    // 停车位详情页面
    if (is_numeric($subPage)) {
        includeParkingDetail($subPage);
        return;
    }

    // 停车位搜索/列表页面
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

    // 加载停车位数据 - 使用真实API
    async function loadParkingSpots() {
        const grid = document.getElementById('parking-grid');
        grid.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> 加载中...</div>';

        try {
            const filters = {
                latitude: '',
                longitude: '',
                radius: document.getElementById('filter-radius')?.value || 5,
                min_price: document.getElementById('filter-min-price')?.value || '',
                max_price: document.getElementById('filter-max-price')?.value || '',
                keyword: document.getElementById('filter-address')?.value || '',
                sort_by: document.getElementById('sort-by')?.value || ''
            };

            // 收集设施筛选
            const facilities = [];
            document.querySelectorAll('input[name="facility"]:checked').forEach(cb => {
                facilities.push(cb.value);
            });
            facilities.forEach(f => { filters[f] = '1'; });

            const result = await searchParkingSpots(filters);
            parkingSpots = result.spots || [];
        } catch (e) {
            console.warn('API调用失败，使用模拟数据:', e.message);
            // 回退到模拟数据
            parkingSpots = [
                {id: 1, title: '市中心地下停车场', description: '位于市中心商业区的地下停车场', address: '北京市朝阳区建国门外大街1号',
                 latitude: 39.909186, longitude: 116.397389, price_per_hour: 15, price_per_day: 120,
                 max_vehicle_height: 2.2, max_vehicle_width: 2.0, is_covered: 1, has_security: 1, has_cctv: 1, has_lighting: 1,
                 is_available: true, owner_username: 'testuser', avg_rating: 4.5, review_count: 128},
                {id: 2, title: '商业区停车位', description: '商业中心地上停车位', address: '北京市朝阳区光华路',
                 latitude: 39.912345, longitude: 116.401234, price_per_hour: 12, price_per_day: 96,
                 max_vehicle_height: 2.5, max_vehicle_width: 2.2, is_covered: 0, has_security: 1, has_cctv: 1, has_lighting: 1,
                 is_available: true, owner_username: 'testuser', avg_rating: 4.2, review_count: 89},
                {id: 3, title: '小区露天车位', description: '居民小区内停车位', address: '北京市朝阳区建国里小区',
                 latitude: 39.907654, longitude: 116.395678, price_per_hour: 10, price_per_day: 80,
                 max_vehicle_height: 2.0, max_vehicle_width: 1.8, is_covered: 0, has_security: 1, has_lighting: 1,
                 is_available: true, owner_username: 'testuser', avg_rating: 4.0, review_count: 45}
            ];
        }

        renderParkingSpots();
        renderPagination();
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

        grid.innerHTML = pageSpots.map(spot => {
            const amenities = [];
            if (spot.is_covered || spot.has_covered) amenities.push('covered');
            if (spot.has_security) amenities.push('security');
            if (spot.has_lighting) amenities.push('lighting');
            if (spot.has_charging) amenities.push('charging');
            if (spot.has_cctv) amenities.push('cctv');
            if (spot.is_24h_access) amenities.push('24h');

            const primaryImage = spot.primary_image || spot.images?.[0]?.image_url || '/assets/images/parking' + ((spot.id % 3) + 1) + '.jpg';

            return `
            <div class="parking-card">
                <div class="parking-card-image">
                    <img src="${primaryImage}" alt="${spot.title}" onerror="this.src='/assets/images/parking1.jpg'">
                    <div class="parking-card-badge">
                        <span class="badge badge-success">可用</span>
                        ${spot.distance ? `<span class="badge badge-info">${spot.distance}km</span>` : ''}
                    </div>
                </div>
                <div class="parking-card-content">
                    <h3>${spot.title}</h3>
                    <p class="parking-address">
                        <i class="fas fa-map-marker-alt"></i> ${spot.address}
                    </p>
                    <div class="parking-features">
                        ${amenities.includes('security') ? '<span class="feature"><i class="fas fa-shield-alt"></i> 安保</span>' : ''}
                        ${amenities.includes('covered') ? '<span class="feature"><i class="fas fa-umbrella"></i> 有顶棚</span>' : ''}
                        ${amenities.includes('lighting') ? '<span class="feature"><i class="fas fa-lightbulb"></i> 照明</span>' : ''}
                        ${amenities.includes('charging') ? '<span class="feature"><i class="fas fa-charging-station"></i> 充电桩</span>' : ''}
                        ${amenities.includes('cctv') ? '<span class="feature"><i class="fas fa-video"></i> 监控</span>' : ''}
                        ${spot.max_vehicle_height ? `<span class="feature"><i class="fas fa-arrows-alt-v"></i> ${spot.max_vehicle_height}m</span>` : ''}
                    </div>
                    <div class="parking-rating">
                        <div class="stars">
                            ${'★'.repeat(Math.floor(spot.avg_rating || 0))}${'☆'.repeat(5 - Math.floor(spot.avg_rating || 0))}
                            <span>${(spot.avg_rating || 0).toFixed(1)} (${spot.review_count || 0}条评价)</span>
                        </div>
                    </div>
                    <div class="parking-price">
                        <div class="price-hour">
                            <strong>${spot.price_per_hour}元/小时</strong>
                            ${spot.price_per_day ? `<span class="price-day">${spot.price_per_day}元/天</span>` : ''}
                        </div>
                        <a href="/booking?spot_id=${spot.id}" class="btn btn-primary">
                            立即预订
                        </a>
                        <a href="/parking/${spot.id}" class="btn btn-outline btn-sm">详情</a>
                    </div>
                </div>
            </div>
        `}).join('');
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

    // 初始化停车位地图（高德地图）
    function initParkingMap() {
        if (typeof AMap === 'undefined') {
            console.error('高德地图API未加载');
            return;
        }

        const map = new AMap.Map('parking-map', {
            zoom: 13,
            center: [116.397389, 39.909186],
            resizeEnable: true
        });

        // 添加停车位标记
        parkingSpots.forEach(spot => {
            const marker = new AMap.Marker({
                position: [spot.longitude, spot.latitude],
                map: map,
                title: spot.title
            });

            const infoWindow = new AMap.InfoWindow({
                content: `
                    <div style="padding: 10px; max-width: 250px;">
                        <h4 style="margin: 0 0 10px 0;">${spot.title}</h4>
                        <p style="margin: 0 0 5px 0; color: #666;">${spot.address}</p>
                        <p style="margin: 0 0 5px 0; color: #f60; font-weight: bold;">${spot.price_per_hour}元/小时</p>
                        <a href="/parking/${spot.id}" style="color: #06c; text-decoration: none;">查看详情 →</a>
                    </div>
                `
            });

            marker.on('click', function() {
                infoWindow.open(map, marker.getPosition());
            });
        });
    }
    </script>
    <?php
}

/**
 * 停车位详情页面
 */
function includeParkingDetail($spotId) {
    ?>
    <div class="container parking-detail-container">
        <div class="detail-loading" id="detail-loading">
            <div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> 加载车位详情...</div>
        </div>
        <div id="detail-content" style="display:none;"></div>
    </div>

    <style>
    .detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .detail-header h1 { margin: 0; font-size: 28px; }
    .detail-address { color: #666; margin: 8px 0; font-size: 16px; }
    .detail-price-box { background: #f0f7ff; padding: 20px; border-radius: 12px; text-align: center; }
    .detail-price-box .price { font-size: 36px; font-weight: bold; color: #f60; }
    .detail-price-box .price-unit { color: #999; font-size: 14px; }
    .detail-section { margin-bottom: 24px; }
    .detail-section h3 { border-bottom: 2px solid #eee; padding-bottom: 12px; margin-bottom: 16px; }
    .facility-tags { display: flex; flex-wrap: wrap; gap: 8px; }
    .facility-tag { background: #f0f7ff; padding: 6px 14px; border-radius: 20px; font-size: 14px; }
    .detail-actions { display: flex; gap: 12px; margin-top: 24px; }
    .detail-actions .btn { flex: 1; padding: 14px; font-size: 16px; }
    .review-item { padding: 16px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px; }
    .review-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
    .review-user { font-weight: 600; }
    .review-stars { color: #f60; }
    .review-content { color: #333; line-height: 1.6; }
    .review-reply { margin-top: 12px; padding: 12px; background: #f8f9fa; border-radius: 8px; font-size: 14px; }
    .image-gallery { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 24px; }
    .image-gallery img { width: 100%; height: 200px; object-fit: cover; border-radius: 8px; }
    </style>

    <script>
    document.addEventListener('DOMContentLoaded', async function() {
        try {
            const spot = await getParkingSpotDetail(<?php echo intval($spotId); ?>);
            const container = document.getElementById('detail-content');

            const facilities = [];
            if (spot.is_covered) facilities.push('🏠 有顶棚');
            if (spot.has_lighting) facilities.push('💡 照明');
            if (spot.has_security) facilities.push('🛡️ 安保');
            if (spot.has_charging) facilities.push('🔋 充电桩');
            if (spot.has_cctv) facilities.push('📹 监控');
            if (spot.is_24h_access) facilities.push('⏰ 24小时');

            const images = (spot.images || []).map(i => i.image_url).filter(Boolean);
            const primaryImage = spot.primary_image || images[0] || '/assets/images/parking1.jpg';

            let reviewsHtml = '';
            if (spot.reviews && spot.reviews.length > 0) {
                reviewsHtml = spot.reviews.map(r => `
                    <div class="review-item">
                        <div class="review-header">
                            <span class="review-user">${r.username || '用户'}</span>
                            <span class="review-stars">${'★'.repeat(r.rating)}${'☆'.repeat(5-r.rating)}</span>
                        </div>
                        ${r.content ? `<div class="review-content">${r.content}</div>` : ''}
                        ${r.owner_reply ? `<div class="review-reply"><strong>车主回复：</strong>${r.owner_reply}</div>` : ''}
                    </div>
                `).join('');
            } else {
                reviewsHtml = '<p class="text-muted">暂无评价</p>';
            }

            container.innerHTML = `
                <div class="detail-header">
                    <div>
                        <h1>${spot.title}</h1>
                        <p class="detail-address"><i class="fas fa-map-marker-alt"></i> ${spot.address}</p>
                        <p><i class="fas fa-user"></i> 车主: ${spot.owner_username || '未知'}</p>
                    </div>
                    <div class="detail-price-box">
                        <div class="price">¥${spot.price_per_hour}</div>
                        <div class="price-unit">/小时</div>
                        ${spot.price_per_day ? `<div>¥${spot.price_per_day}/天</div>` : ''}
                    </div>
                </div>

                <div class="image-gallery">
                    ${images.length > 0 ? images.map(img => `<img src="${img}" onerror="this.style.display='none'">`).join('') : `<img src="${primaryImage}" style="max-width:400px;">`}
                </div>

                <div class="detail-section">
                    <h3><i class="fas fa-align-left"></i> 描述</h3>
                    <p>${spot.description || '暂无描述'}</p>
                </div>

                <div class="detail-section">
                    <h3><i class="fas fa-tools"></i> 设施</h3>
                    <div class="facility-tags">
                        ${facilities.length > 0 ? facilities.map(f => `<span class="facility-tag">${f}</span>`).join('') : '<span class="text-muted">暂无设施信息</span>'}
                    </div>
                </div>

                <div class="detail-section">
                    <h3><i class="fas fa-star"></i> 评价 (${spot.review_count || 0})</h3>
                    ${reviewsHtml}
                </div>

                <div class="detail-actions">
                    <a href="/booking?spot_id=${spot.id}" class="btn btn-primary"><i class="fas fa-calendar-check"></i> 立即预订</a>
                    <button class="btn btn-outline" onclick="toggleFavoriteItem(${spot.id})">
                        <i class="fas ${spot.is_favorite ? 'fa-heart' : 'fa-heart-o'}"></i> ${spot.is_favorite ? '已收藏' : '收藏'}
                    </button>
                </div>
            `;

            document.getElementById('detail-loading').style.display = 'none';
            container.style.display = 'block';

        } catch (e) {
            document.getElementById('detail-loading').innerHTML =
                '<p style="text-align:center;padding:60px;color:#999;">加载失败: ' + e.message + '</p>';
        }
    });

    async function toggleFavoriteItem(spotId) {
        try {
            const result = await toggleFavorite(spotId);
            const data = result.data || result;
            if (data.is_favorite) {
                showSuccessToast('收藏成功');
            } else {
                showSuccessToast('已取消收藏');
            }
            // 刷新页面获取最新状态
            location.reload();
        } catch(e) { showErrorToast(e.message || '操作失败'); }
    }
    </script>
    <?php
}

/**
 * 停车位创建页面
 */
function includeParkingCreate() {
    ?>
    <div class="container create-spot-container">
        <h1 class="page-title"><i class="fas fa-plus-circle"></i> 发布停车位</h1>

        <div class="card">
            <div class="card-body">
                <form id="create-spot-form">
                    <div class="form-group">
                        <label>标题 *</label>
                        <input type="text" id="cs-title" class="form-control" required placeholder="如：XX小区地下车位">
                    </div>

                    <div class="form-group">
                        <label>描述</label>
                        <textarea id="cs-description" class="form-control" rows="3" placeholder="描述车位的位置、环境、注意事项等"></textarea>
                    </div>

                    <div class="form-group">
                        <label>地址 *</label>
                        <input type="text" id="cs-address" class="form-control" required placeholder="如：北京市朝阳区XX路XX号">
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>纬度</label>
                            <input type="number" id="cs-latitude" class="form-control" step="0.000001" placeholder="39.909186">
                        </div>
                        <div class="form-group">
                            <label>经度</label>
                            <input type="number" id="cs-longitude" class="form-control" step="0.000001" placeholder="116.397389">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>每小时价格 * (元)</label>
                            <input type="number" id="cs-price" class="form-control" required min="1" step="0.5" placeholder="15">
                        </div>
                        <div class="form-group">
                            <label>每日价格 (元)</label>
                            <input type="number" id="cs-price-day" class="form-control" min="1" step="0.5" placeholder="120">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>最大车辆高度 (米)</label>
                            <input type="number" id="cs-height" class="form-control" step="0.1" placeholder="2.2">
                        </div>
                        <div class="form-group">
                            <label>最大车辆宽度 (米)</label>
                            <input type="number" id="cs-width" class="form-control" step="0.1" placeholder="2.0">
                        </div>
                    </div>

                    <div class="form-group">
                        <label>设施</label>
                        <div class="facility-checkboxes" style="display:flex;flex-wrap:wrap;gap:16px;">
                            <label class="checkbox"><input type="checkbox" name="facility" value="is_covered"> 有顶棚</label>
                            <label class="checkbox"><input type="checkbox" name="facility" value="has_lighting"> 照明</label>
                            <label class="checkbox"><input type="checkbox" name="facility" value="has_security"> 安保</label>
                            <label class="checkbox"><input type="checkbox" name="facility" value="has_charging"> 充电桩</label>
                            <label class="checkbox"><input type="checkbox" name="facility" value="has_cctv"> 监控</label>
                            <label class="checkbox"><input type="checkbox" name="facility" value="is_24h_access"> 24小时</label>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>可用车位数</label>
                        <input type="number" id="cs-spots" class="form-control" min="1" value="1">
                    </div>

                    <button type="submit" class="btn btn-primary btn-lg"><i class="fas fa-check"></i> 提交审核</button>
                </form>
            </div>
        </div>
    </div>

    <style>
    .create-spot-container { max-width: 700px; margin: 0 auto; }
    .form-control { width: 100%; padding: 12px 16px; border: 1px solid #ddd; border-radius: 8px; font-size: 15px; }
    .form-control:focus { border-color: #4A90D9; outline: none; box-shadow: 0 0 0 3px rgba(74,144,217,0.15); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; margin-bottom: 6px; font-weight: 500; }
    .card { background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .card-body { padding: 30px; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
    </style>

    <script>
    document.getElementById('create-spot-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const facilities = {};
        document.querySelectorAll('input[name="facility"]:checked').forEach(cb => {
            facilities[cb.value] = 1;
        });

        const spotData = {
            title: document.getElementById('cs-title').value,
            description: document.getElementById('cs-description').value,
            address: document.getElementById('cs-address').value,
            latitude: parseFloat(document.getElementById('cs-latitude').value) || 0,
            longitude: parseFloat(document.getElementById('cs-longitude').value) || 0,
            price_per_hour: parseFloat(document.getElementById('cs-price').value),
            price_per_day: parseFloat(document.getElementById('cs-price-day').value) || null,
            max_vehicle_height: parseFloat(document.getElementById('cs-height').value) || null,
            max_vehicle_width: parseFloat(document.getElementById('cs-width').value) || null,
            available_spots: parseInt(document.getElementById('cs-spots').value) || 1,
            ...facilities
        };

        try {
            const btn = this.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 提交中...';

            const result = await createParkingSpot(spotData);
            showSuccessToast('发布成功！等待管理员审核');
            setTimeout(() => { window.location.href = '/dashboard?tab=spots'; }, 1500);
        } catch (e) {
            showErrorToast(e.message || '发布失败');
            const btn = this.querySelector('button[type="submit"]');
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-check"></i> 提交审核';
        }
    });
    </script>
    <?php
}

/**
 * 预订页面 - 创建预订
 */
function includeBooking() {
    $spotId = $_GET['spot_id'] ?? '';
    ?>
    <div class="container booking-container">
        <h1 class="page-title"><i class="fas fa-calendar-check"></i> 创建预订</h1>

        <div class="booking-layout">
            <div class="booking-form-section">
                <div class="card">
                    <div class="card-header">
                        <h3><i class="fas fa-car"></i> 车辆信息</h3>
                    </div>
                    <div class="card-body">
                        <form id="booking-form">
                            <input type="hidden" id="booking-spot-id" value="<?php echo htmlspecialchars($spotId); ?>">

                            <div class="form-group">
                                <label for="vehicle-plate"><i class="fas fa-id-card"></i> 车牌号码</label>
                                <input type="text" id="vehicle-plate" name="vehicle_plate" required placeholder="如：京A12345" class="form-control">
                            </div>

                            <div class="form-group">
                                <label for="start-time"><i class="fas fa-clock"></i> 开始时间</label>
                                <input type="datetime-local" id="start-time" name="start_time" required class="form-control"
                                       value="<?php echo date('Y-m-d\TH:i'); ?>">
                            </div>

                            <div class="form-group">
                                <label for="end-time"><i class="fas fa-clock"></i> 结束时间</label>
                                <input type="datetime-local" id="end-time" name="end_time" required class="form-control"
                                       value="<?php echo date('Y-m-d\TH:i', strtotime('+2 hours')); ?>">
                            </div>

                            <div class="form-group">
                                <label for="booking-notes"><i class="fas fa-sticky-note"></i> 备注（可选）</label>
                                <textarea id="booking-notes" name="notes" rows="3" class="form-control" placeholder="如：车位编号、特殊要求等"></textarea>
                            </div>

                            <button type="submit" class="btn btn-primary btn-block btn-lg">
                                <i class="fas fa-check-circle"></i> 确认预订
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="booking-summary-section">
                <div class="card">
                    <div class="card-header">
                        <h3><i class="fas fa-receipt"></i> 预订摘要</h3>
                    </div>
                    <div class="card-body" id="booking-summary">
                        <div class="loading-spinner">
                            <i class="fas fa-spinner fa-spin"></i> 加载车位信息...
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3><i class="fas fa-credit-card"></i> 支付方式</h3>
                    </div>
                    <div class="card-body">
                        <div class="payment-methods">
                            <label class="payment-method selected">
                                <input type="radio" name="payment_method" value="wallet" checked>
                                <i class="fas fa-wallet"></i>
                                <span>钱包支付</span>
                            </label>
                            <label class="payment-method">
                                <input type="radio" name="payment_method" value="alipay">
                                <i class="fab fa-alipay"></i>
                                <span>支付宝</span>
                            </label>
                            <label class="payment-method">
                                <input type="radio" name="payment_method" value="wechat">
                                <i class="fab fa-weixin"></i>
                                <span>微信支付</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
    document.addEventListener('DOMContentLoaded', async function() {
        const spotId = document.getElementById('booking-spot-id').value;

        // 如果有spot_id，加载车位信息
        if (spotId) {
            try {
                const spot = await getParkingSpotDetail(parseInt(spotId));
                document.getElementById('booking-summary').innerHTML = `
                    <div class="summary-item">
                        <span class="summary-label">车位名称</span>
                        <span class="summary-value">${spot.title}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">地址</span>
                        <span class="summary-value">${spot.address}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">价格</span>
                        <span class="summary-value highlight">${formatPrice(spot.price_per_hour)}/小时</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">预计总价</span>
                        <span class="summary-value highlight" id="estimated-total">¥0.00</span>
                    </div>
                `;

                // 计算价格
                function calcPrice() {
                    const start = new Date(document.getElementById('start-time').value);
                    const end = new Date(document.getElementById('end-time').value);
                    if (start && end && end > start) {
                        const hours = (end - start) / 3600000;
                        const total = (hours * spot.price_per_hour).toFixed(2);
                        document.getElementById('estimated-total').textContent = `¥${total}`;
                    }
                }

                document.getElementById('start-time').addEventListener('change', calcPrice);
                document.getElementById('end-time').addEventListener('change', calcPrice);
                calcPrice();
            } catch (e) {
                document.getElementById('booking-summary').innerHTML =
                    '<div class="error-message">加载车位信息失败: ' + e.message + '</div>';
            }
        }

        // 提交预订
        document.getElementById('booking-form').addEventListener('submit', async function(e) {
            e.preventDefault();

            const startTime = new Date(document.getElementById('start-time').value);
            const endTime = new Date(document.getElementById('end-time').value);

            if (endTime <= startTime) {
                showErrorToast('结束时间必须晚于开始时间');
                return;
            }

            try {
                const btn = this.querySelector('button[type="submit"]');
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 处理中...';

                const paymentMethod = document.querySelector('input[name="payment_method"]:checked')?.value || 'wallet';

                const result = await createBooking({
                    spot_id: parseInt(spotId),
                    vehicle_plate_number: document.getElementById('vehicle-plate').value,
                    start_time: startTime.toISOString(),
                    end_time: endTime.toISOString(),
                    notes: document.getElementById('booking-notes').value
                });

                const booking = result.data || result;

                // 创建支付
                try {
                    await createPayment(booking.id || booking.booking_id, paymentMethod);
                } catch (payError) {
                    console.warn('支付创建提示:', payError.message);
                }

                showSuccessToast('预订创建成功！');
                setTimeout(() => {
                    window.location.href = '/dashboard?tab=bookings';
                }, 1000);

            } catch (e) {
                showErrorToast(e.message || '预订失败');
                const btn = this.querySelector('button[type="submit"]');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-check-circle"></i> 确认预订';
            }
        });
    });
    </script>
    <?php
}

/**
 * 用户仪表板
 */
function includeDashboard() {
    ?>
    <div class="container dashboard-container">
        <h1 class="page-title"><i class="fas fa-tachometer-alt"></i> 我的仪表板</h1>

        <div class="dashboard-tabs">
            <div class="tab-nav">
                <button class="tab-btn active" data-tab="overview">
                    <i class="fas fa-home"></i> 概览
                </button>
                <button class="tab-btn" data-tab="bookings">
                    <i class="fas fa-calendar-check"></i> 我的预订
                </button>
                <button class="tab-btn" data-tab="spots">
                    <i class="fas fa-parking"></i> 我的车位
                </button>
                <button class="tab-btn" data-tab="payments">
                    <i class="fas fa-credit-card"></i> 支付记录
                </button>
                <button class="tab-btn" data-tab="favorites">
                    <i class="fas fa-heart"></i> 我的收藏
                </button>
            </div>

            <div class="tab-content">
                <!-- 概览 -->
                <div class="tab-pane active" id="tab-overview">
                    <div class="stats-grid" id="dashboard-stats">
                        <div class="stat-card">
                            <div class="stat-icon"><i class="fas fa-calendar-check"></i></div>
                            <div class="stat-info"><h3 id="stat-bookings">-</h3><p>总预订</p></div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon"><i class="fas fa-parking"></i></div>
                            <div class="stat-info"><h3 id="stat-spots">-</h3><p>我的车位</p></div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon"><i class="fas fa-heart"></i></div>
                            <div class="stat-info"><h3 id="stat-favorites">-</h3><p>收藏</p></div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon"><i class="fas fa-yen-sign"></i></div>
                            <div class="stat-info"><h3 id="stat-spent">-</h3><p>总消费</p></div>
                        </div>
                    </div>
                    <div class="recent-activity">
                        <h3>最近预订</h3>
                        <div id="recent-bookings-list"><div class="loading-spinner">加载中...</div></div>
                    </div>
                </div>

                <!-- 预订列表 -->
                <div class="tab-pane" id="tab-bookings">
                    <div class="section-header">
                        <h3>我的预订记录</h3>
                    </div>
                    <div id="bookings-list"><div class="loading-spinner">加载中...</div></div>
                    <div class="pagination" id="bookings-pagination"></div>
                </div>

                <!-- 我的车位 -->
                <div class="tab-pane" id="tab-spots">
                    <div class="section-header">
                        <h3>我发布的车位</h3>
                        <a href="/parking/create" class="btn btn-primary"><i class="fas fa-plus"></i> 发布车位</a>
                    </div>
                    <div id="my-spots-list"><div class="loading-spinner">加载中...</div></div>
                    <div class="pagination" id="spots-pagination"></div>
                </div>

                <!-- 支付记录 -->
                <div class="tab-pane" id="tab-payments">
                    <div class="section-header">
                        <h3>支付记录</h3>
                    </div>
                    <div id="payments-list"><div class="loading-spinner">加载中...</div></div>
                    <div class="pagination" id="payments-pagination"></div>
                </div>

                <!-- 收藏 -->
                <div class="tab-pane" id="tab-favorites">
                    <div class="section-header">
                        <h3>我的收藏</h3>
                    </div>
                    <div id="favorites-list"><div class="loading-spinner">加载中...</div></div>
                    <div class="pagination" id="favorites-pagination"></div>
                </div>
            </div>
        </div>
    </div>

    <style>
    .dashboard-tabs { background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .tab-nav { display: flex; border-bottom: 2px solid #eee; overflow-x: auto; }
    .tab-btn { padding: 15px 24px; border: none; background: none; cursor: pointer; font-size: 15px;
               color: #666; border-bottom: 2px solid transparent; margin-bottom: -2px; white-space: nowrap; }
    .tab-btn.active { color: #4A90D9; border-bottom-color: #4A90D9; font-weight: 600; }
    .tab-pane { display: none; padding: 24px; }
    .tab-pane.active { display: block; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .stat-card { background: #f8f9fa; border-radius: 10px; padding: 20px; display: flex; align-items: center; gap: 16px; }
    .stat-icon { font-size: 32px; color: #4A90D9; }
    .stat-info h3 { margin: 0; font-size: 28px; }
    .stat-info p { margin: 4px 0 0; color: #888; font-size: 14px; }
    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .booking-item, .spot-item, .payment-item, .favorite-item {
        display: flex; justify-content: space-between; align-items: center;
        padding: 16px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 8px;
        transition: box-shadow 0.2s;
    }
    .booking-item:hover, .spot-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
    .item-status { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
    .status-pending { background: #FFF3CD; color: #856404; }
    .status-confirmed, .status-in_progress { background: #D1ECF1; color: #0C5460; }
    .status-completed { background: #D4EDDA; color: #155724; }
    .status-cancelled { background: #F8D7DA; color: #721C24; }
    .status-paid { background: #D4EDDA; color: #155724; }
    .status-refunded { background: #FFF3CD; color: #856404; }
    .status-failed { background: #F8D7DA; color: #721C24; }
    </style>

    <script>
    document.addEventListener('DOMContentLoaded', function() {
        // Tab 切换
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
                this.classList.add('active');
                document.getElementById('tab-' + this.dataset.tab).classList.add('active');
            });
        });

        // 加载数据
        loadDashboardData();
    });

    async function loadDashboardData() {
        try {
            // 加载预订
            const bookingsData = await getMyBookings(1, 5);
            const bookings = bookingsData.bookings || [];
            document.getElementById('stat-bookings').textContent = bookingsData.total || bookings.length;

            const recentList = document.getElementById('recent-bookings-list');
            if (bookings.length === 0) {
                recentList.innerHTML = '<p class="text-muted">暂无预订记录</p>';
            } else {
                recentList.innerHTML = bookings.map(b => `
                    <div class="booking-item">
                        <div>
                            <strong>${b.spot_title || '停车位'}</strong>
                            <br><small class="text-muted">${formatDate(b.start_time)} 至 ${formatDate(b.end_time)}</small>
                        </div>
                        <div style="text-align:right">
                            <span class="item-status status-${b.status}">${b.status}</span>
                            <br><small>¥${b.total_price}</small>
                        </div>
                    </div>
                `).join('');
            }

            // 全量预订
            loadBookings();
            loadMySpots();
            loadPayments();
            loadFavorites();

        } catch (e) {
            console.error('加载仪表板数据失败:', e);
        }
    }

    let bookingsPage = 1;
    async function loadBookings() {
        try {
            const data = await getMyBookings(bookingsPage, 20);
            const list = document.getElementById('bookings-list');
            const bookings = data.bookings || [];
            if (bookings.length === 0) {
                list.innerHTML = '<p class="text-muted" style="text-align:center;padding:40px;">暂无预订记录</p>';
            } else {
                list.innerHTML = bookings.map(b => `
                    <div class="booking-item">
                        <div>
                            <strong>${b.spot_title || '停车位#' + b.spot_id}</strong>
                            <br><small>📅 ${formatDate(b.start_time)} ~ ${formatDate(b.end_time)}</small>
                            <br><small>🚗 ${b.vehicle_plate_number || '未填'}</small>
                        </div>
                        <div style="text-align:right">
                            <span class="item-status status-${b.status}">${statusText(b.status)}</span>
                            <br><strong>¥${b.total_price}</strong>
                            <br>
                            ${b.status === 'pending' || b.status === 'confirmed' ?
                                `<button class="btn btn-sm btn-outline" onclick="cancelBookingItem(${b.id})">取消</button>` : ''}
                        </div>
                    </div>
                `).join('');
            }
            renderSimplePagination('bookings-pagination', data.total || 0, 20, bookingsPage, (p) => {
                bookingsPage = p; loadBookings();
            });
        } catch(e) { document.getElementById('bookings-list').innerHTML = '<p class="text-danger">加载失败</p>'; }
    }

    let spotsPage = 1;
    async function loadMySpots() {
        try {
            const data = await getMySpots(spotsPage, 20);
            const spots = data.spots || [];
            document.getElementById('stat-spots').textContent = data.pagination?.total || spots.length;
            const list = document.getElementById('my-spots-list');
            if (spots.length === 0) {
                list.innerHTML = '<p class="text-muted" style="text-align:center;padding:40px;">您还没有发布车位<br><a href="/parking/create" class="btn btn-primary mt-3">立即发布</a></p>';
            } else {
                list.innerHTML = spots.map(s => `
                    <div class="spot-item">
                        <div>
                            <strong>${s.title}</strong>
                            <br><small>📍 ${s.address}</small>
                            <br><small>💰 ¥${s.price_per_hour}/小时</small>
                        </div>
                        <div style="text-align:right">
                            <span class="item-status ${s.is_approved ? 'status-completed' : 'status-pending'}">
                                ${s.is_approved ? '已审核' : '待审核'}
                            </span>
                            <br><small>${s.total_bookings || 0}次预订</small>
                        </div>
                    </div>
                `).join('');
            }
            document.getElementById('tab-spots').querySelector('.stat-card')?.remove();
        } catch(e) {}
    }

    let paymentsPage = 1;
    async function loadPayments() {
        try {
            const data = await getMyPayments(paymentsPage, 20);
            const payments = data.payments || [];
            const total = payments.reduce((sum, p) => sum + parseFloat(p.amount || 0), 0);
            document.getElementById('stat-spent').textContent = '¥' + total.toFixed(0);
            const list = document.getElementById('payments-list');
            if (payments.length === 0) {
                list.innerHTML = '<p class="text-muted" style="text-align:center;padding:40px;">暂无支付记录</p>';
            } else {
                list.innerHTML = payments.map(p => `
                    <div class="payment-item">
                        <div>
                            <strong>交易号: ${p.transaction_id || 'N/A'}</strong>
                            <br><small>📅 ${formatDate(p.created_at)}</small>
                            <br><small>💳 ${p.payment_method || '-'}</small>
                        </div>
                        <div style="text-align:right">
                            <span class="item-status status-${p.status}">${statusText(p.status)}</span>
                            <br><strong>¥${p.amount}</strong>
                        </div>
                    </div>
                `).join('');
            }
            renderSimplePagination('payments-pagination', data.pagination?.total || 0, 20, paymentsPage, (p) => {
                paymentsPage = p; loadPayments();
            });
        } catch(e) {}
    }

    let favoritesPage = 1;
    async function loadFavorites() {
        try {
            const data = await getFavorites(favoritesPage, 20);
            const favorites = data.favorites || [];
            document.getElementById('stat-favorites').textContent = data.pagination?.total || favorites.length;
            const list = document.getElementById('favorites-list');
            if (favorites.length === 0) {
                list.innerHTML = '<p class="text-muted" style="text-align:center;padding:40px;">暂无收藏</p>';
            } else {
                list.innerHTML = favorites.map(f => `
                    <div class="favorite-item">
                        <div>
                            <strong>${f.title}</strong>
                            <br><small>📍 ${f.address}</small>
                        </div>
                        <div style="text-align:right">
                            <strong>¥${f.price_per_hour}/小时</strong>
                            <br><button class="btn btn-sm btn-outline" onclick="removeFavorite(${f.spot_id});this.closest('.favorite-item').remove();">
                                <i class="fas fa-heart-broken"></i> 取消收藏
                            </button>
                        </div>
                    </div>
                `).join('');
            }
            renderSimplePagination('favorites-pagination', data.pagination?.total || 0, 20, favoritesPage, (p) => {
                favoritesPage = p; loadFavorites();
            });
        } catch(e) {}
    }

    async function cancelBookingItem(bookingId) {
        if (!confirm('确定取消该预订？')) return;
        try {
            await cancelBooking(bookingId);
            showSuccessToast('取消成功');
            loadBookings();
        } catch(e) { showErrorToast(e.message || '取消失败'); }
    }

    function statusText(status) {
        const map = { pending: '待确认', confirmed: '已确认', in_progress: '进行中', completed: '已完成',
                      cancelled: '已取消', expired: '已过期', paid: '已支付', refunded: '已退款', failed: '失败' };
        return map[status] || status;
    }

    function renderSimplePagination(containerId, total, limit, current, callback) {
        const pages = Math.ceil(total / limit);
        const el = document.getElementById(containerId);
        if (!el) return;
        if (pages <= 1) { el.innerHTML = ''; return; }
        let html = '<div class="pagination">';
        html += `<button class="page-btn" onclick="(${callback.toString()})(${current - 1})" ${current <= 1 ? 'disabled' : ''}>上一页</button>`;
        for (let i = 1; i <= pages; i++) {
            if (i === current || i === 1 || i === pages || Math.abs(i - current) <= 2) {
                html += `<button class="page-btn ${i === current ? 'active' : ''}" onclick="(${callback.toString()})(${i})">${i}</button>`;
            } else if (Math.abs(i - current) === 3) {
                html += '<span class="page-dots">...</span>';
            }
        }
        html += `<button class="page-btn" onclick="(${callback.toString()})(${current + 1})" ${current >= pages ? 'disabled' : ''}>下一页</button>`;
        html += '</div>';
        el.innerHTML = html;
    }
    </script>
    <?php
}

/**
 * 个人资料页面
 */
function includeProfile() {
    ?>
    <div class="container profile-container">
        <h1 class="page-title"><i class="fas fa-user"></i> 个人资料</h1>

        <div class="profile-layout">
            <div class="profile-sidebar">
                <div class="card text-center">
                    <div class="card-body">
                        <div class="avatar-wrapper">
                            <img id="profile-avatar" src="/assets/images/avatar.txt" alt="头像" style="width:120px;height:120px;border-radius:50%;object-fit:cover;background:#eee;">
                        </div>
                        <h3 id="profile-username">-</h3>
                        <p id="profile-email" class="text-muted">-</p>
                    </div>
                </div>
            </div>

            <div class="profile-main">
                <div class="card">
                    <div class="card-header"><h3><i class="fas fa-edit"></i> 编辑资料</h3></div>
                    <div class="card-body">
                        <form id="profile-form">
                            <div class="form-row">
                                <div class="form-group">
                                    <label>真实姓名</label>
                                    <input type="text" id="pf-real-name" name="real_name" class="form-control">
                                </div>
                                <div class="form-group">
                                    <label>性别</label>
                                    <select id="pf-gender" name="gender" class="form-control">
                                        <option value="">保密</option>
                                        <option value="male">男</option>
                                        <option value="female">女</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label>出生日期</label>
                                <input type="date" id="pf-birth-date" name="birth_date" class="form-control">
                            </div>
                            <div class="form-group">
                                <label>车牌号码</label>
                                <input type="text" id="pf-plate" name="vehicle_plate_number" class="form-control" placeholder="如：京A12345">
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label>车辆品牌</label>
                                    <input type="text" id="pf-brand" name="vehicle_brand" class="form-control">
                                </div>
                                <div class="form-group">
                                    <label>车辆型号</label>
                                    <input type="text" id="pf-model" name="vehicle_model" class="form-control">
                                </div>
                            </div>
                            <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> 保存修改</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <style>
    .profile-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .form-control { width: 100%; padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; }
    .form-control:focus { border-color: #4A90D9; outline: none; box-shadow: 0 0 0 3px rgba(74,144,217,0.15); }
    .card { background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 20px; }
    .card-header { padding: 16px 20px; border-bottom: 1px solid #eee; }
    .card-header h3 { margin: 0; font-size: 16px; }
    .card-body { padding: 20px; }
    .text-center { text-align: center; }
    .text-muted { color: #999; }
    @media (max-width: 768px) { .profile-layout { grid-template-columns: 1fr; } .form-row { grid-template-columns: 1fr; } }
    </style>

    <script>
    document.addEventListener('DOMContentLoaded', async function() {
        try {
            const user = await getUserProfile();
            if (user) {
                document.getElementById('profile-username').textContent = user.username || '-';
                document.getElementById('profile-email').textContent = user.email || '-';
                if (user.avatar_url) document.getElementById('profile-avatar').src = user.avatar_url;
                document.getElementById('pf-real-name').value = user.real_name || '';
                document.getElementById('pf-gender').value = user.gender || '';
                document.getElementById('pf-birth-date').value = user.birth_date || '';
                document.getElementById('pf-plate').value = user.vehicle_plate_number || '';
                document.getElementById('pf-brand').value = user.vehicle_brand || '';
                document.getElementById('pf-model').value = user.vehicle_model || '';
            }
        } catch (e) { console.error('加载用户资料失败:', e); }

        document.getElementById('profile-form').addEventListener('submit', async function(e) {
            e.preventDefault();
            try {
                const btn = this.querySelector('button[type="submit"]');
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 保存中...';
                await updateUserProfile({
                    real_name: document.getElementById('pf-real-name').value,
                    gender: document.getElementById('pf-gender').value,
                    birth_date: document.getElementById('pf-birth-date').value,
                    vehicle_plate_number: document.getElementById('pf-plate').value,
                    vehicle_brand: document.getElementById('pf-brand').value,
                    vehicle_model: document.getElementById('pf-model').value
                });
                showSuccessToast('资料更新成功');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-save"></i> 保存修改';
            } catch (e) {
                showErrorToast(e.message || '保存失败');
                const btn = this.querySelector('button[type="submit"]');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-save"></i> 保存修改';
            }
        });
    });
    </script>
    <?php
}

/**
 * 消息中心页面
 */
function includeMessages() {
    ?>
    <div class="container messages-container">
        <h1 class="page-title"><i class="fas fa-envelope"></i> 消息中心</h1>

        <div class="messages-layout">
            <div class="messages-sidebar">
                <div class="card">
                    <div class="card-body" style="padding:0;">
                        <div class="msg-nav">
                            <button class="msg-nav-btn active" data-box="inbox">
                                <i class="fas fa-inbox"></i> 收件箱
                                <span class="unread-badge" id="unread-badge"></span>
                            </button>
                            <button class="msg-nav-btn" data-box="outbox">
                                <i class="fas fa-paper-plane"></i> 发件箱
                            </button>
                        </div>
                    </div>
                </div>
                <div class="card mt-3">
                    <div class="card-header"><h3><i class="fas fa-plus"></i> 发送消息</h3></div>
                    <div class="card-body">
                        <form id="send-message-form">
                            <div class="form-group">
                                <label>接收者ID</label>
                                <input type="number" id="msg-receiver" class="form-control" required placeholder="用户ID">
                            </div>
                            <div class="form-group">
                                <label>主题</label>
                                <input type="text" id="msg-subject" class="form-control" placeholder="消息主题">
                            </div>
                            <div class="form-group">
                                <label>内容</label>
                                <textarea id="msg-content" class="form-control" rows="4" required placeholder="请输入消息内容..."></textarea>
                            </div>
                            <button type="submit" class="btn btn-primary btn-block">发送</button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="messages-main">
                <div class="card">
                    <div class="card-header">
                        <h3 id="messages-title"><i class="fas fa-inbox"></i> 收件箱</h3>
                    </div>
                    <div class="card-body" id="messages-list" style="min-height:400px;">
                        <div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> 加载中...</div>
                    </div>
                    <div class="card-footer" id="messages-pagination"></div>
                </div>
            </div>
        </div>
    </div>

    <style>
    .messages-layout { display: grid; grid-template-columns: 300px 1fr; gap: 24px; }
    .msg-nav { display: flex; flex-direction: column; }
    .msg-nav-btn { padding: 14px 20px; border: none; background: none; text-align: left; cursor: pointer;
                   font-size: 15px; display: flex; justify-content: space-between; align-items: center;
                   border-bottom: 1px solid #f0f0f0; }
    .msg-nav-btn.active { background: #f0f7ff; color: #4A90D9; font-weight: 600; }
    .msg-nav-btn:hover { background: #f8f9fa; }
    .unread-badge { background: #f44336; color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 10px; }
    .msg-item { padding: 16px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
    .msg-item:hover { background: #f8f9fa; }
    .msg-item.unread { background: #f0f7ff; font-weight: 600; }
    .msg-item .msg-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
    .msg-item .msg-sender { font-weight: 600; }
    .msg-item .msg-time { color: #999; font-size: 13px; }
    .msg-item .msg-preview { color: #666; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .mt-3 { margin-top: 16px; }
    .card-footer { padding: 12px 20px; border-top: 1px solid #eee; }
    @media (max-width: 768px) { .messages-layout { grid-template-columns: 1fr; } }
    </style>

    <script>
    let msgBox = 'inbox';
    let msgPage = 1;

    document.addEventListener('DOMContentLoaded', function() {
        loadMessages();
        updateUnreadBadge();

        document.querySelectorAll('.msg-nav-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.msg-nav-btn').forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                msgBox = this.dataset.box;
                msgPage = 1;
                document.getElementById('messages-title').innerHTML =
                    `<i class="fas ${msgBox === 'inbox' ? 'fa-inbox' : 'fa-paper-plane'}"></i> ${msgBox === 'inbox' ? '收件箱' : '发件箱'}`;
                loadMessages();
            });
        });

        document.getElementById('send-message-form').addEventListener('submit', async function(e) {
            e.preventDefault();
            try {
                await sendMessage(
                    parseInt(document.getElementById('msg-receiver').value),
                    document.getElementById('msg-content').value,
                    document.getElementById('msg-subject').value
                );
                showSuccessToast('消息发送成功');
                this.reset();
                if (msgBox === 'outbox') loadMessages();
            } catch(e) { showErrorToast(e.message || '发送失败'); }
        });
    });

    async function loadMessages() {
        try {
            const data = msgBox === 'inbox' ? await getInbox(msgPage, 20) : await getOutbox(msgPage, 20);
            const messages = data.messages || [];
            const list = document.getElementById('messages-list');
            if (messages.length === 0) {
                list.innerHTML = '<p style="text-align:center;padding:60px;color:#999;">暂无消息</p>';
            } else {
                list.innerHTML = messages.map(m => `
                    <div class="msg-item ${!m.is_read && msgBox === 'inbox' ? 'unread' : ''}"
                         onclick="${msgBox === 'inbox' ? `markRead(${m.id})` : ''}">
                        <div class="msg-header">
                            <span class="msg-sender">
                                ${msgBox === 'inbox' ? '📩' : '📤'} ${m.sender_username || '用户#' + m.sender_id}
                            </span>
                            <span class="msg-time">${formatDate(m.created_at)}</span>
                        </div>
                        ${m.subject ? `<div class="msg-subject" style="font-size:14px;margin-bottom:2px;"><strong>${m.subject}</strong></div>` : ''}
                        <div class="msg-preview">${m.content}</div>
                    </div>
                `).join('');
            }
            renderSimplePagination('messages-pagination', data.pagination?.total || 0, 20, msgPage, (p) => {
                msgPage = p; loadMessages();
            });
        } catch(e) {
            document.getElementById('messages-list').innerHTML = '<p style="text-align:center;padding:60px;color:#999;">加载失败</p>';
        }
    }

    async function markRead(id) {
        try { await markMessageRead(id); } catch(e) {}
    }

    async function updateUnreadBadge() {
        try {
            const count = await getUnreadCount();
            const badge = document.getElementById('unread-badge');
            if (count > 0) {
                badge.textContent = count;
                badge.style.display = 'inline';
            } else {
                badge.style.display = 'none';
            }
        } catch(e) {}
    }
    </script>
    <?php
}

/**
 * 管理后台页面
 */
function includeAdmin() {
    ?>
    <div class="container admin-container">
        <h1 class="page-title"><i class="fas fa-cog"></i> 管理后台</h1>

        <div class="admin-tabs">
            <div class="tab-nav">
                <button class="tab-btn active" data-tab="admin-overview"><i class="fas fa-chart-bar"></i> 概览</button>
                <button class="tab-btn" data-tab="admin-spots"><i class="fas fa-parking"></i> 车位管理</button>
                <button class="tab-btn" data-tab="admin-users"><i class="fas fa-users"></i> 用户管理</button>
            </div>

            <div class="tab-content">
                <!-- 概览 -->
                <div class="tab-pane active" id="tab-admin-overview">
                    <div class="stats-grid" id="admin-stats">
                        <div class="loading-spinner">加载统计中...</div>
                    </div>
                </div>

                <!-- 车位管理 -->
                <div class="tab-pane" id="tab-admin-spots">
                    <div class="section-header">
                        <h3>所有停车位</h3>
                        <select id="admin-spot-filter" onchange="loadAdminSpots()" style="padding:8px;border:1px solid #ddd;border-radius:6px;">
                            <option value="">全部状态</option>
                            <option value="0">待审核</option>
                            <option value="1">已审核</option>
                        </select>
                    </div>
                    <div id="admin-spots-list"><div class="loading-spinner">加载中...</div></div>
                    <div class="pagination" id="admin-spots-pagination"></div>
                </div>

                <!-- 用户管理 -->
                <div class="tab-pane" id="tab-admin-users">
                    <div class="section-header">
                        <h3>所有用户</h3>
                    </div>
                    <div id="admin-users-list"><div class="loading-spinner">加载中...</div></div>
                    <div class="pagination" id="admin-users-pagination"></div>
                </div>
            </div>
        </div>
    </div>

    <style>
    .admin-tabs { background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .admin-table { width: 100%; border-collapse: collapse; }
    .admin-table th, .admin-table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eee; }
    .admin-table th { background: #f8f9fa; font-weight: 600; font-size: 14px; }
    .admin-table tr:hover { background: #f8f9fa; }
    .btn-sm { padding: 6px 14px; font-size: 13px; border-radius: 6px; }
    .btn-success { background: #28a745; color: #fff; border: none; }
    .btn-danger { background: #dc3545; color: #fff; border: none; }
    .btn-warning { background: #ffc107; color: #333; border: none; }
    </style>

    <script>
    let adminSpotsPage = 1, adminUsersPage = 1;

    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('.admin-tabs .tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.admin-tabs .tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.admin-tabs .tab-pane').forEach(p => p.classList.remove('active'));
                this.classList.add('active');
                document.getElementById('tab-' + this.dataset.tab).classList.add('active');
                if (this.dataset.tab === 'admin-overview') loadAdminStats();
                if (this.dataset.tab === 'admin-spots') loadAdminSpots();
                if (this.dataset.tab === 'admin-users') loadAdminUsers();
            });
        });
        loadAdminStats();
    });

    async function loadAdminStats() {
        try {
            const s = await getAdminStats();
            document.getElementById('admin-stats').innerHTML = `
                <div class="stat-card"><div class="stat-icon"><i class="fas fa-users"></i></div>
                    <div class="stat-info"><h3>${s.total?.users || 0}</h3><p>总用户</p></div></div>
                <div class="stat-card"><div class="stat-icon"><i class="fas fa-parking"></i></div>
                    <div class="stat-info"><h3>${s.total?.spots || 0}</h3><p>总车位</p></div></div>
                <div class="stat-card"><div class="stat-icon"><i class="fas fa-clock"></i></div>
                    <div class="stat-info"><h3>${s.total?.pending_approval || 0}</h3><p>待审核</p></div></div>
                <div class="stat-card"><div class="stat-icon"><i class="fas fa-calendar-check"></i></div>
                    <div class="stat-info"><h3>${s.total?.bookings || 0}</h3><p>总预订</p></div></div>
                <div class="stat-card"><div class="stat-icon"><i class="fas fa-yen-sign"></i></div>
                    <div class="stat-info"><h3>¥${parseFloat(s.total?.revenue || 0).toFixed(0)}</h3><p>总收入</p></div></div>
                <div class="stat-card"><div class="stat-icon"><i class="fas fa-star"></i></div>
                    <div class="stat-info"><h3>${s.total?.reviews || 0}</h3><p>评价数</p></div></div>
            `;
        } catch(e) {
            document.getElementById('admin-stats').innerHTML = '<p>加载失败，请确认管理员权限</p>';
        }
    }

    async function loadAdminSpots() {
        try {
            const filter = document.getElementById('admin-spot-filter').value;
            const filters = {};
            if (filter !== '') filters.is_approved = filter;

            const data = await getAdminSpots(filters, adminSpotsPage, 20);
            const spots = data.spots || [];
            const list = document.getElementById('admin-spots-list');
            if (spots.length === 0) {
                list.innerHTML = '<p style="text-align:center;padding:40px;color:#999;">暂无车位</p>';
            } else {
                list.innerHTML = `<table class="admin-table">
                    <thead><tr>
                        <th>ID</th><th>标题</th><th>所有者</th><th>价格</th><th>状态</th><th>预订数</th><th>操作</th>
                    </tr></thead><tbody>
                    ${spots.map(s => `
                        <tr>
                            <td>${s.id}</td>
                            <td>${s.title}</td>
                            <td>${s.owner_username || '用户#' + s.owner_id}</td>
                            <td>¥${s.price_per_hour}/h</td>
                            <td><span class="item-status ${s.is_approved ? 'status-completed' : 'status-pending'}">
                                ${s.is_approved ? '已审核' : '待审核'}</span></td>
                            <td>${s.total_bookings || 0}</td>
                            <td>
                                ${!s.is_approved ?
                                    `<button class="btn btn-sm btn-success" onclick="approveSpotItem(${s.id}, true)">通过</button>
                                     <button class="btn btn-sm btn-danger" onclick="approveSpotItem(${s.id}, false)">拒绝</button>`
                                    : `<span class="text-muted">已处理</span>`}
                            </td>
                        </tr>
                    `).join('')}
                    </tbody></table>`;
            }
            renderSimplePagination('admin-spots-pagination', data.pagination?.total || 0, 20, adminSpotsPage, (p) => {
                adminSpotsPage = p; loadAdminSpots();
            });
        } catch(e) {
            document.getElementById('admin-spots-list').innerHTML = '<p style="text-align:center;padding:40px;color:#999;">加载失败</p>';
        }
    }

    async function approveSpotItem(spotId, approved) {
        const action = approved ? '通过' : '拒绝';
        if (!confirm(`确定${action}该车位？`)) return;
        try {
            await approveSpot(spotId, approved);
            showSuccessToast(`车位已${action}`);
            loadAdminSpots();
        } catch(e) { showErrorToast(e.message || '操作失败'); }
    }

    async function loadAdminUsers() {
        try {
            const data = await getAdminUsers({}, adminUsersPage, 20);
            const users = data.users || [];
            const list = document.getElementById('admin-users-list');
            if (users.length === 0) {
                list.innerHTML = '<p style="text-align:center;padding:40px;color:#999;">暂无用户</p>';
            } else {
                list.innerHTML = `<table class="admin-table">
                    <thead><tr>
                        <th>ID</th><th>用户名</th><th>邮箱</th><th>手机</th><th>角色</th><th>状态</th><th>车位</th><th>注册时间</th>
                    </tr></thead><tbody>
                    ${users.map(u => `
                        <tr>
                            <td>${u.id}</td>
                            <td>${u.username}</td>
                            <td>${u.email}</td>
                            <td>${u.phone || '-'}</td>
                            <td>${u.role === 'admin' ? '管理员' : '用户'}</td>
                            <td><span class="item-status ${u.is_active ? 'status-completed' : 'status-cancelled'}">
                                ${u.is_active ? '正常' : '禁用'}</span></td>
                            <td>${u.spot_count || 0}</td>
                            <td>${formatDate(u.created_at)}</td>
                        </tr>
                    `).join('')}
                    </tbody></table>`;
            }
            renderSimplePagination('admin-users-pagination', data.pagination?.total || 0, 20, adminUsersPage, (p) => {
                adminUsersPage = p; loadAdminUsers();
            });
        } catch(e) {
            document.getElementById('admin-users-list').innerHTML = '<p style="text-align:center;padding:40px;color:#999;">加载失败</p>';
        }
    }
    </script>
    <?php
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