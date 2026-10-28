# Build MobileTaPlay (pure Android) → dist/MobileTaPlay.apk → adb install
# Usage: powershell -File scripts/build-android.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$android = Join-Path $root "android"

$env:ANDROID_HOME = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { "C:\Users\Administrator\AppData\Local\Android\Sdk" }
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Users\Administrator\jdk-21\jdk-21.0.7+6" }
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "==> gradle assembleDebug"
Push-Location $android
& ".\gradlew.bat" assembleDebug --no-daemon
$code = $LASTEXITCODE
Pop-Location
if ($code -ne 0) { throw "gradle failed" }

$apkSrc = Join-Path $android "app\build\outputs\apk\debug\app-debug.apk"
$dist = Join-Path $root "dist"
New-Item -ItemType Directory -Path $dist -Force | Out-Null
$apkOut = Join-Path $dist "MobileTaPlay.apk"
Copy-Item $apkSrc $apkOut -Force
Write-Host "==> $apkOut"

Write-Host "==> adb install"
$adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
if (Test-Path $adb) {
  & $adb devices
  & $adb install -r $apkOut
  if ($LASTEXITCODE -eq 0) { Write-Host "==> installed OK" }
  else { Write-Host "==> install failed" }
} else {
  Write-Host "==> adb not found"
}
