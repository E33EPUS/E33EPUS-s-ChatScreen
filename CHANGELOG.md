# Changelog

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
