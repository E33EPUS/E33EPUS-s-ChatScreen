# Changelog

## v2.0.0

**更名**
- 显示名从 E33EPUS's ChatScreen 改为 **E33Chat**
- 1.20.1 v2.0.0 同步

**Rename**
- Display name changed from E33EPUS's ChatScreen to **E33Chat**
- Synced 1.20.1 v2.0.0

## v1.9.9

**重构**
- ChatBubbleScreen 拆分为 5 个类：ChatEmojiPanel（表情）、ChatQuickChatPanel（常用语）、ChatSettingsMenu（设置菜单）、ChatSearchPanel（搜索）、ChatBubbleScreen（编排层）
- drawTextureIcon / iconTex / BAR_H 改为包内可见，面板类直接引用
- 1.20.1 v1.9.9 架构同步

**新增**
- 聊天搜索：浮动输入框，实时子串匹配，上下箭头/滚轮切换匹配项，黄色高亮边框，计数器显示
- 设置菜单重铸：从 3 列横排改为 4 行竖排上拉，图标居左文字居右，英文字段自适应截断

**Refactor**
- Split ChatBubbleScreen into 5 classes: ChatEmojiPanel (emoji picker), ChatQuickChatPanel (quick phrases), ChatSettingsMenu (gear menu), ChatSearchPanel (search bar), ChatBubbleScreen (orchestrator)
- drawTextureIcon / iconTex / BAR_H relaxed to package-private for panel access
- Synced 1.20.1 v1.9.9 architecture

**New**
- Chat search: floating input above bottom bar, real-time substring matching, up/down/scroll to cycle matches, yellow highlight border, match counter
- Settings menu redesigned: vertical 4-row popup (was horizontal 3-col), icons left + text right, auto-truncate long English labels

## v1.9.8

**修复**
- 清理 ChatMessage 死字段 `addedTime`
- 多条系统消息（tellraw）同时到达时强提示不再互相覆盖，改为排队依次显示

**Fix**
- Removed dead `addedTime` field from ChatMessage
- Multiple simultaneous system messages (tellraw) no longer overwrite each other's strong hint; now queued and displayed in sequence

## v1.9.7

**音效**
- 新增"音效"配置分类，四种消息类型可独立开关提示音：系统消息、@/引用消息、私聊消息、公屏消息
- 默认 @/引用消息和私聊消息触发提示音，系统消息和公屏消息不触发
- 防刷屏选项默认关闭

**Sound**
- New "Sound" config category with independent notification sound toggles for 4 message types: system, @/quote, whisper, public
- Default: @/quote and whisper trigger sounds, system and public do not
- Anti-spam now defaults to off

## v1.9.6

**动画**
- 聊天面板打开：背景不透明度渐入（easeOutCubic），关闭：淡出（easeInQuad）
- 滚屏系统重构：壁钟驱动 easeOutCubic 丝滑动画，滚轮 40px/120ms，拖拽滑动块 80ms，底部自动滚屏 150ms
- 滑动块自动浮现：滚动时显示，停止滚动 1 秒后淡出；悬停/拖拽时常驻
- 新消息到达时列表底部丝滑滚屏，不再瞬移；首次打开直接跳底，无回弹动画

**Animation**
- Panel open: background fades in (easeOutCubic), close: fades out (easeInQuad)
- Scroll system rebuilt: wall-clock-driven easeOutCubic animations — wheel 40px/120ms, drag 80ms, auto-scroll 150ms
- Scrollbar auto-appear: visible while scrolling, fades out 1s after stop; always visible on hover/drag
- Smooth auto-scroll when new messages arrive; instant jump-to-bottom on first open, no bounce

## v1.9.5

**重构**
- 新增 `Animation` 工具类：统一 easeOutCubic/lerpTo/fadeIn/fadeOut/fadeInOut 动画函数
- 新增 `UiLayout` 工具类：统一 centerX/clampX/clampW 布局计算
- ChatBubbleScreen 和 ChatBubbleHudOverlay 动画计算统一使用 Animation 方法

**Refactor**
- Added `Animation` utility class: unified easeOutCubic/lerpTo/fadeIn/fadeOut/fadeInOut animation functions
- Added `UiLayout` utility class: unified centerX/clampX/clampW layout helpers
- ChatBubbleScreen and ChatBubbleHudOverlay animation math now uses Animation methods

## v1.9.4

**优化**
- renderMessages 消息列表遍历从三趟合并为两趟，减少重复迭代
- 替换 indexOf 全列表查找为双指针扫描，消息索引查找从 O(v*m) 降为 O(m)
- HUD 消息预览逐行独立淡出，旧行不再突然消失
- 引用块改为微信风格：置于气泡下方、宽度独立跟随文本、单行省略号
- 去掉聊天面板打开时的游戏背景变暗效果

**Optimizations**
- Merged renderMessages height-calculation passes from two loops into one
- Replaced per-message indexOf full-list scan with two-pointer tracking (O(v*m) → O(m))
- HUD message preview now fades each line independently; old lines fade out instead of vanishing
- Quote block redesigned to WeChat style: below bubble, independent width, single-line with ellipsis
- Removed dark background overlay when chat panel is open

## v1.9.3

**修复**
- 强提示弹窗（热键栏上方的系统消息推送）现在正确保留文本颜色，与聊天气泡/消息预览一致
- 面板宽度默认值提高到 1000 物理像素，最小值提高到 800（400 会挡住部分 UI）；面板宽度计算使用四舍五入避免 GUI 自动缩放下的像素偏差
- 侧边栏玩家头像与频道图标对齐，滚动上限精确计算不再可无限滚出空白
- 配置界面数字输入框自适应位数（面板宽度 4 位、预览宽度 3 位、圆角半径 2 位）

**其他**
- 多处 `printStackTrace` / `Exception ignored` 改为 `LogUtils.getLogger()` 统一日志输出
- NeoForge 1.21.1 同步上述全部改动；两版代码基线合并

***

**Fixes**
- Strong hint popups (system message pushes above the hotbar) now correctly preserve text colors, matching chat bubbles and message preview
- Panel width default raised to 1000 physical pixels, minimum raised to 800 (400 blocked parts of the UI); panel width calculation now rounds guiScale to avoid pixel drift under auto GUI scaling
- Sidebar player avatars now align with the public channel icon; scroll bound is computed accurately and no longer scrolls endlessly into blank space
- Config screen number inputs adapt their max length (4 digits for panel width, 3 for preview width, 2 for corner radius)

**Other**
- Replaced multiple `printStackTrace` / `Exception ignored` with `LogUtils.getLogger()` for unified logging
- NeoForge 1.21.1 synced with all the above; both codebases merged to parity

## v1.9.2

**修复**
- 起床按钮回来了：睡觉时显示原版样式的"起床"按钮（v1.1 屏蔽睡觉强制聊天框时的误伤，此后一直无法提前起床）；ESC 直接起床，按 T 仍可打开聊天
- 可点击文本的下划线在 ModernUI 等字体替换 mod 下不再过粗、错位、超出文本刺穿气泡边框——下划线改回由字体渲染器自绘（1.9.1 的手动补画在亚像素字宽下必然漂移），浮层防刺穿改为整体抬高浮层 z 层实现，顺带修复了删除线刮浮层的同类问题
- 可点击文本的点击判定区域在 ModernUI 下不再右偏（同一根因）

***

**Fixes**
- The Leave Bed button is back: sleeping now shows a vanilla-style Leave Bed button (collateral damage of the v1.1 forced-chat-screen fix — getting up early had been impossible since); ESC wakes you up, T still opens chat
- Underlines on clickable text no longer render too thick, misplaced, or overshooting past the bubble border under font-replacing mods (e.g. ModernUI) — underlines are drawn by the font renderer again (the 1.9.1 manual repainting inevitably drifts with sub-pixel advances); overlay bleed-through is now prevented by z-lifting overlays instead, which also fixes the same strikethrough issue
- Click hitboxes on clickable text are no longer shifted right under ModernUI (same root cause)

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

**新功能**
- 圆角气泡：SDF shader 实现，边缘逐像素抗锯齿，任意 GUI 缩放下均平滑
- 新配置项"气泡圆角半径"（0-10，默认 4，0 = 原来的方角）
- shader 加载失败时自动回退方角渲染，不影响使用
- 昵称类插件支持：消息归属额外尝试匹配 tab 列表显示名（覆盖"聊天名=tab名"的常见配置，未在真实昵称服实测）
- "点击私聊"事件归属：识别插件挂在名字上的 `/tell`/`/msg` 点击事件，从中拿到真实档案名——昵称服上的零猜测归属通道
- 名字匹配兼容 `§` 颜色码（提供原始/剥离双版本候选）
- 默认配色更新：自己的气泡 #1E90FF 蓝底白字（仅对新生成的配置生效）

**修复**
- 聊天历史保存加固：单条消息序列化失败自动降级为纯文本，不再可能因一条异常消息丢失整个历史

**其他**
- 消息处理调试日志改为配置开关 `debug_log`（默认关闭）——正式版不再把聊天内容写入 latest.log，排查问题时可在配置文件中开启

***

**New Features**
- Rounded bubble corners: SDF shader with per-pixel anti-aliased edges, smooth at any GUI scale
- New config option "Bubble Corner Radius" (0-10, default 4, 0 = classic square corners)
- Automatically falls back to square rendering if the shader fails to load
- Nickname plugin support: message attribution also tries tab-list display names (covers the common "chat name = tab name" setup; not yet field-tested on real nickname servers)
- "Click to whisper" attribution: reads the real profile name from `/tell`/`/msg` click events plugins attach to sender names — a zero-guess attribution channel on nickname servers
- Name matching tolerates `§` color codes (raw and stripped candidate variants)
- New default colors: own bubble #1E90FF with white text (applies to fresh configs only)

**Fixes**
- Chat history saving hardened: a message that fails to serialize degrades to plain text instead of aborting the save — one bad message can no longer wipe the whole history

**Misc**
- Message-pipeline debug logging is now gated behind the `debug_log` config option (off by default) — release builds no longer write chat content to latest.log; enable it in the config file when troubleshooting

## v1.6

**新功能**
- 服务器称号/前缀显示：插件添加的 `[称号]`/`[群组]` 等前缀现在带原色显示在玩家名旁，玩家消息与系统消息通道均支持，兼容 `[前缀]<名字>` 与 `<[前缀]名字>` 两种格式（提取失败自动回退裸名）
- 自己的称号自己也可见——服务器回显到达后自动补全到本地气泡
- 消息预览保留消息原有颜色与样式（称号颜色、mod 彩色文本等）

**修复**
- 回显记录改为 10 秒过期，且仅对聊天和 `/msg` `/tell` `/w` `/me` `/say` 记账——修复发送无回显指令后计数残留、误吞后续署名为自己的消息
- 私聊回显旗标同样 10 秒过期——修复自定义私聊格式服务器上残留旗标可能误吞后续收到的私聊
- 聊天历史现在保存带样式的发送者名（旧存档兼容读取）
- 玩家名过长截断时保留颜色
- 网络频道版本校验放宽（`acceptMissingOr`）——连接装了 Forge 但没装本 mod 的服务器不再可能被拒连，"服务端可选"更彻底

**其他**
- 身份判定（own/@提及/引用）与显示名解耦，装饰名不影响消息归属
- 日志前缀统一为 `[e33chat]`

***

**New Features**
- Server title/prefix display: plugin-added prefixes like `[Title]`/`[Group]` now show next to player names with their original colors, on both player and system message channels, supporting both `[Prefix]<Name>` and `<[Prefix]Name>` formats (falls back to bare name if extraction fails)
- Your own title is now visible to yourself — patched into the local bubble once the server echo arrives
- Message previews keep original colors and styles (title colors, mod-colored text, etc.)

**Fixes**
- Pending echoes now expire after 10s and are only tracked for chat and `/msg` `/tell` `/w` `/me` `/say` — fixes stale counters from no-echo commands swallowing later self-attributed messages
- Whisper echo flag also expires after 10s — fixes stale flag potentially swallowing incoming whispers on servers with custom whisper formats
- Chat history now saves styled sender names (old saves still load)
- Long player names keep their colors when truncated
- Relaxed network channel version check (`acceptMissingOr`) — joining Forge servers without this mod can no longer be rejected, making "server optional" truly hold

**Misc**
- Identity logic (own/@mention/quote) decoupled from display names — decorated names never affect message attribution
- Log prefix unified to `[e33chat]`

## v1.5

**新功能**
- 颜色主题切换：深色（默认）/ 浅色，配置界面一键切换
- 新增 `ChatBubbleTheme` 主题系统，所有 UI 颜色集中管理

***

**New Features**
- Color theme toggle: Dark (default) / Light, switchable in config screen
- New `ChatBubbleTheme` system: all UI colors managed in one place

## v1.4

**新功能**
- 侧边栏搜索框：按名字筛选在线玩家
- 右键头像菜单：传送 + 私聊快捷操作
- 侧边栏无在线玩家插画
- 私聊未读闪烁提示：侧边栏玩家列表紫色闪烁标记
- 侧边栏滑入/滑出动画（ease-out cubic）
- 公屏最新消息预览显示在侧边栏"世界频道"行
- 消息预览宽度可配置（`preview_width`，50-400px）

**修复**
- 私聊输入框不再穿帮——`/msg` 拼接完全在背后完成
- 私聊消息不再泄漏到公屏——系统回显三层拦截（标记→检测→吞除）
- 私聊回复不再复读——本地显示与服务端转发完全隔离
- 引用私聊消息不再错位——全量索引追踪，不受过滤视图影响
- NCR 兼容开关开启/关闭均可正确处理私聊

***

**New Features**
- Sidebar search box: filter online players by name
- Avatar right-click menu: Teleport + Whisper quick actions
- No online players illustration in sidebar
- Unread whisper blinking indicator: purple pulsing dot in sidebar player list
- Sidebar slide-in/out animation (ease-out cubic)
- Latest public message preview under "Public" entry in sidebar
- Configurable message preview width (`preview_width`, 50-400px)

**Fixes**
- Input box no longer exposes `/msg` — command splicing is fully behind-the-scenes
- Whisper messages no longer leak to public chat — three-layer system echo suppression
- Whisper replies no longer echo back — local display fully isolated from server forwarding
- Quoting whisper messages no longer mis-tracks — global index tracking unaffected by filtered views
- NCR compat on/off both handle whispers correctly

## v1.3

**新功能**
- 私聊侧边栏：左侧在线玩家列表，显示头像+名字+最新私聊预览，点击切换私聊模式
- 侧边栏收起/展开：标题栏左侧汉堡按钮，收起后聊天面板占满
- 私聊过滤：点击玩家只显示与该玩家的私聊记录，顶部紫色模式指示条；点击"公屏"返回
- 私聊发件隐形拼接：输入框不显示 `/msg`，发送时背后自动拼接，不露破绽

***

**New Features**
- Whisper sidebar: online player list on the left with avatar + name + latest whisper preview, click to switch to whisper mode
- Sidebar toggle: hamburger button at the top-left of the title bar, chat panel fills width when collapsed
- Whisper filtering: clicking a player shows only whisper messages with them, with a purple mode indicator bar; click "Public" to return
- Invisible whisper splicing: `/msg` is prepended behind the scenes on send, never shown in the input box

## v1.2

**修复**
- 修复标准服务器（未安装 No Chat Reports）开启 `chat_report_compat` 后，`[头衔] <玩家名>` 格式的服务器前缀/称号无法提取到发送者显示名的问题

**新功能**
- 消息区域右侧新增滚动条：显示当前位置、点击空白区域翻页、拖拽滑块滚动

***

**Fixes**
- Fixed server prefix/title extraction for standard servers (without No Chat Reports): when `chat_report_compat` is enabled, prefixes like `[VIP] <PlayerName>` in player chat messages are now correctly extracted to the sender display name

**New Features**
- Scrollbar on the right side of the message area: shows scroll position, click empty track to page up/down, drag thumb to scroll

## v1.1

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
