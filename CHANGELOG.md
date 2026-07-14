# Changelog

## v1.1

> 同步自 Forge 1.20.1 v1.1

**修复**
- 修复 Xaero 地图连续分享多个不同坐标后坐标丢失
- 修复 emoji 码点截断导致的宽度测量错误（代理对字符如 😀 使用 `Character.toChars`）
- 修复睡觉时聊天框无法关闭（阻止原版强制弹框，手动 T 键正常打开/ESC 关闭，醒来恢复原状态）
- 修复不同存档聊天记录互相覆盖/泄漏（文件名加 hash 防中文世界名碰撞 + 存/读条件修正）
- 修复醒来时 `setScreen` 跨线程崩溃（`PlayerWakeUpEvent` 服务端线程 → `mc.execute()`）

**新功能**
- Emoji 表情面板：双标签（😊 Emoji + ✧ 颜文字），点击插入输入框
- 时间分隔符间隔可配置（`time_separator_minutes`，1/5/10/15/30分钟/关闭，默认 5 分钟）

**UI**
- 底栏新增 emoji 按钮，设置图标左移，输入框空间优化
- @补全面板 70% 不透明度，表情面板完全不透明

***

**Fixes**
- Fixed Xaero map consecutive waypoint shares not displaying (removed aggressive `<>` dedup + changed `consumeEchoBySystemChat` from `contains` to `equals`)
- Fixed emoji code point truncation causing width measurement errors (surrogate pair characters like 😀 now use `Character.toChars`)
- Fixed chat screen unclosable during sleep (block vanilla forced open, T key works normally, ESC closes, restores state on wake)
- Fixed chat history leaking/wrongly overwriting between saves (file name now includes hash to prevent Chinese world name collision + save/load condition fix)
- Fixed cross-thread crash on wake (`PlayerWakeUpEvent` on server thread → `mc.execute()`)

**New Features**
- Emoji picker panel: two tabs (😊 Emoji + ✧ Kaomoji), click to insert into input
- Time separator interval configurable (`time_separator_minutes`, 1/5/10/15/30 min/off, default 5 min)

**UI**
- Bottom bar: emoji button added, gear icon shifted left, input box width optimized
- @mention popup 70% opacity, emoji panel fully opaque

## v1.0

> 同步自 Forge 1.20.1 v1.0，支持 NeoForge 1.21.1

**修复**
- 修复 `ChatComponent.addMessage` 双重触发导致的@/引用重复音效
- 引用提示音改为风铃声 (`NOTE_BLOCK_CHIME`)——和被@一致
- 修复聊天框打开时强提示/弹窗被隐藏
- 修复指令补全界面 X 坐标错位（不同 GUI 缩放下偏移不同）
- 修复引用预览竖线颜色不统一（统一白色）
- 修复 `isRecentDuplicate` 回显抑制回归（`CHAT_REPORT_COMPAT` 下发送消息被误吞）
- 修复配置界面"兼容性选项"标题滚动时偏移不同步

**UI 优化**
- 气泡宽度完全跟随文本（去掉最小宽度限制），横向内边距 8→6，纵向 5→4
- 头像位置对齐玩家名顶部
- 标题栏顶部缝隙消除，底栏高度 30→26，输入框高度 20→14，图标 16→14
- 标题编辑框尺寸贴合文本，不再错位
- 配置界面重构为数据驱动（增删配置项无需手改索引）
- 动画改为 ease-out 三次方缓出

**新功能**
- 输入 `@` 弹出在线玩家名补全列表（Tab/Enter 选中，Esc 关闭）
- 聊天记录按存档持久化（`chat_history` 配置，默认关闭）
- 关闭聊天框后保留已输入文本
- 预览行数上限 3→8
- 被@/引用播放风铃提示音

**配置**
- 新增 `chat_history`——保留每个存档的聊天记录
- `anti_spam`、`chat_history` 描述精简
- jar 命名格式改为 `e33chat-Forge-1.20.1-1.0`

***

**Fixes**
- Fixed double sound on @mention/quote caused by `ChatComponent.addMessage` dual trigger
- Quote notification now uses `NOTE_BLOCK_CHIME` (same wind chime as @mention)
- Fixed strong hints being hidden when chat screen is open
- Fixed command suggestion X offset (misaligned at different GUI scales)
- Fixed quote bar accent color inconsistency (always white now)
- Fixed `isRecentDuplicate` echo suppression regression (messages swallowed under `CHAT_REPORT_COMPAT`)
- Fixed config screen "Compatibility" header drifting on scroll

**UI**
- Bubble width follows text exactly (removed min width), padding X 8→6, Y 5→4
- Avatar aligned to player name top
- Title bar gap removed, bottom bar 30→26, input height 20→14, icons 16→14
- Title edit box sized to match text, no more misalignment
- Config screen refactored to data-driven entries
- Animation switched to ease-out cubic

**New Features**
- `@` autocomplete popup with online player names (Tab/Enter to select, Esc to close)
- Per-world chat history persistence (`chat_history` config, disabled by default)
- Input text preserved when closing/reopening chat
- Preview lines cap 3→8
- Wind chime sound on @mention/quote

**Config**
- Added `chat_history` — saves chat history per world
- Simplified `anti_spam` and `chat_history` labels
- Jar naming: `e33chat-Forge-1.20.1-1.0`

## v0.2.4-beta

增强 `chat_report_compat` 匹配——改为扫描在线玩家列表而非要求 `<` 开头，支持服务器前缀/称号（如 `【称号】 <PlayerName> 消息`）。匹配到的前缀保留到发送者显示名中。修复服务端兼容性——客户端可加入无 mod 服务端，双端安装时服务端也不再因客户端类加载崩溃（`displayTest="NONE"` + 客户端初始化抽离到 `@OnlyIn(Dist.CLIENT)` 类）。

Enhanced `chat_report_compat` matching — scans for online player names instead of requiring `<` at string start, supporting server prefixes/titles (e.g. `【Title】 <PlayerName> message`). Prefix text preserved in sender display name. Fixed server compatibility — client can now join servers without the mod, and server no longer crashes from client class loading when installed on both sides (`displayTest="NONE"` + client init moved to `@OnlyIn(Dist.CLIENT)` class).

## v0.2.3-beta

新增 `chat_report_compat` 配置项——开启后自动解析 `<玩家名>` 格式的系统消息，提取真实发送者和皮肤，兼容"禁用聊天举报"类模组。修复回显去重在 `system_chat_as_bubble=true` 时失效的问题。配置界面重组为「常规选项」和「兼容性选项」两个分组。

Added `chat_report_compat` config option — automatically parses `<PlayerName>` format system messages to extract real sender and skin, compatible with "No Chat Reports" type mods. Fixed echo dedup failing when `system_chat_as_bubble=true`. Config screen reorganized into two sections.

## v0.2.2-beta

新增 `anti_spam` 配置项——连续相同消息合并为一条，黄色 `xN` 标注重复次数。修复悬停 tooltip 不渲染（进度信息等 HoverEvent）。修复聊天界面内点击事件（RUN_COMMAND）不弹出 GUI 的问题。修复配置界面标签错位。

Added `anti_spam` config option — consecutive identical messages merge into one with yellow `xN` count label. Fixed hover tooltips not rendering (advancement info, etc.). Fixed click events (RUN_COMMAND) not opening GUI while chat screen is active. Fixed swapped config labels.

## v0.2.1-beta

新增配置项 `system_chat_as_bubble`（默认关闭）——开启后所有系统消息也渲染为聊天气泡。修复伪装聊天通道（`handleDisguisedChatMessage`）的去重失效问题，改用发送者身份匹配替代内容 hash 匹配；同时伪装聊天的 `isSystem` 改为根据 `bound.name()` 自动判断。

Added `system_chat_as_bubble` config option (off by default). Fixed echo deduplication for disguised chat channel — replaced content hash matching with sender identity matching. Disguised chat messages now auto-detect system vs player via `bound.name()`.

## v0.2.0-beta

重构了聊天消息拦截架构——不再 cancel 原版消息处理管线，而是改在 `ChatComponent.addMessage` 末端捕获所有 mod 处理后的最终消息，从根本上解决了与其他 mod（Xaero、FTB Team 等）的兼容性问题。系统消息的点击事件（如 FTB 邀请的"接受/拒绝"按钮）现在完整保留；Xaero 路径点分享正确显示为系统消息，点击事件正常；局域网开放提示不再丢失；重写去重机制，用待消费回显队列替代内容扫描，自己连续发重复消息不会被误吞。

Rebuilt the chat interception architecture — instead of cancelling the vanilla message pipeline, messages are now captured from `ChatComponent.addMessage` after all mods have processed them, resolving compatibility issues with other mods (Xaero, FTB Teams, etc.) at the root. Click events on system messages (e.g. FTB invite accept/decline buttons) are fully preserved. Xaero waypoint sharing displays correctly as a system message with working click actions. LAN "Open to LAN" notifications no longer lost. Deduplication rewritten with a pending-echo queue instead of content scanning — sending the same message twice in a row no longer swallows the second one.

## v0.1.8-beta

- **Notification bar** — when scrolled up in chat, a bar appears above the input showing "x new messages" on the left and "You were mentioned" on the right (yellow text, click to jump).
- **@mention strong hint** — getting @mentioned or quoted now triggers a strong hint popup above the hotbar ("You were mentioned" in yellow), configurable via new `mention_strong_hint` option.
- **@mention via avatar** — left-click any player's avatar (including your own) in chat to insert `@playername` into the input.
- **Network layer** — quote and @mention metadata are now synced between players via packets (`ChatMetaPacket`, `QuoteSyncPacket`). The mod now requires **both client and server** installation.
- **World detection refactor** — per-tick world tracking moved to `ChatBubbleClientListener`, fixing a race where mod system messages (e.g. CustomNPCs update notifications) were cleared before the chat screen first opened.
- **Fixed** — sending a message while at the bottom of chat no longer incorrectly triggers the notification bar.
- **Removed** — deprecated gold @mention border config options.

## v0.1.7-beta

- Fix mod system messages being wiped from chat on first open.

## v0.1.4-beta

- Fix world switch message leak.
