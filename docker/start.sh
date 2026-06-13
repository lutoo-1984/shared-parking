#!/bin/bash
# ============================================================
# 共享停车位平台 - Docker 启动脚本（Nginx + PHP-FPM）
# 注意: 不用 set -e，数据库初始化失败不影响 Nginx 启动
# ============================================================

echo "========================================"
echo "  共享停车位平台 - 启动中..."
echo "========================================"

# 目录权限
mkdir -p /var/www/html/uploads/spots /var/www/html/uploads/avatars /var/www/html/logs /run/php
chmod -R 755 /var/www/html/uploads /var/www/html/logs /run/php

# 动态端口（Railway 传入 PORT）
if [ -n "$PORT" ]; then
    echo "→ 使用动态端口: $PORT"
    sed -i "s/listen 80/listen $PORT/g" /etc/nginx/sites-available/default
    sed -i "s/listen \[::\]:80/listen [::]:$PORT/g" /etc/nginx/sites-available/default
fi

# 自动检测 Railway 域名
if [ -z "$APP_URL" ] && [ -n "$RAILWAY_PUBLIC_DOMAIN" ]; then
    export APP_URL="https://$RAILWAY_PUBLIC_DOMAIN"
    echo "→ 自动检测域名: $APP_URL"
fi

# 写入 .env
cat > /var/www/html/.env << EOF
APP_URL=${APP_URL:-http://localhost}
APP_ENV=${APP_ENV:-production}
APP_DEBUG=${APP_DEBUG:-false}
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_DATABASE=${DB_DATABASE:-shared_parking}
DB_USERNAME=${DB_USERNAME:-root}
DB_PASSWORD=${DB_PASSWORD:-}
JWT_SECRET=${JWT_SECRET:-change_this_to_a_random_secret_key_2024}
AMAP_KEY=${AMAP_KEY:-a3cd510ffc1871168cbee271105ad260}
EOF

# 尝试连接数据库并导入表结构（失败不影响启动）
echo "→ 尝试连接数据库..."
php -r "
    \$host = getenv('DB_HOST') ?: 'localhost';
    \$port = getenv('DB_PORT') ?: '3306';
    \$user = getenv('DB_USERNAME') ?: 'root';
    \$pass = getenv('DB_PASSWORD') ?: '';
    \$db   = getenv('DB_DATABASE') ?: 'shared_parking';
    try {
        \$pdo = new PDO('mysql:host='.\$host.';port='.\$port, \$user, \$pass, [PDO::ATTR_TIMEOUT => 5]);
        echo '  → 数据库连接成功！';

        // 尝试创建数据库（如果不存在）
        \$pdo->exec('CREATE DATABASE IF NOT EXISTS '.\$db.' DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        \$pdo->exec('USE '.\$db);

        // 检查是否有表
        \$tables = \$pdo->query('SHOW TABLES')->fetchAll();
        if (count(\$tables) === 0) {
            echo '  → 初始化数据库结构...';
            // schema.sql 可能在 /var/www/html/schema.sql 或 /app/schema.sql
            \$paths = ['/var/www/html/schema.sql', '/app/schema.sql', __DIR__.'/schema.sql'];
            \$sql = null;
            foreach (\$paths as \$p) {
                if (file_exists(\$p)) { \$sql = file_get_contents(\$p); break; }
            }
            if (\$sql) {
                \$pdo->exec(\$sql);
                echo '完成';
            } else {
                echo '跳过(文件未找到)';
            }
        } else {
            echo '  → 数据库已初始化，跳过';
        }
    } catch (Exception \$e) {
        echo '  ⚠ 数据库初始化跳过: '.\$e->getMessage();
    }
" 2>&1

echo ""
echo "========================================"
echo "  ✅ 启动完成！  平台: Shared Parking"
echo "========================================"

# 启动 PHP-FPM（后台）
php-fpm8.2 --daemonize 2>/dev/null || php-fpm --daemonize 2>/dev/null || echo "  ⚠ PHP-FPM 启动警告"

# 启动 Nginx（前台）
echo "→ 启动 Nginx..."
exec nginx -g "daemon off;"
