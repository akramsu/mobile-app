@echo off
REM Run Freshly app on Android Emulator (without Android Studio)

SET GRADLE_USER_HOME=%USERPROFILE%\.gradle
SET ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk

echo ============================================
echo  Freshly Android - Emulator Launcher
echo ============================================
echo.

REM Check if Android SDK exists
if not exist "%ANDROID_SDK%" (
    echo ERROR: Android SDK not found at %ANDROID_SDK%
    echo.
    echo Please set ANDROID_SDK environment variable to your SDK location.
    echo Common locations:
    echo   - %LOCALAPPDATA%\Android\Sdk
    echo   - C:\Android\Sdk
    echo   - %USERPROFILE%\AppData\Local\Android\Sdk
    echo.
    pause
    exit /b 1
)

SET PATH=%ANDROID_SDK%\emulator;%ANDROID_SDK%\platform-tools;%PATH%

echo Checking for running emulators...
adb devices
echo.

REM List available emulators
echo Available emulators:
echo.
emulator -list-avds
echo.

echo Enter emulator name (or press Enter to use first available):
set /p EMULATOR_NAME=

if "%EMULATOR_NAME%"=="" (
    echo Using first available emulator...
    for /f %%i in ('emulator -list-avds') do (
        set EMULATOR_NAME=%%i
        goto :found
    )
    :found
)

if "%EMULATOR_NAME%"=="" (
    echo.
    echo ERROR: No emulators found!
    echo.
    echo To create an emulator:
    echo 1. Open Android Studio
    echo 2. Tools ^> Device Manager
    echo 3. Click "Create Device"
    echo 4. Or use command line: avdmanager create avd -n MyEmulator -k "system-images;android-34;google_apis;x86_64"
    echo.
    pause
    exit /b 1
)

echo.
echo Starting emulator: %EMULATOR_NAME%
echo This may take 1-2 minutes...
echo.

start "Android Emulator" emulator -avd %EMULATOR_NAME% -no-snapshot-load

echo Waiting for emulator to boot...
adb wait-for-device
echo.

echo Emulator is booting up...
echo Waiting for system to be ready...
:wait_loop
adb shell getprop sys.boot_completed 2>nul | find "1" >nul
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :wait_loop
)

echo.
echo Emulator is ready!
echo.

echo Building and installing Freshly app...
echo.

call gradlew.bat installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  APP INSTALLED SUCCESSFULLY!
    echo ============================================
    echo.
    echo Launching Freshly app...
    adb shell am start -n com.freshly.app/.MainActivity
    echo.
    echo The app should now be running on the emulator!
    echo.
) else (
    echo.
    echo ============================================
    echo  INSTALLATION FAILED!
    echo ============================================
    echo.
)

pause
