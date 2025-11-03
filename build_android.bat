@echo off
chcp 65001 >nul
title Android应用编译脚本

echo ============================================================
echo           短信转发 - Android应用编译脚本
echo ============================================================
echo.

:: 检查Java
echo [1/5] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Java，请先安装JDK
    echo.
    echo 下载地址：
    echo   https://adoptium.net/
    echo   或
    echo   https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)
echo ✓ Java已安装
java -version
echo.

:: 检查ANDROID_HOME
echo [2/5] 检查Android SDK...
if not defined ANDROID_HOME (
    echo ⚠️  未设置ANDROID_HOME环境变量
    echo.
    echo 请设置ANDROID_HOME指向Android SDK目录
    echo 或者下载Android SDK命令行工具：
    echo   https://developer.android.com/studio#command-tools
    echo.
    set /p CUSTOM_SDK="是否手动指定SDK路径？(Y/N): "
    if /i "%CUSTOM_SDK%"=="Y" (
        set /p ANDROID_HOME="请输入Android SDK路径: "
    ) else (
        pause
        exit /b 1
    )
)
echo ✓ Android SDK: %ANDROID_HOME%
echo.

:: 进入项目目录
echo [3/5] 进入项目目录...
cd /d "%~dp0android_client"
if %errorlevel% neq 0 (
    echo ❌ 找不到android_client目录
    pause
    exit /b 1
)
echo ✓ 当前目录: %cd%
echo.

:: 检查gradlew
echo [4/5] 检查Gradle Wrapper...
if not exist "gradlew.bat" (
    echo ⚠️  未找到gradlew.bat
    echo.
    echo 正在尝试生成Gradle Wrapper...
    gradle wrapper --gradle-version 8.0 2>nul
    if %errorlevel% neq 0 (
        echo ❌ 生成失败，请先安装Gradle
        echo 下载地址：https://gradle.org/releases/
        pause
        exit /b 1
    )
    echo ✓ Gradle Wrapper已生成
) else (
    echo ✓ Gradle Wrapper已存在
)
echo.

:: 编译
echo [5/5] 开始编译应用...
echo.
echo 这可能需要几分钟，首次编译会下载依赖...
echo ============================================================
echo.

gradlew.bat clean assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo ============================================================
    echo ❌ 编译失败！
    echo ============================================================
    echo.
    echo 常见问题：
    echo   1. SDK版本不匹配 - 安装对应版本的SDK
    echo   2. 网络问题 - 配置Gradle国内镜像
    echo   3. 权限问题 - 以管理员身份运行
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo ✓ 编译成功！
echo ============================================================
echo.
echo APK文件位置：
echo   %cd%\app\build\outputs\apk\debug\app-debug.apk
echo.
echo 您可以将此APK文件传输到手机安装。
echo.

:: 询问是否打开APK所在目录
set /p OPEN_DIR="是否打开APK所在目录？(Y/N): "
if /i "%OPEN_DIR%"=="Y" (
    explorer app\build\outputs\apk\debug
)

echo.
echo ============================================================
echo                    编译完成！
echo ============================================================
pause

