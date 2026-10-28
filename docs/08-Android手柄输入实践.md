# Android 手柄按键优先级 — 官方实践调研

来源：Android Developers《Handle controller actions》  
https://developer.android.com/training/game-controllers/controller-input

## 官方要点

1. **`Activity.dispatchKeyEvent` 对所有 `KeyEvent` 首先触发** — 要覆盖焦点导航，必须在这里 `return true` 截获。
2. **焦点 View** 才收到 `onKeyDown` / `onKeyUp`；D-pad 默认用于 **移动焦点**，会被焦点系统消费。
3. 官方示例：`event.isFromSource(InputDevice.SOURCE_GAMEPAD) && event.repeatCount == 0` 后自定义处理并 `return true`。
4. 兼容层：系统可能把新/冷门手柄映射到标准 `KEYCODE_BUTTON_*` / `KEYCODE_DPAD_*`。

## 对 MobileTaPlay 的结论

| 问题 | 官方做法 |
|---|---|
| 绑定要按两次 | 第一次键被 **焦点/点击** 消费；须在 `dispatchKeyEvent`/`onKeyDown` 对 `SOURCE_GAMEPAD` **无条件截获** |
| D-pad 变成导航 | 绑定态不进入焦点搜索；非绑定态才允许 D-pad 移焦 |
| A 变成“点击行” | 绑定态吞掉 `BUTTON_A`/`ENTER`，避免再触发 Card.onClick 取消绑定 |

## 实现原则

```kotlin
if (binding && event.isFromSource(SOURCE_GAMEPAD)) {
  bind(event.keyCode); return true
}
```

列表 Card：绑定中不再 toggle 关闭（取消只通过再次点触屏或超时）。
