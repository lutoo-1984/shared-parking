<?php
/**
 * 消息管理库
 * 处理站内消息的发送、接收、已读标记等功能
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/constants.php';

/**
 * 消息管理类
 */
class Message {

    /**
     * 发送消息
     */
    public static function sendMessage($senderId, $data) {
        // 验证必填字段
        if (empty($data['receiver_id'])) {
            throw new Exception('接收者ID不能为空', HTTP_BAD_REQUEST);
        }
        if (empty(trim($data['content'] ?? ''))) {
            throw new Exception('消息内容不能为空', HTTP_BAD_REQUEST);
        }

        $receiverId = intval($data['receiver_id']);
        $content = trim($data['content']);
        $subject = trim($data['subject'] ?? '');
        $bookingId = !empty($data['booking_id']) ? intval($data['booking_id']) : null;

        // 验证接收者存在
        $receiver = db()->querySingle(
            "SELECT id, is_active FROM users WHERE id = ?",
            [$receiverId]
        );

        if (!$receiver) {
            throw new Exception('接收者不存在', HTTP_NOT_FOUND);
        }

        if (!$receiver['is_active']) {
            throw new Exception('接收者账户已禁用', HTTP_FORBIDDEN);
        }

        // 不能给自己发消息
        if ($senderId == $receiverId) {
            throw new Exception('不能给自己发送消息', HTTP_BAD_REQUEST);
        }

        // 验证关联的预订（如果提供）
        if ($bookingId) {
            $booking = db()->querySingle(
                "SELECT id FROM bookings WHERE id = ? AND (user_id = ? OR spot_id IN (SELECT id FROM parking_spots WHERE owner_id = ?))",
                [$bookingId, $senderId, $senderId]
            );
            if (!$booking) {
                throw new Exception('预订不存在或无权关联', HTTP_NOT_FOUND);
            }
        }

        $messageId = db()->insert(
            "INSERT INTO messages (sender_id, receiver_id, booking_id, subject, content, is_read, created_at)
             VALUES (?, ?, ?, ?, ?, 0, NOW())",
            [$senderId, $receiverId, $bookingId, $subject, $content]
        );

        if (!$messageId) {
            throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
        }

        return self::getMessageById($messageId);
    }

    /**
     * 获取用户的收件消息列表
     */
    public static function getInbox($userId, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        $offset = ($page - 1) * $limit;

        $messages = db()->query(
            "SELECT m.*,
                    sender.username as sender_username,
                    sender.avatar_url as sender_avatar,
                    receiver.username as receiver_username
             FROM messages m
             JOIN users sender ON m.sender_id = sender.id
             JOIN users receiver ON m.receiver_id = receiver.id
             WHERE m.receiver_id = ?
             ORDER BY m.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ?",
            [$userId]
        )['count'];

        $formatted = array_map('self::formatMessage', $messages);

        return [
            'messages' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ],
            'unread_count' => self::getUnreadCount($userId)
        ];
    }

    /**
     * 获取用户的发件消息列表
     */
    public static function getOutbox($userId, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        $offset = ($page - 1) * $limit;

        $messages = db()->query(
            "SELECT m.*,
                    sender.username as sender_username,
                    sender.avatar_url as sender_avatar,
                    receiver.username as receiver_username,
                    receiver.avatar_url as receiver_avatar
             FROM messages m
             JOIN users sender ON m.sender_id = sender.id
             JOIN users receiver ON m.receiver_id = receiver.id
             WHERE m.sender_id = ?
             ORDER BY m.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM messages WHERE sender_id = ?",
            [$userId]
        )['count'];

        $formatted = array_map('self::formatMessage', $messages);

        return [
            'messages' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 获取两个用户之间的对话
     */
    public static function getConversation($userId, $otherUserId, $page = 1, $limit = 50) {
        $offset = ($page - 1) * $limit;

        $messages = db()->query(
            "SELECT m.*,
                    sender.username as sender_username,
                    sender.avatar_url as sender_avatar
             FROM messages m
             JOIN users sender ON m.sender_id = sender.id
             WHERE (m.sender_id = ? AND m.receiver_id = ?)
                OR (m.sender_id = ? AND m.receiver_id = ?)
             ORDER BY m.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $otherUserId, $otherUserId, $userId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM messages
             WHERE (sender_id = ? AND receiver_id = ?)
                OR (sender_id = ? AND receiver_id = ?)",
            [$userId, $otherUserId, $otherUserId, $userId]
        )['count'];

        // 将对话中的消息标记为已读
        db()->execute(
            "UPDATE messages SET is_read = 1, read_at = NOW()
             WHERE sender_id = ? AND receiver_id = ? AND is_read = 0",
            [$otherUserId, $userId]
        );

        $formatted = array_map('self::formatMessage', $messages);

        return [
            'messages' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 标记消息为已读
     */
    public static function markAsRead($messageId, $userId) {
        $message = db()->querySingle(
            "SELECT * FROM messages WHERE id = ? AND receiver_id = ?",
            [$messageId, $userId]
        );

        if (!$message) {
            throw new Exception('消息不存在或无权操作', HTTP_NOT_FOUND);
        }

        db()->execute(
            "UPDATE messages SET is_read = 1, read_at = NOW() WHERE id = ?",
            [$messageId]
        );

        return self::getMessageById($messageId);
    }

    /**
     * 标记所有消息为已读
     */
    public static function markAllAsRead($userId) {
        db()->execute(
            "UPDATE messages SET is_read = 1, read_at = NOW() WHERE receiver_id = ? AND is_read = 0",
            [$userId]
        );

        return ['affected_count' => db()->execute(
            "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND is_read = 0",
            [$userId]
        )];
    }

    /**
     * 获取未读消息数
     */
    public static function getUnreadCount($userId) {
        $result = db()->querySingle(
            "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND is_read = 0",
            [$userId]
        );
        return intval($result['count'] ?: 0);
    }

    /**
     * 删除消息
     */
    public static function deleteMessage($messageId, $userId) {
        $message = db()->querySingle(
            "SELECT * FROM messages WHERE id = ? AND (sender_id = ? OR receiver_id = ?)",
            [$messageId, $userId, $userId]
        );

        if (!$message) {
            throw new Exception('消息不存在或无权操作', HTTP_NOT_FOUND);
        }

        db()->execute("DELETE FROM messages WHERE id = ?", [$messageId]);
        return true;
    }

    /**
     * 获取单条消息
     */
    private static function getMessageById($messageId) {
        $message = db()->querySingle(
            "SELECT m.*,
                    sender.username as sender_username,
                    sender.avatar_url as sender_avatar,
                    receiver.username as receiver_username,
                    receiver.avatar_url as receiver_avatar
             FROM messages m
             JOIN users sender ON m.sender_id = sender.id
             JOIN users receiver ON m.receiver_id = receiver.id
             WHERE m.id = ?",
            [$messageId]
        );

        return $message ? self::formatMessage($message) : null;
    }

    /**
     * 格式化消息数据
     */
    private static function formatMessage($msg) {
        return [
            'id' => intval($msg['id']),
            'sender_id' => intval($msg['sender_id']),
            'receiver_id' => intval($msg['receiver_id']),
            'booking_id' => $msg['booking_id'] ? intval($msg['booking_id']) : null,
            'subject' => $msg['subject'],
            'content' => $msg['content'],
            'is_read' => boolval($msg['is_read']),
            'read_at' => $msg['read_at'],
            'created_at' => $msg['created_at'],
            'sender_username' => $msg['sender_username'],
            'sender_avatar' => $msg['sender_avatar'],
            'receiver_username' => $msg['receiver_username'],
            'receiver_avatar' => $msg['receiver_avatar'] ?? null
        ];
    }
}
