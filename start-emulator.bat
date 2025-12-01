@echo off
REM Quick emulator starter - just launches the emulator

SET ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk

if not exist "%ANDROID_SDK%" (
    echo ERROR: Android SDK not found!
    echo Please install Android Studio first or set ANDROID_SDK variable.
    pause
    exit /b 1
)

SET PATH=%ANDROID_SDK%\emulator;%ANDROID_SDK%\platform-tools;%PATH%

echo Available emulators:
emulator -list-avds
echo.

echo Enter emulator name (or press Enter to use first):
set /p EMULATOR_NAME=

if "%EMULATOR_NAME%"=="" (
    for /f %%i in ('emulator -list-avds') do (
        set EMULATOR_NAME=%%i
        goto :start
    )
)

:start
if "%EMULATOR_NAME%"=="" (
    echo No emulators found! Create one in Android Studio first.
    pause
    exit /b 1
)

echo Starting emulator: %EMULATOR_NAME%
start "Android Emulator" emulator -avd %EMULATOR_NAME%

echo.
echo Emulator is starting in a new window...
echo Once it's fully loaded, run install-on-emulator.bat to install the app.
echo.
pause
