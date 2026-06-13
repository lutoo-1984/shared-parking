<?php
/**
 * 管理后台库
 * 处理管理员操作：审核、统计、系统配置等
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/constants.php';

/**
 * 管理后台类
 */
class Admin {

    /**
     * 验证管理员权限
     */
    private static function requireAdmin() {
        $user = Auth::getCurrentUser();
        if (!$user) {
            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
        }
        if ($user['role'] !== 'admin') {
            throw new Exception(ERROR_AUTHORIZATION, HTTP_FORBIDDEN);
        }
        return $user;
    }

    /**
     * 获取仪表盘统计数据
     */
    public static function getDashboardStats() {
        self::requireAdmin();

        // 今日统计
        $today = date('Y-m-d');

        $todayNewUsers = db()->querySingle(
            "SELECT COUNT(*) as count FROM users WHERE DATE(created_at) = ?",
            [$today]
        )['count'];

        $todayNewBookings = db()->querySingle(
            "SELECT COUNT(*) as count FROM bookings WHERE DATE(created_at) = ?",
            [$today]
        )['count'];

        $todayRevenue = db()->querySingle(
            "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE DATE(paid_at) = ? AND status = 'paid'",
            [$today]
        )['total'];

        // 总体统计
        $totalUsers = db()->querySingle("SELECT COUNT(*) as count FROM users")['count'];
        $totalSpots = db()->querySingle("SELECT COUNT(*) as count FROM parking_spots WHERE is_active = 1")['count'];
        $pendingApproval = db()->querySingle("SELECT COUNT(*) as count FROM parking_spots WHERE is_approved = 0 AND is_active = 1")['count'];
        $totalBookings = db()->querySingle("SELECT COUNT(*) as count FROM bookings")['count'];
        $activeBookings = db()->querySingle("SELECT COUNT(*) as count FROM bookings WHERE status IN ('pending', 'confirmed', 'in_progress')")['count'];
        $totalRevenue = db()->querySingle("SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE status = 'paid'")['total'];
        $totalReviews = db()->querySingle("SELECT COUNT(*) as count FROM reviews")['count'];

        // 本月统计
        $monthStart = date('Y-m-01');
        $monthRevenue = db()->querySingle(
            "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE paid_at >= ? AND status = 'paid'",
            [$monthStart]
        )['total'];

        $monthNewUsers = db()->querySingle(
            "SELECT COUNT(*) as count FROM users WHERE created_at >= ?",
            [$monthStart]
        )['count'];

        $monthNewSpots = db()->querySingle(
            "SELECT COUNT(*) as count FROM parking_spots WHERE created_at >= ?",
            [$monthStart]
        )['count'];

        return [
            'today' => [
                'new_users' => intval($todayNewUsers),
                'new_bookings' => intval($todayNewBookings),
                'revenue' => floatval($todayRevenue)
            ],
            'month' => [
                'revenue' => floatval($monthRevenue),
                'new_users' => intval($monthNewUsers),
                'new_spots' => intval($monthNewSpots)
            ],
            'total' => [
                'users' => intval($totalUsers),
                'spots' => intval($totalSpots),
                'pending_approval' => intval($pendingApproval),
                'bookings' => intval($totalBookings),
                'active_bookings' => intval($activeBookings),
                'revenue' => floatval($totalRevenue),
                'reviews' => intval($totalReviews)
            ]
        ];
    }

    /**
     * 获取所有停车位（含未审核）
     */
    public static function getAllSpots($filters = [], $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        self::requireAdmin();

        $conditions = ["1=1"];
        $params = [];

        // 审核状态筛选
        if (isset($filters['is_approved'])) {
            $conditions[] = "ps.is_approved = ?";
            $params[] = intval($filters['is_approved']);
        }

        // 激活状态筛选
        if (isset($filters['is_active'])) {
            $conditions[] = "ps.is_active = ?";
            $params[] = intval($filters['is_active']);
        }

        // 所有者筛选
        if (!empty($filters['owner_id'])) {
            $conditions[] = "ps.owner_id = ?";
            $params[] = intval($filters['owner_id']);
        }

        // 关键词搜索
        if (!empty($filters['keyword'])) {
            $conditions[] = "(ps.title LIKE ? OR ps.description LIKE ? OR ps.address LIKE ?)";
            $kw = "%{$filters['keyword']}%";
            $params[] = $kw;
            $params[] = $kw;
            $params[] = $kw;
        }

        $whereClause = "WHERE " . implode(" AND ", $conditions);
        $offset = ($page - 1) * $limit;

        $spots = db()->query(
            "SELECT ps.*, u.username as owner_username, u.phone as owner_phone,
                    (SELECT COUNT(*) FROM bookings WHERE spot_id = ps.id) as total_bookings,
                    (SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE spot_id = ps.id AND status = 'completed') as total_income
             FROM parking_spots ps
             LEFT JOIN users u ON ps.owner_id = u.id
             {$whereClause}
             ORDER BY ps.created_at DESC
             LIMIT ? OFFSET ?",
            array_merge($params, [$limit, $offset])
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM parking_spots ps {$whereClause}",
            $params
        )['count'];

        $formatted = array_map(function($spot) {
            // 获取首张图片
            $image = db()->querySingle(
                "SELECT image_url FROM parking_spot_images WHERE spot_id = ? AND is_primary = 1 LIMIT 1",
                [$spot['id']]
            );
            return [
                'id' => intval($spot['id']),
                'owner_id' => intval($spot['owner_id']),
                'owner_username' => $spot['owner_username'],
                'owner_phone' => $spot['owner_phone'],
                'title' => $spot['title'],
                'address' => $spot['address'],
                'price_per_hour' => floatval($spot['price_per_hour']),
                'is_approved' => boolval($spot['is_approved']),
                'is_active' => boolval($spot['is_active']),
                'view_count' => intval($spot['view_count']),
                'total_bookings' => intval($spot['total_bookings']),
                'total_income' => floatval($spot['total_income']),
                'primary_image' => $image ? $image['image_url'] : null,
                'created_at' => $spot['created_at']
            ];
        }, $spots);

        return [
            'spots' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 获取所有用户
     */
    public static function getAllUsers($filters = [], $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        self::requireAdmin();

        $conditions = ["1=1"];
        $params = [];

        if (isset($filters['role'])) {
            $conditions[] = "u.role = ?";
            $params[] = $filters['role'];
        }

        if (isset($filters['is_verified'])) {
            $conditions[] = "u.is_verified = ?";
            $params[] = intval($filters['is_verified']);
        }

        if (isset($filters['is_active'])) {
            $conditions[] = "u.is_active = ?";
            $params[] = intval($filters['is_active']);
        }

        if (!empty($filters['keyword'])) {
            $conditions[] = "(u.username LIKE ? OR u.email LIKE ? OR u.phone LIKE ? OR u.real_name LIKE ?)";
            $kw = "%{$filters['keyword']}%";
            $params[] = $kw; $params[] = $kw; $params[] = $kw; $params[] = $kw;
        }

        $whereClause = "WHERE " . implode(" AND ", $conditions);
        $offset = ($page - 1) * $limit;

        $users = db()->query(
            "SELECT u.id, u.username, u.email, u.phone, u.real_name, u.avatar_url,
                    u.role, u.is_verified, u.is_active, u.created_at, u.last_login_at,
                    (SELECT COUNT(*) FROM parking_spots WHERE owner_id = u.id AND is_active = 1) as spot_count,
                    (SELECT COUNT(*) FROM bookings WHERE user_id = u.id) as booking_count,
                    (SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE user_id = u.id AND status = 'completed') as total_spent
             FROM users u
             {$whereClause}
             ORDER BY u.created_at DESC
             LIMIT ? OFFSET ?",
            array_merge($params, [$limit, $offset])
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM users u {$whereClause}",
            $params
        )['count'];

        $formatted = array_map(function($u) {
            return [
                'id' => intval($u['id']),
                'username' => $u['username'],
                'email' => $u['email'],
                'phone' => $u['phone'],
                'real_name' => $u['real_name'],
                'avatar_url' => $u['avatar_url'],
                'role' => $u['role'],
                'is_verified' => boolval($u['is_verified']),
                'is_active' => boolval($u['is_active']),
                'spot_count' => intval($u['spot_count']),
                'booking_count' => intval($u['booking_count']),
                'total_spent' => floatval($u['total_spent'] ?: 0),
                'last_login_at' => $u['last_login_at'],
                'created_at' => $u['created_at']
            ];
        }, $users);

        return [
            'users' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 审核停车位
     */
    public static function approveSpot($spotId, $data) {
        self::requireAdmin();

        $isApproved = isset($data['is_approved']) ? intval($data['is_approved']) : 1;
        $approvalNotes = $data['approval_notes'] ?? '';

        $spot = db()->querySingle(
            "SELECT * FROM parking_spots WHERE id = ?",
            [$spotId]
        );

        if (!$spot) {
            throw new Exception('停车位不存在', HTTP_NOT_FOUND);
        }

        db()->execute(
            "UPDATE parking_spots SET is_approved = ?, approval_notes = ?, updated_at = NOW() WHERE id = ?",
            [$isApproved, $approvalNotes, $spotId]
        );

        $status = $isApproved ? '已通过' : '未通过';
        $message = "停车位「{$spot['title']}」审核{$status}";

        // 发送通知给车位所有者
        $notificationMsg = "您的停车位「{$spot['title']}」已审核{$status}";
        if ($approvalNotes) {
            $notificationMsg .= "，审核备注：{$approvalNotes}";
        }

        db()->insert(
            "INSERT INTO messages (sender_id, receiver_id, subject, content, created_at)
             VALUES (?, ?, ?, ?, NOW())",
            [1, $spot['owner_id'], '车位审核通知', $notificationMsg]
        );

        return [
            'success' => true,
            'message' => $message,
            'data' => [
                'id' => intval($spotId),
                'is_approved' => boolval($isApproved),
                'approval_notes' => $approvalNotes
            ]
        ];
    }

    /**
     * 管理用户状态（禁用/启用）
     */
    public static function manageUser($userId, $data) {
        self::requireAdmin();

        $user = db()->querySingle("SELECT * FROM users WHERE id = ?", [$userId]);
        if (!$user) {
            throw new Exception('用户不存在', HTTP_NOT_FOUND);
        }

        if (isset($data['is_active'])) {
            db()->execute(
                "UPDATE users SET is_active = ?, updated_at = NOW() WHERE id = ?",
                [intval($data['is_active']), $userId]
            );
        }

        if (isset($data['role'])) {
            $validRoles = ['user', 'admin'];
            if (!in_array($data['role'], $validRoles)) {
                throw new Exception('无效的角色', HTTP_BAD_REQUEST);
            }
            db()->execute(
                "UPDATE users SET role = ?, updated_at = NOW() WHERE id = ?",
                [$data['role'], $userId]
            );
        }

        return [
            'success' => true,
            'message' => '用户状态已更新',
            'data' => [
                'id' => intval($userId),
                'is_active' => isset($data['is_active']) ? boolval($data['is_active']) : boolval($user['is_active']),
                'role' => $data['role'] ?? $user['role']
            ]
        ];
    }

    /**
     * 获取系统配置
     */
    public static function getSystemSettings() {
        self::requireAdmin();

        $settings = db()->query("SELECT `key`, `value`, `description`, `is_public` FROM system_settings ORDER BY `key`");

        $formatted = [];
        foreach ($settings as $s) {
            $formatted[] = [
                'key' => $s['key'],
                'value' => $s['value'],
                'description' => $s['description'],
                'is_public' => boolval($s['is_public'])
            ];
        }

        return ['settings' => $formatted];
    }

    /**
     * 更新系统配置
     */
    public static function updateSystemSetting($key, $value) {
        self::requireAdmin();

        $existing = db()->querySingle(
            "SELECT id FROM system_settings WHERE `key` = ?",
            [$key]
        );

        if (!$existing) {
            throw new Exception('配置项不存在', HTTP_NOT_FOUND);
        }

        db()->execute(
            "UPDATE system_settings SET `value` = ?, updated_at = NOW() WHERE `key` = ?",
            [$value, $key]
        );

        return [
            'success' => true,
            'message' => '配置已更新',
            'data' => ['key' => $key, 'value' => $value]
        ];
    }
}
