<?php
/**
 * 应用程序常量定义
 */

// 应用程序信息
defined('APP_NAME') or define('APP_NAME', '共享停车位');
defined('APP_VERSION') or define('APP_VERSION', '1.0.0');
defined('APP_ENV') or define('APP_ENV', getenv('APP_ENV') ?: 'development');
defined('APP_DEBUG') or define('APP_DEBUG', filter_var(getenv('APP_DEBUG') ?: true, FILTER_VALIDATE_BOOLEAN));

// 路径常量
if (!defined('ROOT_PATH')) {
    define('ROOT_PATH', dirname(dirname(dirname(__FILE__))));
}
defined('API_PATH') or define('API_PATH', ROOT_PATH . '/api');
defined('WEB_PATH') or define('WEB_PATH', ROOT_PATH . '/web');
defined('UPLOAD_PATH') or define('UPLOAD_PATH', ROOT_PATH . '/uploads');
defined('LOG_PATH') or define('LOG_PATH', ROOT_PATH . '/logs');

// URL常量
defined('APP_URL') or define('APP_URL', getenv('APP_URL') ?: 'http://localhost:8080');
defined('API_URL') or define('API_URL', APP_URL . '/api');
defined('WEB_URL') or define('WEB_URL', APP_URL);

// 时间常量
defined('TIMEZONE') or define('TIMEZONE', 'Asia/Shanghai');
date_default_timezone_set(TIMEZONE);

// 数据库常量
defined('DB_HOST') or define('DB_HOST', getenv('DB_HOST') ?: 'localhost');
defined('DB_PORT') or define('DB_PORT', getenv('DB_PORT') ?: '3306');
defined('DB_DATABASE') or define('DB_DATABASE', getenv('DB_DATABASE') ?: 'shared_parking');
defined('DB_USERNAME') or define('DB_USERNAME', getenv('DB_USERNAME') ?: 'root');
defined('DB_PASSWORD') or define('DB_PASSWORD', getenv('DB_PASSWORD') ?: '');

// JWT配置
defined('JWT_SECRET') or define('JWT_SECRET', getenv('JWT_SECRET') ?: 'your_default_jwt_secret_change_this');
defined('JWT_ALGORITHM') or define('JWT_ALGORITHM', 'HS256');
defined('JWT_EXPIRE_HOURS') or define('JWT_EXPIRE_HOURS', 24 * 7);

// 文件上传配置
defined('UPLOAD_MAX_SIZE') or define('UPLOAD_MAX_SIZE', getenv('UPLOAD_MAX_SIZE') ?: 5242880);
defined('ALLOWED_IMAGE_TYPES') or define('ALLOWED_IMAGE_TYPES', ['image/jpeg', 'image/png', 'image/gif']);
defined('UPLOAD_DIR') or define('UPLOAD_DIR', 'uploads/');

// 停车位相关常量
defined('PRICE_UNIT_HOUR') or define('PRICE_UNIT_HOUR', 'hour');
defined('PRICE_UNIT_DAY') or define('PRICE_UNIT_DAY', 'day');
defined('DEFAULT_SEARCH_RADIUS') or define('DEFAULT_SEARCH_RADIUS', 5000);

// 预订状态
defined('BOOKING_STATUS_PENDING') or define('BOOKING_STATUS_PENDING', 'pending');
defined('BOOKING_STATUS_CONFIRMED') or define('BOOKING_STATUS_CONFIRMED', 'confirmed');
defined('BOOKING_STATUS_IN_PROGRESS') or define('BOOKING_STATUS_IN_PROGRESS', 'in_progress');
defined('BOOKING_STATUS_COMPLETED') or define('BOOKING_STATUS_COMPLETED', 'completed');
defined('BOOKING_STATUS_CANCELLED') or define('BOOKING_STATUS_CANCELLED', 'cancelled');

// 支付状态
defined('PAYMENT_STATUS_PENDING') or define('PAYMENT_STATUS_PENDING', 'pending');
defined('PAYMENT_STATUS_PAID') or define('PAYMENT_STATUS_PAID', 'paid');
defined('PAYMENT_STATUS_REFUNDED') or define('PAYMENT_STATUS_REFUNDED', 'refunded');
defined('PAYMENT_STATUS_FAILED') or define('PAYMENT_STATUS_FAILED', 'failed');

// 支付方式
defined('PAYMENT_METHOD_ALIPAY') or define('PAYMENT_METHOD_ALIPAY', 'alipay');
defined('PAYMENT_METHOD_WECHAT') or define('PAYMENT_METHOD_WECHAT', 'wechat');
defined('PAYMENT_METHOD_CREDIT_CARD') or define('PAYMENT_METHOD_CREDIT_CARD', 'credit_card');

// 用户角色
defined('USER_ROLE_USER') or define('USER_ROLE_USER', 'user');
defined('USER_ROLE_ADMIN') or define('USER_ROLE_ADMIN', 'admin');

// 响应状态码
defined('HTTP_OK') or define('HTTP_OK', 200);
defined('HTTP_CREATED') or define('HTTP_CREATED', 201);
defined('HTTP_BAD_REQUEST') or define('HTTP_BAD_REQUEST', 400);
defined('HTTP_UNAUTHORIZED') or define('HTTP_UNAUTHORIZED', 401);
defined('HTTP_FORBIDDEN') or define('HTTP_FORBIDDEN', 403);
defined('HTTP_NOT_FOUND') or define('HTTP_NOT_FOUND', 404);
defined('HTTP_METHOD_NOT_ALLOWED') or define('HTTP_METHOD_NOT_ALLOWED', 405);
defined('HTTP_CONFLICT') or define('HTTP_CONFLICT', 409);
defined('HTTP_INTERNAL_ERROR') or define('HTTP_INTERNAL_ERROR', 500);

// 错误消息
defined('ERROR_DATABASE') or define('ERROR_DATABASE', '数据库操作失败');
defined('ERROR_VALIDATION') or define('ERROR_VALIDATION', '输入验证失败');
defined('ERROR_AUTHENTICATION') or define('ERROR_AUTHENTICATION', '认证失败');
defined('ERROR_AUTHORIZATION') or define('ERROR_AUTHORIZATION', '权限不足');
defined('ERROR_NOT_FOUND') or define('ERROR_NOT_FOUND', '资源不存在');
defined('ERROR_INTERNAL') or define('ERROR_INTERNAL', '服务器内部错误');

// 成功消息
defined('SUCCESS_CREATED') or define('SUCCESS_CREATED', '创建成功');
defined('SUCCESS_UPDATED') or define('SUCCESS_UPDATED', '更新成功');
defined('SUCCESS_DELETED') or define('SUCCESS_DELETED', '删除成功');
defined('SUCCESS_OPERATION') or define('SUCCESS_OPERATION', '操作成功');

// 地图API配置（高德地图）
defined('AMAP_KEY') or define('AMAP_KEY', 'a3cd510ffc1871168cbee271105ad260');
defined('MAP_DEFAULT_ZOOM') or define('MAP_DEFAULT_ZOOM', 15);
defined('MAP_MAX_ZOOM') or define('MAP_MAX_ZOOM', 18);
defined('MAP_MIN_ZOOM') or define('MAP_MIN_ZOOM', 10);

// 分页配置
defined('DEFAULT_PAGE_SIZE') or define('DEFAULT_PAGE_SIZE', 20);
defined('MAX_PAGE_SIZE') or define('MAX_PAGE_SIZE', 100);

// 缓存配置
defined('CACHE_ENABLED') or define('CACHE_ENABLED', true);
defined('CACHE_TTL') or define('CACHE_TTL', 3600);

// 安全配置
defined('PASSWORD_MIN_LENGTH') or define('PASSWORD_MIN_LENGTH', 6);
defined('PASSWORD_MAX_LENGTH') or define('PASSWORD_MAX_LENGTH', 72);
defined('LOGIN_ATTEMPTS_LIMIT') or define('LOGIN_ATTEMPTS_LIMIT', 5);
defined('LOGIN_LOCKOUT_TIME') or define('LOGIN_LOCKOUT_TIME', 900);

// 验证码配置
defined('SMS_CODE_LENGTH') or define('SMS_CODE_LENGTH', 6);
defined('SMS_CODE_EXPIRE') or define('SMS_CODE_EXPIRE', 300);
defined('SMS_RESEND_INTERVAL') or define('SMS_RESEND_INTERVAL', 60);

// 货币配置
defined('CURRENCY') or define('CURRENCY', 'CNY');
defined('CURRENCY_SYMBOL') or define('CURRENCY_SYMBOL', '¥');

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