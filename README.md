# E33EPUS's ChatScreen

A simple mod that replaces the vanilla chat HUD.

**Client** required, **server** optional.

## Features

**Chat**
- Chat bubbles with player heads, QQ/WeChat style
- Click player head to @mention
- Right-click menu: copy message, quote reply
- Anti-spam: consecutive identical messages merged into xN

**Notifications**
- Strong hints above hotbar for @mentions / quotes / system messages
- Message preview fading above hotbar
- Red dot on HUD icon for unread messages
- Notification sound (chime) when @mentioned or quoted

**Quality of Life**
- Command suggestions while typing
- Chat history persistence per world (enable in config)
- Input text preserved when closing/reopening chat
- Unread notification bar when scrolled up
- Ease-out open/close animation

**Customization**
- Configurable (`config/e33chat-client.toml`): bubble colors, text colors, preview lines/width, animations, strong hints, and more
- Per-world custom title bar (click ✎ to rename)

**If your server uses a "No Chat Reports" type plugin, enable "No Chat Reports" Compat config (0.2.4-beta+)**

## License

MIT

---

# E33EPUS的聊天界面

一个重置了原版聊天框的简易mod。

**客户端**需装，**服务端**可选。

## 功能

**聊天**
- 聊天气泡，带玩家头像，QQ/微信风格
- 点击头像 @提及
- 右键菜单：复制消息、引用回复
- 防刷屏：连续相同消息合并为 xN

**通知**
- 被 @/引用/系统消息时快捷栏上方弹出强提示
- 快捷栏上方消息预览淡入淡出
- HUD 图标红点未读提示
- 被 @/引用时播放风铃提示音

**体验**
- 输入时指令补全
- 聊天记录按存档持久化（配置开启）
- 关闭聊天框后保留已输入文本
- 向上滚动时未读提示条
- 开关动画 ease-out 缓出

**自定义**
- 可配置（`config/e33chat-client.toml`）：气泡色、文字色、预览行数/宽度、动画、强提示等
- 每个世界独立标题栏（点 ✎ 改名）

**如果你的游戏或服务器安装了禁用聊天举报类的插件，在配置中开启 "禁用聊天举报"模组兼容 选项（0.2.4-beta+）**

## 许可

MIT
