# MobileTaPlay

**第三方跨平台魔塔客户端** — 基于 [HTML5 魔塔广场](https://h5mota.com/)（h5mota）在线内容，主打 **手柄 + 触屏** 操作。

> 纯第三方项目，与 h5mota 官方、mota-js 作者无隶属关系。  
> 游戏内容版权归各塔作者；引擎 [mota-js](https://github.com/ckcz123/mota-js) 为 BSD-3-Clause。

## 特性（规划）

- 在线游玩广场 2000+ 塔（不打包游戏内容）
- 手柄映射 → 引擎键盘语义（不破坏录像/排行）
- 触屏优化（安全区 / 可选虚拟键）
- 无账号要求，打开即玩
- 平台优先级：**Android → iOS → Windows → macOS/Linux**

## 技术选型

| 层 | 选型 |
|---|---|
| 移动壳 | **Capacitor v8**（System WebView） |
| 输入桥 | TypeScript `packages/input-bridge` |
| 映射表 | `packages/input-mapping`（可校验、可测） |
| 桌面（后续） | 评估 Tauri 2，复用上述 packages |
| 站点内容 | `https://h5mota.com`（远程加载） |

设计文档见 [`docs/`](./docs)：

1. [需求分析](./docs/01-需求分析.md)
2. [壳框架调研](./docs/02-壳框架调研.md)
3. [Capacitor / Tauri 成熟度](./docs/03-Capacitor-Tauri成熟度.md)
4. [架构设计](./docs/04-架构设计.md)

## 仓库结构

```
packages/input-mapping   # 手柄映射 schema / 默认表 / 校验
packages/input-bridge    # Gamepad/虚拟键 → KeyboardEvent
packages/shell-common    # 常量、设置、域白名单
apps/mobile              # Capacitor 应用（Android/iOS）
docs/                    # 需求与架构
```

## 开发

要求：Node ≥ 22，pnpm 9，Android Studio（M1+），Xcode（iOS 阶段）。

```bash
pnpm install
pnpm test
pnpm lint

# 移动壳
pnpm --filter @mobiletaplay/mobile build
pnpm --filter @mobiletaplay/mobile sync
# 然后用 Android Studio / Xcode 打开 apps/mobile 下的原生工程
```

## 贡献

- 分支：`feat/*`、`fix/*`；PR 模板必填
- CI：lint + unit tests
- Issue：Bug / 功能 / 站点·输入 三类模板

## 许可

MIT（见 [LICENSE](./LICENSE)）。使用在线游戏内容须遵守站点与各塔作者条款；本客户端不重新分发游戏资源。
