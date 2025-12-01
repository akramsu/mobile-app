# Fix Android Studio Gradle Settings
# Run this script to configure Android Studio to use your user directory

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Freshly Android - Gradle Settings Fix" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

$gradleUserHome = "$env:USERPROFILE\.gradle"
Write-Host "Setting Gradle user home to: $gradleUserHome" -ForegroundColor Green
Write-Host ""

# Create the directory if it doesn't exist
if (!(Test-Path $gradleUserHome)) {
    New-Item -ItemType Directory -Path $gradleUserHome -Force | Out-Null
    Write-Host "Created Gradle directory: $gradleUserHome" -ForegroundColor Green
}

# Create gradle.properties file
$gradlePropertiesPath = "$gradleUserHome\gradle.properties"
$gradlePropertiesContent = @"
# Gradle User Home Configuration
# This fixes the Android Studio permission error
org.gradle.user.home=$($gradleUserHome -replace '\\', '\\')
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
"@

Set-Content -Path $gradlePropertiesPath -Value $gradlePropertiesContent -Force
Write-Host "Created: $gradlePropertiesPath" -ForegroundColor Green
Write-Host ""

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Configuration Complete!" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Close Android Studio if it's open" -ForegroundColor White
Write-Host "2. Reopen Android Studio" -ForegroundColor White
Write-Host "3. Go to: File → Settings → Build, Execution, Deployment → Gradle" -ForegroundColor White
Write-Host "4. Set 'Gradle user home' to: $gradleUserHome" -ForegroundColor White
Write-Host "5. Click Apply and OK" -ForegroundColor White
Write-Host "6. File → Invalidate Caches → Invalidate and Restart" -ForegroundColor White
Write-Host ""
Write-Host "OR just use the build.bat script - it works without Android Studio!" -ForegroundColor Cyan
Write-Host ""

Read-Host "Press Enter to exit"
