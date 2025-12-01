# 🔧 Fix Android Studio Gradle Permission Error

## The Problem
Android Studio is trying to download Gradle to:
```
C:\Program Files\Android\Android Studio\jbr\wrapper\dists
```
This requires administrator permissions.

---

## ✅ Solution: Configure Android Studio to Use Your User Directory

### Step 1: Open Gradle Settings in Android Studio

1. Open Android Studio
2. Click **File** → **Settings** (or press `Ctrl + Alt + S`)
3. Navigate to: **Build, Execution, Deployment** → **Build Tools** → **Gradle**

### Step 2: Configure Gradle User Home

In the Gradle settings window:

1. Find **"Gradle user home"** field
2. Set it to: `C:\Users\Akram\.gradle`
   - Or click the folder icon and browse to your user directory
   - Then create a `.gradle` folder inside

3. Under **"Use Gradle from"**, select: **'gradle-wrapper.properties' file**

4. Click **Apply** → **OK**

### Step 3: Invalidate Caches and Restart

1. Click **File** → **Invalidate Caches**
2. Check **"Clear file system cache and Local History"**
3. Check **"Clear downloaded shared indexes"**
4. Click **Invalidate and Restart**

### Step 4: Sync Project

After Android Studio restarts:

1. Click **File** → **Sync Project with Gradle Files**
2. Or click the 🐘 (elephant) icon in the toolbar
3. Wait for sync to complete (may take 1-2 minutes first time)

---

## 🎯 Alternative: Manual Configuration File Edit

If the above doesn't work, close Android Studio and edit this file:

**File:** `.idea/gradle.xml`

Change:
```xml
<option name="gradleJvm" value="#GRADLE_LOCAL_JAVA_HOME" />
```

To:
```xml
<option name="gradleJvm" value="jbr-17" />
```

Then reopen Android Studio.

---

## 🚀 Quick Test

After configuration, try to build in Android Studio:

1. Click **Build** → **Make Project** (or press `Ctrl + F9`)
2. Should see: "BUILD SUCCESSFUL"

---

## ⚠️ If Still Not Working

### Option A: Set System Environment Variable
1. Press `Win + R`, type `sysdm.cpl`, press Enter
2. Click **"Advanced"** tab → **"Environment Variables"**
3. Under **"User variables"**, click **"New"**
4. Variable name: `GRADLE_USER_HOME`
5. Variable value: `C:\Users\Akram\.gradle`
6. Click **OK** → Restart Android Studio

### Option B: Run as Administrator (Not Recommended)
1. Right-click Android Studio shortcut
2. **Properties** → **Compatibility** tab
3. Check **"Run this program as an administrator"**
4. Click **Apply** → **OK**

⚠️ **Security Warning:** Running as admin is not recommended. Use Option A instead.

---

## ✅ Verification

After configuration, check if it works:

1. Close all Android Studio windows
2. Reopen Android Studio
3. Open project: `d:\projects\freshly\android-studio`
4. Wait for Gradle sync
5. Check status bar at bottom - should show "Gradle sync finished"

---

## 💡 Why This Happens

Android Studio by default tries to cache Gradle distributions in its installation directory:
- `C:\Program Files\Android\Android Studio\jbr\wrapper\dists`

This requires admin permissions because it's in Program Files.

The fix redirects it to your user directory:
- `C:\Users\Akram\.gradle`

This directory is writable without admin permissions.

---

## 📝 Summary

**Best Solution:**
1. Settings → Gradle → Set user home to `C:\Users\Akram\.gradle`
2. Use wrapper from 'gradle-wrapper.properties'
3. Invalidate caches and restart
4. Sync project

**Your app works from terminal** because the scripts already set `GRADLE_USER_HOME` correctly!

Android Studio just needs to be told to use the same location.
