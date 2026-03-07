<?php
/**
 * 停车位管理库
 * 处理停车位的CRUD操作、搜索、预订等
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/constants.php';

/**
 * 停车位管理类
 */
class Parking {

    /**
     * 创建停车位
     */
    public static function createSpot($ownerId, $data) {
        // 验证必填字段
        $required = ['title', 'address', 'latitude', 'longitude', 'price_per_hour'];
        foreach ($required as $field) {
            if (empty($data[$field])) {
                throw new Exception("{$field} 字段不能为空", HTTP_BAD_REQUEST);
            }
        }

        // 验证坐标
        $latitude = floatval($data['latitude']);
        $longitude = floatval($data['longitude']);
        if ($latitude < -90 || $latitude > 90 || $longitude < -180 || $longitude > 180) {
            throw new Exception("坐标无效", HTTP_BAD_REQUEST);
        }

        // 验证价格
        $pricePerHour = floatval($data['price_per_hour']);
        if ($pricePerHour <= 0) {
            throw new Exception("价格必须大于0", HTTP_BAD_REQUEST);
        }

        // 准备插入数据
        $fields = [
            'owner_id' => $ownerId,
            'title' => $data['title'],
            'description' => $data['description'] ?? '',
            'address' => $data['address'],
            'latitude' => $latitude,
            'longitude' => $longitude,
            'price_per_hour' => $pricePerHour,
            'price_per_day' => isset($data['price_per_day']) ? floatval($data['price_per_day']) : null,
            'price_unit' => $data['price_unit'] ?? PRICE_UNIT_HOUR,
            'max_vehicle_height' => isset($data['max_vehicle_height']) ? floatval($data['max_vehicle_height']) : null,
            'max_vehicle_width' => isset($data['max_vehicle_width']) ? floatval($data['max_vehicle_width']) : null,
            'available_spots' => isset($data['available_spots']) ? intval($data['available_spots']) : 1,
            'total_spots' => isset($data['total_spots']) ? intval($data['total_spots']) : 1,
            'is_covered' => isset($data['is_covered']) ? intval($data['is_covered']) : 0,
            'has_lighting' => isset($data['has_lighting']) ? intval($data['has_lighting']) : 0,
            'has_security' => isset($data['has_security']) ? intval($data['has_security']) : 0,
            'has_charging' => isset($data['has_charging']) ? intval($data['has_charging']) : 0,
            'has_cctv' => isset($data['has_cctv']) ? intval($data['has_cctv']) : 0,
            'is_24h_access' => isset($data['is_24h_access']) ? intval($data['is_24h_access']) : 0,
            'is_active' => 1,
            'is_approved' => 0 // 新添加的车位需要审核
        ];

        // 构建SQL
        $columns = implode(', ', array_keys($fields));
        $placeholders = implode(', ', array_fill(0, count($fields), '?'));
        $values = array_values($fields);

        // 插入数据
        $db = db();
        try {
            $db->beginTransaction();

            $spotId = $db->insert(
                "INSERT INTO parking_spots ({$columns}) VALUES ({$placeholders})",
                $values
            );

            if (!$spotId) {
                throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
            }

            // 插入默认可用时间（每天00:00-23:59）
            if (isset($data['availability']) && is_array($data['availability'])) {
                self::updateAvailability($spotId, $data['availability']);
            } else {
                // 默认全天可用
                for ($day = 0; $day < 7; $day++) {
                    $db->execute(
                        "INSERT INTO parking_spot_availability (spot_id, day_of_week, start_time, end_time, is_available)
                         VALUES (?, ?, '00:00:00', '23:59:59', 1)",
                        [$spotId, $day]
                    );
                }
            }

            // 处理图片
            if (isset($data['images']) && is_array($data['images'])) {
                self::addImages($spotId, $data['images']);
            }

            $db->commit();

            // 返回创建的车位信息
            return self::getSpotById($spotId, $ownerId);

        } catch (Exception $e) {
            $db->rollBack();
            throw $e;
        }
    }

    /**
     * 更新停车位
     */
    public static function updateSpot($spotId, $ownerId, $data) {
        // 验证车位存在且属于该用户
        $spot = self::getSpotById($spotId, $ownerId);
        if (!$spot) {
            throw new Exception(ERROR_NOT_FOUND, HTTP_NOT_FOUND);
        }

        // 检查车位是否已审核通过（已审核的车位可能限制修改）
        if ($spot['is_approved'] && isset($data['latitude']) || isset($data['longitude']) || isset($data['address'])) {
            throw new Exception("已审核的车位不能修改位置信息", HTTP_FORBIDDEN);
        }

        // 构建更新字段
        $allowedFields = [
            'title', 'description', 'address', 'latitude', 'longitude',
            'price_per_hour', 'price_per_day', 'price_unit',
            'max_vehicle_height', 'max_vehicle_width',
            'available_spots', 'total_spots',
            'is_covered', 'has_lighting', 'has_security', 'has_charging',
            'has_cctv', 'is_24h_access', 'is_active'
        ];

        $updateFields = [];
        $updateValues = [];

        foreach ($allowedFields as $field) {
            if (isset($data[$field])) {
                $updateFields[] = "{$field} = ?";

                // 特殊处理浮点数
                if (in_array($field, ['latitude', 'longitude', 'price_per_hour', 'price_per_day',
                    'max_vehicle_height', 'max_vehicle_width'])) {
                    $updateValues[] = floatval($data[$field]);
                } elseif (in_array($field, ['available_spots', 'total_spots'])) {
                    $updateValues[] = intval($data[$field]);
                } elseif (in_array($field, ['is_covered', 'has_lighting', 'has_security',
                    'has_charging', 'has_cctv', 'is_24h_access', 'is_active'])) {
                    $updateValues[] = intval($data[$field]);
                } else {
                    $updateValues[] = $data[$field];
                }
            }
        }

        if (empty($updateFields)) {
            throw new Exception("没有可更新的字段", HTTP_BAD_REQUEST);
        }

        // 执行更新
        $db = db();
        try {
            $db->beginTransaction();

            // 更新车位信息
            $result = $db->execute(
                "UPDATE parking_spots SET " . implode(', ', $updateFields) . " WHERE id = ? AND owner_id = ?",
                array_merge($updateValues, [$spotId, $ownerId])
            );

            if ($result === false) {
                throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
            }

            // 更新可用时间
            if (isset($data['availability']) && is_array($data['availability'])) {
                self::updateAvailability($spotId, $data['availability']);
            }

            // 更新图片
            if (isset($data['images']) && is_array($data['images'])) {
                self::updateImages($spotId, $data['images']);
            }

            $db->commit();

            // 返回更新后的车位信息
            return self::getSpotById($spotId, $ownerId);

        } catch (Exception $e) {
            $db->rollBack();
            throw $e;
        }
    }

    /**
     * 删除停车位
     */
    public static function deleteSpot($spotId, $ownerId) {
        // 验证车位存在且属于该用户
        $spot = self::getSpotById($spotId, $ownerId);
        if (!$spot) {
            throw new Exception(ERROR_NOT_FOUND, HTTP_NOT_FOUND);
        }

        // 检查是否有未完成的预订
        $activeBookings = db()->querySingle(
            "SELECT COUNT(*) as count FROM bookings
             WHERE spot_id = ? AND status IN ('pending', 'confirmed', 'in_progress')",
            [$spotId]
        );

        if ($activeBookings && $activeBookings['count'] > 0) {
            throw new Exception("该车位还有未完成的预订，不能删除", HTTP_CONFLICT);
        }

        // 软删除：标记为不活跃
        $result = db()->execute(
            "UPDATE parking_spots SET is_active = 0 WHERE id = ? AND owner_id = ?",
            [$spotId, $ownerId]
        );

        if ($result === false) {
            throw new Exception(ERROR_DATABASE, HTTP_INTERNAL_ERROR);
        }

        return true;
    }

    /**
     * 获取停车位详情
     */
    public static function getSpotById($spotId, $userId = null) {
        $spot = db()->querySingle(
            "SELECT ps.*,
                    u.username as owner_username,
                    u.avatar_url as owner_avatar,
                    (SELECT AVG(rating) FROM reviews WHERE spot_id = ps.id) as avg_rating,
                    (SELECT COUNT(*) FROM reviews WHERE spot_id = ps.id) as review_count
             FROM parking_spots ps
             LEFT JOIN users u ON ps.owner_id = u.id
             WHERE ps.id = ? AND ps.is_active = 1",
            [$spotId]
        );

        if (!$spot) {
            return null;
        }

        // 获取图片
        $spot['images'] = db()->query(
            "SELECT image_url, image_order, is_primary
             FROM parking_spot_images
             WHERE spot_id = ?
             ORDER BY image_order, is_primary DESC",
            [$spotId]
        );

        // 获取可用时间
        $spot['availability'] = db()->query(
            "SELECT day_of_week, start_time, end_time, is_available
             FROM parking_spot_availability
             WHERE spot_id = ?
             ORDER BY day_of_week, start_time",
            [$spotId]
        );

        // 检查是否收藏（如果提供了用户ID）
        if ($userId) {
            $favorite = db()->querySingle(
                "SELECT id FROM favorites WHERE user_id = ? AND spot_id = ?",
                [$userId, $spotId]
            );
            $spot['is_favorite'] = $favorite ? true : false;
        } else {
            $spot['is_favorite'] = false;
        }

        // 获取特殊日期
        $spot['special_dates'] = db()->query(
            "SELECT date, start_time, end_time, status, notes
             FROM parking_spot_special_dates
             WHERE spot_id = ? AND date >= CURDATE()
             ORDER BY date
             LIMIT 10",
            [$spotId]
        );

        return $spot;
    }

    /**
     * 搜索停车位
     */
    public static function searchSpots($filters, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        // 构建查询条件
        $conditions = ["ps.is_active = 1", "ps.is_approved = 1"];
        $params = [];

        // 位置筛选
        if (!empty($filters['latitude']) && !empty($filters['longitude']) && !empty($filters['radius'])) {
            // 简单的地理位置筛选（实际应用中应使用空间索引）
            $conditions[] = "(
                6371 * ACOS(
                    COS(RADIANS(?)) * COS(RADIANS(ps.latitude)) *
                    COS(RADIANS(ps.longitude) - RADIANS(?)) +
                    SIN(RADIANS(?)) * SIN(RADIANS(ps.latitude))
                )
            ) <= ?";
            $params[] = $filters['latitude'];
            $params[] = $filters['longitude'];
            $params[] = $filters['latitude'];
            $params[] = $filters['radius'];
        }

        // 价格筛选
        if (!empty($filters['min_price'])) {
            $conditions[] = "ps.price_per_hour >= ?";
            $params[] = floatval($filters['min_price']);
        }
        if (!empty($filters['max_price'])) {
            $conditions[] = "ps.price_per_hour <= ?";
            $params[] = floatval($filters['max_price']);
        }

        // 时间筛选
        if (!empty($filters['start_time']) && !empty($filters['end_time'])) {
            // 检查车位在指定时间是否可用
            // 这里简化处理，实际需要检查可用时间表和预订冲突
            $dayOfWeek = date('w', strtotime($filters['start_time']));
            $startTime = date('H:i:s', strtotime($filters['start_time']));
            $endTime = date('H:i:s', strtotime($filters['end_time']));

            $conditions[] = "EXISTS (
                SELECT 1 FROM parking_spot_availability pa
                WHERE pa.spot_id = ps.id
                AND pa.day_of_week = ?
                AND pa.is_available = 1
                AND pa.start_time <= ?
                AND pa.end_time >= ?
            )";
            $params[] = $dayOfWeek;
            $params[] = $startTime;
            $params[] = $endTime;
        }

        // 设施筛选
        $facilityConditions = [
            'is_covered' => 'has_charging',
            'has_lighting' => 'has_lighting',
            'has_security' => 'has_security',
            'has_charging' => 'has_charging',
            'has_cctv' => 'has_cctv',
            'is_24h_access' => 'is_24h_access'
        ];

        foreach ($facilityConditions as $filterKey => $dbField) {
            if (!empty($filters[$filterKey]) && $filters[$filterKey] == '1') {
                $conditions[] = "ps.{$dbField} = 1";
            }
        }

        // 车辆尺寸筛选
        if (!empty($filters['max_height'])) {
            $conditions[] = "(ps.max_vehicle_height IS NULL OR ps.max_vehicle_height >= ?)";
            $params[] = floatval($filters['max_height']);
        }
        if (!empty($filters['max_width'])) {
            $conditions[] = "(ps.max_vehicle_width IS NULL OR ps.max_vehicle_width >= ?)";
            $params[] = floatval($filters['max_width']);
        }

        // 关键词搜索
        if (!empty($filters['keyword'])) {
            $conditions[] = "(ps.title LIKE ? OR ps.description LIKE ? OR ps.address LIKE ?)";
            $keyword = "%{$filters['keyword']}%";
            $params[] = $keyword;
            $params[] = $keyword;
            $params[] = $keyword;
        }

        // 排序
        $orderBy = "ps.created_at DESC";
        if (!empty($filters['sort_by'])) {
            switch ($filters['sort_by']) {
                case 'price_asc':
                    $orderBy = "ps.price_per_hour ASC";
                    break;
                case 'price_desc':
                    $orderBy = "ps.price_per_hour DESC";
                    break;
                case 'rating':
                    $orderBy = "avg_rating DESC";
                    break;
                case 'distance':
                    if (!empty($filters['latitude']) && !empty($filters['longitude'])) {
                        // 按距离排序需要复杂查询，这里简化
                        $orderBy = "ps.created_at DESC";
                    }
                    break;
            }
        }

        // 构建查询
        $whereClause = count($conditions) > 0 ? "WHERE " . implode(" AND ", $conditions) : "";
        $offset = ($page - 1) * $limit;

        // 获取数据
        $query = "
            SELECT ps.*,
                   u.username as owner_username,
                   u.avatar_url as owner_avatar,
                   (SELECT AVG(rating) FROM reviews WHERE spot_id = ps.id) as avg_rating,
                   (SELECT COUNT(*) FROM reviews WHERE spot_id = ps.id) as review_count
            FROM parking_spots ps
            LEFT JOIN users u ON ps.owner_id = u.id
            {$whereClause}
            ORDER BY {$orderBy}
            LIMIT ? OFFSET ?
        ";

        $params[] = $limit;
        $params[] = $offset;

        $spots = db()->query($query, $params);

        // 获取总数
        $countQuery = "
            SELECT COUNT(*) as total
            FROM parking_spots ps
            {$whereClause}
        ";
        $totalResult = db()->querySingle($countQuery, array_slice($params, 0, -2));
        $total = $totalResult['total'];

        // 获取每个车位的首张图片
        foreach ($spots as &$spot) {
            $image = db()->querySingle(
                "SELECT image_url FROM parking_spot_images
                 WHERE spot_id = ? AND is_primary = 1
                 LIMIT 1",
                [$spot['id']]
            );
            $spot['primary_image'] = $image ? $image['image_url'] : null;
        }

        return [
            'spots' => $spots,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $total,
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 获取用户的车位列表
     */
    public static function getUserSpots($userId, $page = 1, $limit = DEFAULT_PAGE_SIZE) {
        $offset = ($page - 1) * $limit;

        $spots = db()->query(
            "SELECT ps.*,
                    (SELECT COUNT(*) FROM bookings WHERE spot_id = ps.id) as total_bookings,
                    (SELECT SUM(total_price) FROM bookings WHERE spot_id = ps.id AND status = 'completed') as total_income
             FROM parking_spots ps
             WHERE ps.owner_id = ? AND ps.is_active = 1
             ORDER BY ps.created_at DESC
             LIMIT ? OFFSET ?",
            [$userId, $limit, $offset]
        );

        $total = db()->querySingle(
            "SELECT COUNT(*) as count FROM parking_spots WHERE owner_id = ? AND is_active = 1",
            [$userId]
        )['count'];

        // 获取每个车位的首张图片
        foreach ($spots as &$spot) {
            $image = db()->querySingle(
                "SELECT image_url FROM parking_spot_images
                 WHERE spot_id = ? AND is_primary = 1
                 LIMIT 1",
                [$spot['id']]
            );
            $spot['primary_image'] = $image ? $image['image_url'] : null;
        }

        return [
            'spots' => $spots,
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $total,
                'pages' => ceil($total / $limit)
            ]
        ];
    }

    /**
     * 更新可用时间
     */
    private static function updateAvailability($spotId, $availability) {
        // 删除旧的可用时间
        db()->execute("DELETE FROM parking_spot_availability WHERE spot_id = ?", [$spotId]);

        // 插入新的可用时间
        foreach ($availability as $slot) {
            if (!isset($slot['day_of_week']) || !isset($slot['start_time']) || !isset($slot['end_time'])) {
                continue;
            }

            $dayOfWeek = intval($slot['day_of_week']);
            $startTime = $slot['start_time'];
            $endTime = $slot['end_time'];
            $isAvailable = isset($slot['is_available']) ? intval($slot['is_available']) : 1;

            db()->execute(
                "INSERT INTO parking_spot_availability (spot_id, day_of_week, start_time, end_time, is_available)
                 VALUES (?, ?, ?, ?, ?)",
                [$spotId, $dayOfWeek, $startTime, $endTime, $isAvailable]
            );
        }
    }

    /**
     * 添加图片
     */
    private static function addImages($spotId, $images) {
        foreach ($images as $index => $imageUrl) {
            $isPrimary = ($index === 0) ? 1 : 0; // 第一张图片设为主图

            db()->execute(
                "INSERT INTO parking_spot_images (spot_id, image_url, image_order, is_primary)
                 VALUES (?, ?, ?, ?)",
                [$spotId, $imageUrl, $index, $isPrimary]
            );
        }
    }

    /**
     * 更新图片
     */
    private static function updateImages($spotId, $images) {
        // 删除旧的图片
        db()->execute("DELETE FROM parking_spot_images WHERE spot_id = ?", [$spotId]);

        // 添加新的图片
        self::addImages($spotId, $images);
    }

    /**
     * 检查车位在指定时间是否可用
     */
    public static function checkAvailability($spotId, $startTime, $endTime) {
        // 检查可用时间表
        $dayOfWeek = date('w', strtotime($startTime));
        $startTimeStr = date('H:i:s', strtotime($startTime));
        $endTimeStr = date('H:i:s', strtotime($endTime));

        $availability = db()->querySingle(
            "SELECT * FROM parking_spot_availability
             WHERE spot_id = ? AND day_of_week = ?
             AND start_time <= ? AND end_time >= ?
             AND is_available = 1",
            [$spotId, $dayOfWeek, $startTimeStr, $endTimeStr]
        );

        if (!$availability) {
            return false;
        }

        // 检查特殊日期
        $date = date('Y-m-d', strtotime($startTime));
        $specialDate = db()->querySingle(
            "SELECT * FROM parking_spot_special_dates
             WHERE spot_id = ? AND date = ?",
            [$spotId, $date]
        );

        if ($specialDate && $specialDate['status'] !== 'available') {
            return false;
        }

        // 检查是否有冲突的预订
        $conflictingBooking = db()->querySingle(
            "SELECT id FROM bookings
             WHERE spot_id = ? AND status NOT IN ('cancelled', 'completed', 'expired')
             AND (
                 (start_time <= ? AND end_time >= ?) OR
                 (start_time >= ? AND start_time < ?) OR
                 (end_time > ? AND end_time <= ?)
             )",
            [$spotId, $startTime, $endTime, $startTime, $endTime, $startTime, $endTime]
        );

        if ($conflictingBooking) {
            return false;
        }

        return true;
    }

    /**
     * 获取车位的预订时间段
     */
    public static function getSpotBookings($spotId, $startDate, $endDate) {
        return db()->query(
            "SELECT id, start_time, end_time, status, user_id
             FROM bookings
             WHERE spot_id = ? AND start_time >= ? AND end_time <= ?
             ORDER BY start_time",
            [$spotId, $startDate, $endDate]
        );
    }

    /**
     * 增加查看次数
     */
    public static function incrementViewCount($spotId) {
        db()->execute(
            "UPDATE parking_spots SET view_count = view_count + 1 WHERE id = ?",
            [$spotId]
        );
    }
}