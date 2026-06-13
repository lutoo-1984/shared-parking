# 预订功能实现文档

## 概述
预订功能是共享停车位平台的核心功能之一，允许用户预订停车位并管理预订记录。

## 功能特性

### 1. 创建预订
- 选择开始时间和结束时间
- 输入车辆信息（车牌号、品牌、型号、颜色）
- 实时价格计算
- 时间冲突检查
- 表单验证

### 2. 预订管理
- 查看我的预订列表
- 查看预订详情
- 取消预订
- 查看预订状态

### 3. 状态管理
- `pending`: 待确认（创建后状态）
- `confirmed`: 已确认（支付后状态）
- `in_progress`: 进行中（开始时间到达）
- `completed`: 已完成（结束时间到达）
- `cancelled`: 已取消
- `expired`: 已过期

## 技术架构

### Android端
```
├── model/
│   ├── Booking.kt              # 预订数据模型
│   ├── BookingRequest.kt       # 创建预订请求
│   ├── BookingStatus.kt        # 预订状态枚举
│   ├── BookingFormState.kt     # 表单状态
│   ├── VehicleInfo.kt          # 车辆信息
│   ├── BookingListResponse.kt  # 预订列表响应
│   └── AvailabilityResponse.kt # 可用性响应
├── repository/
│   └── BookingRepository.kt    # 预订数据仓库
├── viewmodel/
│   └── BookingViewModel.kt     # 预订ViewModel
├── ui/booking/
│   └── BookingActivity.kt      # 预订页面
└── utils/
    └── Resource.kt             # 资源状态封装
```

### 后端API
```
├── index.php
│   └── handleBookings()        # 预订API入口
│       ├── createBooking()     # 创建预订
│       ├── getBookingDetail()  # 获取详情
│       ├── getMyBookings()     # 获取列表
│       └── cancelBooking()     # 取消预订
└── lib/
    └── parking.php
        └── checkAvailability() # 检查可用性
```

## API接口

### 1. 创建预订
```
POST /api/bookings
Content-Type: application/json
Authorization: Bearer <token>

请求体:
{
  "spot_id": 1,
  "vehicle_plate_number": "京A12345",
  "vehicle_brand": "特斯拉",
  "vehicle_model": "Model 3",
  "vehicle_color": "红色",
  "start_time": "2026-03-23T10:00:00Z",
  "end_time": "2026-03-23T14:00:00Z",
  "notes": "备注信息"
}

响应:
{
  "success": true,
  "message": "预订创建成功",
  "data": {
    "id": 1,
    "user_id": 1,
    "spot_id": 1,
    "vehicle_plate_number": "京A12345",
    "vehicle_brand": "特斯拉",
    "vehicle_model": "Model 3",
    "vehicle_color": "红色",
    "start_time": "2026-03-23 10:00:00",
    "end_time": "2026-03-23 14:00:00",
    "duration_hours": 4.0,
    "total_price": 60.0,
    "status": "pending",
    "check_in_code": "ABC123",
    "notes": "备注信息",
    "spot_title": "小区地下停车场",
    "spot_address": "北京市朝阳区...",
    "price_per_hour": 15.0,
    "owner_username": "车主用户名"
  }
}
```

### 2. 获取预订详情
```
GET /api/bookings/{id}
Authorization: Bearer <token>
```

### 3. 获取我的预订列表
```
GET /api/bookings?page=1&limit=20
Authorization: Bearer <token>
```

### 4. 取消预订
```
PUT /api/bookings/{id}/cancel
Authorization: Bearer <token>

请求体:
{
  "reason": "行程变更"
}
```

### 5. 检查可用性
```
GET /api/parking/availability/{spot_id}?start_time=2026-03-23T10:00:00Z&end_time=2026-03-23T14:00:00Z

响应:
{
  "success": true,
  "data": {
    "available": true,
    "spot_id": 1,
    "start_time": "2026-03-23T10:00:00Z",
    "end_time": "2026-03-23T14:00:00Z",
    "conflicting_bookings": [],
    "message": "车位可用"
  }
}
```

## 业务规则

### 1. 时间验证
- 开始时间不能早于当前时间
- 结束时间必须晚于开始时间
- 最小预订时长：1小时
- 最大预订时长：30天

### 2. 价格计算
```
总价 = 时长(小时) × 单价(元/小时)
时长 = (结束时间 - 开始时间) / (1000 × 60 × 60)
```

### 3. 时间冲突检查
检查同一停车位在选定时间段内是否有以下状态的预订：
- `pending` (待确认)
- `confirmed` (已确认)
- `in_progress` (进行中)

### 4. 车牌号验证
使用正则表达式验证中国车牌号格式：
```
^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,5}[A-Z0-9挂学警港澳]$
```

## 数据库表结构

### bookings表
```sql
CREATE TABLE `bookings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `spot_id` int(11) NOT NULL COMMENT '停车位ID',
  `vehicle_plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号码',
  `vehicle_brand` varchar(50) DEFAULT NULL COMMENT '车辆品牌',
  `vehicle_model` varchar(50) DEFAULT NULL COMMENT '车辆型号',
  `vehicle_color` varchar(20) DEFAULT NULL COMMENT '车辆颜色',
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
```

## 使用流程

### 用户端流程
1. 在停车位详情页点击"立即预订"按钮
2. 跳转到BookingActivity，显示停车位信息
3. 选择开始时间和结束时间
4. 输入车辆信息
5. 系统实时计算价格和检查可用性
6. 点击"确认预订并支付"
7. 验证表单数据
8. 调用API创建预订
9. 创建成功后跳转到支付页面

### 车主端流程
1. 在"我的车位"页面查看预订请求
2. 确认或拒绝预订请求
3. 管理进行中的预订
4. 查看历史预订记录

## 测试用例

### 1. 成功预订
- 所有字段填写正确
- 时间选择合理
- 价格计算正确
- API调用成功

### 2. 表单验证失败
- 必填字段为空
- 车牌号格式错误
- 开始时间早于当前时间
- 结束时间早于开始时间

### 3. 业务逻辑错误
- 时间冲突（车位已被预订）
- 车位不可用（非激活状态）
- 网络连接失败
- 服务器错误响应

### 4. 边缘情况
- 最小预订时长（1小时）
- 最大预订时长（30天）
- 跨午夜预订
- 节假日预订

## 错误处理

### 客户端错误
- 表单验证错误：实时显示错误提示
- 网络错误：显示友好提示，提供重试机制
- 业务错误：根据错误类型提供解决方案

### 服务端错误
- 输入验证：返回详细的错误信息
- 业务验证：返回具体的业务规则错误
- 系统错误：返回通用错误信息，记录日志

## 后续开发计划

### 短期计划
1. 支付功能集成
2. 我的预订页面
3. 预订状态推送通知

### 中期计划
1. 预订评价功能
2. 预订消息沟通
3. 预订统计分析

### 长期计划
1. 智能推荐系统
2. 预订预测算法
3. 动态定价策略

## 注意事项

1. **时间处理**: 所有时间使用ISO 8601格式，时区使用UTC
2. **价格精度**: 价格计算保留2位小数
3. **事务处理**: 创建预订使用数据库事务保证数据一致性
4. **并发控制**: 使用数据库锁防止重复预订
5. **安全考虑**: 验证用户权限，防止越权访问