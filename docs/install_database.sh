#!/bin/bash
# 共享停车位平台数据库安装脚本
# 使用方法: ./install_database.sh [数据库用户名] [数据库密码] [数据库主机] [数据库名]

set -e

# 默认参数
DB_USER="${1:-root}"
DB_PASS="${2:-}"
DB_HOST="${3:-localhost}"
DB_NAME="${4:-shared_parking}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== 共享停车位平台数据库安装脚本 ===${NC}"

# 检查MySQL客户端是否安装
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: MySQL客户端未安装。请先安装MySQL客户端。${NC}"
    exit 1
fi

# 检查SQL文件是否存在
SQL_FILE="schema.sql"
if [ ! -f "$SQL_FILE" ]; then
    echo -e "${RED}错误: 找不到SQL文件 $SQL_FILE${NC}"
    exit 1
fi

echo -e "${YELLOW}数据库配置:${NC}"
echo "用户名: $DB_USER"
echo "主机: $DB_HOST"
echo "数据库名: $DB_NAME"

# 确认操作
read -p "是否继续安装数据库？(y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}安装已取消。${NC}"
    exit 0
fi

# 测试数据库连接
echo -e "${YELLOW}测试数据库连接...${NC}"
if ! mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1" &> /dev/null; then
    echo -e "${RED}错误: 无法连接到MySQL数据库。请检查用户名、密码和主机设置。${NC}"
    exit 1
fi
echo -e "${GREEN}数据库连接成功！${NC}"

# 创建数据库
echo -e "${YELLOW}创建数据库 $DB_NAME...${NC}"
if ! mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"; then
    echo -e "${RED}错误: 创建数据库失败。${NC}"
    exit 1
fi
echo -e "${GREEN}数据库创建成功！${NC}"

# 导入SQL结构
echo -e "${YELLOW}导入数据库结构...${NC}"
if ! mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"; then
    echo -e "${RED}错误: 导入SQL文件失败。${NC}"
    exit 1
fi
echo -e "${GREEN}数据库结构导入成功！${NC}"

# 验证表是否创建成功
echo -e "${YELLOW}验证数据库表...${NC}"
TABLE_COUNT=$(mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -sN -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME'")
if [ "$TABLE_COUNT" -lt 7 ]; then
    echo -e "${RED}警告: 只创建了 $TABLE_COUNT 张表，可能导入不完整。${NC}"
else
    echo -e "${GREEN}成功创建 $TABLE_COUNT 张表！${NC}"
fi

# 显示创建的表
echo -e "${YELLOW}已创建的数据库表:${NC}"
mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SHOW TABLES" | while read -r table; do
    echo "  - $table"
done

# 显示默认用户
echo -e "${YELLOW}默认用户账户:${NC}"
echo "  管理员:"
echo "    用户名: admin"
echo "    密码: admin123"
echo "    邮箱: admin@shared-parking.com"
echo ""
echo "  测试用户:"
echo "    用户名: testuser"
echo "    密码: user123"
echo "    邮箱: user@shared-parking.com"

echo -e "${GREEN}=== 数据库安装完成！ ===${NC}"
echo ""
echo -e "${YELLOW}下一步:${NC}"
echo "1. 复制 .env.example 为 .env"
echo "2. 在 .env 文件中配置数据库连接信息"
echo "3. 运行 'composer install' 安装PHP依赖"
echo "4. 启动开发服务器: 'php -S localhost:8080 -t web'"

exit 0