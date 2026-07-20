# E33Chat

*Rebuilds the vanilla chat HUD in chat-app style*

**Client required, server optional**

| | Forge | NeoForge |
|---|---|---|
| 1.20.1 | ✅ | — |
| 1.21.1 | — | ✅ |

## Vanilla Improvements

- 💬 **Chat bubbles** — Messages with player heads and names
- 💾 **Chat history** — Saved per world / server, restored on rejoin
- 📝 **Input preserved** — Typed text kept when closing chat
- 🚫 **Anti-spam** — Consecutive duplicate messages merged with a counter

## Highlights

- 👥 **Whisper sidebar** — Online player list, click to whisper
- 🔍 **Chat search** — Keyword search with real-time matching
- @ **Mention autocomplete** — Type `@` for a popup player list, or left-click a player head
- 📌 **Quick chat** — Save and quick-fill common phrases
- 😊 **Emoji & kaomoji** — Emoji panel + kaomoji picker
- 📋 **Copy & quote reply** — Right-click a message to copy or quote reply
- 👤 **Player head actions** — Right-click a player head to teleport or whisper
- 🔔 **Preview & hints** — HUD preview at bottom-left; popup hints for @mentions, quotes, and system messages
- 🔊 **Notification sounds** — Per-type toggles for system, @ / quote, whisper, and public messages
- 🎨 **Themes** — Dark/light theme with customizable bubble color, text color, and corner radius
- 🕐 **Time separators** — Timestamp dividers at configurable intervals

## Compatibility

**Server using NCR/FreedomChat?** Enable the compat option in config.

## FAQ

**Server required?** No, but installing the server-side mod enables quote reply, @mention sync, and chat history sync (new players receive recent messages).

**How to configure?** Gear icon (bottom-left) → Menu → Settings, or edit `\config\e33chat-client.toml`. Server config: `\config\e33chat-server.toml`.

**Where is chat history?** `.minecraft\e33chat\history`

**How to disable chat history sync?** Set `history_enabled = false` in `\config\e33chat-server.toml` and restart.

**Modpack?** Go ahead.

**Found a bug?** [Report it here](https://github.com/E33EPUS/E33Chat/issues)

---

# E33Chat

*以聊天APP风格重铸原版聊天框*

**客户端需装，服务端可选**

| | Forge | NeoForge |
|---|---|---|
| 1.20.1 | ✅ | — |
| 1.21.1 | — | ✅ |

## 原版优化

- 💬 **聊天气泡** — 消息带气泡和玩家头像
- 💾 **聊天记录** — 按存档/服务器保存聊天历史，重进后恢复
- 📝 **输入保留** — 关闭聊天框保留已输入文本
- 🚫 **防刷屏** — 连续的重复消息自动合并计数

## 特点

- 👥 **私聊侧边栏** — 在线玩家列表，点玩家头像进行私聊
- 🔍 **聊天搜索** — 输入关键词搜索聊天记录
- @ **提及补全** — 输入 `@` 弹出在线玩家列表，或左键点玩家头像 @ta
- 📌 **常用语面板** — 保存/管理常用短语，点击填充
- 😊 **表情 & 颜文字** — emoji 面板 + 颜文字面板
- 📋 **复制 & 引用回复** — 右键消息 复制 / 引用回复
- 👤 **玩家头像交互** — 右键头像 传送 / 私聊，左键头像 @提及
- 🔔 **消息预览 & 强提示** — 左下角 HUD 预览；被 @ / 引用 / 系统消息弹窗提示
- 🔊 **提示音** — 系统消息 / @ / 引用 / 私聊 / 公屏，独立配置
- 🎨 **主题 & 自定义** — 深色/浅色主题，自定义气泡颜色、文字颜色、圆角半径
- 🕐 **时间分隔线** — 按时间间隔显示分隔标签

## 兼容性

**服务器装了 禁用聊天举报 插件？** 记得在配置里开启兼容选项！

## 常见问题

**服务器需要装吗？** 可不安装，但安装后激活 引用 / @ / 新玩家聊天历史同步

**怎么打开配置？** 聊天面板左下角齿轮 → 菜单 → 设置。配置文件在 `\config\e33chat-client.toml`，服务端配置在 `\config\e33chat-server.toml`

**聊天历史保存在哪？** 在 `.minecraft\e33chat\history`

**怎么关掉聊天历史同步？** 在 `\config\e33chat-server.toml` 里把 `history_enabled` 设为 `false`，重启服务器

**可以放进整合包吗？** 可以

**如何反馈问题？** [反馈问题](https://github.com/E33EPUS/E33Chat/issues)
