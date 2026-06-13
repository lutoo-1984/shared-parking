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

// Composer 自动加载
$vendorAutoload = ROOT_PATH . '/vendor/autoload.php';
if (file_exists($vendorAutoload)) {
    require_once $vendorAutoload;
}

// 自动加载配置和工具类
require_once API_ROOT . '/config/constants.php';
require_once API_ROOT . '/config/database.php';
require_once API_ROOT . '/lib/auth.php';
require_once API_ROOT . '/lib/parking.php';
require_once API_ROOT . '/lib/payment.php';
require_once API_ROOT . '/lib/review.php';
require_once API_ROOT . '/lib/message.php';
require_once API_ROOT . '/lib/admin.php';

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
        case 'upload':
            // 文件上传通过upload.php处理（POST multipart）
            if ($method === 'POST') {
                require_once API_ROOT . '/upload.php';
                exit;
            }
            throw new Exception('方法不允许', 405);
        case 'favorites':
            return handleFavorites($method, $pathSegments, $data);
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
                        // 注册后自动登录，返回 token + user 格式
                        $loginResult = Auth::login([
                            'email' => $data['email'],
                            'password' => $data['password']
                        ]);
                        return [
                            'user' => $loginResult['user'],
                            'token' => $loginResult['token'],
                            'expires_in' => $loginResult['expires_in']
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
                            'user' => $result['user'],
                            'token' => $result['token'],
                            'expires_in' => $result['expires_in']
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
                            $startTime = $data['start_time'] ?? $_GET['start_time'] ?? '';
                            $endTime = $data['end_time'] ?? $_GET['end_time'] ?? '';

                            if (empty($startTime) || empty($endTime)) {
                                throw new Exception('开始时间和结束时间不能为空', HTTP_BAD_REQUEST);
                            }

                            $isAvailable = Parking::checkAvailability($id, $startTime, $endTime);

                            // 获取冲突的预订信息
                            $conflictingBookings = [];
                            if (!$isAvailable) {
                                $conflictingBookings = Parking::getSpotBookings($id, $startTime, $endTime);
                            }

                            return [
                                'success' => true,
                                'data' => [
                                    'available' => $isAvailable,
                                    'spot_id' => $id,
                                    'start_time' => $startTime,
                                    'end_time' => $endTime,
                                    'conflicting_bookings' => $conflictingBookings,
                                    'message' => $isAvailable ? '车位可用' : '车位不可用，时间冲突'
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
/**
 * 获取当前用户ID
 */
function getCurrentUserId() {
    $user = Auth::getCurrentUser();
    if (!$user) return null;
    return intval($user['user_id'] ?? $user['id'] ?? 0) ?: null;
}

function handleBookings($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';
    $id = $pathSegments[2] ?? '';

    switch ($method) {
        case 'GET':
            if (!empty($id)) {
                return getBookingDetail($id);
            } else {
                return getMyBookings();
            }
        case 'POST':
            if (empty($action)) {
                return createBooking($data);
            }
            throw new Exception('操作不存在', 404);
        case 'PUT':
            // PUT /api/bookings/{id}/cancel
            if (!empty($action) && $id === 'cancel') {
                return cancelBooking($action, $data);
            }
            throw new Exception('操作不存在', 404);
        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 创建预订
 */
function createBooking($data) {
    try {
        // 验证用户是否登录
        $userId = getCurrentUserId();
        if (!$userId) {
            return ['success' => false, 'error' => ['code' => 401, 'message' => '请先登录']];
        }

        // 验证必填字段
        $requiredFields = ['spot_id', 'vehicle_plate_number', 'start_time', 'end_time'];
        foreach ($requiredFields as $field) {
            if (empty($data[$field])) {
                return ['success' => false, 'error' => ['code' => 400, 'message' => "缺少必填字段: $field"]];
            }
        }

        $spotId = intval($data['spot_id']);
        $vehiclePlateNumber = trim($data['vehicle_plate_number']);
        $startTime = $data['start_time'];
        $endTime = $data['end_time'];
        $notes = isset($data['notes']) ? trim($data['notes']) : null;

        // 验证停车位是否存在且可用
        $spot = db()->querySingle("SELECT * FROM parking_spots WHERE id = ? AND is_active = 1 AND is_approved = 1", [$spotId]);
        if (!$spot) {
            return ['success' => false, 'error' => ['code' => 404, 'message' => '停车位不存在或不可用']];
        }

        // 验证时间格式
        $startDateTime = DateTime::createFromFormat('Y-m-d\TH:i:s\Z', $startTime);
        $endDateTime = DateTime::createFromFormat('Y-m-d\TH:i:s\Z', $endTime);

        if (!$startDateTime || !$endDateTime) {
            return ['success' => false, 'error' => ['code' => 400, 'message' => '时间格式不正确，请使用ISO格式']];
        }

        // 验证时间范围
        $now = new DateTime();
        if ($startDateTime < $now) {
            return ['success' => false, 'error' => ['code' => 400, 'message' => '开始时间不能早于当前时间']];
        }

        if ($endDateTime <= $startDateTime) {
            return ['success' => false, 'error' => ['code' => 400, 'message' => '结束时间必须晚于开始时间']];
        }

        // 计算时长（小时）
        $interval = $startDateTime->diff($endDateTime);
        $durationHours = $interval->h + ($interval->i / 60) + ($interval->days * 24);

        if ($durationHours < 1) {
            return ['success' => false, 'error' => ['code' => 400, 'message' => '预订时长至少1小时']];
        }

        if ($durationHours > 30 * 24) {
            return ['success' => false, 'error' => ['code' => 400, 'message' => '预订时长不能超过30天']];
        }

        // 检查时间冲突
        $conflictingBookingsParams = [
            $spotId,
            $endDateTime->format('Y-m-d H:i:s'),
            $startDateTime->format('Y-m-d H:i:s'),
            $startDateTime->format('Y-m-d H:i:s'),
            $endDateTime->format('Y-m-d H:i:s'),
            $startDateTime->format('Y-m-d H:i:s'),
            $endDateTime->format('Y-m-d H:i:s')
        ];
        $conflictingBookings = db()->query(
            "SELECT id FROM bookings
             WHERE spot_id = ?
             AND status IN ('pending', 'confirmed', 'in_progress')
             AND (
                 (start_time < ? AND end_time > ?) OR
                 (start_time >= ? AND start_time < ?) OR
                 (end_time > ? AND end_time <= ?)
             )",
            $conflictingBookingsParams
        );

        if (!empty($conflictingBookings)) {
            return ['success' => false, 'error' => ['code' => 409, 'message' => '该时间段车位已被预订']];
        }

        // 计算价格
        $pricePerHour = floatval($spot['price_per_hour']);
        $totalPrice = round($durationHours * $pricePerHour, 2);

        // 生成入场验证码
        $checkInCode = generateCheckInCode();

        // 开始事务
        db()->beginTransaction();

        try {
            // 创建预订记录
            $bookingData = [
                $userId,
                $spotId,
                $vehiclePlateNumber,
                $startDateTime->format('Y-m-d H:i:s'),
                $endDateTime->format('Y-m-d H:i:s'),
                $durationHours,
                $totalPrice,
                'pending',
                $checkInCode,
                $notes,
                date('Y-m-d H:i:s'),
                date('Y-m-d H:i:s')
            ];
            $bookingId = db()->insert(
                "INSERT INTO bookings (user_id, spot_id, vehicle_plate_number, start_time, end_time, duration_hours, total_price, status, check_in_code, notes, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                $bookingData
            );

            // 获取创建的预订详情
            $booking = db()->querySingle(
                "SELECT b.*,
                        ps.title as spot_title,
                        ps.address as spot_address,
                        ps.price_per_hour,
                        u.username as owner_username
                 FROM bookings b
                 JOIN parking_spots ps ON b.spot_id = ps.id
                 JOIN users u ON ps.owner_id = u.id
                 WHERE b.id = ?",
                [$bookingId]
            );

            // 提交事务
            db()->commit();

            return [
                'success' => true,
                'message' => '预订创建成功',
                'data' => formatBookingResponse($booking)
            ];

        } catch (Exception $e) {
            db()->rollBack();
            throw $e;
        }

    } catch (Exception $e) {
        return ['success' => false, 'error' => ['code' => 500, 'message' => '创建预订失败: ' . $e->getMessage()]];
    }
}

/**
 * 获取预订详情
 */
function getBookingDetail($bookingId) {
    try {
        $userId = getCurrentUserId();
        if (!$userId) {
            return ['success' => false, 'error' => ['code' => 401, 'message' => '请先登录']];
        }

        $booking = db()->querySingle(
            "SELECT b.*,
                    ps.title as spot_title,
                    ps.address as spot_address,
                    ps.price_per_hour,
                    u.username as owner_username
             FROM bookings b
             JOIN parking_spots ps ON b.spot_id = ps.id
             JOIN users u ON ps.owner_id = u.id
             WHERE b.id = ? AND (b.user_id = ? OR ps.owner_id = ?)",
            [$bookingId, $userId, $userId]
        );

        if (!$booking) {
            return ['success' => false, 'error' => ['code' => 404, 'message' => '预订不存在或无权访问']];
        }

        return [
            'success' => true,
            'data' => formatBookingResponse($booking)
        ];

    } catch (Exception $e) {
        return ['success' => false, 'error' => ['code' => 500, 'message' => '获取预订详情失败']];
    }
}

/**
 * 获取我的预订列表
 */
function getMyBookings() {
    try {
        $userId = getCurrentUserId();
        if (!$userId) {
            return ['success' => false, 'error' => ['code' => 401, 'message' => '请先登录']];
        }

        // 获取分页参数
        $page = max(1, intval($_GET['page'] ?? 1));
        $limit = max(1, min(50, intval($_GET['limit'] ?? 20)));
        $offset = ($page - 1) * $limit;

        // 获取预订列表
        $bookings = db()->query(
            "SELECT b.*,
                    ps.title as spot_title,
                    ps.address as spot_address,
                    ps.price_per_hour,
                    u.username as owner_username
             FROM bookings b
             JOIN parking_spots ps ON b.spot_id = ps.id
             JOIN users u ON ps.owner_id = u.id
             WHERE b.user_id = ?
             ORDER BY b.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $limit, $offset]
        );

        // 获取总数
        $total = db()->querySingle("SELECT COUNT(*) as count FROM bookings WHERE user_id = ?", [$userId]);
        $totalCount = $total['count'] ?? 0;

        // 格式化响应
        $formattedBookings = array_map('formatBookingResponse', $bookings);

        return [
            'success' => true,
            'data' => [
                'bookings' => $formattedBookings,
                'total' => $totalCount,
                'page' => $page,
                'limit' => $limit,
                'total_pages' => ceil($totalCount / $limit)
            ]
        ];

    } catch (Exception $e) {
        return ['success' => false, 'error' => ['code' => 500, 'message' => '获取预订列表失败']];
    }
}

/**
 * 取消预订
 */
function cancelBooking($bookingId, $data) {
    try {
        $userId = getCurrentUserId();
        if (!$userId) {
            return ['success' => false, 'error' => ['code' => 401, 'message' => '请先登录']];
        }

        // 获取预订信息
        $booking = db()->querySingle(
            "SELECT b.*, ps.owner_id as spot_owner_id
             FROM bookings b
             JOIN parking_spots ps ON b.spot_id = ps.id
             WHERE b.id = ? AND (b.user_id = ? OR ps.owner_id = ?)",
            [$bookingId, $userId, $userId]
        );

        if (!$booking) {
            return ['success' => false, 'error' => ['code' => 404, 'message' => '预订不存在或无权操作']];
        }

        // 检查预订状态是否可以取消
        $status = $booking['status'];
        if (!in_array($status, ['pending', 'confirmed'])) {
            return ['success' => false, 'error' => ['code' => 400, 'message' => '当前状态不能取消预订']];
        }

        // 确定取消方
        $cancelledBy = ($userId == $booking['user_id']) ? 'user' : 'owner';
        $cancellationReason = isset($data['reason']) ? trim($data['reason']) : null;

        // 更新预订状态
        db()->execute(
            "UPDATE bookings SET status = ?, cancelled_by = ?, cancellation_reason = ?, cancelled_at = ?, updated_at = ? WHERE id = ?",
            ['cancelled', $cancelledBy, $cancellationReason, date('Y-m-d H:i:s'), date('Y-m-d H:i:s'), $bookingId]
        );

        return [
            'success' => true,
            'message' => '预订取消成功'
        ];

    } catch (Exception $e) {
        return ['success' => false, 'error' => ['code' => 500, 'message' => '取消预订失败']];
    }
}

/**
 * 格式化预订响应
 */
function formatBookingResponse($booking) {
    if (!$booking) {
        return null;
    }

    // 获取停车位图片
    $spotImages = db()->query(
        "SELECT image_url FROM parking_spot_images WHERE spot_id = ? ORDER BY image_order, is_primary DESC",
        [$booking['spot_id']]
    );
    $imageUrls = array_column($spotImages, 'image_url');

    return [
        'id' => intval($booking['id']),
        'user_id' => intval($booking['user_id']),
        'spot_id' => intval($booking['spot_id']),
        'vehicle_plate_number' => $booking['vehicle_plate_number'],
        'vehicle_brand' => $booking['vehicle_brand'] ?? null,
        'vehicle_model' => $booking['vehicle_model'] ?? null,
        'vehicle_color' => $booking['vehicle_color'] ?? null,
        'start_time' => $booking['start_time'],
        'end_time' => $booking['end_time'],
        'duration_hours' => floatval($booking['duration_hours']),
        'total_price' => floatval($booking['total_price']),
        'status' => $booking['status'],
        'cancelled_by' => $booking['cancelled_by'],
        'cancellation_reason' => $booking['cancellation_reason'],
        'cancelled_at' => $booking['cancelled_at'],
        'check_in_code' => $booking['check_in_code'],
        'check_in_at' => $booking['check_in_at'],
        'check_out_at' => $booking['check_out_at'],
        'notes' => $booking['notes'],
        'created_at' => $booking['created_at'],
        'updated_at' => $booking['updated_at'],
        'spot_title' => $booking['spot_title'],
        'spot_address' => $booking['spot_address'],
        'price_per_hour' => floatval($booking['price_per_hour']),
        'owner_username' => $booking['owner_username'],
        'spot_images' => $imageUrls
    ];
}

/**
 * 生成入场验证码
 */
function generateCheckInCode() {
    return strtoupper(substr(md5(uniqid(mt_rand(), true)), 0, 6));
}

/**
 * 处理支付相关请求
 */
function handlePayments($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';
    $id = $pathSegments[2] ?? '';
    $subAction = $pathSegments[3] ?? '';

    switch ($method) {
        case 'POST':
            // POST /api/payments/create
            if ($action === 'create') {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    $result = Payment::createPayment($userId, $data);
                    return [
                        'success' => true,
                        'message' => '支付订单创建成功',
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            // POST /api/payments/notify/{gateway}
            if ($action === 'notify') {
                try {
                    $gateway = $id ?: 'unknown';
                    $result = Payment::handleNotify($gateway, $data);
                    return [
                        'success' => true,
                        'message' => $result['message'],
                        'data' => $result['payment']
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            // POST /api/payments/{id}/refund
            if (!empty($action) && $id === 'refund') {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    $paymentId = $action;
                    $result = Payment::processRefund($paymentId, $userId, $data);
                    return [
                        'success' => true,
                        'message' => '退款处理成功',
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            throw new Exception('操作不存在', 404);

        case 'GET':
            // GET /api/payments/booking/{booking_id}
            if ($action === 'booking' && !empty($id)) {
                try {
                    $payments = db()->query(
                        "SELECT p.* FROM payments p WHERE p.booking_id = ? ORDER BY p.created_at DESC LIMIT 1",
                        [$id]
                    );
                    $payment = !empty($payments) ? Payment::getPaymentDetail($payments[0]['id']) : null;
                    return [
                        'success' => true,
                        'data' => $payment
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            // GET /api/payments/{id}
            if (!empty($action)) {
                try {
                    $payment = Payment::getPaymentDetail($action);
                    if (!$payment) throw new Exception('支付记录不存在', 404);
                    return [
                        'success' => true,
                        'data' => $payment
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            // GET /api/payments
            try {
                $userId = getCurrentUserId();
                if (!$userId) return errorResponse(401, '请先登录');

                $page = max(1, intval($_GET['page'] ?? 1));
                $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));

                $result = Payment::getUserPayments($userId, $page, $limit);
                return [
                    'success' => true,
                    'data' => $result
                ];
            } catch (Exception $e) {
                return errorResponse($e->getCode() ?: 500, $e->getMessage());
            }

        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 处理评价相关请求
 */
function handleReviews($method, $pathSegments, $data) {
    $action = $pathSegments[1] ?? '';
    $id = $pathSegments[2] ?? '';

    switch ($method) {
        case 'GET':
            if ($action === 'spot' && !empty($id)) {
                try {
                    $page = max(1, intval($_GET['page'] ?? 1));
                    $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));

                    $result = Review::getSpotReviews($id, $page, $limit);
                    return [
                        'success' => true,
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            } elseif ($action === 'user' && !empty($id)) {
                try {
                    $page = max(1, intval($_GET['page'] ?? 1));
                    $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));

                    $result = Review::getUserReviews($id, $page, $limit);
                    return [
                        'success' => true,
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }
            throw new Exception('操作不存在', 404);

        case 'POST':
            if (empty($action)) {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    $result = Review::createReview($userId, $data);
                    return [
                        'success' => true,
                        'message' => '评价创建成功',
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }
            throw new Exception('操作不存在', 404);

        case 'PUT':
            if ($action === 'reply' && !empty($id)) {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    $reply = $data['reply'] ?? '';
                    $result = Review::replyToReview($id, $userId, $reply);
                    return [
                        'success' => true,
                        'message' => '回复成功',
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
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
    $id = $pathSegments[2] ?? '';

    switch ($method) {
        case 'GET':
            try {
                $userId = getCurrentUserId();
                if (!$userId) return errorResponse(401, '请先登录');

                $page = max(1, intval($_GET['page'] ?? 1));
                $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));

                if (!empty($id) && isset($pathSegments[2]) && $pathSegments[2] === 'conversation' && !empty($pathSegments[3])) {
                    // GET /messages/conversation/{user_id}
                    $otherUserId = $pathSegments[3];
                    $result = Message::getConversation($userId, $otherUserId, $page, $limit);
                } elseif (!empty($action) && $action === 'unread') {
                    // GET /messages/unread
                    $count = Message::getUnreadCount($userId);
                    return ['success' => true, 'data' => ['unread_count' => $count]];
                } elseif (!empty($action) && $action === 'outbox') {
                    // GET /messages/outbox
                    $result = Message::getOutbox($userId, $page, $limit);
                } else {
                    // GET /messages - 收件箱
                    $result = Message::getInbox($userId, $page, $limit);
                }

                return ['success' => true, 'data' => $result];
            } catch (Exception $e) {
                return errorResponse($e->getCode() ?: 500, $e->getMessage());
            }

        case 'POST':
            try {
                $userId = getCurrentUserId();
                if (!$userId) return errorResponse(401, '请先登录');

                $result = Message::sendMessage($userId, $data);
                return [
                    'success' => true,
                    'message' => '消息发送成功',
                    'data' => $result
                ];
            } catch (Exception $e) {
                return errorResponse($e->getCode() ?: 500, $e->getMessage());
            }

        case 'PUT':
            if (empty($action)) {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    // PUT /messages - 标记所有为已读
                    $result = Message::markAllAsRead($userId);
                    return ['success' => true, 'message' => '所有消息已标记为已读', 'data' => $result];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            if (!empty($action) && ($pathSegments[2] ?? '') === 'read') {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    $result = Message::markAsRead($action, $userId);
                    return [
                        'success' => true,
                        'message' => '消息已标记为已读',
                        'data' => $result
                    ];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }
            throw new Exception('操作不存在', 404);

        case 'DELETE':
            if (!empty($action)) {
                try {
                    $userId = getCurrentUserId();
                    if (!$userId) return errorResponse(401, '请先登录');

                    Message::deleteMessage($action, $userId);
                    return ['success' => true, 'message' => '消息已删除'];
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
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

    switch ($method) {
        case 'GET':
            switch ($action) {
                case 'stats':
                    try {
                        $result = Admin::getDashboardStats();
                        return ['success' => true, 'data' => $result];
                    } catch (Exception $e) {
                        return errorResponse($e->getCode() ?: 500, $e->getMessage());
                    }

                case 'spots':
                    try {
                        $page = max(1, intval($_GET['page'] ?? 1));
                        $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));
                        $result = Admin::getAllSpots($data, $page, $limit);
                        return ['success' => true, 'data' => $result];
                    } catch (Exception $e) {
                        return errorResponse($e->getCode() ?: 500, $e->getMessage());
                    }

                case 'users':
                    try {
                        $page = max(1, intval($_GET['page'] ?? 1));
                        $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));
                        $result = Admin::getAllUsers($data, $page, $limit);
                        return ['success' => true, 'data' => $result];
                    } catch (Exception $e) {
                        return errorResponse($e->getCode() ?: 500, $e->getMessage());
                    }

                case 'settings':
                    try {
                        $result = Admin::getSystemSettings();
                        return ['success' => true, 'data' => $result];
                    } catch (Exception $e) {
                        return errorResponse($e->getCode() ?: 500, $e->getMessage());
                    }

                case '':
                    return ['success' => true, 'message' => '管理后台API'];

                default:
                    throw new Exception('操作不存在', 404);
            }

        case 'PUT':
            if ($action === 'spots' && !empty($id) && ($pathSegments[3] ?? '') === 'approve') {
                try {
                    $result = Admin::approveSpot($id, $data);
                    return $result;
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            if ($action === 'users' && !empty($id)) {
                try {
                    $result = Admin::manageUser($id, $data);
                    return $result;
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            if ($action === 'settings' && !empty($id)) {
                try {
                    $result = Admin::updateSystemSetting($id, $data['value'] ?? '');
                    return $result;
                } catch (Exception $e) {
                    return errorResponse($e->getCode() ?: 500, $e->getMessage());
                }
            }

            throw new Exception('操作不存在', 404);

        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 统一错误响应
 */
function errorResponse($code, $message) {
    return [
        'success' => false,
        'error' => [
            'code' => $code,
            'message' => $message
        ]
    ];
}

/**
 * 处理收藏相关请求
 */
/**
 * 处理收藏相关请求
 *
 * 路由:
 *   GET    /favorites        - 获取收藏列表
 *   POST   /favorites/{id}   - 添加/切换收藏 (spot_id 在路径中)
 *   DELETE /favorites/{id}   - 移除收藏
 */
function handleFavorites($method, $pathSegments, $data) {
    $spotId = intval($pathSegments[1] ?? 0);

    switch ($method) {
        case 'GET':
            try {
                $userId = getCurrentUserId();
                if (!$userId) return errorResponse(401, '请先登录');

                $page = max(1, intval($_GET['page'] ?? 1));
                $limit = max(1, min(100, intval($_GET['limit'] ?? 20)));
                $offset = ($page - 1) * $limit;

                $favorites = db()->query(
                    "SELECT f.*, ps.title, ps.address, ps.price_per_hour, ps.latitude, ps.longitude,
                            ps.is_covered, ps.has_lighting, ps.has_security, ps.has_charging, ps.has_cctv, ps.is_24h_access,
                            (SELECT AVG(rating) FROM reviews WHERE spot_id = ps.id) as avg_rating,
                            (SELECT COUNT(*) FROM reviews WHERE spot_id = ps.id) as review_count
                     FROM favorites f
                     JOIN parking_spots ps ON f.spot_id = ps.id
                     WHERE f.user_id = ? AND ps.is_active = 1
                     ORDER BY f.created_at DESC
                     LIMIT ? OFFSET ?",
                    [$userId, $limit, $offset]
                );

                foreach ($favorites as &$fav) {
                    $image = db()->querySingle(
                        "SELECT image_url FROM parking_spot_images WHERE spot_id = ? AND is_primary = 1 LIMIT 1",
                        [$fav['spot_id']]
                    );
                    $fav['primary_image'] = $image ? $image['image_url'] : null;
                }

                $total = db()->querySingle(
                    "SELECT COUNT(*) as count FROM favorites WHERE user_id = ?",
                    [$userId]
                )['count'];

                return [
                    'success' => true,
                    'data' => [
                        'favorites' => $favorites,
                        'pagination' => [
                            'page' => $page,
                            'limit' => $limit,
                            'total' => intval($total),
                            'pages' => ceil($total / $limit)
                        ]
                    ]
                ];
            } catch (Exception $e) {
                return errorResponse($e->getCode() ?: 500, $e->getMessage());
            }

        case 'POST':
            try {
                $userId = getCurrentUserId();
                if (!$userId) return errorResponse(401, '请先登录');

                // POST /favorites/{spot_id} - 从路径取spot_id
                if ($spotId <= 0) throw new Exception('车位ID不能为空', 400);

                $spot = db()->querySingle("SELECT id FROM parking_spots WHERE id = ? AND is_active = 1", [$spotId]);
                if (!$spot) throw new Exception('停车位不存在', 404);

                // 切换收藏状态
                $existing = db()->querySingle(
                    "SELECT id FROM favorites WHERE user_id = ? AND spot_id = ?",
                    [$userId, $spotId]
                );

                if ($existing) {
                    db()->execute("DELETE FROM favorites WHERE id = ?", [$existing['id']]);
                    return ['success' => true, 'message' => '已取消收藏', 'data' => ['is_favorite' => false]];
                } else {
                    db()->insert(
                        "INSERT INTO favorites (user_id, spot_id, created_at) VALUES (?, ?, NOW())",
                        [$userId, $spotId]
                    );
                    return ['success' => true, 'message' => '收藏成功', 'data' => ['is_favorite' => true]];
                }
            } catch (Exception $e) {
                return errorResponse($e->getCode() ?: 500, $e->getMessage());
            }

        case 'DELETE':
            try {
                $userId = getCurrentUserId();
                if (!$userId) return errorResponse(401, '请先登录');

                // DELETE /favorites/{spot_id}
                if ($spotId <= 0) throw new Exception('车位ID不能为空', 400);

                $deleted = db()->execute(
                    "DELETE FROM favorites WHERE user_id = ? AND spot_id = ?",
                    [$userId, $spotId]
                );

                return ['success' => true, 'message' => $deleted ? '已取消收藏' : '未找到收藏记录'];
            } catch (Exception $e) {
                return errorResponse($e->getCode() ?: 500, $e->getMessage());
            }

        default:
            throw new Exception('方法不允许', 405);
    }
}

/**
 * 发送成功响应（自动识别错误返回格式并转为HTTP错误）
 */
function sendResponse($data, $statusCode = 200) {
    // 如果handler返回了错误格式，转成HTTP错误响应
    if (is_array($data) && isset($data['success']) && $data['success'] === false && isset($data['error'])) {
        $errCode = $data['error']['code'] ?? 400;
        $errMsg = $data['error']['message'] ?? '未知错误';
        sendError($errMsg, $errCode);
        return;
    }
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