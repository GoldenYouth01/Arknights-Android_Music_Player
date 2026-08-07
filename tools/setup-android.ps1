# 塞壬唱片 Android 音乐 App —— 开发环境搭建脚本（Windows 11 / PowerShell 5.1）
# 幂等：可重复执行；每步用 Test-Path 守卫。
# 用法：powershell -ExecutionPolicy Bypass -File tools\setup-android.ps1

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$dl       = "$env:USERPROFILE\Downloads"
$sdkRoot  = 'D:\AndroidDev\AndroidSDK'
$jdkRoot  = 'D:\AndroidDev\jdk'
$gradleDir = 'D:\AndroidDev\Gradle'
$gradleVer = '8.14.3'
$project  = 'D:\VSCWorkshop\Andriod_learn\music_app'

Write-Host '=== 1/6 JDK 17 ==='
if (-not (Test-Path "$jdkRoot\jdk-17*")) {
    $jdkZip = "$dl\jdk17-win.zip"
    Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile $jdkZip -UseBasicParsing
    New-Item -ItemType Directory -Force -Path $jdkRoot | Out-Null
    Expand-Archive -Path $jdkZip -DestinationPath $jdkRoot -Force
}
$jdkHome = (Get-ChildItem $jdkRoot -Directory -Filter 'jdk-17*' | Select-Object -First 1).FullName
[Environment]::SetEnvironmentVariable('JAVA_HOME', $jdkHome, 'User')
[Environment]::SetEnvironmentVariable('Path', ([Environment]::GetEnvironmentVariable('Path','User') + ";$jdkHome\bin"), 'User')
$env:JAVA_HOME = $jdkHome
$env:Path = "$env:Path;$jdkHome\bin"
& "$jdkHome\bin\java" -version
& "$jdkHome\bin\javac" -version

Write-Host '=== 2/6 Android cmdline-tools ==='
if (-not (Test-Path "$sdkRoot\cmdline-tools\latest\bin\sdkmanager.bat")) {
    $content = (Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/repository2-3.xml' -UseBasicParsing).Content
    $m = [regex]::Match($content, 'path="cmdline-tools;latest"[\s\S]*?<url>(commandlinetools-win-[^<]+\.zip)</url>')
    if (-not $m.Success) { throw '未找到 cmdline-tools 最新包' }
    $zip = "$dl\cmdline-tools.zip"
    Invoke-WebRequest -Uri "https://dl.google.com/android/repository/$($m.Groups[1].Value)" -OutFile $zip -UseBasicParsing
    New-Item -ItemType Directory -Force -Path "$sdkRoot\cmdline-tools\tmp" | Out-Null
    Expand-Archive -Path $zip -DestinationPath "$sdkRoot\cmdline-tools\tmp" -Force
    Move-Item "$sdkRoot\cmdline-tools\tmp\cmdline-tools" "$sdkRoot\cmdline-tools\latest"
    Remove-Item "$sdkRoot\cmdline-tools\tmp" -Recurse -Force
}
[Environment]::SetEnvironmentVariable('ANDROID_HOME', $sdkRoot, 'User')
[Environment]::SetEnvironmentVariable('ANDROID_SDK_ROOT', $sdkRoot, 'User')
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot

Write-Host '=== 3/6 sdkmanager 安装与许可 ==='
$sm = "$sdkRoot\cmdline-tools\latest\bin\sdkmanager.bat"
# PS 无 yes 命令：管道喂 'y'
1..40 | ForEach-Object { 'y' } | & $sm --sdk_root=$sdkRoot --licenses
& $sm --sdk_root=$sdkRoot 'platform-tools' 'platforms;android-36' 'build-tools;36.1.0'
& $sm --sdk_root=$sdkRoot --list_installed

Write-Host '=== 4/6 Gradle ==='
if (-not (Test-Path "$gradleDir\gradle-$gradleVer\bin\gradle.bat")) {
    Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$gradleVer-bin.zip" -OutFile "$dl\gradle.zip" -UseBasicParsing
    Expand-Archive -Path "$dl\gradle.zip" -DestinationPath $gradleDir -Force
}
$gradleBin = "$gradleDir\gradle-$gradleVer\bin"
$userPath = [Environment]::GetEnvironmentVariable('Path','User')
if ($userPath -notlike "*$gradleBin*") {
    [Environment]::SetEnvironmentVariable('Path', "$userPath;$gradleBin", 'User')
}
$env:Path = "$env:Path;$gradleBin"

Write-Host '=== 5/6 Gradle wrapper + local.properties ==='
Set-Location $project
if (-not (Test-Path "$project\gradlew.bat")) {
    & "$gradleBin\gradle.bat" wrapper --gradle-version $gradleVer --distribution-type bin
}
Set-Content -Path "$project\local.properties" -Value "sdk.dir=$($sdkRoot -replace '\\','/')" -Encoding utf8

Write-Host '=== 6/6 构建验证 ==='
& "$project\gradlew.bat" :app:assembleDebug

Write-Host '=== 完成 ==='
Write-Host '真机安装：开发者选项开 USB 调试后执行：'
Write-Host "  $sdkRoot\platform-tools\adb.exe install -r $project\app\build\outputs\apk\debug\app-debug.apk"
