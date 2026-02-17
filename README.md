# 共享停车位平台

一个基于LAMP栈的共享停车位市场平台，允许用户发布、搜索和预订停车位。

## 功能特性

- ✅ 用户注册、登录和验证
- ✅ 停车位发布和管理
- ✅ 地图集成（百度地图）显示停车位位置
- ✅ 预订系统与支付集成（支付宝、微信支付）
- ✅ 用户评价和消息系统
- ✅ 响应式设计，移动端适配
- ✅ 管理员审核和统计功能

## 技术栈

- **后端**: PHP 7.4+, MySQL 8.0+, Apache 2.4+
- **前端**: HTML5, CSS3, JavaScript ES6+
- **数据库**: MySQL
- **地图API**: 百度地图
- **支付**: 支付宝、微信支付
- **认证**: JWT令牌

## 项目结构

```
shared-parking/
├── api/                    # RESTful API后端
│   ├── config/            # 配置文件
│   ├── controllers/       # 控制器
│   ├── models/           # 数据模型
│   ├── middleware/       # 中间件
│   └── utils/            # 工具类
├── web/                   # 前端网站
│   ├── assets/           # 静态资源（CSS, JS, 图片）
│   ├── views/            # 页面视图
│   └── index.php         # 前端入口
├── mobile/               # 移动端应用（预留）
├── docs/                 # 文档
├── tests/               # 测试
└── 配置文件
```

## 安装和设置

### 1. 环境要求

- PHP 7.4 或更高版本
- MySQL 8.0 或更高版本
- Apache 2.4 或更高版本（支持mod_rewrite）
- Composer（PHP依赖管理）

### 2. 克隆项目

```bash
git clone <repository-url>
cd shared-parking
```

### 3. 安装PHP依赖

```bash
composer install
```

### 4. 数据库设置

1. 创建MySQL数据库：
```sql
CREATE DATABASE shared_parking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据库结构（SQL文件将在后续提供）

### 5. 环境配置

1. 复制环境变量模板：
```bash
cp .env.example .env
```

2. 编辑 `.env` 文件，设置您的配置：
   - 数据库连接信息
   - JWT密钥
   - 地图API密钥
   - 支付网关配置

### 6. Apache配置

1. 确保启用了 `mod_rewrite` 模块
2. 将项目目录设置为Apache文档根目录，或配置虚拟主机
3. 确保 `.htaccess` 文件被允许

### 7. 文件权限

设置必要的文件权限：
```bash
chmod -R 755 storage/
chmod -R 755 uploads/
```

## 开发

### 运行开发服务器

使用PHP内置服务器（仅用于开发）：
```bash
php -S localhost:8080 -t web
```

### 数据库迁移

数据库迁移脚本将在后续提供。

### 测试

运行单元测试：
```bash
composer test
```

## API文档

API端点和详细文档将在后续提供。

## 部署

### 生产环境注意事项

1. 将 `APP_ENV` 设置为 `production`
2. 将 `APP_DEBUG` 设置为 `false`
3. 配置SSL证书
4. 设置定期数据库备份
5. 配置监控和日志

### 性能优化建议

- 启用OPCache
- 使用CDN分发静态资源
- 数据库查询优化
- 启用HTTP/2

## 许可证

MIT

## 支持

如有问题或建议，请提交Issue或联系开发团队。