# 塞壬唱片 · Siren Player

一个便携式 Android 音乐 App，音乐源为 **Monster Siren Records（塞壬唱片，鹰角网络官方音乐厂牌）**，
界面风格复刻其官网 `https://monster-siren.hypergryph.com/music`（近黑背景 + 青蓝辉光 + 竖向专辑行列表）。

## 功能

- **专辑浏览**：专辑列表（最新在前）+ 专辑详情（歌曲列表），支持**下拉刷新 / 一键刷新**随时获取最新内容
- **音乐播放**：Media3 后台流式播放（WAV 无损，支持拖动 seek）、媒体通知、锁屏控制、全屏播放页（模糊封面背景）、上滑播放队列抽屉
- **搜索**：歌曲走本地索引（可刷新目录），专辑走 API；中文 / 英文大小写不敏感
- **离线下载**：WorkManager 后台下载（每首约 54MB），进度展示、离线播放、删除释放空间

## 技术栈

Kotlin · Jetpack Compose (Material3 暗黑主题) · Media3 (ExoPlayer) · Retrofit + kotlinx-serialization · Coil 3 · WorkManager · DataStore（免 Room/KSP）

## 环境要求

- JDK 17+，Android SDK（`compileSdk 36 / targetSdk 36 / minSdk 26`）
- Gradle 8.14.3（项目内 wrapper）

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

## 数据来源（公开 API，无需鉴权）

- `GET /api/albums` —— 全量专辑列表
- `GET /api/album/{cid}/detail` —— 专辑详情 + 歌曲（不含音频 URL）
- `GET /api/song/{cid}` —— 歌曲音频 URL（`sourceUrl`）
- `GET /api/songs` —— 全量歌曲目录（本地搜索索引）
- `GET /api/search?keyword=X` —— 专辑搜索

注意：音频为未压缩 PCM WAV（48kHz/24bit，约 2.3Mbps，一首 3 分钟约 54MB），播放采用流式（Range/seek），下载会占用较大存储。

## 项目结构

```
app/src/main/java/com/example/musicsiren/
  data/       远程 API、DownloadStore、Repository、SongCatalog（随时刷新索引）
  domain/     领域模型（Album / Song / DownloadRecord）
  playback/   Media3 服务 + 全局播放状态
  ui/         主题、组件、屏幕、导航
  work/       下载 Worker
```
