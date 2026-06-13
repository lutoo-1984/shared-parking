#!/bin/bash
# ============================================================
# 共享停车位平台 - Docker 启动脚本（Nginx + PHP-FPM）
# ============================================================
set -e

echo "========================================"
echo "  共享停车位平台 - 启动中..."
echo "========================================"

# 目录权限
mkdir -p /var/www/html/uploads/spots /var/www/html/uploads/avatars /var/www/html/logs
chmod -R 755 /var/www/html/uploads /var/www/html/logs

# 确保 PHP-FPM socket 目录存在
mkdir -p /run/php
chmod 755 /run/php

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

# 写入 .env（供 PHP 代码读取）
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

# 等待数据库并初始化表结构
if [ -n "$DB_HOST" ] && [ -n "$DB_PASSWORD" ]; then
    echo "→ 等待数据库就绪..."
    for i in $(seq 1 30); do
        if php -r "
            \$pdo = @new PDO(
                'mysql:host=${DB_HOST};port=${DB_PORT:-3306}',
                '${DB_USERNAME:-root}',
                '${DB_PASSWORD}',
                [PDO::ATTR_TIMEOUT => 3]
            );
            echo 'connected';
        " 2>/dev/null | grep -q 'connected'; then
            echo "  → 数据库连接成功！"
            php -r "
                try {
                    \$pdo = new PDO('mysql:host=${DB_HOST};port=${DB_PORT:-3306};dbname=${DB_DATABASE:-shared_parking}','${DB_USERNAME:-root}','${DB_PASSWORD}');
                    \$tables = \$pdo->query('SHOW TABLES')->fetchAll();
                    if (count(\$tables) === 0) {
                        echo '  → 导入数据库结构...';
                        \$sql = file_get_contents('/var/www/html/schema.sql');
                        \$pdo->exec(\$sql);
                        echo '完成';
                    }
                } catch (Exception \$e) { echo '  ⚠ '.\$e->getMessage(); }
            " 2>&1
            break
        fi
        sleep 2
    done
fi

echo "========================================"
echo "  ✅ 启动完成！  平台: Shared Parking"
echo "========================================"

# 启动 PHP-FPM（后台）
php-fpm8.2 --daemonize || php-fpm --daemonize || true

# 启动 Nginx（前台）
exec nginx -g "daemon off;"
