# E33EPUS's ChatScreen

Replace the vanilla chat with chat bubbles.

Client only. Server optional.

## What it does

- Shows chat as bubbles with player heads, like QQ/WeChat
- Click a head to @ someone
- Right-click a message to copy or quote reply
- Plays a chime when someone @mentions or quotes you
- Pops a hint above the hotbar for @mentions and system messages
- Message preview fades in above the hotbar
- Red dot on the chat icon when you have unread messages
- Command suggestions while typing
- Saves chat history per world so you don't lose it after quitting (enable `chat_history` in config)
- Remembers what you typed if you close chat without sending
- Anti-spam: repeated messages get merged into "x2", "x3", etc.
- Arrow keys and mouse wheel to scroll through history
- Click the pencil icon to rename the title bar per world

## Config

Everything in `config/e33chat-client.toml` — bubble colors, text colors, preview size, animations, hints, and more.

**If your server uses "No Chat Reports", turn on the compat option in config (0.2.4-beta+)**

## License

MIT

---

# E33EPUS的聊天界面

用聊天气泡替换原版聊天栏。

只需客户端装，服务端可选。

## 功能

- 气泡聊天，带头像，像 QQ/微信那样
- 点人头像 @他
- 右键消息：复制或引用回复
- 被 @或引用时有风铃声提示
- 被 @或系统消息在快捷栏上方弹出提示
- 消息预览在快捷栏上方淡入淡出
- 有未读消息时聊天图标显示红点
- 输入指令时自动补全
- 聊天记录按存档保存，退游戏不丢（在配置里开 `chat_history`）
- 输入内容没发送就关掉，下次打开还在
- 防刷屏：连续发相同消息自动合并成 x2、x3
- 方向键和滚轮翻聊天历史
- 点 ✎ 给每个世界改标题栏名字

## 配置

`config/e33chat-client.toml` 里什么都能改——气泡颜色、文字颜色、预览大小、动画、提示等等。

**如果你的服务器装了"禁用聊天举报"插件，在配置里打开兼容选项（0.2.4-beta+）**

## 许可

MIT
