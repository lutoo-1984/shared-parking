<?php
/**
 * 评价管理库
 * 处理评价的创建、查询、回复等功能
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/constants.php';

/**
 * 评价管理类
 */
class Review {

    /**
     * 创建评价
     */
    public static function createReview($userId, $data) {
        // 验证必填字段
        if (empty($data['booking_id'])) {
            throw new Exception('预订ID不能为空', HTTP_BAD_REQUEST);
        }
        if (empty($data['rating']) || intval($data['rating']) < 1 || intval($data['rating']) > 5) {
            throw new Exception('评分必须为1-5星', HTTP_BAD_REQUEST);
        }

        $bookingId = intval($data['booking_id']);
        $rating = intval($data['rating']);

        // 验证预订存在且属于该用户
        $booking = db()->querySingle(
            "SELECT b.*, ps.owner_id
             FROM bookings b
             JOIN parking_spots ps ON b.spot_id = ps.id
             WHERE b.id = ? AND b.user_id = ?",
            [$bookingId, $userId]
        );

        if (!$booking) {
            throw new Exception('预订不存在或无权评价', HTTP_NOT_FOUND);
        }

        // 只能评价已完成的预订
        if ($booking['status'] !== 'completed') {
            throw new Exception('只能评价已完成的预订', HTTP_BAD_REQUEST);
        }

        // 检查是否已评价过
        $existingReview = db()->querySingle(
            "SELECT id FROM reviews WHERE booking_id = ?",
            [$bookingId]
        );

        if ($existingReview) {
            throw new Exception('该预订已评价过', HTTP_CONFLICT);
        }

        $spotId = intval($booking['spot_id']);
        $title = $data['title'] ?? '';
        $content = $data['content'] ?? '';

        // 创建评价
        $reviewId = db()->insert(
            "INSERT INTO reviews (booking_id, user_id, spot_id, rating, title, content, is_verified, is_visible, created_at, updated_at)
             VALUES (?, ?, ?, ?, ?, ?, 1, 1, NOW(), NOW())",
            [$bookingId, $userId, $spotId, $rating, $title, $content]
        );

        if (!$reviewId) {
            throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
        }

        return self::getReviewById($reviewId);
    }

    /**
     * 获取单个评价详情
     */
    public static function getReviewById($reviewId) {
        $review = db()->querySingle(
            "SELECT r.*, u.username, u.avatar_url
             FROM reviews r
             JOIN users u ON r.user_id = u.id
             WHERE r.id = ? AND r.is_visible = 1",
            [$reviewId]
        );

        if (!$review) {
            return null;
        }

        return self::formatReview($review);
    }

    /**
     * 获取车位的评价列表
     */
    public static function getSpotReviews($spotId, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        $offset = ($page - 1) * $limit;

        $reviews = db()->query(
            "SELECT r.*, u.username, u.avatar_url
             FROM reviews r
             JOIN users u ON r.user_id = u.id
             WHERE r.spot_id = ? AND r.is_visible = 1
             ORDER BY r.created_at DESC
             LIMIT ? OFFSET ?",
            [$spotId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM reviews WHERE spot_id = ? AND is_visible = 1",
            [$spotId]
        )['count'];

        // 获取评分统计
        $ratingStats = db()->querySingle(
            "SELECT AVG(rating) as avg_rating, COUNT(*) as total_count,
                    SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) as five_star,
                    SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) as four_star,
                    SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) as three_star,
                    SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) as two_star,
                    SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) as one_star
             FROM reviews WHERE spot_id = ? AND is_visible = 1",
            [$spotId]
        );

        $formatted = array_map('self::formatReview', $reviews);

        return [
            'reviews' => $formatted,
            'rating_stats' => [
                'average' => round(floatval($ratingStats['avg_rating'] ?: 0), 1),
                'total' => intval($ratingStats['total_count'] ?: 0),
                'distribution' => [
                    5 => intval($ratingStats['five_star'] ?: 0),
                    4 => intval($ratingStats['four_star'] ?: 0),
                    3 => intval($ratingStats['three_star'] ?: 0),
                    2 => intval($ratingStats['two_star'] ?: 0),
                    1 => intval($ratingStats['one_star'] ?: 0)
                ]
            ],
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 获取用户的评价列表
     */
    public static function getUserReviews($userId, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        $offset = ($page - 1) * $limit;

        $reviews = db()->query(
            "SELECT r.*, u.username, u.avatar_url
             FROM reviews r
             JOIN users u ON r.user_id = u.id
             WHERE r.user_id = ?
             ORDER BY r.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM reviews WHERE user_id = ?",
            [$userId]
        )['count'];

        $formatted = array_map('self::formatReview', $reviews);

        return [
            'reviews' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 车主回复评价
     */
    public static function replyToReview($reviewId, $ownerId, $reply) {
        if (empty(trim($reply))) {
            throw new Exception('回复内容不能为空', HTTP_BAD_REQUEST);
        }

        $review = db()->querySingle(
            "SELECT r.*, ps.owner_id
             FROM reviews r
             JOIN parking_spots ps ON r.spot_id = ps.id
             WHERE r.id = ?",
            [$reviewId]
        );

        if (!$review) {
            throw new Exception('评价不存在', HTTP_NOT_FOUND);
        }

        if ($review['owner_id'] != $ownerId) {
            throw new Exception('只有车位所有者才能回复评价', HTTP_FORBIDDEN);
        }

        db()->execute(
            "UPDATE reviews SET owner_reply = ?, updated_at = NOW() WHERE id = ?",
            [$reply, $reviewId]
        );

        return self::getReviewById($reviewId);
    }

    /**
     * 格式化评价数据
     */
    private static function formatReview($review) {
        return [
            'id' => intval($review['id']),
            'booking_id' => intval($review['booking_id']),
            'user_id' => intval($review['user_id']),
            'spot_id' => intval($review['spot_id']),
            'rating' => intval($review['rating']),
            'title' => $review['title'],
            'content' => $review['content'],
            'owner_reply' => $review['owner_reply'],
            'is_verified' => boolval($review['is_verified']),
            'username' => $review['username'],
            'avatar_url' => $review['avatar_url'],
            'created_at' => $review['created_at'],
            'updated_at' => $review['updated_at']
        ];
    }
}
