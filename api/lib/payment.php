<?php
/**
 * 支付管理库
 * 处理支付创建、通知、查询、退款等功能
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/constants.php';

/**
 * 支付管理类
 */
class Payment {

    /**
     * 创建支付订单
     */
    public static function createPayment($userId, $data) {
        // 验证必填字段
        if (empty($data['booking_id'])) {
            throw new Exception('预订ID不能为空', HTTP_BAD_REQUEST);
        }

        $bookingId = intval($data['booking_id']);
        $paymentMethod = $data['payment_method'] ?? 'wallet';

        // 验证支付方式
        $validMethods = ['alipay', 'wechat', 'credit_card', 'wallet'];
        if (!in_array($paymentMethod, $validMethods)) {
            throw new Exception('不支持的支付方式', HTTP_BAD_REQUEST);
        }

        // 获取预订信息
        $booking = db()->querySingle(
            "SELECT b.*, ps.title as spot_title, ps.owner_id
             FROM bookings b
             JOIN parking_spots ps ON b.spot_id = ps.id
             WHERE b.id = ? AND b.user_id = ?",
            [$bookingId, $userId]
        );

        if (!$booking) {
            throw new Exception('预订不存在或无权操作', HTTP_NOT_FOUND);
        }

        // 检查预订状态
        if (!in_array($booking['status'], ['pending', 'confirmed'])) {
            throw new Exception('当前预订状态不能进行支付', HTTP_BAD_REQUEST);
        }

        // 检查是否已有支付记录
        $existingPayment = db()->querySingle(
            "SELECT id, status FROM payments WHERE booking_id = ? AND user_id = ? ORDER BY created_at DESC LIMIT 1",
            [$bookingId, $userId]
        );

        if ($existingPayment) {
            if ($existingPayment['status'] === 'paid') {
                throw new Exception('该预订已完成支付', HTTP_CONFLICT);
            }
            // 如果有未完成的支付，返回已存在的支付ID
            if ($existingPayment['status'] === 'pending') {
                return self::getPaymentDetail($existingPayment['id']);
            }
        }

        $amount = floatval($booking['total_price']);

        // 开始事务
        $db = db();
        try {
            $db->beginTransaction();

            // 生成交易流水号
            $transactionId = self::generateTransactionId();

            // 创建支付记录
            $paymentId = $db->insert(
                "INSERT INTO payments (booking_id, user_id, payment_method, transaction_id, amount, currency, status, created_at, updated_at)
                 VALUES (?, ?, ?, ?, ?, 'CNY', 'pending', NOW(), NOW())",
                [$bookingId, $userId, $paymentMethod, $transactionId, $amount]
            );

            if (!$paymentId) {
                throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
            }

            // 更新预订状态为已确认
            if ($booking['status'] === 'pending') {
                $db->execute(
                    "UPDATE bookings SET status = 'confirmed', updated_at = NOW() WHERE id = ?",
                    [$bookingId]
                );
            }

            $db->commit();

            $payment = self::getPaymentDetail($paymentId);
            return $payment;

        } catch (Exception $e) {
            $db->rollBack();
            throw $e;
        }
    }

    /**
     * 处理支付通知（模拟支付网关回调）
     */
    public static function handleNotify($gateway, $data) {
        // 验证签名等安全校验（简化版本）
        $transactionId = $data['transaction_id'] ?? '';
        $paymentId = $data['payment_id'] ?? '';

        if (empty($transactionId) && empty($paymentId)) {
            throw new Exception('无效的通知参数', HTTP_BAD_REQUEST);
        }

        // 查找支付记录
        if (!empty($paymentId)) {
            $payment = db()->querySingle(
                "SELECT * FROM payments WHERE id = ?",
                [$paymentId]
            );
        } else {
            $payment = db()->querySingle(
                "SELECT * FROM payments WHERE transaction_id = ?",
                [$transactionId]
            );
        }

        if (!$payment) {
            throw new Exception('支付记录不存在', HTTP_NOT_FOUND);
        }

        if ($payment['status'] === 'paid') {
            return ['message' => '支付已完成', 'payment' => self::getPaymentDetail($payment['id'])];
        }

        // 模拟支付成功处理
        $db = db();
        try {
            $db->beginTransaction();

            // 更新支付状态
            $db->execute(
                "UPDATE payments SET status = 'paid', paid_at = NOW(), updated_at = NOW() WHERE id = ?",
                [$payment['id']]
            );

            // 更新预订状态为进行中（如果预订时间已到或即将到来）
            $booking = db()->querySingle(
                "SELECT * FROM bookings WHERE id = ?",
                [$payment['booking_id']]
            );

            if ($booking && $booking['status'] === 'confirmed') {
                $db->execute(
                    "UPDATE bookings SET status = 'in_progress', updated_at = NOW() WHERE id = ?",
                    [$booking['id']]
                );
            }

            $db->commit();

            return [
                'message' => '支付成功',
                'payment' => self::getPaymentDetail($payment['id'])
            ];

        } catch (Exception $e) {
            $db->rollBack();
            throw $e;
        }
    }

    /**
     * 获取支付详情
     */
    public static function getPaymentDetail($paymentId) {
        $payment = db()->querySingle(
            "SELECT p.*, b.spot_id, b.total_price as booking_amount,
                    b.start_time, b.end_time, b.status as booking_status
             FROM payments p
             JOIN bookings b ON p.booking_id = b.id
             WHERE p.id = ?",
            [$paymentId]
        );

        if (!$payment) {
            return null;
        }

        return [
            'id' => intval($payment['id']),
            'booking_id' => intval($payment['booking_id']),
            'user_id' => intval($payment['user_id']),
            'payment_method' => $payment['payment_method'],
            'transaction_id' => $payment['transaction_id'],
            'amount' => floatval($payment['amount']),
            'currency' => $payment['currency'],
            'status' => $payment['status'],
            'refund_amount' => floatval($payment['refund_amount'] ?: 0),
            'refund_reason' => $payment['refund_reason'],
            'refunded_at' => $payment['refunded_at'],
            'paid_at' => $payment['paid_at'],
            'created_at' => $payment['created_at'],
            'updated_at' => $payment['updated_at'],
            'spot_id' => intval($payment['spot_id']),
            'booking_amount' => floatval($payment['booking_amount']),
            'start_time' => $payment['start_time'],
            'end_time' => $payment['end_time'],
            'booking_status' => $payment['booking_status']
        ];
    }

    /**
     * 获取用户支付记录列表
     */
    public static function getUserPayments($userId, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        $offset = ($page - 1) * $limit;

        $payments = db()->query(
            "SELECT p.*, b.spot_id, b.total_price as booking_amount,
                    b.start_time, b.end_time
             FROM payments p
             JOIN bookings b ON p.booking_id = b.id
             WHERE p.user_id = ?
             ORDER BY p.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM payments WHERE user_id = ?",
            [$userId]
        )['count'];

        $formatted = array_map(function($p) {
            return [
                'id' => intval($p['id']),
                'booking_id' => intval($p['booking_id']),
                'payment_method' => $p['payment_method'],
                'transaction_id' => $p['transaction_id'],
                'amount' => floatval($p['amount']),
                'currency' => $p['currency'],
                'status' => $p['status'],
                'refund_amount' => floatval($p['refund_amount'] ?: 0),
                'paid_at' => $p['paid_at'],
                'created_at' => $p['created_at'],
                'spot_id' => intval($p['spot_id']),
                'booking_amount' => floatval($p['booking_amount']),
                'start_time' => $p['start_time'],
                'end_time' => $p['end_time']
            ];
        }, $payments);

        return [
            'payments' => $formatted,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => intval($total),
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 处理退款
     */
    public static function processRefund($paymentId, $userId, $data) {
        $payment = db()->querySingle(
            "SELECT p.*, b.status as booking_status
             FROM payments p
             JOIN bookings b ON p.booking_id = b.id
             WHERE p.id = ?",
            [$paymentId]
        );

        if (!$payment) {
            throw new Exception('支付记录不存在', HTTP_NOT_FOUND);
        }

        // 仅管理员或支付用户可发起退款
        $currentUser = Auth::getCurrentUser();
        if (!$currentUser || ($currentUser['user_id'] != $payment['user_id'] && $currentUser['role'] !== 'admin')) {
            throw new Exception(ERROR_AUTHORIZATION, HTTP_FORBIDDEN);
        }

        if ($payment['status'] !== 'paid') {
            throw new Exception('当前支付状态不能退款', HTTP_BAD_REQUEST);
        }

        $refundAmount = isset($data['refund_amount']) ? floatval($data['refund_amount']) : floatval($payment['amount']);
        $refundReason = $data['refund_reason'] ?? '用户申请退款';

        if ($refundAmount <= 0 || $refundAmount > floatval($payment['amount'])) {
            throw new Exception('退款金额无效', HTTP_BAD_REQUEST);
        }

        $db = db();
        try {
            $db->beginTransaction();

            $db->execute(
                "UPDATE payments SET status = 'refunded', refund_amount = ?, refund_reason = ?, refunded_at = NOW(), updated_at = NOW() WHERE id = ?",
                [$refundAmount, $refundReason, $paymentId]
            );

            // 取消关联的预订
            $db->execute(
                "UPDATE bookings SET status = 'cancelled', cancelled_by = 'system', cancellation_reason = ?, cancelled_at = NOW(), updated_at = NOW() WHERE id = ?",
                ['支付已退款: ' . $refundReason, $payment['booking_id']]
            );

            $db->commit();

            return self::getPaymentDetail($paymentId);

        } catch (Exception $e) {
            $db->rollBack();
            throw $e;
        }
    }

    /**
     * 生成交易流水号
     */
    private static function generateTransactionId() {
        $prefix = 'PAY';
        $date = date('YmdHis');
        $rand = strtoupper(substr(md5(uniqid(mt_rand(), true)), 0, 8));
        return $prefix . $date . $rand;
    }
}
