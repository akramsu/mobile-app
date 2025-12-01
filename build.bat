@echo off
REM Build script for Freshly Android App
REM This script sets the Gradle user home to avoid permission issues

SET GRADLE_USER_HOME=%USERPROFILE%\.gradle
echo Using Gradle home: %GRADLE_USER_HOME%

echo.
echo Building Freshly Android App...
echo.

call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================
    echo BUILD SUCCESSFUL!
    echo ======================================
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ======================================
    echo BUILD FAILED!
    echo ======================================
    echo.
)

pause
