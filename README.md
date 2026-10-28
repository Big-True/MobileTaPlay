# MobileTaPlay

**第三方 Android 魔塔客户端** — 加载 [HTML5 魔塔广场](https://h5mota.com/)（h5mota）在线塔，主打 **手柄 + 触屏**。

**当前版本：`0.2.2`** · [下载 APK](https://github.com/Big-True/MobileTaPlay/releases/latest)

> 纯第三方项目，与 h5mota 官方、mota-js 作者无隶属关系。  
> 游戏内容版权归各塔作者；引擎 [mota-js](https://github.com/ckcz123/mota-js) 为 BSD-3-Clause。  
> 隐私与完整声明见 [docs/10-隐私与致谢.md](./docs/10-隐私与致谢.md)。

## 特性

- 在线浏览 / 游玩广场塔（不打包游戏内容）
- **纯手柄操作闭环**：十字移焦点 · A 确认 · B 返回 · Y/Start 菜单 · L/R 切图  
  焦点描边 / 抽屉陷阱 / Chip→卡片 / 关于页滚动均可用手柄完成
- 游戏内手柄映射（可改绑）· 虚拟键盘 · 离线缓存本地可玩
- Jetpack Compose 壳层：首页 · 详情 · 搜索 · 列表（最近 / 收藏 / 缓存）
- 无账号，打开即用

当前仅 **Android**（minSdk 26）。

## 下载与安装

1. 打开 [Releases](https://github.com/Big-True/MobileTaPlay/releases/latest) 下载 `MobileTaPlay.apk`
2. 允许「未知来源」后安装

或本地构建：

```powershell
# JDK 21 + Android SDK
cd android
.\gradlew assembleDebug
copy app\build\outputs\apk\debug\app-debug.apk ..\dist\MobileTaPlay.apk
adb install -r ..\dist\MobileTaPlay.apk
```

## 手柄按键（壳层）

| 键 | 作用 |
|---|---|
| 十字 / 摇杆 | 移动焦点 |
| A | 确认 |
| B | 返回 |
| Y / Start | 首页侧栏菜单 |
| L1 / R1 | 详情截图切换 |

游戏内映射在 **设置** 中修改。

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

CI：单元测试 + `assembleDebug`；打 Tag / 手动 **Release** 工作流可上传 APK。

**约定**：功能提交请递增 `versionName` / `versionCode`（`android/app/build.gradle.kts`）。

## 仓库结构

```
android/     # 原生 Android（Kotlin + Compose）
docs/        # 需求、架构、手柄实践、隐私与致谢
scripts/     # 构建脚本
dist/        # 本地产物 MobileTaPlay.apk
```

## 应用内关于

设置 → **关于**：版本、第三方声明、隐私摘要与致谢（游戏内 ⚙ 亦可进入）。

## 贡献

- 分支：`feat/*`、`fix/*`
- Issue：Bug / 功能模板
- 发行：变更说明 + Tag `vX.Y.Z` + Release 附 APK

## 许可

MIT（见 [LICENSE](./LICENSE)）。使用在线游戏内容须遵守站点与各塔作者条款；本客户端不重新分发游戏资源。
