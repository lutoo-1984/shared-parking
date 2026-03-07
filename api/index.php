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
require_once API_ROOT . '/lib/auth.php';
require_once API_ROOT . '/lib/parking.php';

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
                    try {
                        $result = Auth::register($data);
                        return [
                            'success' => true,
                            'message' => SUCCESS_CREATED,
                            'data' => $result
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_BAD_REQUEST,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                case 'login':
                    try {
                        $result = Auth::login($data);
                        return [
                            'success' => true,
                            'message' => '登录成功',
                            'data' => $result
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_UNAUTHORIZED,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                case 'logout':
                    // 客户端应删除本地存储的token
                    return [
                        'success' => true,
                        'message' => '退出登录成功'
                    ];

                case 'send-captcha':
                    try {
                        $phone = $data['phone'] ?? '';
                        $type = $data['type'] ?? 'register';

                        if (empty($phone)) {
                            throw new Exception('手机号不能为空', HTTP_BAD_REQUEST);
                        }

                        $result = Auth::sendVerificationCode($phone, $type);
                        return [
                            'success' => true,
                            'message' => '验证码发送成功',
                            'data' => $result
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_BAD_REQUEST,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                case 'verify-code':
                    try {
                        $email = $data['email'] ?? '';
                        $phone = $data['phone'] ?? '';
                        $code = $data['code'] ?? '';
                        $type = $data['type'] ?? 'register';

                        if (empty($code)) {
                            throw new Exception('验证码不能为空', HTTP_BAD_REQUEST);
                        }

                        Auth::verifyCode($email, $phone, $code, $type);

                        // 如果是注册验证，完成用户验证
                        if ($type === 'register' && !empty($email)) {
                            Auth::completeVerification($email);
                        }

                        return [
                            'success' => true,
                            'message' => '验证码验证成功'
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_BAD_REQUEST,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                default:
                    throw new Exception('操作不存在', 404);
            }

        case 'GET':
            // 获取当前用户信息
            if ($action === 'me') {
                try {
                    $user = Auth::getCurrentUser();
                    if (!$user) {
                        throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                    }

                    $userProfile = Auth::getUserProfile($user['user_id']);
                    return [
                        'success' => true,
                        'data' => $userProfile
                    ];
                } catch (Exception $e) {
                    return [
                        'success' => false,
                        'error' => [
                            'code' => $e->getCode() ?: HTTP_UNAUTHORIZED,
                            'message' => $e->getMessage()
                        ]
                    ];
                }
            }
            throw new Exception('操作不存在', 404);

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
                    try {
                        // 获取当前用户
                        $currentUser = Auth::getCurrentUser();
                        if (!$currentUser) {
                            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                        }

                        // 获取用户资料
                        $userProfile = Auth::getUserProfile($currentUser['user_id']);
                        return [
                            'success' => true,
                            'data' => $userProfile
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                case '':
                    // 获取用户列表（仅管理员）
                    try {
                        $currentUser = Auth::getCurrentUser();
                        if (!$currentUser || $currentUser['role'] !== 'admin') {
                            throw new Exception(ERROR_AUTHORIZATION, HTTP_FORBIDDEN);
                        }

                        // 分页参数
                        $page = isset($data['page']) ? max(1, intval($data['page'])) : 1;
                        $limit = isset($data['limit']) ? min(MAX_PAGE_SIZE, max(1, intval($data['limit']))) : DEFAULT_PAGE_SIZE;
                        $offset = ($page - 1) * $limit;

                        // 获取用户列表
                        $users = db()->query(
                            "SELECT id, username, email, phone, real_name, avatar_url,
                                    role, is_verified, is_active, created_at, last_login_at
                             FROM users
                             ORDER BY id DESC
                             LIMIT ? OFFSET ?",
                            [$limit, $offset]
                        );

                        // 获取总数
                        $total = db()->querySingle("SELECT COUNT(*) as count FROM users")['count'];

                        return [
                            'success' => true,
                            'data' => [
                                'users' => $users,
                                'pagination' => [
                                    'page' => $page,
                                    'limit' => $limit,
                                    'total' => $total,
                                    'pages' => ceil($total / $limit)
                                ]
                            ]
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                default:
                    // 获取特定用户详情（仅管理员或自己）
                    try {
                        $userId = intval($action);
                        if ($userId <= 0) {
                            throw new Exception(ERROR_NOT_FOUND, HTTP_NOT_FOUND);
                        }

                        $currentUser = Auth::getCurrentUser();
                        if (!$currentUser) {
                            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                        }

                        // 检查权限：管理员或查看自己的资料
                        if ($currentUser['role'] !== 'admin' && $currentUser['user_id'] != $userId) {
                            throw new Exception(ERROR_AUTHORIZATION, HTTP_FORBIDDEN);
                        }

                        $userProfile = Auth::getUserProfile($userId);
                        return [
                            'success' => true,
                            'data' => $userProfile
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }
            }

        case 'PUT':
            if ($action === 'profile') {
                try {
                    // 获取当前用户
                    $currentUser = Auth::getCurrentUser();
                    if (!$currentUser) {
                        throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                    }

                    // 更新用户资料
                    $result = Auth::updateUserProfile($currentUser['user_id'], $data);
                    return [
                        'success' => true,
                        'message' => SUCCESS_UPDATED,
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return [
                        'success' => false,
                        'error' => [
                            'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                            'message' => $e->getMessage()
                        ]
                    ];
                }
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
                        // 获取停车位详情
                        try {
                            // 获取当前用户（如果已登录）
                            $currentUser = Auth::getCurrentUser();
                            $userId = $currentUser ? $currentUser['user_id'] : null;

                            $spot = Parking::getSpotById($id, $userId);
                            if (!$spot) {
                                throw new Exception(ERROR_NOT_FOUND, HTTP_NOT_FOUND);
                            }

                            // 增加查看次数
                            Parking::incrementViewCount($id);

                            return [
                                'success' => true,
                                'data' => $spot
                            ];
                        } catch (Exception $e) {
                            return [
                                'success' => false,
                                'error' => [
                                    'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                    'message' => $e->getMessage()
                                ]
                            ];
                        }
                    } else {
                        // 获取停车位列表（搜索）
                        try {
                            // 解析查询参数
                            $filters = $data;
                            $page = isset($data['page']) ? max(1, intval($data['page'])) : 1;
                            $limit = isset($data['limit']) ? min(MAX_PAGE_SIZE, max(1, intval($data['limit']))) : DEFAULT_PAGE_SIZE;

                            $result = Parking::searchSpots($filters, $page, $limit);
                            return [
                                'success' => true,
                                'data' => $result
                            ];
                        } catch (Exception $e) {
                            return [
                                'success' => false,
                                'error' => [
                                    'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                    'message' => $e->getMessage()
                                ]
                            ];
                        }
                    }

                case 'search':
                    // 搜索停车位（与/spots相同，为了兼容性保留）
                    try {
                        $filters = $data;
                        $page = isset($data['page']) ? max(1, intval($data['page'])) : 1;
                        $limit = isset($data['limit']) ? min(MAX_PAGE_SIZE, max(1, intval($data['limit']))) : DEFAULT_PAGE_SIZE;

                        $result = Parking::searchSpots($filters, $page, $limit);
                        return [
                            'success' => true,
                            'data' => $result
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                case 'my':
                    // 获取我的车位
                    try {
                        $currentUser = Auth::getCurrentUser();
                        if (!$currentUser) {
                            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                        }

                        $page = isset($data['page']) ? max(1, intval($data['page'])) : 1;
                        $limit = isset($data['limit']) ? min(MAX_PAGE_SIZE, max(1, intval($data['limit']))) : DEFAULT_PAGE_SIZE;

                        $result = Parking::getUserSpots($currentUser['user_id'], $page, $limit);
                        return [
                            'success' => true,
                            'data' => $result
                        ];
                    } catch (Exception $e) {
                        return [
                            'success' => false,
                            'error' => [
                                'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                'message' => $e->getMessage()
                            ]
                        ];
                    }

                case 'availability':
                    // 检查车位可用性
                    if (!empty($id)) {
                        try {
                            $startTime = $data['start_time'] ?? '';
                            $endTime = $data['end_time'] ?? '';

                            if (empty($startTime) || empty($endTime)) {
                                throw new Exception('开始时间和结束时间不能为空', HTTP_BAD_REQUEST);
                            }

                            $isAvailable = Parking::checkAvailability($id, $startTime, $endTime);
                            return [
                                'success' => true,
                                'data' => [
                                    'is_available' => $isAvailable,
                                    'spot_id' => $id,
                                    'start_time' => $startTime,
                                    'end_time' => $endTime
                                ]
                            ];
                        } catch (Exception $e) {
                            return [
                                'success' => false,
                                'error' => [
                                    'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                                    'message' => $e->getMessage()
                                ]
                            ];
                        }
                    }
                    throw new Exception('车位ID不能为空', HTTP_BAD_REQUEST);

                case '':
                    // 返回停车位API信息
                    return [
                        'success' => true,
                        'message' => '停车位API',
                        'endpoints' => [
                            'GET /api/parking/spots' => '获取停车位列表/搜索',
                            'GET /api/parking/spots/{id}' => '获取停车位详情',
                            'POST /api/parking/spots' => '创建停车位',
                            'PUT /api/parking/spots/{id}' => '更新停车位',
                            'DELETE /api/parking/spots/{id}' => '删除停车位',
                            'GET /api/parking/my' => '获取我的车位',
                            'GET /api/parking/availability/{id}' => '检查车位可用性'
                        ]
                    ];

                default:
                    throw new Exception('操作不存在', 404);
            }

        case 'POST':
            if ($action === 'spots') {
                // 创建停车位
                try {
                    $currentUser = Auth::getCurrentUser();
                    if (!$currentUser) {
                        throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                    }

                    $result = Parking::createSpot($currentUser['user_id'], $data);
                    return [
                        'success' => true,
                        'message' => SUCCESS_CREATED,
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return [
                        'success' => false,
                        'error' => [
                            'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                            'message' => $e->getMessage()
                        ]
                    ];
                }
            }
            throw new Exception('操作不存在', 404);

        case 'PUT':
            if ($action === 'spots' && !empty($id)) {
                // 更新停车位
                try {
                    $currentUser = Auth::getCurrentUser();
                    if (!$currentUser) {
                        throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                    }

                    $result = Parking::updateSpot($id, $currentUser['user_id'], $data);
                    return [
                        'success' => true,
                        'message' => SUCCESS_UPDATED,
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return [
                        'success' => false,
                            'error' => [
                            'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                            'message' => $e->getMessage()
                        ]
                    ];
                }
            }
            throw new Exception('操作不存在', 404);

        case 'DELETE':
            if ($action === 'spots' && !empty($id)) {
                // 删除停车位
                try {
                    $currentUser = Auth::getCurrentUser();
                    if (!$currentUser) {
                        throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
                    }

                    $result = Parking::deleteSpot($id, $currentUser['user_id']);
                    return [
                        'success' => true,
                        'message' => SUCCESS_DELETED,
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return [
                        'success' => false,
                        'error' => [
                            'code' => $e->getCode() ?: HTTP_INTERNAL_ERROR,
                            'message' => $e->getMessage()
                        ]
                    ];
                }
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