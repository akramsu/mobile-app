# 🚀 Freshly Android - Build & Run Instructions

## ⚠️ Important: Gradle Permission Issue Workaround

Android Studio is trying to use `C:\Program Files\Android\Android Studio\jbr\wrapper\dists` which requires admin permissions. We've configured the project to use your user directory instead.

---

## ✅ Solution 1: Use Command Line (RECOMMENDED)

### Build the App
Double-click `build.bat` or run in terminal:
```bash
cd d:\projects\freshly\android-studio
build.bat
```

This will:
- Set `GRADLE_USER_HOME` to your user directory
- Build the debug APK
- Output: `app\build\outputs\apk\debug\app-debug.apk`

### Install on Device
Double-click `run.bat` or run in terminal:
```bash
cd d:\projects\freshly\android-studio
run.bat
```

This will:
- Build the app
- Install it on your connected Android device or emulator
- Make sure USB debugging is enabled!

### Manual Terminal Commands
```bash
cd d:\projects\freshly\android-studio

# Set Gradle home to avoid permission issues
export GRADLE_USER_HOME="$HOME/.gradle"

# Build APK
./gradlew.bat assembleDebug

# Install on device
./gradlew.bat installDebug

# Clean and rebuild
./gradlew.bat clean assembleDebug
```

---

## ✅ Solution 2: Fix Android Studio Settings

### Option A: Change Gradle Home in Android Studio
1. Open Android Studio
2. **File → Settings** (Ctrl+Alt+S)
3. **Build, Execution, Deployment → Build Tools → Gradle**
4. Under **Gradle user home**, set to: `C:\Users\Akram\.gradle`
5. Click **Apply** and **OK**
6. **File → Invalidate Caches → Invalidate and Restart**

### Option B: Run Android Studio as Administrator
1. Right-click **Android Studio** shortcut
2. Select **Run as administrator**
3. Open the project: `d:\projects\freshly\android-studio`
4. Build normally

⚠️ **Warning**: Running as admin is not recommended for security reasons. Use Solution 1 or Option A instead.

---

## ✅ Solution 3: Install APK Directly

Since we already built the APK successfully, you can install it directly:

### On Android Device:
1. Copy `app\build\outputs\apk\debug\app-debug.apk` to your device
2. Open the APK file on your device
3. Allow installation from unknown sources if prompted
4. Install and run!

### Using ADB:
```bash
# Connect device via USB with debugging enabled
adb install app\build\outputs\apk\debug\app-debug.apk

# Or if device already has the app
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 Current Build Status

✅ **BUILD SUCCESSFUL** (1m 20s)
- APK Size: **19MB**
- Location: `app\build\outputs\apk\debug\app-debug.apk`
- Min SDK: **26** (Android 8.0+)
- Target SDK: **34** (Android 14)

---

## 🔧 Troubleshooting

### "Could not create parent directory for lock file"
**Cause**: Android Studio trying to use its JBR directory without permissions.

**Solutions** (in order of preference):
1. ✅ Use `build.bat` script (sets environment variable)
2. ✅ Change Gradle home in Android Studio settings (see Option A above)
3. ✅ Install the pre-built APK directly
4. ⚠️ Run Android Studio as administrator (not recommended)

### Gradle Daemon Issues
If you see daemon-related errors:
```bash
cd d:\projects\freshly\android-studio
./gradlew.bat --stop
export GRADLE_USER_HOME="$HOME/.gradle"
./gradlew.bat assembleDebug --no-daemon
```

### Slow Build Times
First build takes 1-2 minutes. Subsequent builds are faster (10-15 seconds).

```bash
# Speed up builds with parallel execution
./gradlew.bat assembleDebug --parallel
```

### Device Not Detected
```bash
# Check if device is connected
adb devices

# If no devices shown:
# 1. Enable USB debugging on your Android device
# 2. Connect via USB
# 3. Accept the debugging prompt on your device
```

---

## 🎯 Quick Start Guide

### For Developers:
1. **Recommended**: Use `build.bat` to compile
2. Use `run.bat` to install on device
3. Or open in Android Studio with fixed Gradle settings

### For Testing:
1. Just install `app\build\outputs\apk\debug\app-debug.apk` on your Android device
2. Launch the app
3. Enjoy! 🎉

---

## 📊 Build Output

```
BUILD SUCCESSFUL in 1m 20s
38 actionable tasks: 38 executed

Warnings (non-breaking):
- 6 deprecation warnings for Accompanist Pager
  (Library still works perfectly, migration optional)

APK: app\build\outputs\apk\debug\app-debug.apk (19MB)
```

---

## 🚀 Next Steps

1. **Build**: Run `build.bat`
2. **Install**: Run `run.bat` with device connected
3. **Test**: Launch Freshly app on your Android device
4. **Develop**: Open in Android Studio with fixed settings

---

## 📝 Environment Configuration

The project is now configured to use:
- **Gradle Home**: `%USERPROFILE%\.gradle` (C:\Users\Akram\.gradle)
- **Gradle Version**: 8.13
- **JDK**: 21.0.7 (from Android Studio)
- **Kotlin**: 1.9.10
- **Build Tool**: Gradle with Kotlin DSL

This avoids the permission issue with Android Studio's JBR directory.

---

## ✅ Success Checklist

- [x] Build successful from command line
- [x] APK generated (19MB)
- [x] Gradle configured to use user directory
- [x] Build scripts created (build.bat, run.bat)
- [x] Alternative installation methods documented
- [ ] Install on Android device (your next step!)
- [ ] Test all app features
- [ ] Enjoy your native Android app! 🎉

---

*Last updated: December 1, 2025*
*Build Status: SUCCESS ✅*
