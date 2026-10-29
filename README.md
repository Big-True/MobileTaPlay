# MobileTaPlay

**第三方 Android 魔塔客户端** — 加载 [HTML5 魔塔广场](https://h5mota.com/)（h5mota）在线塔，主打 **手柄 + 触屏**。

**当前版本：`0.2.5`** · [下载 APK](https://github.com/Big-True/MobileTaPlay/releases/latest)

> 纯第三方项目，与 h5mota 官方、mota-js 作者无隶属关系。  
> 游戏内容版权归各塔作者；引擎 [mota-js](https://github.com/ckcz123/mota-js) 为 BSD-3-Clause。  
> 隐私与声明见 [docs/10-隐私与致谢.md](./docs/10-隐私与致谢.md)。

## 特性

**内容**
- 瀑布流首页 · **筛选面板**（搜索 / 排序 / 大类 / 标签 / 颜色）
- 详情：统计、标签、作者、简介、收藏
- 评论页 · 通关信息页 · 作者作品搜索
- 最近 / 星标收藏 / 离线缓存 · 卡片列数 2/3/4

**手柄**
- 壳层纯手柄：焦点环、筛选面板、评论/帮助滚动
- 游戏内映射可改绑 · 虚拟键盘
- Select 三态：窗口 → 全屏 → 顶栏焦点
- 首页「?」帮助：按键 → 操作 速查

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

## 手柄（摘要）

| 场景 | 按键 | 操作 |
|---|---|---|
| 通用 | 十字 / A / B | 移动焦点 / 确认 / 返回 |
| 筛选 | B | 先退出搜索，再收起筛选 |
| 详情 | L1 / R1 | 截图切换 |
| 游戏 | Select | 窗口 ↔ 全屏 ↔ 顶栏焦点 |
| 游戏 | 其余 | 设置·手柄映射 |

完整说明见 App 内首页 **? 帮助**。

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

**约定**：功能提交递增 `versionName` / `versionCode`。

## 仓库结构

```
android/   # Kotlin + Compose
docs/      # 需求 / 架构 / 手柄 / 隐私致谢
scripts/   # 构建脚本
dist/      # 本地产物 MobileTaPlay.apk
```

## 许可

MIT（见 [LICENSE](./LICENSE)）。请遵守站点与各塔作者条款；不重新分发游戏资源。
