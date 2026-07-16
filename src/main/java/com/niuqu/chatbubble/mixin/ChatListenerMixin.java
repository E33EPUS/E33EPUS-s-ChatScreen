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
                    LOGGER.info("[E33Chat] System(" + logTag + ") | text='" + text + "' | name=" + name + " | content='" + content + "'");
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
            if (idx >= 0) {
                String displayName = (rawStr.substring(0, idx) + name).trim();
                String cleanContent = rawStr.substring(idx + pattern.length());
                ChatMessageStore.setPendingMeta(new SenderMeta(
                    senderId != null ? senderId : new UUID(0, 0),
                    Component.literal(displayName),
                    Component.literal(cleanContent),
                    false,
                    name,
                    isWhisper, whisperPartner
                ));
                return;
            }
        }

        Component playerContent = raw;
        if (isWhisper) {
            playerContent = Component.literal(extractWhisperContent(rawStr, gameProfile.getName()));
        }
        LOGGER.info("[E33Chat] PlayerChat | raw='" + rawStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner + " | content='" + playerContent.getString() + "'");
        ChatMessageStore.setPendingMeta(new SenderMeta(
            senderId != null ? senderId : new UUID(0, 0),
            Component.literal(gameProfile.getName()),
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
        if (isWhisper && hasSender) {
            disContent = Component.literal(extractWhisperContent(msgStr, bound.name().getString()));
        }
        LOGGER.info("[E33Chat] Disguised | raw='" + msgStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner + " | content='" + disContent.getString() + "'");
        ChatMessageStore.setPendingMeta(new SenderMeta(
            new UUID(0, 0),
            hasSender ? bound.name() : Component.translatable("e33chat.sender.system"),
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
        LOGGER.info("[E33Chat] System(echo check) | text='" + sysText + "' | flag=" + hasEchoFlag + " | kw=" + hasKw);
        if (hasEchoFlag && hasKw) {
            ChatMessageStore.consumeWhisperEcho();
            LOGGER.info("[E33Chat] System(echo suppressed) | text='" + sysText + "'");
            ChatMessageStore.markSuppressCapture();
            return;
        }
        LOGGER.info("[E33Chat] System | text='" + sysText + "' | overlay=" + overlay);

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
            }
            if (foundName != null) {
                UUID senderId = ChatMessageStore.lookupPlayerUUID(foundName);
                String displayName = (text.substring(0, nameStart) + foundName).trim();
                String cleanContent = text.substring(contentStart);
                ChatMessageStore.setPendingMeta(new SenderMeta(
                    senderId,
                    Component.literal(displayName),
                    Component.literal(cleanContent),
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
