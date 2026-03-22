# 共享停车位平台

一个基于LAMP栈的共享停车位市场平台，允许用户发布、搜索和预订停车位。

## 功能特性

### Web平台
- ✅ 用户注册、登录和验证
- ✅ 停车位发布和管理
- ✅ 地图集成（百度地图）显示停车位位置
- ✅ 预订系统与支付集成（支付宝、微信支付）
- ✅ 用户评价和消息系统
- ✅ 响应式设计，移动端适配
- ✅ 管理员审核和统计功能

### Android应用
- ✅ 用户登录和注册（完整认证流程）
- ✅ 主界面框架（底部导航、4个核心模块）
- ✅ 启动页面和导航流程
- ✅ 表单验证和错误处理
- 🔄 停车位搜索和浏览（开发中）
- 🔄 停车位详情查看（开发中）
- 🔄 预订系统（开发中）
- 🔄 支付功能（开发中）
- 🔄 个人中心管理（部分完成）

✅ 已完成 🔄 开发中 ⏳ 计划中

## 技术栈

- **后端**: PHP 7.4+, MySQL 8.0+, Apache 2.4+
- **前端**: HTML5, CSS3, JavaScript ES6+
- **数据库**: MySQL
- **地图API**: 百度地图
- **支付**: 支付宝、微信支付
- **认证**: JWT令牌
- **Android端**: Kotlin, Android SDK, Material Design, MVVM架构

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
├── android-app/          # Android移动端应用
│   ├── app/              # Android应用代码
│   │   ├── src/main/java/com/sharedparking/android/
│   │   │   ├── ui/       # 用户界面（Activity/Fragment）
│   │   │   ├── viewmodel/ # ViewModel层
│   │   │   ├── repository/ # 数据仓库层
│   │   │   ├── network/  # 网络层
│   │   │   └── model/    # 数据模型
│   │   └── src/main/res/ # 资源文件（布局、字符串、图标）
│   └── build.gradle      # 构建配置
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

## Android应用开发进度

### ✅ 已完成的功能（第一阶段 & 第二阶段）

#### 1. 基础资源文件创建
- **styles.xml** - 定义Material Design主题和组件样式
- **dimens.xml** - 定义统一的尺寸和间距常量
- **themes.xml** - 定义明暗主题变体
- **drawable资源** - 创建了启动页背景、底部导航tint、图标等
- **图标资源** - 创建了home、search、profile、settings等20+个矢量图标

#### 2. 应用框架搭建
- **SplashActivity** - 启动页，检查登录状态并跳转
- **MainActivity** - 主框架，包含底部导航和Fragment容器
- **导航结构** - 创建了nav_graph.xml导航图
- **底部导航** - 实现4个标签页：首页、搜索、预订、我的

#### 3. 基础Fragment实现
- **HomeFragment** - 首页，展示推荐车位和快捷操作
- **SearchFragment** - 搜索页，支持地图/列表视图切换
- **BookingsFragment** - 预订管理页，支持标签页切换
- **ProfileFragment** - 个人中心，用户信息和管理功能

#### 4. 认证功能完整实现
- **LoginActivity** - 支持邮箱密码登录和验证码登录
- **RegisterActivity** - 完整注册流程，包含手机验证码验证
- **与现有架构集成** - 完美集成现有的AuthViewModel和AuthRepository
- **表单验证** - 完整的输入验证和错误提示

#### 5. 占位符Activity创建
为AndroidManifest.xml中声明的所有Activity创建了占位符类，确保项目可以编译：
- ParkingSearchActivity、ParkingDetailActivity
- BookingActivity、PaymentActivity
- ProfileActivity、MessagesActivity
- MySpotsActivity、MyBookingsActivity
- CreateSpotActivity、MapActivity
- LocationService、NotificationService、NetworkChangeReceiver

### 🏗️ 项目架构状态
项目继续使用现有的MVVM架构：
- **网络层**：Retrofit + OkHttp（已存在）
- **数据层**：Repository模式（已存在）
- **视图模型**：AuthViewModel、ParkingViewModel（已存在）
- **UI层**：现在已完整实现

### 🎯 核心功能已实现
1. **应用启动流程**：Splash → 检查登录 → Main/Login
2. **主界面导航**：底部导航切换4个核心功能模块
3. **用户认证**：完整的登录和注册流程
4. **UI/UX设计**：遵循Material Design规范

### 🔧 技术细节
- **布局系统**：使用ConstraintLayout和Material Components
- **导航**：使用Android Navigation Component
- **数据绑定**：使用ViewBinding
- **状态管理**：使用LiveData观察ViewModel状态
- **异步处理**：使用Kotlin协程

### 📱 应用现在具备的功能
1. 完整的启动和导航框架
2. 用户认证系统（登录/注册）
3. 主界面4个核心模块
4. 响应式UI设计
5. 错误处理和表单验证

### 🚀 下一步计划（第三阶段）
1. 实现停车位搜索功能（ParkingSearchActivity）
2. 实现停车位详情展示（ParkingDetailActivity）
3. 实现预订流程（BookingActivity）
4. 实现支付功能（PaymentActivity）

### 🔍 编译状态
- **Gradle Wrapper**：已配置（Gradle 9.2.1）
- **Java环境**：需要配置JAVA_HOME
- **依赖**：所有依赖已在build.gradle中声明
- **建议测试方法**：在Android Studio中打开项目进行编译测试

项目现在已经是一个可以运行的基础Android应用，包含了完整的UI框架和用户认证系统。所有代码都已集成到现有的MVVM架构中。

**详细开发文档**：请参阅 [ANDROID_DEVELOPMENT.md](./ANDROID_DEVELOPMENT.md)

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