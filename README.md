# 塞壬唱片 · Siren Player

[![GitHub release](https://img.shields.io/github/v/release/GoldenYouth01/Arknights-Android_Music_Player?label=Release&color=A0EDFF)](https://github.com/GoldenYouth01/Arknights-Android_Music_Player/releases/latest)
[![GitHub stars](https://img.shields.io/github/stars/GoldenYouth01/Arknights-Android_Music_Player?style=social)](https://github.com/GoldenYouth01/Arknights-Android_Music_Player/stargazers)
[![License](https://img.shields.io/github/license/GoldenYouth01/Arknights-Android_Music_Player?color=blue)](LICENSE)

作者听夏活音乐听上瘾了，现有的塞壬唱片还是个网页，不太方便，于是做了一个便携式 Android 音乐 App，音乐源为 **Monster Siren Records（塞壬唱片）**。

## 下载

▶️ **[下载最新版 APK](https://github.com/GoldenYouth01/Arknights-Android_Music_Player/releases/latest/download/app-release.apk)**

- 版本 `v0.1.0` · 约 13 MB · 正式签名版，可直接安装
- 历史版本见 [Releases 页面](https://github.com/GoldenYouth01/Arknights-Android_Music_Player/releases)
- 安装提示：Android 手机下载后直接打开 APK 安装；如提示"未知来源"，在设置中允许本次安装即可

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

## Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=GoldenYouth01/Arknights-Android_Music_Player&type=Date)](https://star-history.com/#GoldenYouth01/Arknights-Android_Music_Player&Date)
