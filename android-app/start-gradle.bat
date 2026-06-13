@echo off
rem 设置正确的JAVA_HOME路径
set JAVA_HOME=C:\Program Files\Java\jdk-17

rem 检查JAVA_HOME是否存在
if not exist "%JAVA_HOME%" (
    echo 错误: JAVA_HOME目录不存在: %JAVA_HOME%
    echo 请检查Java安装路径
    pause
    exit /b 1
)

rem 检查java命令是否可用
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo 错误: 在JAVA_HOME\bin目录中找不到java.exe可执行文件
    pause
    exit /b 1
)

echo 使用JAVA_HOME: %JAVA_HOME%
echo Java版本:
"%JAVA_HOME%\bin\java" -version

rem 运行gradlew
call gradlew %*