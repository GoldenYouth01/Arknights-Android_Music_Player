# 塞壬唱片 · Siren Player

作者听夏活音乐听上瘾了，现有的塞壬唱片还是个网页，不太方便，于是做了一个便携式 Android 音乐 App，音乐源为 **Monster Siren Records（塞壬唱片）**。

## 功能

- **专辑浏览**：专辑列表+ 专辑详情，支持**下拉刷新 / 一键刷新**随时获取最新内容
- **音乐播放**：Media3 后台流式播放（WAV 无损）、媒体通知、锁屏控制、全屏播放页、上滑播放队列抽屉
- **搜索**：歌曲走本地索引，专辑走 API；中文 / 英文大小写不敏感
- **离线下载**：WorkManager 后台下载，进度展示、离线播放、删除释放空间

## 技术栈

Kotlin · Jetpack Compose  · Media3 · Retrofit + kotlinx-serialization · Coil 3 · WorkManager · DataStore

## 环境要求

- JDK 17+，Android SDK
- Gradle 8.14.3
## 搭建与运行

```powershell
# 1) 搭建开发环境（JDK17 + SDK + Gradle wrapper），幂等可重复执行
powershell -ExecutionPolicy Bypass -File tools\setup-android.ps1

# 2) 构建
.\gradlew.bat :app:assembleDebug

# 3) 单元测试（DTO 解析契约 + 目录搜索 + 真实 API 连通）
.\gradlew.bat :app:testDebugUnitTest

# 4) 真机安装（开发者选项开 USB 调试）
D:\AndroidDev\AndroidSDK\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

## 项目结构

```
app/src/main/java/com/example/musicsiren/
  data/       远程 API、DownloadStore、Repository、SongCatalog（随时刷新索引）
  domain/     领域模型（Album / Song / DownloadRecord）
  playback/   Media3 服务 + 全局播放状态
  ui/         主题、组件、屏幕、导航
  work/       下载 Worker
```

## 版本发布

### 版本号规则（严格管理）

- **语义化版本**：`MAJOR.MINOR.PATCH`（如 `0.1.0`）。`versionName` 即展示给用户的版本号
- **versionCode**：单调递增的整数，Android 系统据此判断是否为"更新"——**每次发布必须比上一次大**
- **唯一来源**：两个版本号都集中在 `gradle.properties` 的 `VERSION_CODE` / `VERSION_NAME`，不要手改 `app/build.gradle.kts`
- **Tag 约定**：每次发布对应一个 `v{versionName}` 的 git tag

### 发布步骤（一条命令）

```powershell
# 前置：gh 已登录
gh auth login

# 自动 patch+1（0.1.0 → 0.1.1，versionCode+1）并发布
powershell -ExecutionPolicy Bypass -File tools\release.ps1

# 或指定版本
powershell -ExecutionPolicy Bypass -File tools\release.ps1 -VersionName 0.2.0 -Notes "新增 XX 功能"
```

脚本会依次完成：改版本号 → 构建 release APK → 提交 → 打 tag → 推送 → 创建 GitHub Release 并上传 APK。

### 签名密钥（重要）

Release APK 使用专用 keystore 签名，密钥文件在 **仓库外**（`D:\AndroidDev\keystores\`），签名信息存于 `keystore.properties`（已 gitignore）。

> ⚠️ **请妥善备份 keystore 与密码**：丢失后将无法再发布可覆盖更新的新版本（Android 要求同一签名才能升级）。他人 clone 仓库时无此文件，release 构建将不打签名（仅用于调试，不可安装到已装过正式版的设备）。
