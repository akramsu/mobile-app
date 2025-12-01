#!/bin/bash
# Run Freshly app on Android Emulator

export GRADLE_USER_HOME="$HOME/.gradle"
export ANDROID_SDK="$LOCALAPPDATA/Android/Sdk"

# Check common Android SDK locations
if [ ! -d "$ANDROID_SDK" ]; then
    ANDROID_SDK="$HOME/AppData/Local/Android/Sdk"
fi

if [ ! -d "$ANDROID_SDK" ]; then
    ANDROID_SDK="/c/Users/$USER/AppData/Local/Android/Sdk"
fi

if [ ! -d "$ANDROID_SDK" ]; then
    echo "ERROR: Android SDK not found!"
    echo "Please set ANDROID_SDK environment variable."
    exit 1
fi

export PATH="$ANDROID_SDK/emulator:$ANDROID_SDK/platform-tools:$PATH"

echo "============================================"
echo " Freshly Android - Emulator Launcher"
echo "============================================"
echo ""

echo "Checking for running emulators..."
adb devices
echo ""

echo "Available emulators:"
emulator -list-avds
echo ""

# Get first available emulator
EMULATOR_NAME=$(emulator -list-avds | head -n 1)

if [ -z "$EMULATOR_NAME" ]; then
    echo "ERROR: No emulators found!"
    echo ""
    echo "Create one in Android Studio: Tools > Device Manager > Create Device"
    exit 1
fi

echo "Using emulator: $EMULATOR_NAME"
echo ""

# Check if emulator is already running
if adb devices | grep -q "emulator"; then
    echo "Emulator is already running!"
else
    echo "Starting emulator: $EMULATOR_NAME"
    echo "This may take 1-2 minutes..."
    emulator -avd "$EMULATOR_NAME" -no-snapshot-load &
    
    echo "Waiting for emulator to boot..."
    adb wait-for-device
    
    echo "Waiting for system to be ready..."
    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    
    echo "Emulator is ready!"
fi

echo ""
echo "Building and installing Freshly app..."
echo ""

./gradlew.bat installDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "============================================"
    echo " APP INSTALLED SUCCESSFULLY!"
    echo "============================================"
    echo ""
    echo "Launching Freshly app..."
    adb shell am start -n com.freshly.app/.MainActivity
    echo ""
    echo "The app should now be running on the emulator!"
else
    echo ""
    echo "============================================"
    echo " INSTALLATION FAILED!"
    echo "============================================"
fi

echo ""
