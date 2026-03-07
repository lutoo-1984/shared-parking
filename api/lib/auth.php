<?php
/**
 * 用户认证库
 * 处理用户注册、登录、JWT令牌等认证相关功能
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/constants.php';

use Firebase\JWT\JWT;
use Firebase\JWT\Key;

/**
 * 用户认证类
 */
class Auth {

    /**
     * 用户注册
     */
    public static function register($data) {
        // 验证必填字段
        $required = ['username', 'email', 'password', 'phone'];
        foreach ($required as $field) {
            if (empty($data[$field])) {
                throw new Exception("{$field} 字段不能为空", HTTP_BAD_REQUEST);
            }
        }

        // 验证邮箱格式
        if (!filter_var($data['email'], FILTER_VALIDATE_EMAIL)) {
            throw new Exception("邮箱格式不正确", HTTP_BAD_REQUEST);
        }

        // 验证手机号格式（中国）
        if (!preg_match('/^1[3-9]\d{9}$/', $data['phone'])) {
            throw new Exception("手机号格式不正确", HTTP_BAD_REQUEST);
        }

        // 验证密码强度
        if (strlen($data['password']) < PASSWORD_MIN_LENGTH) {
            throw new Exception("密码长度至少" . PASSWORD_MIN_LENGTH . "位", HTTP_BAD_REQUEST);
        }

        // 检查用户名是否已存在
        if (self::usernameExists($data['username'])) {
            throw new Exception("用户名已存在", HTTP_CONFLICT);
        }

        // 检查邮箱是否已存在
        if (self::emailExists($data['email'])) {
            throw new Exception("邮箱已存在", HTTP_CONFLICT);
        }

        // 检查手机号是否已存在
        if (self::phoneExists($data['phone'])) {
            throw new Exception("手机号已存在", HTTP_CONFLICT);
        }

        // 密码哈希
        $passwordHash = password_hash($data['password'], PASSWORD_DEFAULT);

        // 生成验证码（简化版本，实际应发送短信/邮箱验证）
        $verificationCode = rand(100000, 999999);

        // 开始事务
        $db = db();
        try {
            $db->beginTransaction();

            // 插入用户
            $userId = $db->insert(
                "INSERT INTO users (username, email, phone, password_hash, is_verified, created_at)
                 VALUES (?, ?, ?, ?, 0, NOW())",
                [$data['username'], $data['email'], $data['phone'], $passwordHash]
            );

            if (!$userId) {
                throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
            }

            // 插入验证码记录
            $db->execute(
                "INSERT INTO verification_codes (email, phone, code, type, expires_at, created_at)
                 VALUES (?, ?, ?, 'register', DATE_ADD(NOW(), INTERVAL ? SECOND), NOW())",
                [$data['email'], $data['phone'], $verificationCode, SMS_CODE_EXPIRE]
            );

            // 提交事务
            $db->commit();

            // 返回用户信息（不包含密码）
            return [
                'id' => $userId,
                'username' => $data['username'],
                'email' => $data['email'],
                'phone' => $data['phone'],
                'is_verified' => false,
                'message' => '注册成功，请查收验证码完成验证'
            ];

        } catch (Exception $e) {
            $db->rollBack();
            throw $e;
        }
    }

    /**
     * 用户登录
     */
    public static function login($data) {
        // 验证必填字段
        if (empty($data['email']) || empty($data['password'])) {
            throw new Exception("邮箱和密码不能为空", HTTP_BAD_REQUEST);
        }

        // 检查登录失败次数
        self::checkLoginAttempts($data['email']);

        // 获取用户信息
        $user = self::getUserByEmail($data['email']);
        if (!$user) {
            self::recordFailedLogin($data['email']);
            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
        }

        // 检查用户状态
        if (!$user['is_active']) {
            throw new Exception("账户已被禁用", HTTP_FORBIDDEN);
        }

        // 验证密码
        if (!password_verify($data['password'], $user['password_hash'])) {
            self::recordFailedLogin($data['email']);
            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
        }

        // 检查账户是否被锁定
        if ($user['locked_until'] && strtotime($user['locked_until']) > time()) {
            $lockTime = date('Y-m-d H:i:s', strtotime($user['locked_until']));
            throw new Exception("账户已锁定，解锁时间: {$lockTime}", HTTP_FORBIDDEN);
        }

        // 重置登录失败计数
        self::resetFailedLogin($user['email']);

        // 更新最后登录时间
        self::updateLastLogin($user['id']);

        // 生成JWT令牌
        $token = self::generateJWT($user);

        // 返回用户信息和令牌
        return [
            'user' => [
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'phone' => $user['phone'],
                'real_name' => $user['real_name'],
                'avatar_url' => $user['avatar_url'],
                'role' => $user['role'],
                'is_verified' => $user['is_verified']
            ],
            'token' => $token,
            'expires_in' => JWT_EXPIRE_HOURS * 3600
        ];
    }

    /**
     * 生成JWT令牌
     */
    public static function generateJWT($user) {
        $issuedAt = time();
        $expire = $issuedAt + (JWT_EXPIRE_HOURS * 3600);

        $payload = [
            'iss' => APP_URL,  // 签发者
            'aud' => APP_URL,  // 接收者
            'iat' => $issuedAt, // 签发时间
            'exp' => $expire,   // 过期时间
            'data' => [
                'user_id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'role' => $user['role']
            ]
        ];

        return JWT::encode($payload, JWT_SECRET, JWT_ALGORITHM);
    }

    /**
     * 验证JWT令牌
     */
    public static function verifyJWT($token) {
        try {
            $decoded = JWT::decode($token, new Key(JWT_SECRET, JWT_ALGORITHM));
            return (array) $decoded->data;
        } catch (Exception $e) {
            throw new Exception("令牌无效或已过期: " . $e->getMessage(), HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 获取当前用户
     */
    public static function getCurrentUser() {
        $headers = apache_request_headers();
        $authHeader = $headers['Authorization'] ?? '';

        if (empty($authHeader)) {
            return null;
        }

        // 提取Bearer令牌
        if (preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
            $token = $matches[1];
            return self::verifyJWT($token);
        }

        return null;
    }

    /**
     * 检查用户名是否存在
     */
    private static function usernameExists($username) {
        $result = db()->querySingle(
            "SELECT id FROM users WHERE username = ?",
            [$username]
        );
        return $result !== false;
    }

    /**
     * 检查邮箱是否存在
     */
    private static function emailExists($email) {
        $result = db()->querySingle(
            "SELECT id FROM users WHERE email = ?",
            [$email]
        );
        return $result !== false;
    }

    /**
     * 检查手机号是否存在
     */
    private static function phoneExists($phone) {
        $result = db()->querySingle(
            "SELECT id FROM users WHERE phone = ?",
            [$phone]
        );
        return $result !== false;
    }

    /**
     * 通过邮箱获取用户
     */
    private static function getUserByEmail($email) {
        return db()->querySingle(
            "SELECT id, username, email, phone, password_hash, real_name, avatar_url,
                    role, is_verified, is_active, failed_login_attempts, locked_until
             FROM users WHERE email = ?",
            [$email]
        );
    }

    /**
     * 检查登录尝试次数
     */
    private static function checkLoginAttempts($email) {
        $user = db()->querySingle(
            "SELECT failed_login_attempts, locked_until
             FROM users WHERE email = ?",
            [$email]
        );

        if ($user && $user['failed_login_attempts'] >= LOGIN_ATTEMPTS_LIMIT) {
            // 如果还没有锁定时间，设置锁定时间
            if (!$user['locked_until']) {
                $lockedUntil = date('Y-m-d H:i:s', time() + LOGIN_LOCKOUT_TIME);
                db()->execute(
                    "UPDATE users SET locked_until = ? WHERE email = ?",
                    [$lockedUntil, $email]
                );
                throw new Exception("登录失败次数过多，账户已锁定15分钟", HTTP_FORBIDDEN);
            } elseif (strtotime($user['locked_until']) > time()) {
                $lockTime = date('Y-m-d H:i:s', strtotime($user['locked_until']));
                throw new Exception("账户已锁定，解锁时间: {$lockTime}", HTTP_FORBIDDEN);
            }
        }
    }

    /**
     * 记录登录失败
     */
    private static function recordFailedLogin($email) {
        db()->execute(
            "UPDATE users
             SET failed_login_attempts = failed_login_attempts + 1,
                 locked_until = CASE
                     WHEN failed_login_attempts + 1 >= ? THEN DATE_ADD(NOW(), INTERVAL ? SECOND)
                     ELSE locked_until
                 END
             WHERE email = ?",
            [LOGIN_ATTEMPTS_LIMIT, LOGIN_LOCKOUT_TIME, $email]
        );
    }

    /**
     * 重置登录失败计数
     */
    private static function resetFailedLogin($email) {
        db()->execute(
            "UPDATE users
             SET failed_login_attempts = 0,
                 locked_until = NULL
             WHERE email = ?",
            [$email]
        );
    }

    /**
     * 更新最后登录时间
     */
    private static function updateLastLogin($userId) {
        db()->execute(
            "UPDATE users SET last_login_at = NOW() WHERE id = ?",
            [$userId]
        );
    }

    /**
     * 验证验证码
     */
    public static function verifyCode($email, $phone, $code, $type = 'register') {
        $result = db()->querySingle(
            "SELECT id, expires_at, is_used
             FROM verification_codes
             WHERE (email = ? OR phone = ?) AND code = ? AND type = ?
             ORDER BY created_at DESC LIMIT 1",
            [$email, $phone, $code, $type]
        );

        if (!$result) {
            throw new Exception("验证码无效", HTTP_BAD_REQUEST);
        }

        if ($result['is_used']) {
            throw new Exception("验证码已使用", HTTP_BAD_REQUEST);
        }

        if (strtotime($result['expires_at']) < time()) {
            throw new Exception("验证码已过期", HTTP_BAD_REQUEST);
        }

        // 标记为已使用
        db()->execute(
            "UPDATE verification_codes SET is_used = 1 WHERE id = ?",
            [$result['id']]
        );

        return true;
    }

    /**
     * 完成用户验证
     */
    public static function completeVerification($email) {
        $result = db()->execute(
            "UPDATE users SET is_verified = 1 WHERE email = ?",
            [$email]
        );

        if ($result === false) {
            throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
        }

        return true;
    }

    /**
     * 发送验证码（简化版本）
     */
    public static function sendVerificationCode($phone, $type = 'register') {
        // 检查发送频率
        $lastSent = db()->querySingle(
            "SELECT created_at FROM verification_codes
             WHERE phone = ? AND type = ?
             ORDER BY created_at DESC LIMIT 1",
            [$phone, $type]
        );

        if ($lastSent && time() - strtotime($lastSent['created_at']) < SMS_RESEND_INTERVAL) {
            throw new Exception("请等待" . SMS_RESEND_INTERVAL . "秒后重新发送", HTTP_BAD_REQUEST);
        }

        // 生成验证码
        $code = rand(100000, 999999);

        // 保存验证码
        db()->execute(
            "INSERT INTO verification_codes (phone, code, type, expires_at, created_at)
             VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? SECOND), NOW())",
            [$phone, $code, $type, SMS_CODE_EXPIRE]
        );

        // 这里应该调用短信服务API发送验证码
        // 暂时返回模拟结果
        return [
            'success' => true,
            'message' => '验证码已发送',
            'code' => $code, // 实际环境中不应返回验证码
            'expires_in' => SMS_CODE_EXPIRE
        ];
    }

    /**
     * 获取用户资料
     */
    public static function getUserProfile($userId) {
        $user = db()->querySingle(
            "SELECT u.id, u.username, u.email, u.phone, u.real_name, u.avatar_url,
                    u.role, u.is_verified, u.created_at, u.last_login_at,
                    up.gender, up.birth_date, up.id_card_number, up.driver_license_number,
                    up.vehicle_plate_number, up.vehicle_brand, up.vehicle_model,
                    up.vehicle_color, up.vehicle_height, up.vehicle_width
             FROM users u
             LEFT JOIN user_profiles up ON u.id = up.user_id
             WHERE u.id = ?",
            [$userId]
        );

        if (!$user) {
            throw new Exception(ERROR_NOT_FOUND, HTTP_NOT_FOUND);
        }

        // 移除敏感信息
        unset($user['password_hash']);

        return $user;
    }

    /**
     * 更新用户资料
     */
    public static function updateUserProfile($userId, $data) {
        $allowedFields = [
            'real_name', 'avatar_url', 'gender', 'birth_date',
            'vehicle_plate_number', 'vehicle_brand', 'vehicle_model',
            'vehicle_color', 'vehicle_height', 'vehicle_width'
        ];

        $updateFields = [];
        $updateValues = [];

        foreach ($allowedFields as $field) {
            if (isset($data[$field])) {
                $updateFields[] = "{$field} = ?";
                $updateValues[] = $data[$field];
            }
        }

        if (empty($updateFields)) {
            throw new Exception("没有可更新的字段", HTTP_BAD_REQUEST);
        }

        // 更新用户表
        $result = db()->execute(
            "UPDATE users SET " . implode(', ', $updateFields) . " WHERE id = ?",
            array_merge($updateValues, [$userId])
        );

        if ($result === false) {
            throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
        }

        return true;
    }
}