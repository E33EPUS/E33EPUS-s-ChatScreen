# E33EPUS's ChatScreen

A simple mod that replaces the vanilla chat HUD.

Client required, server optional.

## Features

- Messages as bubbles with player heads, chat-app style
- Whisper sidebar: click a player to chat privately — `/msg` spliced behind the scenes, never shown
- Server titles/prefixes displayed next to player names with original colors
- Dark / Light color themes
- Quick chat panel: save common phrases, click to fill the input
- Emoji & kaomoji picker
- `@` autocomplete popup; left-click a head to @, right-click a head to teleport/whisper
- Right-click a message: copy / quote reply
- Popup hints for system / @mention / quote, message preview at bottom-left
- Per-world chat history (saved to `.minecraft/e33chat/history/`), anti-spam merging, input preserved when closing chat
- More to come

## Config

Config file: `config/e33chat-client.toml`

Themes, custom bubble/text colors, preview lines, animations, popup hints, and more.

If your server uses "No Chat Reports", turn on the compat option in config.

## License

MIT License

---

# E33EPUS的聊天界面

一个替换了原版聊天栏的简易模组。

客户端需装，服务端可选。

## 功能

- 消息带气泡和玩家头像，聊天APP风格
- 私聊侧边栏：点玩家头像进入私聊——`/msg` 背后自动拼接，输入框不穿帮
- 服务器称号/前缀带原色显示在玩家名旁
- 深色 / 浅色主题
- 常用语面板：保存常用短语，点击填入输入框
- Emoji & 颜文字表情面板
- 输入 `@` 弹出补全列表；左键头像 @ ta，右键头像传送/私聊
- 右键消息：复制 / 引用回复
- 系统消息 / @ / 引用弹窗提示，屏幕左下角消息预览
- 按存档保存聊天记录（存放在 `.minecraft/e33chat/history/`）、防刷屏合并、关闭聊天保留已输入文本
- 敬请期待

## 配置

配置文件：`config/e33chat-client.toml`

主题、自定义气泡/文字颜色、预览行数、动画、弹窗提示等等。

**如果你的服务器装了"禁用聊天举报"插件，在配置里打开兼容选项**

## 许可

MIT License
