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

一个替换了原版聊天栏的简易模组。

客户端需装，服务端可选。

## 功能

- 消息带气泡和玩家头像，聊天APP风格
- 左键玩家头像可 @ ta
- 右键消息：复制消息 / 引用回复
- 系统消息 / @ / 引用消息 弹窗提示
- 屏幕左下角消息预览
- 保存存档聊天记录（配置里开启 `chat_history`）
- 保留已输入文本
- 防刷屏：连续相同消息自动合并
- 敬请期待

## 配置

配置文件：`config/e33chat-client.toml` 

- 自定义气泡颜色、文字颜色、预览行数、动画、弹窗提示等等。

- **如果你的服务器装了"禁用聊天举报"插件，在配置里打开兼容选项（0.2.4-beta+）**

## 许可

MIT License
