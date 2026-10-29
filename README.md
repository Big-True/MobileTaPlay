# MobileTaPlay

**第三方 Android 魔塔客户端** — 加载 [HTML5 魔塔广场](https://h5mota.com/)（h5mota）在线塔，主打 **手柄 + 触屏**。

**当前版本：`0.2.3`** · [下载 APK](https://github.com/Big-True/MobileTaPlay/releases/latest)

> 纯第三方项目，与 h5mota 官方、mota-js 作者无隶属关系。  
> 游戏内容版权归各塔作者；引擎 [mota-js](https://github.com/ckcz123/mota-js) 为 BSD-3-Clause。  
> 隐私与声明见 [docs/10-隐私与致谢.md](./docs/10-隐私与致谢.md)。

## 特性

**内容**
- 在线浏览 / 游玩广场塔（不打包游戏内容）
- 信息流瀑布流首页 · 详情（评分/评论/点赞/标签/作者/说明）
- 搜索 · 最近 / 星标收藏 / 离线缓存列表
- 卡片列数可设 2 / 3 / 4 列
- 无账号，打开即用

**手柄**
- 壳层：十字移焦点 · A 确认 · B 返回 · Y/Start 菜单 · L1/R1 切图
- 游戏内映射可改绑 · 虚拟键盘 · Select 三态（窗口 / 全屏 / 顶栏焦点）

**平台**
- Android minSdk 26 · Kotlin + Jetpack Compose
- 掌机双屏 / 横竖屏适配

## 下载与安装

1. [Releases](https://github.com/Big-True/MobileTaPlay/releases/latest) 下载 `MobileTaPlay.apk`
2. 允许「未知来源」后安装

```powershell
cd android
.\gradlew assembleDebug
copy app\build\outputs\apk\debug\app-debug.apk ..\dist\MobileTaPlay.apk
adb install -r ..\dist\MobileTaPlay.apk
```

## 手柄按键

**壳层（列表 / 详情 / 设置）**

| 键 | 作用 |
|---|---|
| 十字 / 摇杆 | 移动焦点 |
| A | 确认 |
| B | 返回 |
| Y / Start | 侧栏菜单 |
| L1 / R1 | 详情截图 |

**游戏内**

| 键 | 作用 |
|---|---|
| Select | 窗口 → 全屏 → 顶栏焦点 → 循环 |
| Start | 快速读档 |
| 其余 | 可在设置中改绑 |

## 开发

| 项 | 要求 |
|---|---|
| JDK | 21 |
| Android SDK | compileSdk 35 |
| 工程 | `android/` |

```powershell
cd android
.\gradlew :app:testDebugUnitTest
.\gradlew :app:assembleDebug
```

CI：单测 + `assembleDebug`；Tag / 手动 **Release** 工作流上传 APK。  
**约定**：功能提交递增 `versionName` / `versionCode`。

## 仓库结构

```
android/   # Kotlin + Compose
docs/      # 需求 / 架构 / 手柄 / 隐私致谢
scripts/   # 构建脚本
dist/      # 本地产物 MobileTaPlay.apk
```

## 关于

设置 → **关于**：版本、第三方声明、隐私摘要与致谢。

## 许可

MIT（见 [LICENSE](./LICENSE)）。请遵守站点与各塔作者条款；不重新分发游戏资源。
