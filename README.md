# MobileTaPlay

**第三方 Android 魔塔客户端** — 加载 [HTML5 魔塔广场](https://h5mota.com/)（h5mota）在线塔，主打 **手柄 + 触屏**。

> 纯第三方项目，与 h5mota 官方、mota-js 作者无隶属关系。  
> 游戏内容版权归各塔作者；引擎 [mota-js](https://github.com/ckcz123/mota-js) 为 BSD-3-Clause。  
> 隐私与完整声明见 [docs/10-隐私与致谢.md](./docs/10-隐私与致谢.md)。

## 特性

- 在线浏览 / 游玩广场塔（不打包游戏内容）
- Jetpack Compose 壳层：首页 · 详情 · 搜索 · 列表（最近 / 收藏 / 缓存）
- 手柄映射（可在设置里改绑），虚拟键盘
- 离线缓存：详情页缓存后本地可玩
- 无账号，打开即用

当前仅 **Android**（minSdk 26）。iOS / 桌面不在本期范围。

## 下载与安装

- 预编译包：[GitHub Releases](https://github.com/Big-True/MobileTaPlay/releases)（`MobileTaPlay.apk`）
- 或本地构建后安装：

```powershell
# 需要 JDK 21 与 Android SDK
powershell -File scripts/build-android.ps1
adb install -r dist/MobileTaPlay.apk
```

## 开发

| 项 | 要求 |
|---|---|
| JDK | 21 |
| Android SDK | compileSdk 35 |
| 工程 | `android/`（Kotlin + Compose） |

```powershell
cd android
.\gradlew :app:testDebugUnitTest
.\gradlew :app:assembleDebug
```

CI（GitHub Actions）：单元测试 + `assembleDebug`。

## 仓库结构

```
android/     # 原生 Android 工程（Kotlin + Compose）
docs/        # 需求、架构、手柄实践、隐私与致谢
scripts/     # 构建脚本
dist/        # 本地产物 MobileTaPlay.apk
```

## 应用内关于

设置 → **关于**：版本号、第三方声明、隐私摘要与致谢（游戏内点 ⚙ 亦可进入）。

## 贡献

- 分支：`feat/*`、`fix/*`
- Issue：Bug / 功能模板
- 简单发行流程：变更说明 + Tag `vX.Y.Z` + Release 附 APK

## 许可

MIT（见 [LICENSE](./LICENSE)）。使用在线游戏内容须遵守站点与各塔作者条款；本客户端不重新分发游戏资源。
