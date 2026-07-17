# Changelog

## v1.9.1

**修复**
- 自带下划线样式的消息（如 Xaero 路径点分享）不再出现双下划线，其下划线也不再刮破表情菜单等浮层——现在聊天列表中的所有下划线都由 mod 按绘制顺序自绘

***

**Fixes**
- Messages with intrinsic underline styling (e.g. Xaero waypoint shares) no longer show a double underline, and their underlines no longer bleed through overlay panels — all underlines in the chat list are now repainted by the mod in plain paint order

## v1.9

**新功能**
- 配置界面重做：左侧分类栏（外观/通知/行为/兼容），悬停选项名显示详细说明，调试日志开关加入界面
- 配置项支持第三方配置界面（如 Configured）的本地化显示

**修复**
- 三处输入框（主输入、常用语、侧边栏搜索）文字垂直居中，占位符与输入文字位置完全一致，聚焦不再跳位
- 可点击文本的下划线不再穿透浮层面板（此前打开表情菜单等浮层时会有一道"划痕"）
- 发送指令不再生成本地气泡——与原版一致，指令文本不进聊天记录（私聊指令除外）

**其他**
- 配置文件注释全部改为英文（生态惯例）；文件结构与键名不变，现有配置无缝保留

***

**New Features**
- Reworked config screen: category sidebar (Appearance / Notifications / Behavior / Compat), hover an option name for a detailed description, debug log toggle now in the GUI
- Config options now localize in third-party config UIs (e.g. Configured)

**Fixes**
- Text in all three input boxes (main input, quick chat, sidebar search) is now vertically centered; placeholder and typed text share the exact same position, no more jump on focus
- Underlines on clickable text no longer bleed through overlay panels (previously visible as a "scratch" across the emoji panel and other overlays)
- Sending a command no longer creates a local bubble — matching vanilla, command text stays out of the chat log (whisper commands excepted)

**Misc**
- Config file comments are now in English (ecosystem convention); file structure and keys unchanged, existing configs carry over seamlessly

## v1.8

**消息分类重构**
- 新增"翻译键"确定性分类层：私聊、公屏聊天、指令反馈、原版广播（进度/死亡/进出服）、OP 回显、/say、/me、队伍消息按原版翻译键精确路由，不再依赖文本内容猜测；键在 NCR/FreedomChat 转换后依然保留，因此转换服同样受益
- 未知格式自动回落到原有启发式识别（插件自定义格式不受影响）

**修复**
- /tp、/kill 等指令的 OP 回显（`[名字: ...]`）不再被误判为该玩家的聊天气泡
- 转换服上的私聊名字与内容直接取自消息结构（保留样式与颜色），且不再依赖客户端语言
- 你发出私聊后 10 秒内对方的回复不再可能被回显抑制误吞
- Xaero 地图路径点分享在聊天转换服务器上不再被渲染成气泡

***

**Message Classification Rework**
- New deterministic classification layer based on vanilla translation keys: whispers, public chat, command feedback, vanilla broadcasts (advancements / deaths / joins), op echoes, /say, /me, and team messages are routed by their exact translation keys instead of text guessing; keys survive NCR/FreedomChat conversion, so converted servers benefit equally
- Unknown formats fall back to the existing heuristics (custom plugin formats unaffected)

**Fixes**
- Op echoes of commands like /tp and /kill (`[Name: ...]`) are no longer misattributed as that player's chat bubble
- Whisper sender and content on converted servers are taken directly from the message structure (styles and colors preserved), independent of client language
- A partner's reply within 10 seconds of your outgoing whisper can no longer be swallowed by echo suppression
- Xaero waypoint shares are no longer rendered as bubbles on chat-converting servers

## v1.7

> 同步自 Forge 1.20.1 v1.3–v1.7（一次性补齐五个版本）

**v1.3-v1.4 私聊系统**
- 私聊侧边栏：在线玩家列表（头像+名字+最新私聊预览），点击进入私聊模式，紫色模式指示条
- 私聊发送隐形拼接 `/msg`，输入框不穿帮；回显三层拦截，私聊不泄漏公屏、不复读
- 侧边栏搜索框、右键头像菜单（传送/私聊）、未读私聊紫色闪烁、滑入滑出动画、无人在线插画
- 公屏最新消息预览显示在"世界频道"行；消息预览宽度可配置（`preview_width`）

**v1.5 主题**
- 颜色主题切换：深色（默认）/ 浅色，配置界面一键切换
- 常用语面板（`quick_chat` 配置）

**v1.6 装饰名**
- 服务器称号/前缀带原色显示在玩家名旁（`[前缀]<名字>` 与 `<[前缀]名字>` 均支持，失败回退裸名）
- 自己的称号在服务器回显后补全到本地气泡；消息预览保留原有颜色样式
- 回显记录/私聊回显旗标 10 秒过期，不再误吞后续消息
- 聊天历史保存带样式的发送者名（旧存档兼容）
- 网络通道声明为可选——连接未装本 mod 的服务器不再可能被拒连

**v1.7 圆角与昵称**
- 圆角气泡：SDF shader 逐像素抗锯齿，新配置"气泡圆角半径"（0-10，默认 4），shader 加载失败自动回退方角
- 昵称插件支持：消息归属尝试 tab 显示名 + `/tell` 点击事件通道 + `§` 颜色码兼容
- 聊天历史保存加固：单条消息序列化失败降级纯文本，不再丢整个历史
- 默认配色：自己的气泡 #1E90FF 蓝底白字（仅新配置生效）
- 调试日志改为 `debug_log` 配置开关（默认关闭）

**NeoForge 1.21.1 适配说明**
- 私聊类型判定改用 `Holder.is()`（1.21.1 `ChatType.Bound` 记录化）
- 历史序列化走 `registryAccess`（1.20.5+ Component JSON 要求）
- 圆角 shader 按 1.21.1 顶点 API 重写提交路径

***

> Synced from Forge 1.20.1 v1.3–v1.7 (five versions in one pass)

**v1.3-v1.4 Whisper System**
- Whisper sidebar: online player list (avatar + name + latest whisper preview), click to enter whisper mode with purple indicator bar
- Invisible `/msg` splicing on send; three-layer echo suppression — whispers never leak to public chat or echo back
- Sidebar search box, avatar right-click menu (Teleport/Whisper), purple unread blink, slide animation, no-players illustration
- Latest public message preview under the "Public" entry; configurable preview width (`preview_width`)

**v1.5 Themes**
- Color theme toggle: Dark (default) / Light, switchable in config screen
- Quick chat phrases panel (`quick_chat` config)

**v1.6 Decorated Names**
- Server titles/prefixes shown next to player names in original colors (both `[Prefix]<Name>` and `<[Prefix]Name>`, falls back to bare name)
- Your own title patched into local bubbles once the server echo arrives; previews keep original colors
- Pending echoes and whisper echo flags expire after 10s — no more swallowed messages
- Chat history saves styled sender names (old saves still load)
- Network channel declared optional — joining servers without this mod can no longer be rejected

**v1.7 Rounded Corners & Nicknames**
- Rounded bubble corners: SDF shader with per-pixel anti-aliasing, new "Bubble Corner Radius" config (0-10, default 4), automatic square fallback if the shader fails to load
- Nickname plugin support: attribution tries tab-list display names + `/tell` click events + `§` color code tolerance
- Chat history hardening: a message that fails to serialize degrades to plain text instead of wiping the whole history
- New default colors: own bubble #1E90FF with white text (fresh configs only)
- Debug logging gated behind `debug_log` config (off by default)

**NeoForge 1.21.1 Porting Notes**
- Whisper type detection now uses `Holder.is()` (`ChatType.Bound` is a record in 1.21.1)
- History serialization goes through `registryAccess` (required for Component JSON since 1.20.5)
- Rounded-corner shader submission path rewritten for the 1.21.1 vertex API

## v1.2

> 同步自 Forge 1.20.1 v1.2

**修复**
- 修复标准服务器（未安装 No Chat Reports）开启 `chat_report_compat` 后，`[头衔] <玩家名>` 格式的服务器前缀/称号无法提取到发送者显示名的问题
- 修复配置在游戏内修改后重启丢失的问题（NeoForge 的 `ModConfigSpec.set()` 不会自动保存，需手动调用 `save()`）

**新功能**
- 消息区域右侧新增滚动条：显示当前位置、点击空白区域翻页、拖拽滑块滚动

***

**Fixes**
- Fixed server prefix/title extraction for standard servers (without No Chat Reports): when `chat_report_compat` is enabled, prefixes like `[VIP] <PlayerName>` in player chat messages are now correctly extracted to the sender display name
- Fixed in-game config changes lost on restart (NeoForge's `ModConfigSpec.set()` does not auto-save; added explicit `save()` call)

**New Features**
- Scrollbar on the right side of the message area: shows scroll position, click empty track to page up/down, drag thumb to scroll

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
