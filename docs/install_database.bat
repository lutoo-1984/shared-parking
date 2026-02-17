@echo off
REM 共享停车位平台数据库安装脚本 (Windows)
REM 使用方法: install_database.bat [数据库用户名] [数据库密码] [数据库主机] [数据库名]

setlocal enabledelayedexpansion

REM 默认参数
set "DB_USER=%~1"
if "%DB_USER%"=="" set "DB_USER=root"

set "DB_PASS=%~2"
if "%DB_PASS%"=="" set "DB_PASS="

set "DB_HOST=%~3"
if "%DB_HOST%"=="" set "DB_HOST=localhost"

set "DB_NAME=%~4"
if "%DB_NAME%"=="" set "DB_NAME=shared_parking"

echo ===============================================
echo     共享停车位平台数据库安装脚本
echo ===============================================

REM 检查MySQL客户端是否安装
where mysql >nul 2>nul
if errorlevel 1 (
    echo 错误: MySQL客户端未安装。请先安装MySQL客户端。
    pause
    exit /b 1
)

REM 检查SQL文件是否存在
set "SQL_FILE=schema.sql"
if not exist "%SQL_FILE%" (
    echo 错误: 找不到SQL文件 %SQL_FILE%
    pause
    exit /b 1
)

echo.
echo 数据库配置:
echo   用户名: %DB_USER%
echo   主机: %DB_HOST%
echo   数据库名: %DB_NAME%
echo.

REM 确认操作
set /p "CONFIRM=是否继续安装数据库？(Y/N): "
if /i not "!CONFIRM!"=="Y" (
    echo 安装已取消。
    pause
    exit /b 0
)

REM 测试数据库连接
echo.
echo 测试数据库连接...
mysql -h "%DB_HOST%" -u "%DB_USER%" -p"%DB_PASS%" -e "SELECT 1" >nul 2>nul
if errorlevel 1 (
    echo 错误: 无法连接到MySQL数据库。请检查用户名、密码和主机设置。
    pause
    exit /b 1
)
echo 数据库连接成功！

REM 创建数据库
echo.
echo 创建数据库 %DB_NAME%...
mysql -h "%DB_HOST%" -u "%DB_USER%" -p"%DB_PASS%" -e "CREATE DATABASE IF NOT EXISTS \`%DB_NAME%\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci" >nul 2>nul
if errorlevel 1 (
    echo 错误: 创建数据库失败。
    pause
    exit /b 1
)
echo 数据库创建成功！

REM 导入SQL结构
echo.
echo 导入数据库结构...
mysql -h "%DB_HOST%" -u "%DB_USER%" -p"%DB_PASS%" "%DB_NAME%" < "%SQL_FILE%" >nul 2>nul
if errorlevel 1 (
    echo 错误: 导入SQL文件失败。
    pause
    exit /b 1
)
echo 数据库结构导入成功！

REM 验证表是否创建成功
echo.
echo 验证数据库表...
for /f "tokens=*" %%i in ('mysql -h "%DB_HOST%" -u "%DB_USER%" -p"%DB_PASS%" "%DB_NAME%" -sN -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '%DB_NAME%'"') do set "TABLE_COUNT=%%i"

if %TABLE_COUNT% LSS 7 (
    echo 警告: 只创建了 %TABLE_COUNT% 张表，可能导入不完整。
) else (
    echo 成功创建 %TABLE_COUNT% 张表！
)

REM 显示创建的表
echo.
echo 已创建的数据库表:
mysql -h "%DB_HOST%" -u "%DB_USER%" -p"%DB_PASS%" "%DB_NAME%" -e "SHOW TABLES" | findstr /v /c:"Tables_in"
echo.

REM 显示默认用户
echo 默认用户账户:
echo   管理员:
echo     用户名: admin
echo     密码: admin123
echo     邮箱: admin@shared-parking.com
echo.
echo   测试用户:
echo     用户名: testuser
echo     密码: user123
echo     邮箱: user@shared-parking.com

echo.
echo ===============================================
echo     数据库安装完成！
echo ===============================================
echo.
echo 下一步:
echo 1. 复制 .env.example 为 .env
echo 2. 在 .env 文件中配置数据库连接信息
echo 3. 运行 "composer install" 安装PHP依赖
echo 4. 启动开发服务器: "php -S localhost:8080 -t web"
echo.

pause