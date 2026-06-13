# 🚄 部署到 Railway.app（免费方案）

Railway 是一个云平台，支持直接部署 Docker 容器，**免费额度足够跑这个小项目**。

## 前置条件

1. 一个 [GitHub](https://github.com) 账号
2. 一个 [Railway](https://railway.app) 账号（用 GitHub 登录）

---

## 部署步骤

### 第一步：推送代码到 GitHub

```bash
# 在项目目录下
git init
git add .
git commit -m "准备部署到 Railway"
# 在 GitHub 上创建仓库后关联
git remote add origin https://github.com/你的用户名/shared-parking.git
git push -u origin main
```

### 第二步：在 Railway 创建项目

1. 打开 [Railway](https://railway.app/dashboard)
2. 点击 **New Project** → **Deploy from GitHub repo**
3. 选择刚才推送的仓库
4. Railway 会自动检测 `Dockerfile` 并开始构建

### 第三步：添加 MySQL 数据库

1. 在 Railway 项目页面点击 **New**
2. 选择 **Database** → **MySQL**
3. 等待 MySQL 部署完成（约 1 分钟）
4. 点击 MySQL 服务，在 **Variables** 标签里可以看到以下变量：
   - `MYSQL_URL`（格式：`mysql://user:password@host:port/database`）
   - 或者拆分的 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_USER`、`MYSQL_PASSWORD`、`MYSQL_DATABASE`

### 第四步：配置环境变量

在 App 服务（不是 MySQL）的 **Variables** 标签里添加以下变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `DB_HOST` | `${{MYSQL_HOST}}` | 从 MySQL 服务自动获取 |
| `DB_PORT` | `${{MYSQL_PORT}}` | 自动获取 |
| `DB_DATABASE` | `${{MYSQL_DATABASE}}` | 自动获取 |
| `DB_USERNAME` | `${{MYSQL_USER}}` | 自动获取 |
| `DB_PASSWORD` | `${{MYSQL_PASSWORD}}` | 自动获取 |
| `JWT_SECRET` | `你的随机字符串` | **务必修改！** 至少32位随机字符 |
| `AMAP_KEY` | `a3cd510ffc1871168cbee271105ad260` | 你的高德地图 Key |
| `APP_DEBUG` | `false` | 生产环境关掉调试 |

> Railway 支持变量引用语法 `${{VAR_NAME}}`，会自动替换。
> MySQL 服务的变量名可能带有 `MYSQL_` 前缀，具体看实际生成的变量名。

### 第五步：部署！

- 配置好变量后，Railway 会自动重新部署
- 等待约 2~3 分钟，看到 **Deploy Success** 即可
- 在 **Settings** → **Public Domain** 点 **Generate Domain** 生成你的专属域名
- 比如 `https://shared-parking.up.railway.app`

---

## 接下来

### 配置高德地图白名单

1. 登录 [高德开放平台](https://lbs.amap.com)
2. 进入控制台 → 应用管理
3. 找到你的 Web JS API Key
4. 在 **域名白名单** 中加入你的 Railway 域名（如 `https://shared-parking.up.railway.app`）

### 创建管理员账号

部署后访问 `/api/auth/register` 注册一个用户，然后直接连数据库把 `role` 改为 `admin`：

```sql
UPDATE users SET role = 'admin' WHERE email = '你的邮箱@xxx.com';
```

---

## 升级到方案 A（自己的服务器）

当需要迁移到自己的服务器时：

```bash
# 1. 从 Railway 导出数据库
railway run mysqldump -u $MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE > backup.sql

# 2. 在服务器上运行 Docker Compose
docker compose up -d

# 3. 导入数据
docker compose exec -T mysql mysql -u root -p$DB_ROOT_PASSWORD shared_parking < backup.sql
```

迁移过程半小时内完成，用户无感。

---

## 免费额度说明

Railway 免费计划每月提供：
- **$5 或 $10 额度**（视活动情况）
- 这个项目预计每月消耗 **$2~$3**
- 超出会暂停，下月恢复，**不会扣费**

所以只要你不超量使用，**长期免费**。
