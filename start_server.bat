@echo off
chcp 65001 >nul
title 短信验证码接收服务器

echo ================================================
echo           短信验证码接收服务器
echo ================================================
echo.
echo 正在启动服务器...
echo.

cd /d "%~dp0"

python server.py

echo.
echo 服务器已关闭
pause

