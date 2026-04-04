#!/bin/bash

echo "========================================"
echo "   AIEdge PC 端 - 一键启动"
echo "   作者: 溪土红薯"
echo "   GitHub: https://github.com/XiTu893"
echo "========================================"
echo ""

if ! command -v bun &> /dev/null; then
    echo "[错误] 未找到 Bun，请先安装 Bun"
    echo "下载地址: https://bun.sh/"
    echo ""
    exit 1
fi

echo "[信息] 检查 Bun 安装..."
bun --version

echo ""
echo "[信息] 检查依赖..."
if [ ! -d "node_modules" ]; then
    echo "[信息] 正在安装依赖..."
    bun install
    if [ $? -ne 0 ]; then
        echo "[错误] 依赖安装失败"
        exit 1
    fi
fi

echo ""
echo "[信息] 启动 AIEdge..."
echo ""
bun start
