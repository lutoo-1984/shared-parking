# ============================================================
# 共享停车位平台 - 生产 Dockerfile
# 基于 PHP 8.2 Apache + Composer
# ============================================================
FROM php:8.2-apache-bookworm

LABEL maintainer="Shared Parking Team"
LABEL description="共享停车位平台 - 后端API + Web前端"

# ===== 系统依赖 =====
RUN apt-get update && apt-get install -y --no-install-recommends \
    libzip-dev \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# ===== PHP 扩展 =====
RUN docker-php-ext-install \
    pdo_mysql \
    zip \
    bcmath \
    && docker-php-ext-enable pdo_mysql

# ===== Apache 配置 =====
RUN a2enmod rewrite headers expires deflate && \
    a2dismod mpm_event && \
    a2enmod mpm_prefork

# 设置 DocumentRoot 到项目根目录（.htaccess 使用相对路径）
ENV APACHE_DOCUMENT_ROOT=/var/www/html
RUN sed -ri -e 's!/var/www/html!${APACHE_DOCUMENT_ROOT}!g' \
    /etc/apache2/sites-available/*.conf \
    /etc/apache2/apache2.conf \
    /etc/apache2/conf-available/*.conf

# 允许 .htaccess（AllowOverride）
RUN sed -i '/<Directory \/var\/www\/>/,/<\/Directory>/ s/AllowOverride None/AllowOverride All/' \
    /etc/apache2/apache2.conf

# ===== Composer =====
COPY --from=composer:latest /usr/bin/composer /usr/local/bin/composer

# ===== 复制项目文件 =====
WORKDIR /var/www/html
COPY . .

# ===== 安装 PHP 依赖 =====
RUN composer install --no-dev --optimize-autoloader --no-interaction \
    || (echo "Composer install failed, trying with available packages" && \
        composer install --no-dev --no-interaction --ignore-platform-reqs)

# ===== 创建必要目录 =====
RUN mkdir -p /var/www/html/uploads/spots \
    /var/www/html/uploads/avatars \
    /var/www/html/logs \
    && chmod -R 755 /var/www/html/uploads \
    /var/www/html/logs \
    && chown -R www-data:www-data /var/www/html/uploads \
    /var/www/html/logs

# ===== PHP 配置 =====
COPY docker/php/custom.ini /usr/local/etc/php/conf.d/custom.ini

# ===== 启动脚本 =====
COPY docker/start.sh /usr/local/bin/start.sh
RUN chmod +x /usr/local/bin/start.sh

EXPOSE 80

CMD ["/usr/local/bin/start.sh"]
