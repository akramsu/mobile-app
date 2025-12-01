@echo off
REM Run script for Freshly Android App
REM This script sets the Gradle user home and installs the app on a connected device

SET GRADLE_USER_HOME=%USERPROFILE%\.gradle
echo Using Gradle home: %GRADLE_USER_HOME%

echo.
echo Building and installing Freshly Android App...
echo.
echo Make sure your Android device is connected or emulator is running!
echo.

call gradlew.bat installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================
    echo APP INSTALLED SUCCESSFULLY!
    echo ======================================
    echo.
    echo You can now launch the app on your device.
    echo.
) else (
    echo.
    echo ======================================
    echo INSTALLATION FAILED!
    echo ======================================
    echo.
    echo Make sure:
    echo 1. Android device is connected via USB
    echo 2. USB debugging is enabled
    echo 3. Or emulator is running
    echo.
)

pause
