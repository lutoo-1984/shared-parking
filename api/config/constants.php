<?php
/**
 * 应用程序常量定义
 */

// 应用程序信息
define('APP_NAME', '共享停车位');
define('APP_VERSION', '1.0.0');
define('APP_ENV', getenv('APP_ENV') ?: 'development');
define('APP_DEBUG', filter_var(getenv('APP_DEBUG') ?: true, FILTER_VALIDATE_BOOLEAN));

// 路径常量
define('ROOT_PATH', dirname(dirname(dirname(__FILE__))));
define('API_PATH', ROOT_PATH . '/api');
define('WEB_PATH', ROOT_PATH . '/web');
define('UPLOAD_PATH', ROOT_PATH . '/uploads');
define('LOG_PATH', ROOT_PATH . '/logs');

// URL常量
define('APP_URL', getenv('APP_URL') ?: 'http://localhost:8080');
define('API_URL', APP_URL . '/api');
define('WEB_URL', APP_URL);

// 时间常量
define('TIMEZONE', 'Asia/Shanghai');
date_default_timezone_set(TIMEZONE);

// 数据库常量
define('DB_HOST', getenv('DB_HOST') ?: 'localhost');
define('DB_PORT', getenv('DB_PORT') ?: '3306');
define('DB_DATABASE', getenv('DB_DATABASE') ?: 'shared_parking');
define('DB_USERNAME', getenv('DB_USERNAME') ?: 'root');
define('DB_PASSWORD', getenv('DB_PASSWORD') ?: '');

// JWT配置
define('JWT_SECRET', getenv('JWT_SECRET') ?: 'your_default_jwt_secret_change_this');
define('JWT_ALGORITHM', 'HS256');
define('JWT_EXPIRE_HOURS', 24 * 7); // 7天

// 文件上传配置
define('UPLOAD_MAX_SIZE', getenv('UPLOAD_MAX_SIZE') ?: 5242880); // 5MB
define('ALLOWED_IMAGE_TYPES', ['image/jpeg', 'image/png', 'image/gif']);
define('UPLOAD_DIR', 'uploads/');

// 停车位相关常量
define('PRICE_UNIT_HOUR', 'hour');
define('PRICE_UNIT_DAY', 'day');
define('DEFAULT_SEARCH_RADIUS', 5000); // 5公里

// 预订状态
define('BOOKING_STATUS_PENDING', 'pending');
define('BOOKING_STATUS_CONFIRMED', 'confirmed');
define('BOOKING_STATUS_IN_PROGRESS', 'in_progress');
define('BOOKING_STATUS_COMPLETED', 'completed');
define('BOOKING_STATUS_CANCELLED', 'cancelled');

// 支付状态
define('PAYMENT_STATUS_PENDING', 'pending');
define('PAYMENT_STATUS_PAID', 'paid');
define('PAYMENT_STATUS_REFUNDED', 'refunded');
define('PAYMENT_STATUS_FAILED', 'failed');

// 支付方式
define('PAYMENT_METHOD_ALIPAY', 'alipay');
define('PAYMENT_METHOD_WECHAT', 'wechat');
define('PAYMENT_METHOD_CREDIT_CARD', 'credit_card');

// 用户角色
define('USER_ROLE_USER', 'user');
define('USER_ROLE_ADMIN', 'admin');

// 响应状态码
define('HTTP_OK', 200);
define('HTTP_CREATED', 201);
define('HTTP_BAD_REQUEST', 400);
define('HTTP_UNAUTHORIZED', 401);
define('HTTP_FORBIDDEN', 403);
define('HTTP_NOT_FOUND', 404);
define('HTTP_METHOD_NOT_ALLOWED', 405);
define('HTTP_CONFLICT', 409);
define('HTTP_INTERNAL_ERROR', 500);

// 错误消息
define('ERROR_DATABASE', '数据库操作失败');
define('ERROR_VALIDATION', '输入验证失败');
define('ERROR_AUTHENTICATION', '认证失败');
define('ERROR_AUTHORIZATION', '权限不足');
define('ERROR_NOT_FOUND', '资源不存在');
define('ERROR_INTERNAL', '服务器内部错误');

// 成功消息
define('SUCCESS_CREATED', '创建成功');
define('SUCCESS_UPDATED', '更新成功');
define('SUCCESS_DELETED', '删除成功');
define('SUCCESS_OPERATION', '操作成功');

// 地图API配置
define('BAIDU_MAP_AK', 'SN0sXrTjayBcd13iBAaVYswawCGXhOG1');
define('MAP_DEFAULT_ZOOM', 15);
define('MAP_MAX_ZOOM', 18);
define('MAP_MIN_ZOOM', 10);

// 分页配置
define('DEFAULT_PAGE_SIZE', 20);
define('MAX_PAGE_SIZE', 100);

// 缓存配置
define('CACHE_ENABLED', true);
define('CACHE_TTL', 3600); // 1小时

// 会话配置
define('SESSION_LIFETIME', 120); // 2小时，单位：分钟

// 安全配置
define('PASSWORD_MIN_LENGTH', 6);
define('PASSWORD_MAX_LENGTH', 72);
define('LOGIN_ATTEMPTS_LIMIT', 5);
define('LOGIN_LOCKOUT_TIME', 900); // 15分钟，单位：秒

// 验证码配置
define('SMS_CODE_LENGTH', 6);
define('SMS_CODE_EXPIRE', 300); // 5分钟，单位：秒
define('SMS_RESEND_INTERVAL', 60); // 1分钟，单位：秒

// 货币配置
define('CURRENCY', 'CNY');
define('CURRENCY_SYMBOL', '¥');

// 环境检测函数
function isProduction() {
    return APP_ENV === 'production';
}

function isDevelopment() {
    return APP_ENV === 'development';
}

function isTesting() {
    return APP_ENV === 'testing';
}

// 调试函数
function debugLog($message, $data = null) {
    if (APP_DEBUG) {
        error_log('[DEBUG] ' . $message . ($data ? ' - ' . json_encode($data) : ''));
    }
}