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

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.UUID;

@Mixin(value = ChatListener.class, priority = 500)
public class ChatListenerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Pulls styled server prefixes out of the decorated line: "[Group]<Steve> hi" -> "[Group]Steve"
    private static Component extractDecoratedName(Component fullLine, String contentStr,
                                                  String rawName, Component fallback) {
        if (contentStr == null || contentStr.isEmpty()) return fallback;
        String fullStr = fullLine.getString();
        int idx = fullStr.lastIndexOf(contentStr);
        if (idx <= 0) return fallback;
        int a = 0, b = idx;
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
            String name = info.getProfile().getName();
            int idx = text.indexOf(name);
            if (idx >= 0 && idx < 30) {
                if (text.contains("悄悄") || text.contains("whisper") || text.contains("对你说") || text.contains("to you")
                    || text.contains("私聊") || text.contains("密语") || text.contains("密聊")) {
                    String content = extractWhisperContent(text, name);
                    UUID senderId = ChatMessageStore.lookupPlayerUUID(name);
                    LOGGER.info("[e33chat] System(" + logTag + ") | text='" + text + "' | name=" + name + " | content='" + content + "'");
                    return new SenderMeta(
                        senderId,
                        Component.literal(name),
                        Component.literal(content),
                        false,
                        name,
                        true, name
                    );
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
        if (Minecraft.getInstance().level != null) {
            var registry = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.CHAT_TYPE);
            var key = registry.getResourceKey(bound.chatType()).orElse(null);
            if (ChatType.MSG_COMMAND_INCOMING.equals(key)) {
                isWhisper = true;
                whisperPartner = gameProfile.getName();
            } else if (ChatType.MSG_COMMAND_OUTGOING.equals(key)) {
                isWhisper = true;
                whisperPartner = bound.targetName() != null ? bound.targetName().getString() : null;
            }
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
        LOGGER.info("[e33chat] PlayerChat | raw='" + rawStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner + " | sender='" + senderName.getString() + "' | content='" + playerContent.getString() + "'");
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
        if (Minecraft.getInstance().level != null) {
            var registry = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.CHAT_TYPE);
            var key = registry.getResourceKey(bound.chatType()).orElse(null);
            if (ChatType.MSG_COMMAND_INCOMING.equals(key)) {
                isWhisper = true;
                whisperPartner = hasSender ? bound.name().getString() : null;
            } else if (ChatType.MSG_COMMAND_OUTGOING.equals(key)) {
                isWhisper = true;
                whisperPartner = bound.targetName() != null ? bound.targetName().getString() : null;
            }
        }
        Component disContent = message;
        Component disSender = hasSender ? bound.name() : Component.translatable("e33chat.sender.system");
        if (isWhisper && hasSender) {
            disContent = Component.literal(extractWhisperContent(msgStr, bound.name().getString()));
        } else if (hasSender) {
            Component fullLine = bound.decorate(message);
            disSender = extractDecoratedName(fullLine, msgStr, bound.name().getString(), disSender);
        }
        LOGGER.info("[e33chat] Disguised | raw='" + msgStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner + " | sender='" + disSender.getString() + "' | content='" + disContent.getString() + "'");
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
        LOGGER.info("[e33chat] System(echo check) | text='" + sysText + "' | flag=" + hasEchoFlag + " | kw=" + hasKw);
        if (hasEchoFlag && hasKw) {
            ChatMessageStore.consumeWhisperEcho();
            LOGGER.info("[e33chat] System(echo suppressed) | text='" + sysText + "'");
            ChatMessageStore.markSuppressCapture();
            return;
        }
        LOGGER.info("[e33chat] System | text='" + sysText + "' | overlay=" + overlay);

        if (ChatBubbleConfig.CHAT_REPORT_COMPAT.get()) {
            String text = message.getString();
            var connection = Minecraft.getInstance().player.connection;
            String foundName = null;
            int nameStart = -1, contentStart = -1;
            if (connection != null) {
                for (var info : connection.getOnlinePlayers()) {
                    String name = info.getProfile().getName();
                    String pattern = "<" + name + "> ";
                    int idx = text.indexOf(pattern);
                    if (idx >= 0) {
                        foundName = name;
                        nameStart = idx;
                        contentStart = idx + pattern.length();
                        break;
                    }
                }
                // Prefix inside the brackets: "<[Title]Steve> msg"
                if (foundName == null) {
                    for (var info : connection.getOnlinePlayers()) {
                        String name = info.getProfile().getName();
                        int idx = text.indexOf(name + "> ");
                        if (idx <= 0) continue;
                        int open = text.lastIndexOf('<', idx);
                        if (open >= 0 && text.indexOf('>', open) == idx + name.length()) {
                            foundName = name;
                            nameStart = open;
                            contentStart = idx + name.length() + 2;
                            break;
                        }
                    }
                }
            }
            if (foundName != null) {
                UUID senderId = ChatMessageStore.lookupPlayerUUID(foundName);
                String cleanContent = text.substring(contentStart);
                Component displayName = extractDecoratedName(message, cleanContent, foundName,
                    Component.literal((text.substring(0, nameStart) + foundName).trim()));
                Component contentComp = ChatMessageStore.sliceStyled(message, contentStart, text.length());
                ChatMessageStore.setPendingMeta(new SenderMeta(
                    senderId,
                    displayName,
                    contentComp,
                    false,
                    foundName,
                    false, null
                ));
                return;
            }
            // Check for whisper in system message
            SenderMeta wm = detectWhisperInSystemMessage(text, "whisper compat");
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
