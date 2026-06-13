#!/bin/bash
# ============================================================
# 共享停车位平台 - Docker 容器启动脚本
# 支持 Railway.app、Render.com、传统服务器
# ============================================================
set -e

echo "========================================"
echo "  共享停车位平台 - 启动中..."
echo "========================================"

# 确保 uploads 目录存在且有权限
mkdir -p /var/www/html/uploads/spots /var/www/html/uploads/avatars
chmod -R 755 /var/www/html/uploads
chown -R www-data:www-data /var/www/html/uploads

# 确保 logs 目录存在
mkdir -p /var/www/html/logs
chmod 755 /var/www/html/logs
chown -R www-data:www-data /var/www/html/logs

# 适配 Railway / Render 等平台的动态端口
# Railway 传入 $PORT，Render 传入 $PORT
if [ -n "$PORT" ]; then
    echo "→ 使用动态端口: $PORT"
    # 修改 Apache 监听端口
    sed -i "s/Listen 80/Listen $PORT/g" /etc/apache2/ports.conf
    sed -i "s/:80>/:$PORT>/g" /etc/apache2/sites-available/000-default.conf
    # 同时更新 APP_URL 环境变量如果没有设置的话
    if [ -z "$APP_URL" ] && [ -n "$RAILWAY_PUBLIC_DOMAIN" ]; then
        export APP_URL="https://$RAILWAY_PUBLIC_DOMAIN"
        echo "→ 自动检测 Railway 域名: $APP_URL"
    fi
fi

# 确保 PHP 能读取环境变量（替换 constants.php 中的默认值）
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

# 等待数据库就绪（如果配置了数据库）
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

            # 尝试自动导入表结构
            php -r "
                try {
                    \$pdo = new PDO(
                        'mysql:host=${DB_HOST};port=${DB_PORT:-3306};dbname=${DB_DATABASE:-shared_parking}',
                        '${DB_USERNAME:-root}',
                        '${DB_PASSWORD}',
                        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
                    );
                    // 检查是否有表
                    \$tables = \$pdo->query('SHOW TABLES')->fetchAll();
                    if (count(\$tables) === 0) {
                        echo '  → 导入数据库结构...';
                        \$sql = file_get_contents('/var/www/html/schema.sql');
                        \$pdo->exec(\$sql);
                        echo '完成';
                    }
                } catch (Exception \$e) {
                    echo '  ⚠ 自动导入失败: ' . \$e->getMessage();
                }
            " 2>&1
            break
        fi
        if [ $i -eq 10 ]; then
            echo "  ⚠ 数据库连接超时，继续启动"
        else
            sleep 2
        fi
    done
fi

echo "========================================"
echo "  ✅ 启动完成！"
echo "  平台: Shared Parking"
echo "========================================"

# 启动 Apache（前台运行）
exec apache2-foreground
