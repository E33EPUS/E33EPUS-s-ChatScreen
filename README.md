# E33Chat

*以聊天APP风格重铸原版聊天框 / Rebuilds the vanilla chat HUD in chat-app style*

**客户端需装，服务端可选 / Client required, server optional**

| | Forge | NeoForge |
|---|---|---|
| 1.20.1 | ✅ | — |
| 1.21.1 | — | ✅ |

## 原版优化 / Vanilla Improvements

- 💬 **聊天气泡** — 消息带气泡和玩家头像 / Messages as bubbles with player heads
- 💾 **聊天记录** — 按存档/服务器保存聊天历史，重进后恢复 / Per-world chat history
- 📝 **输入保留** — 关闭聊天框保留已输入文本 / Input text preserved when closing chat
- 🚫 **防刷屏** — 连续的重复消息自动合并计数 / Anti-spam merging

## 特点 / Highlights

- 👥 **私聊侧边栏** — 在线玩家列表，点玩家头像进行私聊 / Whisper sidebar with online player list
- 🔍 **聊天搜索** — 输入关键词搜索聊天记录 / Search chat history by keyword
- @ **提及补全** — 输入 `@` 弹出在线玩家列表 / Type `@` for autocomplete popup
- 📌 **常用语面板** — 保存/管理常用短语，点击填充 / Save and quick-fill common phrases
- 😊 **表情 & 颜文字** — emoji 面板 + 颜文字面板 / Emoji and kaomoji picker
- 💬 **引用回复** — 右键消息选"引用"，回复时携带上下文 / Right-click to quote reply
- 🔔 **消息预览 & 强提示** — 左下角 HUD 预览；被 @ / 引用 / 系统消息弹窗提示 / HUD preview and popup hints for @mentions, quotes, and system messages
- 🔊 **提示音** — 系统消息 / @ / 引用 / 私聊 / 公屏，独立配置 / Notification sounds with per-type toggles
- 🎨 **主题 & 自定义** — 深色/浅色主题，自定义气泡颜色、文字颜色、圆角半径 / Dark/light theme with customizable bubble and text colors
- 🕐 **时间分隔线** — 按时间间隔显示分隔标签 / Time separator labels at configurable intervals

## 兼容性 / Compatibility

**服务器装了 禁用聊天举报 插件？** 记得在配置里开启兼容选项！

## FAQ

**服务器需要装吗？** 可不安装，但安装后激活 引用 / @ 功能
**Server required?** No, but installing the server-side component enables quote reply and @mention sync.

**怎么打开配置？** 聊天面板左下角齿轮 → 菜单 → 设置。配置文件在 `\config\chatbubble-client.toml` 
**How to configure?** Gear icon (bottom-left) → Menu → Settings, or edit `\config\chatbubble-client.toml`.

**聊天历史保存在哪？** 在 `.minecraft\e33chat\history`
**Where is chat history stored?** `.minecraft\e33chat\history`

**可以放进整合包吗？** 可以
**Modpack?** Go ahead.

**如何反馈问题？** [反馈问题](https://github.com/E33EPUS/E33EPUS-s-ChatScreen/issues)
**Found a bug?** [Report it here](https://github.com/E33EPUS/E33EPUS-s-ChatScreen/issues)
