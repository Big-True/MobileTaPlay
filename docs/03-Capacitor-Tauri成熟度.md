# Capacitor 与 Tauri 2 成熟度对比

> 产品：MobileTaPlay · 场景：远程 WebView 壳 + Input Bridge  
> 优先级：Android → iOS → Win → macOS/Linux  
> 数据截止：调研时 GitHub Releases / 官方 Support Policy（约 2026-10）

---

## 1. 结论先看

| 维度 | Capacitor | Tauri 2 | 谁更成熟 |
|---|---|---|---|
| **移动（Android/iOS）生产就绪** | ★★★★★ | ★★★★ | **Capacitor** |
| **桌面（Win/mac/Linux）** | ★★ | ★★★★★ | **Tauri** |
| **版本节奏与维护** | 稳定小步，Support Policy 清晰 | 迭代更快，桌面极活跃 | 各有优势 |
| **官方支持周期** | 明文 Maintenance / Extended | 按 major 维护，无同等表 | **Capacitor** |
| **发布/商店路径** | 移动商店文档厚 | Play/App Store 文档齐，桌面打包强 | **Capacitor（移动）** |
| **远程网页壳场景** | 可做，非默认（要自定义） | Brownfield 就是为此设计 | **Tauri** |
| **学习/团队门槛** | TS + 少量 Swift/Kotlin | TS + Rust（可少写） | **Capacitor 略易** |
| **对 MobileTaPlay 的「敢用程度」** | Android/iOS 可直接开工 | 全端可开工，移动要自测 | **看端** |

**一句话：**  
**移动成熟度 Capacitor 更高；桌面成熟度 Tauri 更高。**  
MobileTaPlay 前两端是 Android/iOS → **按成熟度应优先 Capacitor**；若要「一壳打五端」，Tauri 2 桌面极稳、移动已可用但比 Capacitor 更年轻。

---

## 2. 版本与维护现状

### 2.1 Capacitor（ionic-team）

| 项 | 现状 |
|---|---|
| 当前主线 | **v8**（2025-12-08 起 Active） |
| 最新稳定 | v8.5.x（调研时 8.5.3，2026-10） |
| 下一大版本 | v9.0.0-alpha.x 进行中 |
| 支持策略 | **成文 Support Policy**：Active → Maintenance → Extended → EOL |
| v8 线 | Active；v7 Extended Support；v6 及更早 EOL |
| 最低系统 | iOS 15 / Android 7.0（API 24） |
| 工具链要求（v8） | Node 22 · Xcode 26 · Android Studio 2025.2.1 |
| 背书 | Ionic / OutSystems 公司维护 |
| 开源 | MIT，始终开源 |
| Issue 积压 | 约 80+ open（相对少，流程成熟） |

**成熟度含义：**  
移动 Hybrid 从 Cordova 时代算起积累深；v8 仍在 **Active**，企业级 Support Policy、迁移指南（含 UIScene、edge-to-edge 等）齐全。**做 Android/iOS 网页壳，Capacitor 是「已经踩过坑」的那类成熟。**

### 2.2 Tauri（tauri-apps）

| 项 | 现状 |
|---|---|
| 当前主线 | **v2**（已到 2.12.x 补丁线） |
| 最新稳定 | tauri v2.12.5（调研时，2026-10） |
| 下一大版本 | v3.0.0-alpha.x |
| 移动 | **Tauri 2 官方支持 Android / iOS**（Play / App Store 文档齐全） |
| 最低 Android | 7.0（SDK 24），与 Capacitor v8 对齐 |
| 安全 | 主要版本做安全审计（文档公开） |
| 背书 | 独立基金会式社区 + 核心团队，活跃 |
| 开源 | MIT / Apache-2.0 |
| Issue 积压 | 约 1.3k open（范围更广：桌面+移动+插件） |
| 发布节奏 | 补丁很勤（NSIS、打包、依赖安全等） |

**成熟度含义：**  
**桌面（尤其 Win/mac）非常成熟**，可上生产。  
**移动是 v2 正式能力，但生态年龄短于 Capacitor**：Android Studio 工程生成、Play 发布、签名链路已文档化，但是「WebView 游戏 + 外接手柄」这类边缘场景，官方案例少于 Ionic 系，需要自己 spike。

---

## 3. 分场景成熟度（MobileTaPlay 视角）

### 3.1 Android（优先级 #1）

| | Capacitor | Tauri 2 |
|---|---|---|
| 打包 APK/AAB | 极熟 | 已支持（`tauri android build`） |
| System WebView | 官方路径 | 官方路径 |
| 外接手柄 | 需实测 / 原生 KeyEvent | 同左 |
| 生态插件 | 丰富（含 community） | 官方插件 + 社区，偏桌面向 |
| **成熟度** | **更高** | 可用，略新 |

### 3.2 iOS（优先级 #2）

| | Capacitor | Tauri 2 |
|---|---|---|
| WKWebView 壳 | 十年+ 混合栈经验 | Tauri 2 移动路径 |
| SPM / Xcode 新版本跟进 | 快（UIScene、SPM 迁移已有） | 跟进中 |
| GameController 手柄 | 要写原生插件 | 要写 Rust/移动插件 |
| 商店 / 隐私清单 | 文档厚（Privacy Manifest） | 有 App Store 文档 |
| **成熟度** | **更高** | 可用，坑略少人踩过 |

### 3.3 Windows（优先级 #3）

| | Capacitor | Tauri 2 |
|---|---|---|
| 桌面 WebView | 不是主战场 | WebView2，一等公民 |
| 打包/安装器 | 弱 | NSIS/MSI 成熟 |
| Gamepad | 依赖系统 WebView | 同 WebView2，行为接近 Chrome |
| **成熟度** | 低 | **高** |

### 3.4 macOS / Linux（优先级 #4）

| | Capacitor | Tauri 2 |
|---|---|---|
| macOS | 弱 | 成熟（签名/公证文档全） |
| Linux | 弱 | 成熟但 WebKitGTK 有边缘问题（官方有 Linux Graphics 排障页） |
| **成熟度** | 低 | **高（Linux 需抽测）** |

---

## 4. 「网页壳 + 远程站点」成熟度专项

MobileTaPlay **不打包本地 www，而是加载 h5mota.com 并注入 Input Bridge**：

| 能力 | Capacitor | Tauri 2 |
|---|---|---|
| 默认场景 | 本地 Web 资源 | 本地资源；**Brownfield 兼容已有前端/远程** |
| 打开远程 URL | 可行，需配置/小插件 | 可行，文档有 Brownfield |
| 初始化脚本注入 | `WKUserScript` / `evaluateJavascript` 自封装 | `initialization_script` 等更直接 |
| CSP / 外域 | 要自己处理 | capabilities + CSP 可控 |
| **场景匹配成熟度** | 中 | **较高** |

→ 若只论「包一个远程魔塔站」，**Tauri 的产品定位更贴**；  
→ 若只论「Android/iOS WebView App 量产」，**Capacitor 更贴**。

---

## 5. 风险成熟度（踩坑密度）

| 风险类型 | Capacitor | Tauri 2 |
|---|---|---|
| 升级大版本 | 有完整 Migrate 指南，痛苦可控 | 1→2 迁移已完成一轮；2→3 在 alpha |
| 商店审核（移动） | 路径成熟 | 能上，案例相对少 |
| WebView 行为差异 | 碎片已知，Ionic 社区问答多 | 碎片同等存在，问答偏桌面 |
| 依赖/供应链 | npm + 原生 SDK | Rust crates + npm；有 cargo audit |
| 长期维护承诺 | 公司 + 成文支持期 | 社区核心团队，无同等 SLA 表 |

---

## 6. 对 MobileTaPlay 的选型含义

### 不变的部分
- **Input Bridge / mapping.json 仍是共享 TS**，与壳成熟度无关；
- 手柄在 Android/iOS 的真实表现 **两家都必须 spike**，成熟度差在「有没有人帮你踩过坑」。

### 会变的部分

| 策略 | 何时合理 |
|---|---|
| **A. Capacitor 先手机** | 要 Android/iOS **尽快稳定量产**（当前优先级下更稳） |
| **B. Tauri 2 全端** | 要 **单一工程**，且接受移动稍新、团队能碰 Rust |
| **C. Capacitor 移动 + Tauri 桌面** | 兼顾「移动成熟度」和「桌面成熟度」，工程两份 |

**成熟度投票：**  
- 只看 Android→iOS：**Capacitor 胜**  
- 只看 Win→mac/Linux：**Tauri 2 胜**  
- 看全优先级加权：**仍略偏 Capacitor 做前两端，Tauri 后补桌面**（与 `02-壳框架调研` 主推荐一致）

---

## 7. 版本选用建议（锁框架时）

| | 建议 |
|---|---|
| Capacitor | **锁 v8 稳定线**（Active），暂不跟 v9 alpha |
| Tauri | **锁 v2.12.x 稳定线**，不要 v3 alpha |
| 两者 | CI 固定版本；大版本升级单独立项 |

---

## 8. 数据来源

- https://github.com/ionic-team/capacitor/releases （v8.5.x / v9 alpha）
- https://capacitorjs.com/docs/main/reference/support-policy
- https://github.com/tauri-apps/tauri/releases （tauri v2.12.x / v3 alpha）
- https://v2.tauri.app/distribute/google-play/
- https://v2.tauri.app/ （架构、Brownfield、安全审计）

---

## 9. 一句话

**Capacitor 是「移动 WebView 壳」里的老兵（v8 Active，支持期成文）；Tauri 2 是「桌面 WebView 壳」里的标杆、移动已正式可用但更年轻。MobileTaPlay 若 Android/iOS 优先，按成熟度应先 Capacitor；若坚持一壳五端，Tauri 2 可行，但移动手柄与远程注入务必先做 spike，且锁 v2 稳定线。**
