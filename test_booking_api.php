<?php
/**
 * 预订API测试脚本
 */

require_once 'api/config/constants.php';
require_once 'api/config/database.php';
require_once 'api/lib/auth.php';

// 测试数据
$testData = [
    'spot_id' => 1,
    'vehicle_plate_number' => '京A12345',
    'vehicle_brand' => '特斯拉',
    'vehicle_model' => 'Model 3',
    'vehicle_color' => '红色',
    'start_time' => '2026-03-23T10:00:00Z',
    'end_time' => '2026-03-23T14:00:00Z',
    'notes' => '测试预订'
];

echo "=== 预订API测试 ===\n\n";

// 测试1: 创建预订（需要先模拟登录）
echo "测试1: 创建预订\n";
echo "请求数据: " . json_encode($testData, JSON_UNESCAPED_UNICODE) . "\n";

// 模拟用户登录（这里需要实际的JWT token）
// 在实际测试中，需要先调用登录API获取token

echo "\n注意: 实际测试需要:\n";
echo "1. 启动API服务器 (php -S localhost:8080 -t api)\n";
echo "2. 使用有效的JWT token\n";
echo "3. 使用真实的停车位ID\n\n";

// 测试2: 检查可用性
echo "测试2: 检查停车位可用性\n";
$availabilityUrl = "http://localhost:8080/api/parking/availability/1?start_time=2026-03-23T10:00:00Z&end_time=2026-03-23T14:00:00Z";
echo "请求URL: $availabilityUrl\n";
echo "预期响应: {\"available\": true/false, \"message\": \"...\"}\n\n";

// 测试3: 获取我的预订
echo "测试3: 获取我的预订列表\n";
$bookingsUrl = "http://localhost:8080/api/bookings";
echo "请求URL: $bookingsUrl\n";
echo "需要Authorization头: Bearer <token>\n\n";

// 测试4: 取消预订
echo "测试4: 取消预订\n";
echo "请求URL: PUT http://localhost:8080/api/bookings/1/cancel\n";
echo "需要预订ID和有效的token\n\n";

echo "=== 测试步骤 ===\n";
echo "1. 确保数据库已初始化 (运行schema.sql)\n";
echo "2. 启动API服务器: php -S localhost:8080 -t api\n";
echo "3. 使用Postman或curl测试API端点\n";
echo "4. 验证响应格式和业务逻辑\n";

// 简单的curl测试示例
echo "\n=== curl测试示例 ===\n";
echo "# 检查可用性\n";
echo "curl -X GET \"$availabilityUrl\"\n\n";

echo "# 创建预订 (需要token)\n";
echo "curl -X POST http://localhost:8080/api/bookings \\\n";
echo "  -H \"Content-Type: application/json\" \\\n";
echo "  -H \"Authorization: Bearer YOUR_TOKEN\" \\\n";
echo "  -d '" . json_encode($testData) . "'\n";

?>