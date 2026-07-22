package com.niuqu.chatbubble.chat;

import java.time.LocalTime;
import java.util.UUID;
import net.minecraft.text.Text;

public record ChatMessage(
    UUID senderUUID, Text senderName, Text content, LocalTime time,
    boolean isOwn, boolean isSystem, String replyContent, String replySender,
    String messageHash, int duplicateCount, String rawPlayerName,
    boolean whisper, String whisperPartner
) {
    public static ChatMessage system(String raw, long nowMs) {
        return new ChatMessage(new UUID(0, 0),
            Text.translatable("e33chat.sender.system"),
            Text.literal(raw), LocalTime.now(), false, true,
            null, null, String.valueOf(raw.hashCode()), 1, null, false, null);
    }

    public static ChatMessage player(UUID uuid, String name, String content, long nowMs, boolean own) {
        return new ChatMessage(uuid, Text.literal(name), Text.literal(content), LocalTime.now(),
            own, false, null, null, String.valueOf(content.hashCode()), 1, name, false, null);
    }
}
