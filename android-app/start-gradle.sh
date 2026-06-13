#!/bin/bash

# 设置正确的JAVA_HOME路径
export JAVA_HOME="/c/Program Files/Java/jdk-17"

# 检查JAVA_HOME是否存在
if [ ! -d "$JAVA_HOME" ]; then
    echo "错误: JAVA_HOME目录不存在: $JAVA_HOME"
    echo "请检查Java安装路径"
    exit 1
fi

# 检查java命令是否可用
if [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "错误: 在JAVA_HOME/bin目录中找不到java可执行文件"
    exit 1
fi

echo "使用JAVA_HOME: $JAVA_HOME"
echo "Java版本:"
"$JAVA_HOME/bin/java" -version

# 运行gradlew
./gradlew "$@"