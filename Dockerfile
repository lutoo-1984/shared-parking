# ============================================================
# 共享停车位平台 - 生产 Dockerfile
# 基于 Debian + Nginx + PHP-FPM（稳定、高性能）
# ============================================================
FROM debian:bookworm-slim

# 手动修改计数器 — 用于强制 Railway 不使用构建缓存
# 如果遇到缓存问题，增大此数字即可：1
ARG CACHE_BUST=1

LABEL maintainer="Shared Parking Team"
LABEL description="共享停车位平台 - Nginx + PHP-FPM"

ENV DEBIAN_FRONTEND=noninteractive

# ===== 系统包 =====
RUN apt-get update && apt-get install -y \
    nginx \
    php8.2 \
    php8.2-fpm \
    php8.2-mysql \
    php8.2-mbstring \
    php8.2-bcmath \
    php8.2-zip \
    php8.2-xml \
    php8.2-curl \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# ===== Composer 官方安装 =====
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer

# ===== 项目文件 =====
WORKDIR /var/www/html
COPY . .

# ===== Composer 依赖 =====
RUN composer install --no-dev --optimize-autoloader --no-interaction || true

# ===== 目录权限 =====
RUN mkdir -p /var/www/html/uploads/spots /var/www/html/uploads/avatars /var/www/html/logs \
    && chmod -R 755 /var/www/html/uploads /var/www/html/logs

# ===== PHP 配置 =====
COPY docker/php/custom.ini /etc/php/8.2/fpm/conf.d/99-custom.ini

# ===== PHP-FPM socket 配置（与 Nginx 配置匹配）=====
RUN sed -i 's|listen = /run/php/php8.2-fpm.sock|listen = /run/php/php8.2-fpm.sock|' /etc/php/8.2/fpm/pool.d/www.conf && \
    sed -i 's/^listen.owner = www-data/listen.owner = www-data/' /etc/php/8.2/fpm/pool.d/www.conf && \
    sed -i 's/^listen.group = www-data/listen.group = www-data/' /etc/php/8.2/fpm/pool.d/www.conf

# ===== Nginx 配置 =====
COPY docker/nginx/default.conf /etc/nginx/sites-available/default
RUN rm -f /etc/nginx/sites-enabled/default && \
    ln -sf /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default

# ===== 启动脚本 =====
COPY docker/start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 80

CMD ["/start.sh"]
