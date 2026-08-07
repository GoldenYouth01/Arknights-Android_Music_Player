# 塞壬唱片 Siren Player —— 版本发布脚本
# 统一处理：版本号修改（gradle.properties 唯一来源）→ 构建 release APK → 提交 → 打 tag → 推送 → 创建 GitHub Release 并上传 APK
#
# 用法（在项目根目录）：
#   powershell -ExecutionPolicy Bypass -File tools\release.ps1                     # 自动 patch+1（0.1.0 → 0.1.1），versionCode+1
#   powershell -ExecutionPolicy Bypass -File tools\release.ps1 -VersionName 0.2.0  # 指定版本名，versionCode 自动 +1
#   powershell -ExecutionPolicy Bypass -File tools\release.ps1 -VersionName 1.0.0 -VersionCode 10 -Notes "正式版"
#
# 前置要求：gh 已登录（gh auth status 应为 Logged in）

param(
    [string]$VersionName,     # 目标版本名，如 "0.2.0"；不填则自动 patch + 1
    [int]$VersionCode = 0,    # 目标 versionCode；不填则自动 +1（Android 升级要求严格递增）
    [switch]$SkipBuild,       # 跳过构建（仅当 release APK 已构建好时使用）
    [string]$Notes            # Release 备注；不填则用 --generate-notes 自动从提交生成
)

$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
Set-Location $project

# 用 .NET API 按 UTF-8 读写，避免 PowerShell 默认编码把中文注释写坏
function Get-Prop([string]$key) {
    $path = Join-Path $project "gradle.properties"
    $content = [System.IO.File]::ReadAllText($path)
    $m = [regex]::Match($content, "(?m)^$key=(.+)$")
    if (-not $m.Success) { throw "gradle.properties 缺少 $key" }
    $m.Groups[1].Value.Trim()
}
function Set-Prop([string]$key, [string]$value) {
    $path = Join-Path $project "gradle.properties"
    $content = [System.IO.File]::ReadAllText($path)
    $content = [regex]::Replace($content, "(?m)^$key=.*$", "$key=$value")
    [System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding($false)))
}

$oldName = Get-Prop 'VERSION_NAME'
$oldCode = [int](Get-Prop 'VERSION_CODE')

if ($VersionName) {
    if ($VersionName -notmatch '^\d+\.\d+\.\d+$') { throw "版本名需为语义化格式 x.y.z，例如 0.2.0" }
    $newName = $VersionName
} else {
    $parts = $oldName.Split('.')
    $parts[2] = [string]([int]$parts[2] + 1)
    $newName = $parts -join '.'
}
$newCode = if ($VersionCode -gt 0) { $VersionCode } else { $oldCode + 1 }
if ($newCode -le $oldCode) { throw "versionCode 必须大于当前 $oldCode（Android 更新机制要求严格递增）" }

Write-Host "发布 v$newName (versionCode $newCode)，当前 v$oldName / $oldCode"

# 1) 改版本（唯一来源 gradle.properties）
Set-Prop 'VERSION_NAME' $newName
Set-Prop 'VERSION_CODE' $newCode

# 2) 构建 release APK
if (-not $SkipBuild) {
    Write-Host '构建 assembleRelease ...'
    & .\gradlew.bat :app:assembleRelease --console=plain
    if ($LASTEXITCODE -ne 0) { throw '构建失败' }
}
$apk = "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apk)) { throw "未找到 APK: $apk" }

# 3) 提交版本号变更
git add gradle.properties
git commit -m "Release v$newName (versionCode $newCode)"

# 4) 打 tag 并推送（commit + tag）
git tag -a "v$newName" -m "v$newName"
git push origin main
git push origin "v$newName"

# 5) 创建 GitHub Release 并上传 APK
$args = @('release', 'create', "v$newName", $apk, '--title', "v$newName")
if ($Notes) {
    $args += @('--notes', $Notes)
} else {
    $args += @('--generate-notes')
}
& gh @args
if ($LASTEXITCODE -ne 0) { throw 'gh release create 失败' }

Write-Host ''
Write-Host "已发布: https://github.com/GoldenYouth01/Arknights-Android_Music_Player/releases/tag/v$newName"
