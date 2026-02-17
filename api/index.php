<?php
/**
 * API入口文件
 * 处理所有RESTful API请求
 */

// 设置错误报告
error_reporting(E_ALL);
ini_set('display_errors', '1');

// 定义常量
define('API_ROOT', dirname(__FILE__));
define('ROOT_PATH', dirname(API_ROOT));

// 自动加载配置和工具类
require_once API_ROOT . '/config/constants.php';
require_once API_ROOT . '/config/database.php';

// 设置响应头
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// 处理预检请求（CORS）
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// 获取请求信息
$method = $_SERVER['REQUEST_METHOD'];
$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$apiBase = '/api/';

// 移除API基础路径
if (strpos($path, $apiBase) === 0) {
    $path = substr($path, strlen($apiBase));
}

// 分割路径为数组
$pathSegments = explode('/', trim($path, '/'));
$endpoint = $pathSegments[0] ?? '';

// 获取请求数据
$input = file_get_contents('php://input');
$data = [];
if (!empty($input)) {
    $data = json_decode($input, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        $data = $_POST;
    }
}

// 合并GET参数
if ($method === 'GET') {
    $data = array_merge($data, $_GET);
}

// 简单的路由处理
try {
    $response = handleRequest($method, $endpoint, $pathSegments, $data);
    sendResponse($response);
} catch (Exception $e) {
    sendError($e->getMessage(), 500);
}

/**
 * 处理请求路由
 */
function handleRequest($method, $endpoint, $pathSegments, $data) {
    switch ($endpoint) {
        case 'auth':
            return handleAuth($method, $pathSegments, $data);
        case 'users':
            return handleUsers($method, $pathSegments, $data);
        case 'parking':
            return handleParking($method, $pathSegments, $data);
        case 'bookings':
            return handleBookings($method, $pathSegments, $data);
        case 'payments':
            return handlePayments($method, $pathSegments, $data);
        case 'reviews':
            return handleReviews($method, $pathSegments, $data);
        case 'messages':
            return handleMessages($method, $pathSegments, $data);
        case 'admin':
            return handleAdmin($method, $pathSegments, $data);
        case '':
            return [
                'success' => true,
                'message' => '共享停车位平台API',
                'version' => APP_VERSION,
                'endpoints' => [
                    'GET /api/auth' => '获取认证相关API信息',
                    'POST /api/auth/register' => '用户注册',
                    'POST /api/auth/login' => '用户登录',
                    'GET /api/parking/spots' => '获取停车位列表',
                    'POST /api/parking/spots' => '创建停车位',
                    'GET /api/parking/spots/{id}' => '获取停车位详情',
                    'POST /api/bookings' => '创建预订',
                    'GET /api/bookings/{id}' => '获取预订详情',
                    'POST /api/payments/create' => '创建支付',
                    'GET /api/users/profile' => '获取用户资料',
                ]
            ];
        default:
            throw new Exception('端点不存在', 404);
    }
}

/**
 * 处理认证相关请求
 */
function handleAuth($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';

    switch ($method) {
        case 'POST':
            switch ($action) {
                case 'register':
                    return ['success' => true, 'message' => '注册功能待实现', 'data' => $data];
                case 'login':
                    return ['success' => true, 'message' => '登录功能待实现', 'data' => $data];
                case 'logout':
                    return ['success' => true, 'message' => '退出登录功能待实现'];
                default:
                    throw new Exception('操作不存在', 404);
            }
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理用户相关请求
 */
function handleUsers($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';

    switch ($method) {
        case 'GET':
            switch ($action) {
                case 'profile':
                    return ['success' => true, 'message' => '获取用户资料功能待实现'];
                case '':
                    return ['success' => true, 'message' => '获取用户列表功能待实现'];
                default:
                    // 假设是用户ID
                    return ['success' => true, 'message' => '获取用户详情功能待实现', 'user_id' => $action];
            }
        case 'PUT':
            if ($action === 'profile') {
                return ['success' => true, 'message' => '更新用户资料功能待实现', 'data' => $data];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理停车位相关请求
 */
function handleParking($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';
    $id = $pathSegments[2] ?? '';

    switch ($method) {
        case 'GET':
            switch ($action) {
                case 'spots':
                    if (!empty($id)) {
                        return ['success' => true, 'message' => '获取停车位详情功能待实现', 'spot_id' => $id];
                    } else {
                        return ['success' => true, 'message' => '获取停车位列表功能待实现'];
                    }
                case 'search':
                    return ['success' => true, 'message' => '搜索停车位功能待实现', 'params' => $data];
                case '':
                    return ['success' => true, 'message' => '停车位API'];
                default:
                    throw new Exception('操作不存在', 404);
            }
        case 'POST':
            if ($action === 'spots') {
                return ['success' => true, 'message' => '创建停车位功能待实现', 'data' => $data];
            }
            throw new Exception('操作不存在', 404);
        case 'PUT':
            if ($action === 'spots' && !empty($id)) {
                return ['success' => true, 'message' => '更新停车位功能待实现', 'spot_id' => $id, 'data' => $data];
            }
            throw new Exception('操作不存在', 404);
        case 'DELETE':
            if ($action === 'spots' && !empty($id)) {
                return ['success' => true, 'message' => '删除停车位功能待实现', 'spot_id' => $id];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理预订相关请求
 */
function handleBookings($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';
    $id = $pathSegments[2] ?? '';

    switch ($method) {
        case 'GET':
            if (!empty($id)) {
                return ['success' => true, 'message' => '获取预订详情功能待实现', 'booking_id' => $id];
            } else {
                return ['success' => true, 'message' => '获取预订列表功能待实现'];
            }
        case 'POST':
            if (empty($action)) {
                return ['success' => true, 'message' => '创建预订功能待实现', 'data' => $data];
            }
            throw new Exception('操作不存在', 404);
        case 'PUT':
            if (!empty($id) && $action === 'cancel') {
                return ['success' => true, 'message' => '取消预订功能待实现', 'booking_id' => $id];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理支付相关请求
 */
function handlePayments($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';

    switch ($method) {
        case 'POST':
            switch ($action) {
                case 'create':
                    return ['success' => true, 'message' => '创建支付功能待实现', 'data' => $data];
                case 'notify':
                    $gateway = $pathSegments[2] ?? '';
                    return ['success' => true, 'message' => '支付通知处理功能待实现', 'gateway' => $gateway, 'data' => $data];
                default:
                    throw new Exception('操作不存在', 404);
            }
        case 'GET':
            if (!empty($action)) {
                return ['success' => true, 'message' => '获取支付状态功能待实现', 'payment_id' => $action];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理评价相关请求
 */
function handleReviews($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';

    switch ($method) {
        case 'GET':
            if ($action === 'user' && !empty($pathSegments[2])) {
                return ['success' => true, 'message' => '获取用户评价功能待实现', 'user_id' => $pathSegments[2]];
            }
            throw new Exception('操作不存在', 404);
        case 'POST':
            if (empty($action)) {
                return ['success' => true, 'message' => '创建评价功能待实现', 'data' => $data];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理消息相关请求
 */
function handleMessages($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';

    switch ($method) {
        case 'GET':
            if (empty($action)) {
                return ['success' => true, 'message' => '获取消息列表功能待实现'];
            }
            throw new Exception('操作不存在', 404);
        case 'POST':
            if (empty($action)) {
                return ['success' => true, 'message' => '发送消息功能待实现', 'data' => $data];
            }
            throw new Exception('操作不存在', 404);
        case 'PUT':
            if (!empty($action) && $pathSegments[2] === 'read') {
                return ['success' => true, 'message' => '标记消息为已读功能待实现', 'message_id' => $action];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理管理相关请求
 */
function handleAdmin($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';
    $id = $pathSegments[2] ?? '';

    // 这里应该验证管理员权限
    switch ($method) {
        case 'GET':
            switch ($action) {
                case 'spots':
                    return ['success' => true, 'message' => '获取所有停车位功能待实现'];
                case 'users':
                    return ['success' => true, 'message' => '获取所有用户功能待实现'];
                case 'stats':
                    return ['success' => true, 'message' => '获取统计信息功能待实现'];
                case '':
                    return ['success' => true, 'message' => '管理API'];
                default:
                    throw new Exception('操作不存在', 404);
            }
        case 'PUT':
            if ($action === 'spots' && !empty($id) && $pathSegments[3] === 'approve') {
                return ['success' => true, 'message' => '审核停车位功能待实现', 'spot_id' => $id];
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 发送成功响应
 */
function sendResponse($data, $statusCode = 200) {
    http_response_code($statusCode);
    echo json_encode([
        'success' => true,
        'timestamp' => date('Y-m-d H:i:s'),
        'data' => $data
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    exit();
}

/**
 * 发送错误响应
 */
function sendError($message, $statusCode = 400) {
    http_response_code($statusCode);
    echo json_encode([
        'success' => false,
        'timestamp' => date('Y-m-d H:i:s'),
        'error' => [
            'code' => $statusCode,
            'message' => $message
        ]
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    exit();
}

// 全局错误处理
set_error_handler(function($errno, $errstr, $errfile, $errline) {
    sendError("服务器错误: $errstr", 500);
});

set_exception_handler(function($exception) {
    sendError("未捕获异常: " . $exception->getMessage(), 500);
});