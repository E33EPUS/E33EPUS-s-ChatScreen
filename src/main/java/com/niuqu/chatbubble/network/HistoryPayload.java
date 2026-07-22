package com.niuqu.chatbubble.network;

import com.niuqu.chatbubble.ChatMessageStore;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HistoryPayload(List<ChatMessageStore.HistoryEntry> entries)
        implements CustomPayload {

    public static final CustomPayload.Id<HistoryPayload> ID =
        new CustomPayload.Id<>(Identifier.of("e33chat", "chat_history"));

    public static final PacketCodec<PacketByteBuf, HistoryPayload> CODEC = PacketCodec.of(
        (value, buf) -> buf.writeCollection(value.entries, (b, e) -> {
            b.writeString(e.senderUUID().toString());
            b.writeString(e.senderName());
            b.writeString(e.content());
            b.writeString(e.time().toString());
            b.writeBoolean(e.isSystem());
            b.writeString(e.replyContent() != null ? e.replyContent() : "");
            b.writeString(e.replySender() != null ? e.replySender() : "");
        }),
        buf -> new HistoryPayload(buf.readList(b -> new ChatMessageStore.HistoryEntry(
            UUID.fromString(b.readString()),
            b.readString(),
            b.readString(),
            LocalTime.parse(b.readString()),
            b.readBoolean(),
            nullOrEmpty(b.readString()),
            nullOrEmpty(b.readString())
        )))
    );

    private static String nullOrEmpty(String s) { return s == null || s.isEmpty() ? null : s; }

    @Override
    public Id<HistoryPayload> getId() { return ID; }
}
