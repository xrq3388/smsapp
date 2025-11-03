@echo off
chcp 65001 >nul
title 上传代码到GitHub

echo ============================================================
echo           上传短信转发系统到GitHub
echo ============================================================
echo.

cd /d "%~dp0"

echo [1/6] 检查Git是否安装...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未安装Git
    echo.
    echo 请先安装Git：
    echo 下载地址：https://git-scm.com/download/win
    echo.
    echo 或者使用网页版上传（参考：GitHub快速上传指南.txt）
    pause
    exit /b 1
)
echo ✓ Git已安装
echo.

echo [2/6] 初始化Git仓库...
if not exist ".git" (
    git init
    echo ✓ Git仓库初始化完成
) else (
    echo ✓ Git仓库已存在
)
echo.

echo [3/6] 配置远程仓库...
git remote remove origin 2>nul
git remote add origin https://github.com/xrq3388/smsapp.git
echo ✓ 远程仓库已配置
echo.

echo [4/6] 添加文件到Git...
echo.
echo 正在添加文件...

REM 确保.gitignore存在
if not exist ".gitignore" (
    echo # Python > .gitignore
    echo __pycache__/ >> .gitignore
    echo *.py[cod] >> .gitignore
    echo verification_codes.txt >> .gitignore
    echo. >> .gitignore
    echo # Android >> .gitignore
    echo *.apk >> .gitignore
    echo build/ >> .gitignore
    echo .gradle/ >> .gitignore
    echo local.properties >> .gitignore
)

git add .github/
git add android_client/
git add *.py
git add *.txt
git add *.md
git add *.bat
git add .gitignore

REM 排除隐私文件
git reset verification_codes.txt 2>nul

echo ✓ 文件已添加
echo.

echo [5/6] 提交代码...
git commit -m "短信验证码转发系统 - 初始提交" -m "- 电脑服务端程序(Python)" -m "- Android客户端程序" -m "- GitHub Actions自动编译配置"
if %errorlevel% neq 0 (
    echo ℹ️  没有新的更改需要提交，或者已经提交过了
)
echo.

echo [6/6] 推送到GitHub...
echo.
echo ⚠️  即将推送到 https://github.com/xrq3388/smsapp.git
echo.
echo 请在弹出的窗口中输入您的GitHub凭据
echo   用户名 - xrq3388
echo   密码 - 您的密码或Personal Access Token
echo.
echo ℹ️  提示：GitHub现在推荐使用Personal Access Token代替密码
echo     如何获取Token请参考 GitHub上传详细指南.txt
echo.
pause

git branch -M main
git push -u origin main

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
    echo 2. 点击 Actions 查看自动编译状态
    echo 3. 等待3-5分钟后下载编译好的APK
    echo.
    echo 详细说明请参考：GitHub在线编译使用指南.txt
    echo.
) else (
    echo.
    echo ============================================================
    echo ❌ 上传失败
    echo ============================================================
    echo.
    echo 可能的原因：
    echo 1. 用户名或密码错误
    echo 2. 需要使用Personal Access Token（推荐）
    echo 3. 网络连接问题
    echo.
    echo 解决方法：
    echo 1. 参考：GitHub上传详细指南.txt
    echo 2. 或使用网页版上传：GitHub快速上传指南.txt
    echo.
)

pause

