@echo off
chcp 65001 >nul
echo ========================================
echo    AIEdge PC 端 - 一键启动
echo    作者: 溪土红薯
echo    GitHub: https://github.com/XiTu893
echo ========================================
echo.

where bun >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Bun，请先安装 Bun
    echo 下载地址: https://bun.sh/
    echo.
    pause
    exit /b 1
)

echo [信息] 检查 Bun 安装...
bun --version

echo.
echo [信息] 检查依赖...
if not exist "node_modules\" (
    echo [信息] 正在安装依赖...
    call bun install
    if %ERRORLEVEL% NEQ 0 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)

echo.
echo [信息] 启动 AIEdge...
echo.
call bun start
pause
