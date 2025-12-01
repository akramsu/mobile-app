@echo off
REM Install Freshly app on running emulator/device

SET GRADLE_USER_HOME=%USERPROFILE%\.gradle
SET ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk
SET PATH=%ANDROID_SDK%\platform-tools;%PATH%

echo ============================================
echo  Installing Freshly App
echo ============================================
echo.

echo Checking for connected devices/emulators...
adb devices
echo.

echo Building and installing app...
call gradlew.bat installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  SUCCESS!
    echo ============================================
    echo.
    echo Launching app...
    adb shell am start -n com.freshly.app/.MainActivity
    echo.
    echo App is now running!
) else (
    echo.
    echo Installation failed!
    echo Make sure an emulator is running or a device is connected.
)

echo.
pause
