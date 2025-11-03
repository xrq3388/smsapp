@echo off
chcp 65001 >nul
title 简易上传到GitHub

echo ============================================================
echo           简易上传脚本
echo ============================================================
echo.

cd /d "%~dp0"

echo 准备推送到GitHub...
echo.
echo 仓库地址：https://github.com/xrq3388/smsapp.git
echo 用户名：xrq3388
echo.
echo ⚠️  重要提示：
echo 1. 推荐使用Personal Access Token代替密码（更安全）
echo 2. Token获取方法：https://github.com/settings/tokens
echo 3. 点击 Generate new token - classic，勾选 repo 权限
echo.
echo ============================================================
echo 现在将提示您输入密码或Token...
echo ============================================================
echo.
pause

REM 初始化Git（如果还没有）
if not exist ".git" (
    echo 正在初始化Git仓库...
    git init
    git remote add origin https://github.com/xrq3388/smsapp.git
)

REM 添加文件
echo 正在添加文件...
git add .github/ android_client/ *.py *.txt *.md *.bat .gitignore 2>nul
git reset verification_codes.txt 2>nul

REM 提交
echo 正在提交...
git commit -m "短信验证码转发系统 - 初始提交"

REM 推送（在URL中包含用户名，这样会提示输入密码）
echo.
echo ============================================================
echo 正在推送到GitHub...
echo 请在下方输入密码或Personal Access Token：
echo ============================================================
echo.

git branch -M main
git push -u https://xrq3388@github.com/xrq3388/smsapp.git main

if %errorlevel% equ 0 (
    echo.
    echo ============================================================
    echo ✅ 上传成功！
    echo ============================================================
    echo.
    echo 仓库地址：https://github.com/xrq3388/smsapp
    echo.
    echo 接下来：
    echo 1. 访问上述地址查看代码
    echo 2. 点击 Actions 查看自动编译
    echo 3. 等待3-5分钟后下载APK
    echo.
) else (
    echo.
    echo ============================================================
    echo ❌ 上传失败
    echo ============================================================
    echo.
    echo 可能原因：
    echo 1. 密码或Token错误
    echo 2. 网络连接问题
    echo 3. 仓库权限问题
    echo.
    echo 解决方法：
    echo 方法1：使用Personal Access Token
    echo   - 访问：https://github.com/settings/tokens
    echo   - 生成新Token，勾选 repo 权限
    echo   - 使用Token代替密码
    echo.
    echo 方法2：使用网页版上传（最简单）
    echo   - 访问：https://github.com/xrq3388/smsapp
    echo   - 点击 Add file - Upload files
    echo   - 拖拽文件上传
    echo.
)

pause

