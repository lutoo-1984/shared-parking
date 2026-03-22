# Android应用开发文档

## 项目概述

共享停车位平台的Android客户端应用，采用MVVM架构，Material Design设计语言，提供完整的停车位搜索、预订、支付和个人管理功能。

## 开发状态

**当前阶段**：已完成第一阶段（基础UI框架）和第二阶段（认证功能）

**总体进度**：40%

## 详细进度记录

### ✅ 第一阶段：基础UI框架搭建（已完成）

#### 1. 资源文件创建
- **styles.xml** - 应用主题和组件样式定义
- **dimens.xml** - 统一的尺寸和间距常量
- **themes.xml** - 明暗主题变体定义
- **drawable资源** - 图标、背景、选择器等视觉资源
- **图标库** - 20+个Material Design风格矢量图标

#### 2. 应用框架实现
- **SplashActivity** - 启动页面，自动检测登录状态
- **MainActivity** - 主容器，包含底部导航和Fragment管理
- **导航系统** - Android Navigation Component集成
- **底部导航** - 4个核心功能标签页

#### 3. 基础Fragment
- **HomeFragment** - 首页，展示推荐内容和快捷操作
- **SearchFragment** - 搜索界面，支持地图/列表视图
- **BookingsFragment** - 预订管理，支持标签页分类
- **ProfileFragment** - 个人中心，用户信息和管理功能

### ✅ 第二阶段：认证功能实现（已完成）

#### 1. 登录功能
- **LoginActivity** - 支持邮箱密码和验证码两种登录方式
- **表单验证** - 实时输入验证和错误提示
- **状态管理** - Loading、Success、Error状态处理
- **自动跳转** - 登录成功后自动跳转到主页面

#### 2. 注册功能
- **RegisterActivity** - 完整的用户注册流程
- **手机验证码** - 发送、验证、倒计时功能
- **表单验证** - 用户名、邮箱、手机号、密码验证
- **服务条款** - 必须同意才能注册

#### 3. 与现有架构集成
- **AuthViewModel** - 复用现有的认证ViewModel
- **AuthRepository** - 复用现有的数据仓库
- **API集成** - 与后端RESTful API无缝对接
- **Token管理** - 自动保存和恢复登录状态

### 🔄 第三阶段：核心功能实现（进行中）

#### 1. 停车位搜索功能
- **ParkingSearchActivity** - 高级搜索和筛选功能
- **地图集成** - 百度地图API集成（待实现）
- **列表展示** - 停车位卡片式列表展示

#### 2. 停车位详情功能
- **ParkingDetailActivity** - 详细信息展示页面
- **图片轮播** - 停车位图片展示
- **设施信息** - 停车位设施标签展示
- **收藏功能** - 添加/移除收藏

#### 3. 预订功能
- **BookingActivity** - 预订流程页面
- **时间选择** - 选择预订时间段
- **价格计算** - 根据时长自动计算价格
- **车辆信息** - 填写车辆信息

#### 4. 支付功能
- **PaymentActivity** - 支付页面
- **支付方式** - 支付宝、微信支付、银行卡
- **支付状态** - 支付成功/失败处理

### ⏳ 第四阶段：辅助功能实现（计划中）

#### 1. 个人中心完善
- **ProfileActivity** - 个人资料编辑
- **MySpotsActivity** - 我的车位管理
- **MyBookingsActivity** - 我的预订历史

#### 2. 消息功能
- **MessagesActivity** - 消息中心
- **实时通信** - 与车位主的消息交流

#### 3. 设置功能
- **SettingsActivity** - 应用设置
- **通知管理** - 推送通知设置
- **关于页面** - 应用信息

## 技术架构

### MVVM架构模式
```
UI层 (Activity/Fragment) → ViewModel层 → Repository层 → API/本地存储
```

### 技术栈
- **开发语言**：Kotlin 100%
- **UI框架**：Material Design Components
- **架构组件**：ViewModel, LiveData, Room（预留）
- **网络库**：Retrofit 2.9.0 + OkHttp 4.12.0
- **图片加载**：Glide 4.16.0
- **导航**：Navigation Component 2.7.5
- **协程**：Kotlin Coroutines 1.7.3
- **依赖注入**：手动依赖注入（预留Hilt迁移）

### 项目结构
```
android-app/
├── app/
│   ├── src/main/java/com/sharedparking/android/
│   │   ├── ui/                    # 用户界面
│   │   │   ├── auth/             # 认证相关
│   │   │   ├── home/             # 首页
│   │   │   ├── search/           # 搜索
│   │   │   ├── bookings/         # 预订
│   │   │   ├── profile/          # 个人中心
│   │   │   ├── parking/          # 停车位相关
│   │   │   ├── payment/          # 支付相关
│   │   │   ├── messages/         # 消息相关
│   │   │   └── map/              # 地图相关
│   │   ├── viewmodel/            # ViewModel层
│   │   ├── repository/           # 数据仓库层
│   │   ├── network/              # 网络层
│   │   ├── model/                # 数据模型
│   │   └── application/          # 应用类
│   └── src/main/res/             # 资源文件
│       ├── layout/               # 布局文件
│       ├── values/               # 资源值
│       ├── drawable/             # 图片资源
│       ├── menu/                 # 菜单
│       └── navigation/           # 导航图
└── build.gradle                  # 构建配置
```

## 编译和运行

### 环境要求
- **JDK**：17或更高版本
- **Android SDK**：API 34 (Android 14)
- **Gradle**：9.2.1
- **Android Studio**：Flamingo 2022.2.1 或更高版本

### 编译步骤
1. 配置Java环境变量
   ```bash
   export JAVA_HOME="C:\Program Files\Java\jdk-17"
   export PATH="$JAVA_HOME\bin:$PATH"
   ```

2. 使用Gradle编译
   ```bash
   cd android-app
   ./gradlew assembleDebug
   ```

3. 安装到设备
   ```bash
   ./gradlew installDebug
   ```

### Android Studio导入
1. 打开Android Studio
2. 选择 "Open" → 导航到 `android-app` 目录
3. 等待项目同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮

## 测试计划

### 单元测试
- ViewModel测试
- Repository测试
- 工具类测试

### UI测试
- Activity/Fragment测试
- 导航测试
- 用户交互测试

### 集成测试
- API集成测试
- 端到端流程测试

## 已知问题和解决方案

### 1. Java环境配置
**问题**：系统中缺少JAVA_HOME配置
**解决方案**：
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

# Linux/macOS
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### 2. Gradle下载慢
**问题**：首次运行需要下载Gradle发行版
**解决方案**：
1. 使用国内镜像
2. 手动下载Gradle放到用户目录
3. 使用Android Studio内置的Gradle

### 3. 百度地图API密钥
**问题**：需要配置百度地图API密钥
**解决方案**：
1. 申请百度地图开发者账号
2. 获取API密钥
3. 在AndroidManifest.xml中配置
```xml
<meta-data
    android:name="com.baidu.lbsapi.API_KEY"
    android:value="YOUR_BAIDU_MAP_API_KEY" />
```

## 开发规范

### 代码规范
- 使用Kotlin官方代码风格
- 类名使用大驼峰命名法
- 方法名使用小驼峰命名法
- 资源文件使用小写加下划线

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 代码重构
- test: 测试相关
- chore: 构建过程或辅助工具

### 分支管理
- main: 主分支，稳定版本
- develop: 开发分支
- feature/*: 功能分支
- bugfix/*: 修复分支
- release/*: 发布分支

## 后续计划

### 短期目标（1-2周）
1. 完成停车位搜索功能
2. 实现停车位详情页面
3. 完善预订流程
4. 集成百度地图

### 中期目标（3-4周）
1. 实现支付功能
2. 完善个人中心
3. 添加消息系统
4. 优化性能

### 长期目标（1-2月）
1. 添加推送通知
2. 实现离线功能
3. 添加数据分析
4. 发布到应用商店

## 贡献指南

1. Fork项目仓库
2. 创建功能分支
3. 提交代码变更
4. 创建Pull Request
5. 通过代码审查

## 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues: [项目Issues页面]
- 邮箱: [开发团队邮箱]
- 文档: [项目Wiki]

---
*最后更新：2026-03-10*
*版本：1.0.0-alpha*