package com.niuqu.chatbubble.mixin;

import com.mojang.authlib.GameProfile;
import com.niuqu.chatbubble.ChatBubbleConfig;
import com.niuqu.chatbubble.ChatMessageStore;
import com.niuqu.chatbubble.ChatMessageStore.SenderMeta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = ChatListener.class, priority = 500)
public class ChatListenerMixin {

    // Pulls styled server prefixes out of the decorated line: "[Group]<Steve> hi" -> "[Group]Steve"
    private static Component extractDecoratedName(Component fullLine, String contentStr,
                                                  String rawName, Component fallback) {
        if (contentStr == null || contentStr.isEmpty()) return fallback;
        String fullStr = fullLine.getString();
        int idx = fullStr.lastIndexOf(contentStr);
        if (idx <= 0) return fallback;
        return cleanNameArea(fullLine, 0, idx, rawName, fallback);
    }

    private static Component cleanNameArea(Component fullLine, int a, int b,
                                           String rawName, Component fallback) {
        String fullStr = fullLine.getString();
        while (a < b && Character.isWhitespace(fullStr.charAt(a))) a++;
        while (b > a) {
            char ch = fullStr.charAt(b - 1);
            if (Character.isWhitespace(ch) || ch == ':' || ch == '：' || ch == '»') b--;
            else break;
        }
        if (a >= b) return fallback;
        Component nameArea = ChatMessageStore.sliceStyled(fullLine, a, b);
        String ns = nameArea.getString();
        if (rawName != null && !rawName.isEmpty()) {
            String bracketed = "<" + rawName + ">";
            int p = ns.indexOf(bracketed);
            if (p >= 0) {
                var out = Component.empty();
                if (p > 0) out.append(ChatMessageStore.sliceStyled(nameArea, 0, p));
                out.append(ChatMessageStore.sliceStyled(nameArea, p + 1, p + 1 + rawName.length()));
                int tail = p + bracketed.length();
                if (tail < ns.length()) out.append(ChatMessageStore.sliceStyled(nameArea, tail, ns.length()));
                return out;
            }
            // Team-decorated names sit inside the brackets: "<[Team]Steve>" -> "[Team]Steve"
            if (ns.length() > 2 && ns.charAt(0) == '<' && ns.charAt(ns.length() - 1) == '>') {
                return ChatMessageStore.sliceStyled(nameArea, 1, ns.length() - 1);
            }
        }
        return nameArea;
    }

    // Nick plugins put the tab-list display name in chat instead of the profile name;
    // legacy plugins may embed section-sign color codes in names, so offer stripped variants too
    private static String[] nameCandidates(net.minecraft.client.multiplayer.PlayerInfo info) {
        var out = new java.util.LinkedHashSet<String>();
        String profile = info.getProfile().getName();
        addNameVariants(out, profile);
        var tab = info.getTabListDisplayName();
        if (tab != null) addNameVariants(out, tab.getString().trim());
        return out.toArray(new String[0]);
    }

    private static void addNameVariants(java.util.Set<String> out, String name) {
        if (name == null || name.isEmpty()) return;
        out.add(name);
        String stripped = name.replaceAll("§.", "");
        if (!stripped.isEmpty()) out.add(stripped);
    }

    // Vanilla broadcasts (advancements/deaths/joins) lead with a clickable player name,
    // which tell-click would wrongly claim as chat — keep them as system messages
    private static boolean isVanillaBroadcast(Component message) {
        if (message.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) {
            String key = tc.getKey();
            return key.startsWith("chat.type.advancement.")
                || key.startsWith("death.")
                || key.startsWith("multiplayer.player.")
                || key.startsWith("commands.");
        }
        return false;
    }

    // Plugins attach "click to whisper" events to sender names — the command holds the
    // real profile name, giving deterministic attribution even on nickname servers
    private static SenderMeta detectByTellClick(Component message, String text) {
        if (isVanillaBroadcast(message)) return null;
        var player = Minecraft.getInstance().player;
        if (player == null || player.connection == null) return null;
        final int[] pos = {0};
        final int[] range = {-1, -1};
        final String[] tellName = {null};
        final String[] clickedText = {null};
        message.visit((style, str) -> {
            int s = pos[0], e = s + str.length();
            pos[0] = e;
            var click = style.getClickEvent();
            if (tellName[0] == null && click != null
                && click.getAction() == net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND
                && click.getValue() != null) {
                String cmd = click.getValue();
                for (String p : new String[]{"/tell ", "/msg ", "/w ", "/whisper "}) {
                    if (cmd.startsWith(p)) {
                        String n = cmd.substring(p.length()).trim();
                        int sp = n.indexOf(' ');
                        if (sp > 0) n = n.substring(0, sp);
                        if (!n.isEmpty()) {
                            tellName[0] = n;
                            range[0] = s;
                            range[1] = e;
                            clickedText[0] = str;
                        }
                        break;
                    }
                }
            }
            return java.util.Optional.<Object>empty();
        }, net.minecraft.network.chat.Style.EMPTY);
        if (tellName[0] == null || range[0] > 32) return null;

        net.minecraft.client.multiplayer.PlayerInfo sender = null;
        for (var info : player.connection.getOnlinePlayers()) {
            String profile = info.getProfile().getName();
            if (profile.equals(tellName[0]) || profile.replaceAll("§.", "").equals(tellName[0])) {
                sender = info;
                break;
            }
        }
        if (sender == null) return null;

        // The clicked segment must actually be the sender's displayed name — feedback like
        // "杀死了E33EPUS" carries a whole-line /tell click whose first segment is not a name
        String clicked = clickedText[0].replaceAll("§.", "").trim();
        boolean clickedIsName = false;
        for (String cand : nameCandidates(sender)) {
            if (!cand.isEmpty() && clicked.contains(cand)) { clickedIsName = true; break; }
        }
        if (!clickedIsName) return null;

        int b = range[1];
        if (b < text.length() && text.charAt(b) == '>') b++;
        int contentStart = b;
        while (contentStart < text.length()) {
            char ch = text.charAt(contentStart);
            if (Character.isWhitespace(ch) || ch == ':' || ch == '：' || ch == '»' || ch == '-') contentStart++;
            else break;
        }
        if (contentStart >= text.length()) return null;

        String profile = sender.getProfile().getName();
        Component displayName = cleanNameArea(message, 0, b, tellName[0], Component.literal(profile));
        Component content = ChatMessageStore.sliceStyled(message, contentStart, text.length());
        ChatMessageStore.debugLog("[e33chat] System(tell click) | text='" + text + "' | name=" + profile + " | display='" + displayName.getString() + "' | content='" + content.getString() + "'");
        return new SenderMeta(
            sender.getProfile().getId(),
            displayName,
            content,
            false,
            profile,
            false, null
        );
    }

    private static String extractWhisperContent(String fullText, String senderName) {
        if (senderName == null || senderName.isEmpty()) return fullText;
        int idx = fullText.indexOf(senderName);
        if (idx < 0) return fullText;
        String after = fullText.substring(idx + senderName.length());
        for (String sep : new String[]{": ", "：", " :", " ："}) {
            int i = after.lastIndexOf(sep);
            if (i >= 0) return after.substring(i + sep.length());
        }
        return after.trim();
    }

    private static SenderMeta detectWhisperInSystemMessage(String text, String logTag) {
        var connection = Minecraft.getInstance().player.connection;
        if (connection == null) return null;
        for (var info : connection.getOnlinePlayers()) {
            String profile = info.getProfile().getName();
            for (String cand : nameCandidates(info)) {
                int idx = text.indexOf(cand);
                if (idx >= 0 && idx < 30) {
                    if (text.contains("悄悄") || text.contains("whisper") || text.contains("对你说") || text.contains("to you")
                        || text.contains("私聊") || text.contains("密语") || text.contains("密聊")) {
                        String content = extractWhisperContent(text, cand);
                        UUID senderId = info.getProfile().getId();
                        ChatMessageStore.debugLog("[e33chat] System(" + logTag + ") | text='" + text + "' | name=" + cand + " | content='" + content + "'");
                        return new SenderMeta(
                            senderId,
                            Component.literal(cand),
                            Component.literal(content),
                            false,
                            profile,
                            true, profile
                        );
                    }
                }
            }
        }
        return null;
    }

    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"))
    private void onPlayerChat(PlayerChatMessage message, GameProfile gameProfile,
                              ChatType.Bound bound, CallbackInfo ci) {
        UUID senderId = gameProfile.getId();
        Component raw = message.decoratedContent();
        String rawStr = raw.getString();
        if (rawStr.startsWith("xaero-waypoint:")
            || rawStr.startsWith("xaero_waypoint:")
            || rawStr.startsWith("xaero_waypoint_add:")) {
            return;
        }

        boolean isWhisper = false;
        String whisperPartner = null;
        if (bound.chatType().is(ChatType.MSG_COMMAND_INCOMING)) {
            isWhisper = true;
            whisperPartner = gameProfile.getName();
        } else if (bound.chatType().is(ChatType.MSG_COMMAND_OUTGOING)) {
            isWhisper = true;
            whisperPartner = bound.targetName().map(Component::getString).orElse(null);
        }
        if (ChatBubbleConfig.CHAT_REPORT_COMPAT.get()) {
            String name = gameProfile.getName();
            String pattern = "<" + name + "> ";
            int idx = rawStr.indexOf(pattern);
            int contentStart = idx >= 0 ? idx + pattern.length() : -1;
            int prefixEnd = idx;
            if (contentStart < 0) {
                int i2 = rawStr.indexOf(name + "> ");
                if (i2 > 0) {
                    int open = rawStr.lastIndexOf('<', i2);
                    if (open >= 0 && rawStr.indexOf('>', open) == i2 + name.length()) {
                        contentStart = i2 + name.length() + 2;
                        prefixEnd = open;
                    }
                }
            }
            if (contentStart >= 0) {
                String cleanContent = rawStr.substring(contentStart);
                Component displayName = extractDecoratedName(raw, cleanContent, name,
                    Component.literal((rawStr.substring(0, prefixEnd) + name).trim()));
                Component contentComp = ChatMessageStore.sliceStyled(raw, contentStart, rawStr.length());
                ChatMessageStore.setPendingMeta(new SenderMeta(
                    senderId != null ? senderId : new UUID(0, 0),
                    displayName,
                    contentComp,
                    false,
                    name,
                    isWhisper, whisperPartner
                ));
                return;
            }
        }

        Component playerContent = raw;
        Component senderName = Component.literal(gameProfile.getName());
        if (isWhisper) {
            playerContent = Component.literal(extractWhisperContent(rawStr, gameProfile.getName()));
        } else {
            Component fullLine = bound.decorate(raw);
            senderName = extractDecoratedName(fullLine, rawStr, gameProfile.getName(), senderName);
        }
        ChatMessageStore.debugLog("[e33chat] PlayerChat | raw='" + rawStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner + " | sender='" + senderName.getString() + "' | content='" + playerContent.getString() + "'");
        ChatMessageStore.setPendingMeta(new SenderMeta(
            senderId != null ? senderId : new UUID(0, 0),
            senderName,
            playerContent,
            false,
            gameProfile.getName(),
            isWhisper, whisperPartner
        ));
    }

    @Inject(method = "handleDisguisedChatMessage", at = @At("HEAD"))
    private void onDisguisedChat(Component message, ChatType.Bound bound, CallbackInfo ci) {
        String msgStr = message.getString();
        if (msgStr.startsWith("xaero-waypoint:")
            || msgStr.startsWith("xaero_waypoint:")
            || msgStr.startsWith("xaero_waypoint_add:")) {
            return;
        }
        boolean hasSender = bound.name() != null;

        boolean isWhisper = false;
        String whisperPartner = null;
        if (bound.chatType().is(ChatType.MSG_COMMAND_INCOMING)) {
            isWhisper = true;
            whisperPartner = hasSender ? bound.name().getString() : null;
        } else if (bound.chatType().is(ChatType.MSG_COMMAND_OUTGOING)) {
            isWhisper = true;
            whisperPartner = bound.targetName().map(Component::getString).orElse(null);
        }
        Component disContent = message;
        Component disSender = hasSender ? bound.name() : Component.translatable("e33chat.sender.system");
        if (isWhisper && hasSender) {
            disContent = Component.literal(extractWhisperContent(msgStr, bound.name().getString()));
        } else if (hasSender) {
            Component fullLine = bound.decorate(message);
            disSender = extractDecoratedName(fullLine, msgStr, bound.name().getString(), disSender);
        }
        ChatMessageStore.debugLog("[e33chat] Disguised | raw='" + msgStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner + " | sender='" + disSender.getString() + "' | content='" + disContent.getString() + "'");
        ChatMessageStore.setPendingMeta(new SenderMeta(
            new UUID(0, 0),
            disSender,
            disContent,
            !hasSender,
            hasSender ? bound.name().getString() : null,
            isWhisper, whisperPartner
        ));
    }

    @Inject(method = "handleSystemMessage", at = @At("HEAD"))
    private void onSystemChat(Component message, boolean overlay, CallbackInfo ci) {
        if (overlay) return;

        String sysText = message.getString();
        // Suppress outgoing whisper echo
        boolean hasEchoFlag = ChatMessageStore.hasPendingWhisperEcho();
        boolean hasKw = sysText.contains("悄悄") || sysText.contains("whispers") || sysText.contains("whisper")
            || sysText.contains("私聊") || sysText.contains("密语") || sysText.contains("密聊");
        ChatMessageStore.debugLog("[e33chat] System(echo check) | text='" + sysText + "' | flag=" + hasEchoFlag + " | kw=" + hasKw);
        if (hasEchoFlag && hasKw) {
            ChatMessageStore.consumeWhisperEcho();
            ChatMessageStore.debugLog("[e33chat] System(echo suppressed) | text='" + sysText + "'");
            ChatMessageStore.markSuppressCapture();
            return;
        }
        ChatMessageStore.debugLog("[e33chat] System | text='" + sysText + "' | overlay=" + overlay);

        if (ChatBubbleConfig.CHAT_REPORT_COMPAT.get()) {
            String text = message.getString();
            var connection = Minecraft.getInstance().player.connection;
            String foundName = null, foundProfile = null;
            UUID foundUuid = null;
            int nameStart = -1, contentStart = -1;
            if (connection != null) {
                exact:
                for (var info : connection.getOnlinePlayers()) {
                    for (String cand : nameCandidates(info)) {
                        String pattern = "<" + cand + "> ";
                        int idx = text.indexOf(pattern);
                        if (idx >= 0) {
                            foundName = cand;
                            foundProfile = info.getProfile().getName();
                            foundUuid = info.getProfile().getId();
                            nameStart = idx;
                            contentStart = idx + pattern.length();
                            break exact;
                        }
                    }
                }
                // Prefix inside the brackets: "<[Title]Steve> msg"
                if (foundName == null) {
                    inside:
                    for (var info : connection.getOnlinePlayers()) {
                        for (String cand : nameCandidates(info)) {
                            int idx = text.indexOf(cand + "> ");
                            if (idx <= 0) continue;
                            int open = text.lastIndexOf('<', idx);
                            if (open >= 0 && text.indexOf('>', open) == idx + cand.length()) {
                                foundName = cand;
                                foundProfile = info.getProfile().getName();
                                foundUuid = info.getProfile().getId();
                                nameStart = open;
                                contentStart = idx + cand.length() + 2;
                                break inside;
                            }
                        }
                    }
                }
            }
            if (foundName != null) {
                String cleanContent = text.substring(contentStart);
                Component displayName = extractDecoratedName(message, cleanContent, foundName,
                    Component.literal((text.substring(0, nameStart) + foundName).trim()));
                Component contentComp = ChatMessageStore.sliceStyled(message, contentStart, text.length());
                ChatMessageStore.setPendingMeta(new SenderMeta(
                    foundUuid,
                    displayName,
                    contentComp,
                    false,
                    foundProfile,
                    false, null
                ));
                return;
            }
            // Check for whisper in system message
            SenderMeta wm = detectWhisperInSystemMessage(text, "whisper compat");
            if (wm != null) { ChatMessageStore.setPendingMeta(wm); return; }

            // Deterministic fallback: "click to whisper" event on the sender name
            SenderMeta tc = detectByTellClick(message, text);
            if (tc != null) { ChatMessageStore.setPendingMeta(tc); return; }

            boolean isSystem = !ChatBubbleConfig.SYSTEM_CHAT_AS_BUBBLE.get();
            ChatMessageStore.setPendingMeta(new SenderMeta(
                new UUID(0, 0),
                Component.translatable("e33chat.sender.system"),
                message,
                isSystem,
                null,
                false, null
            ));
            return;
        }

        // Check for whisper in system message (non-compat path)
        String text = message.getString();
        SenderMeta wm = detectWhisperInSystemMessage(text, "whisper");
        if (wm != null) { ChatMessageStore.setPendingMeta(wm); return; }

        boolean isSystem = !ChatBubbleConfig.SYSTEM_CHAT_AS_BUBBLE.get();
        ChatMessageStore.setPendingMeta(new SenderMeta(
            new UUID(0, 0),
            Component.translatable("e33chat.sender.system"),
            message,
            isSystem,
            null,
            false, null
        ));
    }
}
