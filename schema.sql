-- 共享停车位平台数据库结构
-- 版本: 1.0.0
-- 创建时间: 2026-03-07

-- 使用utf8mb4字符集以支持完整Unicode
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
-- 注意：安装脚本会创建数据库，这里只是SQL结构

-- 用户表
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱地址',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` enum('user','admin') DEFAULT 'user' COMMENT '用户角色',
  `is_verified` tinyint(1) DEFAULT 0 COMMENT '是否已验证',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否激活',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `failed_login_attempts` int(11) DEFAULT 0 COMMENT '登录失败次数',
  `locked_until` datetime DEFAULT NULL COMMENT '锁定直到时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户资料表（扩展信息）
CREATE TABLE `user_profiles` (
  `user_id` int(11) NOT NULL,
  `gender` enum('male','female','other') DEFAULT NULL COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `id_card_number` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `driver_license_number` varchar(50) DEFAULT NULL COMMENT '驾驶证号',
  `vehicle_plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号码',
  `vehicle_brand` varchar(50) DEFAULT NULL COMMENT '车辆品牌',
  `vehicle_model` varchar(50) DEFAULT NULL COMMENT '车辆型号',
  `vehicle_color` varchar(20) DEFAULT NULL COMMENT '车辆颜色',
  `vehicle_height` decimal(5,2) DEFAULT NULL COMMENT '车辆高度(米)',
  `vehicle_width` decimal(5,2) DEFAULT NULL COMMENT '车辆宽度(米)',
  `wechat_openid` varchar(100) DEFAULT NULL COMMENT '微信OpenID',
  `alipay_user_id` varchar(100) DEFAULT NULL COMMENT '支付宝用户ID',
  `emergency_contact` varchar(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系电话',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_id_card` (`id_card_number`),
  UNIQUE KEY `uk_driver_license` (`driver_license_number`),
  CONSTRAINT `fk_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料表';

-- 停车位表
CREATE TABLE `parking_spots` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner_id` int(11) NOT NULL COMMENT '车位所有者ID',
  `title` varchar(100) NOT NULL COMMENT '车位标题',
  `description` text DEFAULT NULL COMMENT '车位描述',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `latitude` decimal(10,8) NOT NULL COMMENT '纬度',
  `longitude` decimal(11,8) NOT NULL COMMENT '经度',
  `price_per_hour` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '每小时价格',
  `price_per_day` decimal(10,2) DEFAULT NULL COMMENT '每天价格',
  `price_unit` enum('hour','day') DEFAULT 'hour' COMMENT '价格单位',
  `max_vehicle_height` decimal(5,2) DEFAULT NULL COMMENT '最大车辆高度(米)',
  `max_vehicle_width` decimal(5,2) DEFAULT NULL COMMENT '最大车辆宽度(米)',
  `available_spots` int(11) DEFAULT 1 COMMENT '可用车位数量',
  `total_spots` int(11) DEFAULT 1 COMMENT '总车位数量',
  `is_covered` tinyint(1) DEFAULT 0 COMMENT '是否有顶棚',
  `has_lighting` tinyint(1) DEFAULT 0 COMMENT '是否有照明',
  `has_security` tinyint(1) DEFAULT 0 COMMENT '是否有安保',
  `has_charging` tinyint(1) DEFAULT 0 COMMENT '是否有充电桩',
  `has_cctv` tinyint(1) DEFAULT 0 COMMENT '是否有监控',
  `is_24h_access` tinyint(1) DEFAULT 0 COMMENT '是否24小时可进出',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否激活',
  `is_approved` tinyint(1) DEFAULT 0 COMMENT '是否已审核通过',
  `approval_notes` text DEFAULT NULL COMMENT '审核备注',
  `view_count` int(11) DEFAULT 0 COMMENT '查看次数',
  `book_count` int(11) DEFAULT 0 COMMENT '预订次数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_location` (`latitude`,`longitude`),
  KEY `idx_price` (`price_per_hour`),
  KEY `idx_status` (`is_active`,`is_approved`),
  KEY `idx_created_at` (`created_at`),
  FULLTEXT KEY `ft_search` (`title`,`description`,`address`),
  CONSTRAINT `fk_parking_spots_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车位表';

-- 停车位图片表
CREATE TABLE `parking_spot_images` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `image_url` varchar(255) NOT NULL COMMENT '图片URL',
  `image_order` int(11) DEFAULT 0 COMMENT '图片顺序',
  `is_primary` tinyint(1) DEFAULT 0 COMMENT '是否为主图',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_spot_id` (`spot_id`),
  KEY `idx_order` (`spot_id`,`image_order`),
  CONSTRAINT `fk_spot_images_spot` FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车位图片表';

-- 停车位可用时间表
CREATE TABLE `parking_spot_availability` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `day_of_week` tinyint(1) NOT NULL COMMENT '星期几（0=周日,1=周一,...,6=周六）',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `is_available` tinyint(1) DEFAULT 1 COMMENT '是否可用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spot_day_time` (`spot_id`,`day_of_week`,`start_time`,`end_time`),
  CONSTRAINT `fk_availability_spot` FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车位可用时间表';

-- 停车位特殊日期表（如节假日、维护日）
CREATE TABLE `parking_spot_special_dates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `date` date NOT NULL COMMENT '特殊日期',
  `start_time` time DEFAULT '00:00:00' COMMENT '开始时间',
  `end_time` time DEFAULT '23:59:59' COMMENT '结束时间',
  `status` enum('available','unavailable','maintenance') DEFAULT 'unavailable' COMMENT '状态',
  `notes` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spot_date` (`spot_id`,`date`),
  CONSTRAINT `fk_special_dates_spot` FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车位特殊日期表';

-- 预订表
CREATE TABLE `bookings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `vehicle_plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号码',
  `start_time` datetime NOT NULL COMMENT '预订开始时间',
  `end_time` datetime NOT NULL COMMENT '预订结束时间',
  `duration_hours` decimal(5,2) NOT NULL COMMENT '持续时间（小时）',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价格',
  `status` enum('pending','confirmed','in_progress','completed','cancelled','expired') DEFAULT 'pending' COMMENT '预订状态',
  `cancelled_by` enum('user','owner','system') DEFAULT NULL COMMENT '取消方',
  `cancellation_reason` text DEFAULT NULL COMMENT '取消原因',
  `cancelled_at` datetime DEFAULT NULL COMMENT '取消时间',
  `check_in_code` varchar(20) DEFAULT NULL COMMENT '入场验证码',
  `check_in_at` datetime DEFAULT NULL COMMENT '入场时间',
  `check_out_at` datetime DEFAULT NULL COMMENT '出场时间',
  `notes` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_spot_id` (`spot_id`),
  KEY `idx_time_range` (`start_time`,`end_time`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_bookings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_bookings_spot` FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预订表';

-- 支付表
CREATE TABLE `payments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `booking_id` int(11) NOT NULL COMMENT '预订ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `payment_method` enum('alipay','wechat','credit_card','wallet') DEFAULT NULL COMMENT '支付方式',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '交易ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `currency` varchar(3) DEFAULT 'CNY' COMMENT '货币',
  `status` enum('pending','paid','refunded','failed') DEFAULT 'pending' COMMENT '支付状态',
  `refund_amount` decimal(10,2) DEFAULT 0 COMMENT '退款金额',
  `refund_reason` text DEFAULT NULL COMMENT '退款原因',
  `refunded_at` datetime DEFAULT NULL COMMENT '退款时间',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `payment_details` json DEFAULT NULL COMMENT '支付详情',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_booking_id` (`booking_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  CONSTRAINT `fk_payments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付表';

-- 评价表
CREATE TABLE `reviews` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `booking_id` int(11) NOT NULL COMMENT '预订ID',
  `user_id` int(11) NOT NULL COMMENT '评价用户ID',
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `rating` tinyint(1) NOT NULL COMMENT '评分（1-5星）',
  `title` varchar(100) DEFAULT NULL COMMENT '评价标题',
  `content` text DEFAULT NULL COMMENT '评价内容',
  `owner_reply` text DEFAULT NULL COMMENT '车位主回复',
  `is_verified` tinyint(1) DEFAULT 0 COMMENT '是否已验证（实际使用过）',
  `is_visible` tinyint(1) DEFAULT 1 COMMENT '是否可见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_review` (`booking_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_spot_id` (`spot_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_reviews_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_reviews_spot` FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 消息表
CREATE TABLE `messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL COMMENT '发送者ID',
  `receiver_id` int(11) NOT NULL COMMENT '接收者ID',
  `booking_id` int(11) DEFAULT NULL COMMENT '关联的预订ID',
  `subject` varchar(100) DEFAULT NULL COMMENT '消息主题',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender_id`,`created_at`),
  KEY `idx_receiver` (`receiver_id`,`created_at`,`is_read`),
  KEY `idx_booking` (`booking_id`),
  CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_messages_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 收藏表
CREATE TABLE `favorites` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spot` (`user_id`,`spot_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_spot_id` (`spot_id`),
  CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorites_spot` FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 系统配置表
CREATE TABLE `system_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(100) NOT NULL COMMENT '配置键',
  `value` text DEFAULT NULL COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置描述',
  `is_public` tinyint(1) DEFAULT 0 COMMENT '是否公开',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 验证码表（用于短信/邮箱验证）
CREATE TABLE `verification_codes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱地址',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `code` varchar(20) NOT NULL COMMENT '验证码',
  `type` enum('register','login','reset_password','change_phone','change_email') NOT NULL COMMENT '验证码类型',
  `is_used` tinyint(1) DEFAULT 0 COMMENT '是否已使用',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_email_code` (`email`,`code`,`type`),
  KEY `idx_phone_code` (`phone`,`code`,`type`),
  KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- 操作日志表
CREATE TABLE `audit_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL COMMENT '操作用户ID',
  `action` varchar(50) NOT NULL COMMENT '操作类型',
  `table_name` varchar(50) DEFAULT NULL COMMENT '操作表名',
  `record_id` int(11) DEFAULT NULL COMMENT '记录ID',
  `old_values` json DEFAULT NULL COMMENT '旧值',
  `new_values` json DEFAULT NULL COMMENT '新值',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text DEFAULT NULL COMMENT '用户代理',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action` (`action`),
  KEY `idx_table_record` (`table_name`,`record_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入默认数据
-- 1. 插入默认管理员用户（密码: admin123）
INSERT INTO `users` (`id`, `username`, `email`, `phone`, `password_hash`, `real_name`, `role`, `is_verified`, `is_active`) VALUES
(1, 'admin', 'admin@shared-parking.com', '13800138000', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '系统管理员', 'admin', 1, 1);

-- 2. 插入默认测试用户（密码: user123）
INSERT INTO `users` (`id`, `username`, `email`, `phone`, `password_hash`, `real_name`, `role`, `is_verified`, `is_active`) VALUES
(2, 'testuser', 'user@shared-parking.com', '13800138001', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '测试用户', 'user', 1, 1);

-- 3. 插入默认系统配置
INSERT INTO `system_settings` (`key`, `value`, `description`, `is_public`) VALUES
('platform_name', '共享停车位平台', '平台名称', 1),
('platform_description', '连接车位主与车主的共享停车平台', '平台描述', 1),
('contact_email', 'support@shared-parking.com', '客服邮箱', 1),
('contact_phone', '400-123-4567', '客服电话', 1),
('min_booking_hours', '1', '最小预订小时数', 1),
('max_booking_days', '30', '最大预订天数', 1),
('commission_rate', '10', '平台佣金比例（%）', 0),
('refund_policy_days', '2', '可退款天数', 1),
('auto_cancel_minutes', '30', '未支付自动取消时间（分钟）', 0),
('search_radius_km', '5', '默认搜索半径（公里）', 1);

-- 重置外键检查
SET FOREIGN_KEY_CHECKS = 1;